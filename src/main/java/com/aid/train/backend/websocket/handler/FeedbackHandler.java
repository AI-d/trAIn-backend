package com.aid.train.backend.websocket.handler;

import com.aid.train.backend.websocket.dto.server.FeedbackMessage;
import com.aid.train.backend.websocket.service.WebSocketSessionManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * 실시간 피드백 전송을 담당하는 WebSocket 핸들러
 *
 * 역할:
 * - 실시간 분석 결과를 클라이언트에게 전송
 * - 발화 속도(WPM), 추임새, 음량, 감정 등의 피드백 제공
 * - 독립적인 WebSocket 채널로 동작
 *
 * 엔드포인트:
 * - /ws/feedback/{sessionId}
 *
 * 사용 흐름:
 * 1. 클라이언트가 대화 시작 시 Feedback WebSocket 연결
 * 2. 서버가 실시간 분석 결과를 주기적으로 전송
 * 3. 클라이언트가 UI에 피드백 표시
 *
 * 특징:
 * - 단방향 통신 (서버 -> 클라이언트)
 * - 클라이언트는 메시지를 보내지 않음
 * - 연결만 유지하고 피드백 수신
 *
 * @author 김경민
 * @since 2025-10-15
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FeedbackHandler extends TextWebSocketHandler {

    private final WebSocketSessionManager wsSessionManager;
    private final ObjectMapper objectMapper;

    /**
     * Feedback WebSocket 연결이 성공했을 때 호출됩니다.
     *
     * 처리 내용:
     * 1. URI에서 sessionId 추출
     * 2. 연결 성공 로그 기록
     *
     * 참고:
     * - 실제 세션 등록은 AudioHandler에서 수행됨
     * - 여기서는 피드백 채널 연결만 확인
     *
     * @param session WebSocket 연결 객체
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String sessionId = extractSessionId(session);

        log.info("FeedbackHandler - WebSocket 연결 시작 - sessionId: {}", sessionId);

        try {
            // 연결 확인 메시지 전송
            String welcomeMsg = objectMapper.writeValueAsString(
                    FeedbackMessage.builder()
                            .sessionId(sessionId)
                            .tip("실시간 피드백이 시작됩니다")
                            .build()
            );

            session.sendMessage(new TextMessage(welcomeMsg));

            log.info("FeedbackHandler - 연결 확인 완료 - sessionId: {}", sessionId);

        } catch (Exception e) {
            log.error("FeedbackHandler - 연결 확인 실패 - sessionId: {}", sessionId, e);
        }
    }

    /**
     * 클라이언트로부터 텍스트 메시지를 수신했을 때 호출됩니다.
     *
     * 참고:
     * - Feedback 채널은 단방향이므로 일반적으로 메시지를 받지 않음
     * - PING 같은 제어 메시지만 처리
     *
     * @param session WebSocket 연결 객체
     * @param message 텍스트 메시지
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String sessionId = extractSessionId(session);
        String payload = message.getPayload();

        log.debug("FeedbackHandler - 메시지 수신 - sessionId: {}, payload: {}",
                sessionId, payload);

        // PING 메시지 처리
        if ("PING".equals(payload)) {
            session.sendMessage(new TextMessage("PONG"));
        }
    }

    /**
     * Feedback WebSocket 연결이 종료되었을 때 호출됩니다.
     *
     * @param session WebSocket 연결 객체
     * @param status 종료 상태
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String sessionId = extractSessionId(session);

        log.info("FeedbackHandler - WebSocket 연결 종료 - sessionId: {}, status: {}",
                sessionId, status);
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

        log.error("FeedbackHandler - WebSocket 에러 - sessionId: {}", sessionId, exception);
    }

    /**
     * 실시간 피드백을 클라이언트에게 전송합니다.
     *
     * 언제 호출되나?
     * - SpeechAnalyzer에서 분석 완료 시
     * - 주기적인 피드백 생성 시
     *
     * @param sessionId 대화 세션 ID
     * @param feedback 피드백 메시지
     */
    public void sendFeedback(String sessionId, FeedbackMessage feedback) {
        try {
            // sessionId로 Audio WebSocket 세션 조회
            // (Feedback은 별도 연결이지만 sessionId는 동일)
            WebSocketSession wsSession = wsSessionManager.getSession(sessionId);

            if (wsSession == null || !wsSession.isOpen()) {
                log.warn("FeedbackHandler - WebSocket 세션 없음 - sessionId: {}", sessionId);
                return;
            }

            // FeedbackMessage를 JSON으로 변환
            String json = objectMapper.writeValueAsString(feedback);

            // 전송
            wsSession.sendMessage(new TextMessage(json));

            log.debug("FeedbackHandler - 피드백 전송 완료 - sessionId: {}, wpm: {}, fillerCount: {}",
                    sessionId, feedback.getWpm(), feedback.getFillerCount());

        } catch (Exception e) {
            log.error("FeedbackHandler - 피드백 전송 실패 - sessionId: {}", sessionId, e);
        }
    }

    /**
     * 여러 sessionId에게 동시에 피드백을 전송합니다.
     *
     * 사용 예시:
     * - 전체 사용자에게 공지사항 전송
     * - 특정 그룹에게 일괄 피드백
     *
     * @param sessionIds 대화 세션 ID 목록
     * @param feedback 피드백 메시지
     */
    public void broadcastFeedback(Iterable<String> sessionIds, FeedbackMessage feedback) {
        sessionIds.forEach(sessionId -> sendFeedback(sessionId, feedback));
    }

    /**
     * WebSocket URI에서 sessionId를 추출합니다.
     *
     * URI 형식: /ws/feedback/{sessionId}
     * 예시: /ws/feedback/abc-123 -> "abc-123"
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