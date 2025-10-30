package com.aid.train.backend.websocket.handler;

import com.aid.train.backend.domain.session.dto.response.TranscriptResponse;
import com.aid.train.backend.domain.session.entity.Transcript;
import com.aid.train.backend.domain.session.service.TranscriptService;
import com.aid.train.backend.websocket.service.SessionCoordinator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class TranscriptHandler extends TextWebSocketHandler {

    private final TranscriptService transcriptService;
    private final ObjectMapper objectMapper;
    private final SessionCoordinator sessionCoordinator;
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    /**
     * WebSocket 연결이 성공했을 때 호출됩니다.
     *
     * 처리 내용:
     * 1. URI에서 sessionId 추출
     * 2. SessionCoordinator를 통해 전체 세션 초기화
     *    - WebSocket 세션 등록
     *    - DB 조회 및 매핑
     *
     * @param session WebSocket 연결 객체
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session){
        String sessionId = extractSessionId(session);
        sessions.put(sessionId, session);
        log.info("TranscriptHandler - WebSocket 연결 시작 - sessionId: {}", sessionId);

    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
        String sessionId = extractSessionId(session);
        String payload = message.getPayload();

        try {
            // JSON 파싱하여 메시지 타입 확인
            JsonNode json = objectMapper.readTree(payload);
            String type = json.get("type").asText();

            if("TRANSCRIPT".equals(type)) {
                String speaker = json.get("speaker").asText().toUpperCase();
                String content = json.get("text").asText();
                long startMs = json.has("startTimeMs")? json.get("startTimeMs").asLong() : 0L;
                long endMs = json.has("endTimeMs")? json.get("endTimeMs").asLong() : 0L;;

                if("USER".equals(speaker)) {
                    transcriptService.saveUserTranscript(sessionId, content, startMs, endMs);
                } else if("AI".equals(speaker)) {
                    transcriptService.saveAiTranscript(sessionId, content, startMs, endMs);
                }

                log.info("transcript 저장 - sessionId: {}, speaker: {}, content: {}", sessionId, speaker, content);
            }

            if("SESSION_RECONNECT".equals(type)) {
                try {
                    List<Transcript> transcriptsList = transcriptService.getTranscripts(sessionId);

                    // 응답 메시지 생성
                    ObjectNode response = objectMapper.createObjectNode();
                    response.put("type", "SESSION_RECOVERY");
                    response.put("success", true);

                    List<TranscriptResponse> transcripts = transcriptsList.stream()
                            .map(transcript -> TranscriptResponse.from(transcript))
                            .collect(Collectors.toList());

                    JsonNode transcriptsNode = objectMapper.valueToTree(transcripts);
                    response.set("transcripts", transcriptsNode);

                    session.sendMessage(new TextMessage(response.toString()));

                } catch (Exception e) {
                    log.error("세션 복구 실패 - sessionId: {}", sessionId, e);

                    ObjectNode errorResponse = objectMapper.createObjectNode();
                    errorResponse.put("type", "SESSION_RECOVERY");
                    errorResponse.put("success", false);
                    errorResponse.put("errorMessage", "대화 내역 복구 실패");

                    session.sendMessage(new TextMessage(errorResponse.toString()));
                }

            }

        } catch (Exception e) {
            log.error("transcript 저장 실패 - sessionId: {}", sessionId, e);
        }
    }


    /**
     * WebSocket 연결이 종료되었을 때 호출됩니다.
     *
     * 처리 내용:
     * 1. SessionCoordinator를 통해 전체 세션 종료
     *    - GPT 세션 종료
     *    - WebRTC 상태 제거
     *    - DB 매핑 제거
     *    - WebSocket 세션 제거
     *
     * @param session WebSocket 연결 객체
     * @param status 종료 상태
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String sessionId = extractSessionId(session);
        sessions.remove(sessionId);

        log.info("TranscriptHandler - WebSocket 연결 종료 - sessionId: {}, status: {}",
                sessionId, status);

        try {
            // SessionCoordinator 를 통해 전체 종료
            sessionCoordinator.terminateSession(sessionId);

            log.info("TranscriptHandler - 세션 종료 완료 - sessionId: {}", sessionId);

        } catch (Exception e) {
            log.error("TranscriptHandler - 세션 종료 실패 - sessionId: {}", sessionId, e);
        }
    }

    /**
     * WebSocket URI에서 sessionId를 추출합니다.
     *
     * URI 형식: /ws/audio/{sessionId}
     * 예시: /ws/audio/abc-123 -> "abc-123"
     *
     * @param session WebSocket 연결 객체
     * @return sessionId
     */
    private String extractSessionId(WebSocketSession session) {
        String path = session.getUri().getPath();
        String[] parts = path.split("/");
        return parts[parts.length - 1];
    }

    /**
     * 재연결 시 대화 기록을 확인해서 프론트엔드로 대화 내용을 전송합니다.
     *
     */
}
