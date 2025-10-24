package com.aid.train.backend.domain.feedback.service;

import com.aid.train.backend.domain.session.entity.DialogueSession;
import com.aid.train.backend.domain.session.entity.Transcript;
import com.aid.train.backend.domain.session.enums.Speaker;
import com.aid.train.backend.domain.feedback.dto.request.FeedbackCreateRequest;
import com.aid.train.backend.global.exception.TrainException;
import com.aid.train.backend.global.exception.enums.ErrorCode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

/**
 * AI를 활용한 피드백 프롬프트 생성 및 처리 서비스
 *
 * ChatGPT 4.0을 사용하여 대화 내용을 분석하고 피드백을 생성합니다.
 * 전체 대화 맥락에서 사용자의 커뮤니케이션 스킬을 종합적으로 평가합니다.
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FeedbackPromptService {

    private final ChatModel chatModel;
    private final ObjectMapper objectMapper;

    /**
     * AI를 통해 대화 세션의 피드백을 생성합니다.
     *
     * @param dialogueSession 분석할 대화 세션
     * @return 생성된 피드백 요청 객체
     * @throws TrainException AI 분석 실패 시 발생
     */
    public FeedbackCreateRequest generateFeedbackFromAI(DialogueSession dialogueSession) {
        log.info("AI 피드백 생성 시작 - sessionId: {}", dialogueSession.getSessionId());

        try {
            // 1. 프롬프트 생성
            String prompt = generateFeedbackPrompt(dialogueSession);

            // 2. AI API 호출
            String aiResponse = callChatGPT(prompt);

            // 3. AI 응답 파싱
            FeedbackCreateRequest feedbackRequest = parseAIResponse(
                    aiResponse, dialogueSession.getSessionId(), prompt, aiResponse);

            log.info("AI 피드백 생성 완료 - sessionId: {}, totalScore: {}",
                    dialogueSession.getSessionId(), feedbackRequest.totalScore());

            return feedbackRequest;

        } catch (Exception e) {
            log.error("AI 피드백 생성 실패 - sessionId: {}", dialogueSession.getSessionId(), e);
            throw new TrainException(ErrorCode.AI_ANALYSIS_FAILED,
                    "AI 피드백 생성 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * ChatGPT API를 호출하여 응답을 받습니다.
     *
     * @param prompt 분석 요청 프롬프트
     * @return AI 응답 텍스트
     * @throws Exception API 호출 실패 시
     */
    private String callChatGPT(String prompt) throws Exception {
        log.info("ChatGPT 4.0 호출 시작 - prompt length: {}", prompt.length());

        try {
            Prompt chatPrompt = new Prompt(prompt);
            ChatResponse response = chatModel.call(chatPrompt);

            String aiResponse = response.getResult().getOutput().getContent();
            log.info("ChatGPT 4.0 응답 받음 - response length: {}", aiResponse.length());

            return aiResponse;

        } catch (Exception e) {
            log.error("ChatGPT 4.0 호출 실패", e);
            throw new Exception("AI API 호출에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * AI 응답을 파싱하여 FeedbackCreateRequest 객체로 변환합니다.
     *
     * @param aiResponse AI 원본 응답
     * @param sessionId 세션 ID
     * @param aiPrompt 사용된 프롬프트
     * @param aiRawResponse AI 원본 응답
     * @return 파싱된 피드백 생성 요청
     */
    private FeedbackCreateRequest parseAIResponse(String aiResponse, String sessionId,
                                                  String aiPrompt, String aiRawResponse) {
        log.info("AI 응답 파싱 시작 - sessionId: {}", sessionId);

        try {
            // JSON 블록 추출
            String jsonContent = extractJsonFromResponse(aiResponse);

            // JSON 파싱
            JsonNode jsonNode = objectMapper.readTree(jsonContent);

            // 점수 검증 및 보정
            int totalScore = jsonNode.get("totalScore").asInt();
            int speechRateScore = jsonNode.get("speechRateScore").asInt();
            int fillerWordsScore = jsonNode.get("fillerWordsScore").asInt();
            int politenessScore = jsonNode.get("politenessScore").asInt();
            int clarityScore = jsonNode.get("clarityScore").asInt();

            // 점수 합계 검증
            int calculatedTotal = speechRateScore + fillerWordsScore + politenessScore + clarityScore;
            if (Math.abs(totalScore - calculatedTotal) > 5) {
                log.warn("AI 응답 점수 불일치 수정 - expected: {}, calculated: {}", totalScore, calculatedTotal);
                totalScore = calculatedTotal; // 계산된 값으로 보정
            }

            // FeedbackCreateRequest 생성
            return FeedbackCreateRequest.builder()
                    .sessionId(sessionId)
                    .totalScore(totalScore)
                    .speechRateScore(speechRateScore)
                    .fillerWordsScore(fillerWordsScore)
                    .politenessScore(politenessScore)
                    .clarityScore(clarityScore)
                    .improvementPoints(jsonNode.get("improvementPoints").toString())
                    .originalTranscript(jsonNode.get("originalTranscript").asText())
                    .alternativeA(jsonNode.get("alternativeA").asText())
                    .alternativeB(jsonNode.get("alternativeB").asText())
                    .alternativeC(jsonNode.get("alternativeC").asText())
                    .aiPrompt(aiPrompt)
                    .aiRawResponse(aiRawResponse)
                    .build();

        } catch (Exception e) {
            log.error("AI 응답 JSON 파싱 실패 - sessionId: {}", sessionId, e);
            throw new TrainException(ErrorCode.AI_RESPONSE_PARSE_ERROR,
                    "AI 응답 파싱에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * AI 응답에서 JSON 블록을 추출합니다.
     *
     * @param response AI 원본 응답
     * @return 추출된 JSON 문자열
     */
    private String extractJsonFromResponse(String response) {
        // ```json 블록에서 JSON 추출
        int jsonStart = response.indexOf("```json");
        int jsonEnd = response.indexOf("```", jsonStart + 7);

        if (jsonStart != -1 && jsonEnd != -1) {
            return response.substring(jsonStart + 7, jsonEnd).trim();
        }

        // JSON 블록이 없으면 전체 응답에서 { } 블록 찾기
        int braceStart = response.indexOf("{");
        int braceEnd = response.lastIndexOf("}");

        if (braceStart != -1 && braceEnd != -1) {
            return response.substring(braceStart, braceEnd + 1);
        }

        throw new TrainException(ErrorCode.AI_RESPONSE_PARSE_ERROR,
                "AI 응답에서 JSON을 찾을 수 없습니다.");
    }

    /**
     * 피드백용 프롬프트를 생성합니다.
     * 전체 대화 맥락에서 사용자의 커뮤니케이션 스킬을 분석합니다.
     *
     * @param dialogueSession 대화 세션
     * @return 생성된 프롬프트
     */
    private String generateFeedbackPrompt(DialogueSession dialogueSession) {
        List<Transcript> transcripts = dialogueSession.getTranscripts();

        log.info("피드백 프롬프트 생성 - sessionId: {}, transcriptCount: {}",
                dialogueSession.getSessionId(), transcripts.size());

        if (transcripts.isEmpty()) {
            throw new TrainException(ErrorCode.INSUFFICIENT_DIALOGUE_CONTENT,
                    "분석할 대화 내용이 없습니다.");
        }

        // 분석 가능한 사용자 발화만 필터링
        List<Transcript> analyzableTranscripts = transcripts.stream()
                .filter(Transcript::isAnalyzable) // 새로운 메서드 활용
                .sorted(Comparator.comparing(Transcript::getTimestamp)) // timestamp로 정렬
                .toList();

        if (analyzableTranscripts.isEmpty()) {
            throw new TrainException(ErrorCode.INSUFFICIENT_DIALOGUE_CONTENT,
                    "분석 가능한 사용자 발화가 충분하지 않습니다.");
        }

        StringBuilder conversationBuilder = new StringBuilder();
        conversationBuilder.append("=== 시나리오 정보 ===\n");
        conversationBuilder.append("제목: ").append(dialogueSession.getScenario().getTitle()).append("\n");
        conversationBuilder.append("설명: ").append(dialogueSession.getScenario().getDescription()).append("\n\n");

        conversationBuilder.append("=== 전체 대화 내용 ===\n");
        for (Transcript transcript : transcripts) {
            String speaker = transcript.getSpeaker() == Speaker.USER ? "사용자" : "AI";
            conversationBuilder.append(String.format("[%s] %s\n", speaker, transcript.getContent()));
        }

        // 대표 발화 선택 (가장 의미있고 분석가치가 있는 발화)
        String representativeTranscript = analyzableTranscripts.stream()
                .max(Comparator.comparingInt(t -> t.getContent().length()))
                .map(Transcript::getContent)
                .orElse(analyzableTranscripts.get(0).getContent());

        return String.format("""
                당신은 대화 훈련 전문 AI 코치입니다.
                
                다음 대화에서 사용자의 커뮤니케이션 스킬을 종합적으로 분석하여 피드백을 제공해주세요.
                대화 전체 맥락에서 사용자가 AI와 어떻게 소통하는지, 상황에 맞는 적절한 반응을 보이는지 평가해주세요.
                
                %s
                
                다음 4가지 기준으로 점수를 매기고 개선안을 제시해주세요:
                
                1. 발화속도 (0-30점): 너무 빠르거나 느리지 않은 적절한 말하기 속도
                2. 추임새 (0-20점): "음...", "그...", "아..." 등 불필요한 표현 빈도 (적을수록 좋음)
                3. 공손도 (0-25점): 상대방에 대한 예의와 존중 표현
                4. 명료성 (0-25점): 의사 전달의 명확성과 구체성
                
                **분석 대상 발화**: "%s"
                
                이 발화를 중심으로 3가지 스타일의 개선안을 제시해주세요:
                - A안 (간결한 스타일): 불필요한 표현을 제거하고 핵심만 전달
                - B안 (공손한 스타일): 정중한 표현과 예의를 강조
                - C안 (따뜻한 스타일): 친근하고 감정적 교감을 포함
                
                응답은 반드시 다음 JSON 형식으로 해주세요:
                ```json
                {
                    "totalScore": 85,
                    "speechRateScore": 25,
                    "fillerWordsScore": 15,
                    "politenessScore": 23,
                    "clarityScore": 22,
                    "improvementPoints": [
                        {
                            "type": "filler_words",
                            "description": "추임새 줄이기",
                            "count": 3,
                            "suggestion": "말하기 전에 잠시 생각하는 시간을 가져보세요"
                        }
                    ],
                    "originalTranscript": "%s",
                    "alternativeA": "간결한 개선안",
                    "alternativeB": "공손한 개선안", 
                    "alternativeC": "따뜻한 개선안"
                }
                ```
                """, conversationBuilder, representativeTranscript, representativeTranscript);
    }
}