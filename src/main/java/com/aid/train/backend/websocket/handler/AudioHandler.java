package com.aid.train.backend.websocket.handler;

import com.aid.train.backend.websocket.dto.server.GptSession;
import com.aid.train.backend.websocket.service.GptSessionManager;
import com.aid.train.backend.websocket.service.SessionCoordinator;
import com.aid.train.backend.websocket.service.WebRtcStateManager;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

/**
 * 음성 데이터 송수신을 담당하는 WebSocket 핸들러
 *
 * 역할:
 * - 클라이언트로부터 음성 데이터 수신
 * - SessionCoordinator를 통해 GPT로 라우팅
 * - GPT 응답을 클라이언트로 전송 (SessionCoordinator가 처리)
 *
 * 처리 흐름:
 * 1. 연결 시작: afterConnectionEstablished()
 *    -> SessionCoordinator.initializeSession()
 *
 * 2. 음성 수신: handleBinaryMessage()
 *    -> SessionCoordinator.routeAudioToGpt()
 *
 * 3. 연결 종료: afterConnectionClosed()
 *    -> SessionCoordinator.terminateSession()
 *
 * @author 김경민
 * @since 2025-10-15
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AudioHandler extends AbstractWebSocketHandler {

    private final SessionCoordinator sessionCoordinator;
    private final WebRtcStateManager webRtcStateManager;
    private final GptSessionManager gptSessionManager;

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        JsonObject data = JsonParser.parseString(message.getPayload()).getAsJsonObject();
        String sessionId = extractSessionId(session);

        if ("SESSION_INIT".equals(data.get("type").getAsString())) {
            Long scenarioId = data.get("scenarioId").getAsLong();
            // session_init 메세지로 부터 유저 식별정보 받아오기
            // 식별자로 유저 ID 조회

            // 시나리오 ID를 세션 속성에 저장
            session.getAttributes().put("scenarioId", scenarioId);
            // 유저 ID를 세션 속성에 저장


            // 세션 초기화 (GPT 연결, DialogueSession 생성 등)
            sessionCoordinator.initializeSession(sessionId, session, scenarioId);
            log.info("GPT 세션 생성 완료 - sessionId: {}", sessionId);

        } else if("speech.end".equals(data.get("type").getAsString())) {
            log.info("speech.end 수신 - sessionId: {}", sessionId);
            //sessionCoordinator.commitUserAudio(sessionId);
        }


    }
        /**
         * WebSocket 연결이 성공했을 때 호출됩니다.
         *
         * 처리 내용:
         * 1. URI에서 sessionId 추출
         * 2. SessionCoordinator를 통해 전체 세션 초기화
         *    - WebSocket 세션 등록
         *    - DB 조회 및 매핑
         *    - GPT 연결
         *    - WebRTC 상태 초기화
         *
         * @param session WebSocket 연결 객체
         */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String sessionId = extractSessionId(session);


        log.info("AudioHandler - WebSocket 연결 시작 - sessionId: {}", sessionId);

        try {
            // SessionCoordinator를 통해 전체 초기화
            // sessionCoordinator.initializeSession(sessionId, session, scenarioId);

            log.info("AudioHandler - 세션 초기화 완료 - sessionId: {}", sessionId);

        } catch (Exception e) {
            log.error("AudioHandler - 세션 초기화 실패 - sessionId: {}", sessionId, e);
            session.close(CloseStatus.SERVER_ERROR);
        }
    }

    /**
     * 바이너리 메시지(음성 데이터)를 수신했을 때 호출됩니다.
     *
     * 처리 내용:
     * 1. 바이너리 데이터 추출
     * 2. WebRTC 연결 상태 확인
     * 3. SessionCoordinator를 통해 GPT로 라우팅
     *
     * 데이터 형식:
     * - 클라이언트에서 Base64 인코딩된 음성 데이터 전송
     * - 여기서는 바이너리로 수신
     *
     * @param session WebSocket 연결 객체
     * @param message 바이너리 메시지 (음성 데이터)
     */
    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        String sessionId = extractSessionId(session);
        byte[] audioData = message.getPayload().array();

        try {

            // WebRTC 확인
            if (!webRtcStateManager.isConnected(sessionId)) {
                log.warn("WebRTC 연결 안됨 - sessionId: {}", sessionId);
                return;
            }

            GptSession gptSession = gptSessionManager.getGptSession(sessionId);
            if(gptSession == null) {
                log.warn("gpt 세션 미준비, 큐에 저장 - sessionId: {}, ready: {}", sessionId, gptSession != null ? gptSession.getIsReady() : "false");
                sessionCoordinator.queueAudio(sessionId, audioData);
                return;
            }

            if(audioData == null || audioData.length == 0) {
                log.warn("AudioHandler - 빈 오디오 데이터 - sessionId: {}", sessionId);
                return;
            }

            // gpt에 음성 전송
            sessionCoordinator.routeAudioToGpt(sessionId, audioData);

        } catch (Exception e) {
            log.error("오디오 처리 실패 - sessionId: {}", sessionId, e);
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

        log.info("AudioHandler - WebSocket 연결 종료 - sessionId: {}, status: {}",
                sessionId, status);

        try {
            // SessionCoordinator 를 통해 전체 종료
            sessionCoordinator.terminateSession(sessionId);

            log.info("AudioHandler - 세션 종료 완료 - sessionId: {}", sessionId);

        } catch (Exception e) {
            log.error("AudioHandler - 세션 종료 실패 - sessionId: {}", sessionId, e);
        }
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

        log.error("AudioHandler - WebSocket 에러 - sessionId: {}", sessionId, exception);

        try {
            // 에러 발생 시 세션 종료
            sessionCoordinator.terminateSession(sessionId);

        } catch (Exception e) {
            log.error("AudioHandler - 에러 처리 중 실패 - sessionId: {}", sessionId, e);
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