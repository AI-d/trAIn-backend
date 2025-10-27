package com.aid.train.backend.websocket.handler;

import com.aid.train.backend.domain.session.service.TranscriptService;
import com.aid.train.backend.websocket.service.SessionCoordinator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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

                if("USER".equals(speaker)) {
                    transcriptService.saveUserTranscript(sessionId, content);
                } else if("AI".equals(speaker)) {
                    transcriptService.saveAiTranscript(sessionId, content);
                }

                log.info("transcript 저장 - sessionId: {}, speaker: {}, content: {}", sessionId, speaker, content);
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
}
