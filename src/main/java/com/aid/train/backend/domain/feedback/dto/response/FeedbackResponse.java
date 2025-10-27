package com.aid.train.backend.domain.feedback.dto.response;

import com.aid.train.backend.domain.feedback.entity.Feedback;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 피드백 상세 응답 DTO
 *
 * <p>단일 피드백의 상세 정보를 클라이언트에 전달합니다. AI 분석 결과, 점수, 3가지 스타일의 개선안,
 * 사용자 선택 결과(선택 완료 여부 포함)까지 모두 포괄합니다.</p>
 *
 * <p><b>사용 시나리오</b></p>
 * <ul>
 *   <li>피드백 생성 직후 상세 응답 반환</li>
 *   <li>특정 세션의 피드백 상세 조회</li>
 *   <li>개선안 선택 이후 갱신된 상세 정보 반환</li>
 * </ul>
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Schema(description = "피드백 상세 정보")
@Builder
public record FeedbackResponse(
        /**
         * 피드백 고유 ID
         *
         * <p>데이터베이스에서 생성된 피드백 레코드의 식별자입니다.</p>
         */
        @Schema(description = "피드백 ID", example = "1")
        Long id,

        /**
         * 연관된 세션 ID
         *
         * <p>이 피드백이 생성된 대화 세션의 비즈니스 식별자입니다.</p>
         */
        @Schema(description = "세션 ID", example = "550e8400-e29b-41d4-a716-446655440000")
        String sessionId,

        /**
         * 시나리오 ID
         *
         * <p>대화가 진행된 시나리오의 내부 식별자입니다.</p>
         */
        @Schema(description = "시나리오 ID", example = "1")
        Long scenarioId,

        /**
         * 시나리오 제목
         *
         * <p>대화가 진행된 시나리오의 제목입니다. 피드백 컨텍스트 파악에 도움을 줍니다.</p>
         */
        @Schema(description = "시나리오 제목", example = "상사에게 휴가 요청하기")
        String scenarioTitle,

        /**
         * 전체 점수(0-100)
         *
         * <p>AI가 분석한 종합 점수입니다(하위 4개 지표 합산).</p>
         */
        @Schema(description = "전체 점수 (0-100)", example = "72", minimum = "0", maximum = "100")
        Integer totalScore,

        /**
         * 점수 등급(A-F)
         *
         * <p>총점 기준의 등급입니다: A(90+), B(80-89), C(70-79), D(60-69), F(60 미만).</p>
         */
        @Schema(description = "점수 등급", example = "C", allowableValues = {"A", "B", "C", "D", "F"})
        String scoreGrade,

        /**
         * 발화속도 점수(0-25)
         *
         * <p>적절한 말하기 속도를 유지했는지에 대한 평가 점수입니다.</p>
         */
        @Schema(description = "발화속도 점수 (0-25)", example = "23", minimum = "0", maximum = "25")
        Integer speechRateScore,

        /**
         * 추임새 점수(0-25)
         *
         * <p>“음…”, “그…” 등 불필요한 추임새 빈도에 대한 평가 점수입니다.
         * 점수가 높을수록 추임새가 적어 더 좋습니다.</p>
         */
        @Schema(description = "추임새 점수 (0-25) - 높을수록 좋음", example = "12", minimum = "0", maximum = "25")
        Integer fillerWordsScore,

        /**
         * 공손도 점수(0-25)
         *
         * <p>예의·존중 표현의 적절성을 평가한 점수입니다.</p>
         */
        @Schema(description = "공손도 점수 (0-25)", example = "21", minimum = "0", maximum = "25")
        Integer politenessScore,

        /**
         * 명료성 점수(0-25)
         *
         * <p>의사 전달의 명확성과 구체성에 대한 평가 점수입니다.</p>
         */
        @Schema(description = "명료성 점수 (0-25)", example = "16", minimum = "0", maximum = "25")
        Integer clarityScore,

        /**
         * 개선점 목록(JSON)
         *
         * <p>AI가 추출한 세부 개선 포인트 목록으로, JSON 배열 형태의 문자열입니다.
         * 각 항목은 유형, 설명, 제안사항(및 필요 시 count 등)을 포함합니다.</p>
         */
        @Schema(description = "개선점 목록 (JSON)", example = "[{\"type\":\"filler_words\",\"description\":\"추임새 줄이기\",\"count\":5}]")
        String improvementPoints,

        /**
         * 원본 발화 내용
         *
         * <p>피드백 대상이 된 사용자 발화의 주요 문장입니다.</p>
         */
        @Schema(description = "원본 발화 내용", example = "팀장님, 내일... 음... 휴가 가능하신가요?")
        String originalTranscript,

        /**
         * 개선안 A(간결한 스타일)
         *
         * <p>불필요한 표현을 제거하고 핵심만 전달하는 스타일입니다.</p>
         */
        @Schema(description = "개선안 A (간결한 스타일)", example = "팀장님, 내일 휴가 가능한가요?")
        String alternativeA,

        /**
         * 개선안 B(공손한 스타일)
         *
         * <p>정중한 표현과 예의를 강조하는 스타일입니다.</p>
         */
        @Schema(description = "개선안 B (공손한 스타일)", example = "팀장님, 내일 하루 휴가를 사용해도 될까요?")
        String alternativeB,

        /**
         * 개선안 C(따뜻한 스타일)
         *
         * <p>친근하고 감정적 교감을 포함하는 스타일입니다.</p>
         */
        @Schema(description = "개선안 C (따뜻한 스타일)", example = "팀장님, 내일 개인적인 일로 휴가를 쓰고 싶은데 괜찮을까요?")
        String alternativeC,

        /**
         * 사용자가 선택한 개선안 타입
         *
         * <p>사용자가 최종 선택한 개선안 유형입니다. 미선택 시 {@code null}입니다.</p>
         */
        @Schema(description = "선택된 개선안 타입", example = "C", allowableValues = {"A", "B", "C", "CUSTOM"})
        Feedback.ChosenAlternative chosenAlternative,

        /**
         * 최종 선택된 개선안 내용
         *
         * <p>사용자가 최종 선택하거나 직접 작성한 문장입니다. 미선택 시 {@code null}입니다.</p>
         */
        @Schema(description = "최종 선택된 개선안 내용", example = "팀장님, 내일 휴가 가능한가요?")
        String finalChoice,

        /**
         * 선택 완료 여부
         *
         * <p>개선안 선택 프로세스가 완료되었는지 여부입니다.</p>
         */
        @Schema(description = "선택 완료 여부", example = "true")
        Boolean isChoiceComplete,

        /**
         * 피드백 생성 시각
         */
        @Schema(description = "피드백 생성 시간", example = "2025-10-24T14:30:00")
        LocalDateTime createdAt,

        /**
         * 피드백 수정 시각
         *
         * <p>개선안 선택 등으로 갱신된 최종 시각입니다.</p>
         */
        @Schema(description = "피드백 수정 시간", example = "2025-10-24T14:35:00")
        LocalDateTime updatedAt,

        /**
         * 전체 대화 흐름 분석
         *
         * <p>AI 생성 시 포함되며, 단순 조회 응답에서는 {@code null}일 수 있습니다.</p>
         */
        @Schema(description = "전체 대화 흐름 분석 (생성 시에만 포함)")
        OverallAnalysis overallAnalysis,

        /**
         * 문장별 세부 분석 목록
         *
         * <p>각 사용자 발화에 대한 상세 분석 목록입니다(생성 시 포함).</p>
         */
        @Schema(description = "문장별 세부 분석 목록 (생성 시에만 포함)")
        List<SentenceAnalysis> sentenceAnalyses,

        /**
         * 대화 전체 개선안
         *
         * <p>현재 대화 패턴과 이상적 패턴, 그리고 개선 예시 대화를 포함합니다.</p>
         */
        @Schema(description = "대화 전체 개선안 (생성 시에만 포함)")
        ConversationImprovement conversationImprovement
) {
    /**
     * {@link Feedback} 엔티티로부터 {@code FeedbackResponse}를 생성합니다.
     *
     * <p>종합 분석 필드는 포함하지 않습니다(기존 호환성 유지).</p>
     *
     * @param feedback 피드백 엔티티
     * @return 상세 응답 DTO
     */
    public static FeedbackResponse from(Feedback feedback) {
        return FeedbackResponse.builder()
                .id(feedback.getId())
                .sessionId(feedback.getDialogueSession().getSessionId())
                .scenarioId(feedback.getDialogueSession().getScenario().getId())
                .scenarioTitle(feedback.getDialogueSession().getScenario().getTitle())
                .totalScore(feedback.getTotalScore())
                .scoreGrade(feedback.getScoreGrade())
                .speechRateScore(feedback.getSpeechRateScore())
                .fillerWordsScore(feedback.getFillerWordsScore())
                .politenessScore(feedback.getPolitenessScore())
                .clarityScore(feedback.getClarityScore())
                .improvementPoints(feedback.getImprovementPoints())
                .originalTranscript(feedback.getOriginalTranscript())
                .alternativeA(feedback.getAlternativeA())
                .alternativeB(feedback.getAlternativeB())
                .alternativeC(feedback.getAlternativeC())
                .chosenAlternative(feedback.getChosenAlternative())
                .finalChoice(feedback.getFinalChoice())
                .isChoiceComplete(feedback.isChoiceComplete())
                .createdAt(feedback.getCreatedAt())
                .updatedAt(feedback.getUpdatedAt())
                // 새 필드들은 null (기존 호환성)
                .overallAnalysis(null)
                .sentenceAnalyses(null)
                .conversationImprovement(null)
                .build();
    }

    /**
     * 점수 요약만 포함하는 간단한 응답을 생성합니다.
     *
     * <p>히스토리 목록/통계 등에서 상세 개선안 없이 점수만 필요할 때 사용합니다.</p>
     *
     * @param feedback 피드백 엔티티
     * @return 점수 정보만 포함한 응답 DTO
     */
    public static FeedbackResponse fromWithScoreOnly(Feedback feedback) {
        return FeedbackResponse.builder()
                .id(feedback.getId())
                .sessionId(feedback.getDialogueSession().getSessionId())
                .totalScore(feedback.getTotalScore())
                .scoreGrade(feedback.getScoreGrade())
                .speechRateScore(feedback.getSpeechRateScore())
                .fillerWordsScore(feedback.getFillerWordsScore())
                .politenessScore(feedback.getPolitenessScore())
                .clarityScore(feedback.getClarityScore())
                .createdAt(feedback.getCreatedAt())
                .build();
    }

    /**
     * 저장된 {@link Feedback} 엔티티에 종합 분석 결과를 결합하여 응답을 생성합니다.
     *
     * @param feedback                저장된 피드백 엔티티
     * @param overallAnalysis         전체 대화 흐름 분석
     * @param sentenceAnalyses        문장별 세부 분석 목록
     * @param conversationImprovement 대화 전체 개선안
     * @return 종합 분석을 포함한 상세 응답 DTO
     */
    public static FeedbackResponse withComprehensiveAnalysis(
            Feedback feedback,
            OverallAnalysis overallAnalysis,
            List<SentenceAnalysis> sentenceAnalyses,
            ConversationImprovement conversationImprovement) {

        return FeedbackResponse.builder()
                .id(feedback.getId())
                .sessionId(feedback.getDialogueSession().getSessionId())
                .scenarioId(feedback.getDialogueSession().getScenario().getId())
                .scenarioTitle(feedback.getDialogueSession().getScenario().getTitle())
                .totalScore(feedback.getTotalScore())
                .scoreGrade(feedback.getScoreGrade())
                .speechRateScore(feedback.getSpeechRateScore())
                .fillerWordsScore(feedback.getFillerWordsScore())
                .politenessScore(feedback.getPolitenessScore())
                .clarityScore(feedback.getClarityScore())
                .improvementPoints(feedback.getImprovementPoints())
                .originalTranscript(feedback.getOriginalTranscript())
                .alternativeA(feedback.getAlternativeA())
                .alternativeB(feedback.getAlternativeB())
                .alternativeC(feedback.getAlternativeC())
                .chosenAlternative(feedback.getChosenAlternative())
                .finalChoice(feedback.getFinalChoice())
                .isChoiceComplete(feedback.isChoiceComplete())
                .createdAt(feedback.getCreatedAt())
                .updatedAt(feedback.getUpdatedAt())
                // 새로운 종합 분석 포함
                .overallAnalysis(overallAnalysis)
                .sentenceAnalyses(sentenceAnalyses)
                .conversationImprovement(conversationImprovement)
                .build();
    }

    /**
     * 전체 대화 흐름 분석
     */
    @Schema(description = "전체 대화 흐름 분석")
    @Builder
    public record OverallAnalysis(
            @Schema(description = "대화 흐름 평가", example = "요청 전달이 지연되었으나 최종적으로 의도를 명확히 했습니다.")
            String conversationFlow,

            @Schema(description = "소통 패턴 분석", example = "추임새가 잦아 자신감 부족이 느껴지지만 전반적으로 정중합니다.")
            String communicationPattern,

            @Schema(description = "전반적인 개선점 목록")
            List<OverallImprovement> overallImprovements
    ) {
    }

    /**
     * 전반적인 개선점
     */
    @Schema(description = "전반적인 개선점")
    @Builder
    public record OverallImprovement(
            @Schema(description = "개선 카테고리", example = "confidence", allowableValues = {"conversation_flow", "confidence", "structure"})
            String category,

            @Schema(description = "개선 영역 설명", example = "자신감 있는 소통")
            String description,

            @Schema(description = "구체적인 개선 방법", example = "말하기 전 핵심을 정리하고 차분히 전달해 보세요.")
            String suggestion
    ) {
    }

    /**
     * 문장별 분석 결과
     */
    @Schema(description = "문장별 분석 결과")
    @Builder
    public record SentenceAnalysis(
            @Schema(description = "문장 순서", example = "1")
            Integer sequence,

            @Schema(description = "원본 문장", example = "음... 그... 팀장님, 저기... 혹시...")
            String content,

            @Schema(description = "발견된 문제점들")
            List<SentenceIssue> issues,

            @Schema(description = "개선된 문장", example = "팀장님, 내일 개인적인 일이 있어 휴가를 요청드리고 싶습니다.")
            String improvedVersion
    ) {
    }

    /**
     * 문장별 문제점
     */
    @Schema(description = "문장별 문제점")
    @Builder
    public record SentenceIssue(
            @Schema(description = "문제 유형", example = "filler_words", allowableValues = {"filler_words", "incomplete_sentence", "vague_explanation"})
            String type,

            @Schema(description = "문제 발생 횟수", example = "6")
            Integer count,

            @Schema(description = "문제의 심각도", example = "high", allowableValues = {"high", "medium", "low"})
            String impact,

            @Schema(description = "개선 제안", example = "추임새를 줄이고 한 번에 명확히 말씀하세요.")
            String suggestion
    ) {
    }

    /**
     * 대화 전체 개선안
     */
    @Schema(description = "대화 전체 개선안")
    @Builder
    public record ConversationImprovement(
            @Schema(description = "현재 대화 패턴", example = "추임새 → 불완전한 설명 → 재설명 → 모호한 확인")
            String currentPattern,

            @Schema(description = "이상적인 대화 패턴", example = "인사 → 명확한 요청 → 간단한 이유 → 감사 표현")
            String improvedPattern,

            @Schema(description = "전체 대화 개선 예시", example = "팀장님, 안녕하세요. 내일 급한 개인 사정으로 연차를 사용하고자 합니다. 승인 부탁드립니다.")
            String fullImprovedDialogue
    ) {
    }
}
