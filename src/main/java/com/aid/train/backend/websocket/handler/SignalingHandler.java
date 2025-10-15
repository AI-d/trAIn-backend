package com.aid.train.backend.websocket.handler;

import com.aid.train.backend.websocket.dto.client.AIRealtimeClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class SignalingHandler extends AbstractWebSocketHandler {

    private final ObjectMapper objectMapper;

    // 세션 ID → WebSocketSession 매핑
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String sessionId = extractSessionId(session);
        sessions.put(sessionId, session);
        log.info("WebSocket 연결 성공: sessionId={}", sessionId);

        // 연결 성공 메시지 전송
        session.sendMessage(new TextMessage("Connected to session: " + sessionId));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

        String sessionId = extractSessionId(session);
        JsonNode json = objectMapper.readTree(message.getPayload());
        String type = json.get("type").asText();

        log.info("메시지 수신 - sessionId={}, type={}", sessionId, type);

        switch (type) {
            case "OFFER":
                handleOffer(session, json);
                break;

            case "ANSWER":
                handleAnswer(session, json);
                break;

            case "ICE_CANDIDATE":
                handleIceCandidate(session, json);   // ICE 후보 처리
                break;



            default:
                log.info("알 수 없는 메세지 타입: {}", type);
        }


        // 경민님께서 작성하신 echo 코드
        /*String sessionId = extractSessionId(session);
        String payload = message.getPayload();

        log.info("메시지 수신 - sessionId={}, payload={}", sessionId, payload);

        // Sprint 2: Echo 테스트 (받은 메시지 그대로 돌려보냄)
        String response = String.format("Echo from %s: %s", sessionId, payload);
        session.sendMessage(new TextMessage(response));

        log.info("메시지 전송 - sessionId={}", sessionId);*/
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        String sessionId = extractSessionId(session);
        byte[] audioBytes = message.getPayload().array();

        log.info("Binary 메시지 수신 - sessionId={}, length={}", sessionId, audioBytes.length);

        sendAudioToAI(sessionId, audioBytes);
    }

    private void sendAudioToAI(String sessionId, byte[] audioBytes) {

    }


    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String sessionId = extractSessionId(session);
        sessions.remove(sessionId);
        log.info("WebSocket 연결 종료: sessionId={}, status={}", sessionId, status);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        String sessionId = extractSessionId(session);
        log.error("WebSocket 에러 - sessionId={}", sessionId, exception);
    }

    /**
     * WebSocket URI에서 sessionId 추출
     * 예: /ws/signaling/test-123 → "test-123"
     */
    private String extractSessionId(WebSocketSession session) {
        String path = session.getUri().getPath();
        // /ws/signaling/test-123 → test-123
        String[] parts = path.split("/");
        return parts[parts.length - 1];  // 마지막 부분이 sessionId
    }


    private void handleOffer(WebSocketSession session, JsonNode json) {
        String sessionId = extractSessionId(session);

        sessions.put(sessionId, session);

        log.info("Offer 수신 - sessionId: {}", sessionId);
    }

    private void handleAnswer(WebSocketSession session, JsonNode json) {
        String sessionId = extractSessionId(session);
        log.info("Answer 수신 - sessionId: {}", sessionId);
    }

    private void handleIceCandidate(WebSocketSession session, JsonNode json) {
        String sessionId = extractSessionId(session);
        JsonNode candidate = json.get("candidate");

        log.info("ICE 후보 수신 - sessionId: {}, candidate: {}", sessionId, candidate);
    }
}
