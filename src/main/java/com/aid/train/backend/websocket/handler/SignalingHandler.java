package com.aid.train.backend.websocket.handler;

import com.aid.train.backend.websocket.dto.server.AnswerMessage;
import com.aid.train.backend.websocket.dto.client.IceCandidateMessage;
import com.aid.train.backend.websocket.dto.client.OfferMessage;
import com.aid.train.backend.websocket.model.MessageType;
import com.aid.train.backend.websocket.service.GptSessionManager;
import com.aid.train.backend.websocket.service.WebRtcStateManager;
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

/**
 * WebRTC Signaling을 처리하는 WebSocket 핸들러
 *
 * 역할:
 * - SDP Offer/Answer 교환
 * - ICE Candidate 교환
 * - WebRTC 연결 설정 중개
 *
 * 처리 메시지:
 * - OFFER: 클라이언트가 보낸 SDP Offer
 * - ANSWER: 서버/다른 피어가 보낸 SDP Answer (필요 시)
 * - ICE_CANDIDATE: ICE 후보 정보
 *
 * Signaling 흐름:
 * 1. 클라이언트가 OFFER 전송
 * 2. 서버가 ANSWER 응답
 * 3. 양측이 ICE_CANDIDATE 교환
 * 4. WebRTC 연결 수립
 *
 * @author 김경민
 * @since 2025-10-13
 * @version 1.2.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SignalingHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;
    private final WebRtcStateManager webRtcStateManager;
    private final GptSessionManager gptSessionManager;

    /**
     * sessionId -> WebSocketSession 매핑
     * Signaling 메시지를 특정 세션으로 전송하기 위해 필요
     */
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    /**
     * Signaling WebSocket 연결이 성공했을 때 호출됩니다.
     *
     * @param session WebSocket 연결 객체
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String sessionId = extractSessionId(session);
        sessions.put(sessionId, session);

        log.info("SignalingHandler - WebSocket 연결 성공 - sessionId: {}", sessionId);

        // 연결 성공 메시지 전송
        String welcomeMsg = String.format("{\"type\":\"connected\",\"sessionId\":\"%s\"}", sessionId);
        session.sendMessage(new TextMessage(welcomeMsg));
    }

    /**
     * Signaling 메시지를 수신했을 때 호출됩니다.
     *
     * 처리 메시지 타입:
     * - OFFER: SDP Offer 처리
     * - ANSWER: SDP Answer 처리 (P2P 시나리오)
     * - ICE_CANDIDATE: ICE 후보 처리
     *
     * @param session WebSocket 연결 객체
     * @param message 텍스트 메시지 (JSON)
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String sessionId = extractSessionId(session);
        String payload = message.getPayload();

        try {
            // JSON 파싱하여 메시지 타입 확인
            JsonNode jsonNode = objectMapper.readTree(payload);
            String typeStr = jsonNode.get("type").asText().toUpperCase();
            MessageType messageType = MessageType.valueOf(typeStr);

            log.info("SignalingHandler - 메시지 수신 - sessionId: {}, type: {}",
                    sessionId, messageType);

            switch (messageType) {
                case OFFER -> handleOffer(session, payload);
                case ANSWER -> handleAnswer(session, payload);
                case ICE_CANDIDATE -> handleIceCandidate(session, payload);
                default -> log.warn("SignalingHandler - 알 수 없는 메시지 타입 - type: {}", messageType);
            }

        } catch (Exception e) {
            log.error("SignalingHandler - 메시지 처리 실패 - sessionId: {}", sessionId, e);

            // 에러 응답 전송
            String errorMsg = String.format(
                    "{\"type\":\"error\",\"message\":\"메시지 처리 실패: %s\"}",
                    e.getMessage()
            );
            session.sendMessage(new TextMessage(errorMsg));
        }
    }

    /**
     * SDP Offer를 처리합니다.
     *
     * 처리 내용:
     * 1. Offer 메시지 파싱
     * 2. WebRTC 상태를 CONNECTING으로 업데이트
     * 3. Answer 생성 (실제 WebRTC 구현 필요)
     * 4. Answer 응답 전송
     *
     * @param session WebSocket 연결 객체
     * @param payload JSON 메시지
     */
    private void handleOffer(WebSocketSession session, String payload) throws Exception {
        String sessionId = extractSessionId(session);

        // Offer 파싱
        OfferMessage offer = objectMapper.readValue(payload, OfferMessage.class);

        log.info("SignalingHandler - OFFER 수신 - sessionId: {}, sdp 길이: {}",
                sessionId, offer.getSdp().length());

        // WebRTC 상태 업데이트
        webRtcStateManager.updateState(sessionId, WebRtcStateManager.State.CONNECTING);

        // Gpt Realtime API 로 Offer 전송
        String gptAnswerSdp = gptSessionManager.connectToGptRealtime(sessionId, offer.getSdp());

        // TODO: 실제 WebRTC Answer 생성 로직
        // 현재는 Mock Answer 반환
        AnswerMessage answer = AnswerMessage.builder()
                .sdp(gptAnswerSdp)
                .build();

        String answerJson = objectMapper.writeValueAsString(answer);
        session.sendMessage(new TextMessage(answerJson));

        log.info("SignalingHandler - ANSWER 전송 완료 - sessionId: {}", sessionId);
    }

    /**
     * SDP Answer를 처리합니다.
     *
     * P2P 시나리오에서 사용
     * 서버가 Offer를 보내고 클라이언트가 Answer를 보낼 때
     *
     * @param session WebSocket 연결 객체
     * @param payload JSON 메시지
     */
    private void handleAnswer(WebSocketSession session, String payload) throws Exception {
        String sessionId = extractSessionId(session);

        // Answer 파싱
        AnswerMessage answer = objectMapper.readValue(payload, AnswerMessage.class);

        log.info("SignalingHandler - ANSWER 수신 - sessionId: {}, sdp 길이: {}",
                sessionId, answer.getSdp().length());

        // TODO: Answer 처리 로직
        // PeerConnection에 setRemoteDescription 등
    }

    /**
     * ICE Candidate를 처리합니다.
     *
     * 처리 내용:
     * 1. ICE Candidate 파싱
     * 2. 상대방에게 전달 (P2P) 또는 로컬 처리
     *
     * @param session WebSocket 연결 객체
     * @param payload JSON 메시지
     */
    private void handleIceCandidate(WebSocketSession session, String payload) throws Exception {
        String sessionId = extractSessionId(session);

        // ICE Candidate 파싱
        IceCandidateMessage message = objectMapper.readValue(payload, IceCandidateMessage.class);

        // 중첩된 candidate 객체에서 실제 candidate 문자열 추출
        String candidateString = message.getCandidate() != null
                ? message.getCandidate().getCandidate()
                : null;

        log.info("SignalingHandler - ICE_CANDIDATE 수신 - sessionId: {}, candidate: {}, sdpMid: {}, sdpMLineIndex: {}",
                sessionId,
                candidateString,
                message.getCandidate() != null ? message.getCandidate().getSdpMid() : null,
                message.getCandidate() != null ? message.getCandidate().getSdpMLineIndex() : null);

        // TODO: ICE Candidate 처리 로직
        // PeerConnection에 addIceCandidate 등

        // Echo back (테스트용)
        session.sendMessage(new TextMessage(payload));
    }

    /**
     * Signaling WebSocket 연결이 종료되었을 때 호출됩니다.
     *
     * @param session WebSocket 연결 객체
     * @param status 종료 상태
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String sessionId = extractSessionId(session);
        sessions.remove(sessionId);

        log.info("SignalingHandler - WebSocket 연결 종료 - sessionId: {}, status: {}",
                sessionId, status);

        // WebRTC 상태 업데이트
        webRtcStateManager.updateState(sessionId, WebRtcStateManager.State.DISCONNECTED);
    }

    /**
     * WebSocket 전송 에러 발생 시 호출됩니다.
     *
     * @param session WebSocket 연결 객체
     * @param exception 발생한 예외
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        String sessionId = extractSessionId(session);

        log.error("SignalingHandler - WebSocket 에러 - sessionId: {}", sessionId, exception);

        // WebRTC 상태를 FAILED로 업데이트
        webRtcStateManager.updateState(sessionId, WebRtcStateManager.State.FAILED);
    }

    /**
     * 특정 세션에게 메시지를 전송합니다.
     *
     * @param sessionId 대화 세션 ID
     * @param message 전송할 메시지
     */
    public void sendMessage(String sessionId, String message) {
        WebSocketSession session = sessions.get(sessionId);

        if (session == null || !session.isOpen()) {
            log.warn("SignalingHandler - 세션 없음 - sessionId: {}", sessionId);
            return;
        }

        try {
            session.sendMessage(new TextMessage(message));
            log.debug("SignalingHandler - 메시지 전송 완료 - sessionId: {}", sessionId);
        } catch (Exception e) {
            log.error("SignalingHandler - 메시지 전송 실패 - sessionId: {}", sessionId, e);
        }
    }

    /**
     * WebSocket URI에서 sessionId를 추출합니다.
     *
     * URI 형식: /ws/signaling/{sessionId}
     * 예시: /ws/signaling/abc-123 -> "abc-123"
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
