package com.aid.train.backend.websocket.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket 연결을 관리하는 매니저
 *
 * 역할:
 * - sessionId별 WebSocket 연결 객체 저장
 * - 세션 등록/조회/제거
 * - 현재 접속자 수 추적
 *
 * 왜 필요한가?
 * - 여러 사용자가 동시에 접속하므로 각 연결을 구분해서 관리해야 함
 * - 특정 사용자에게 메시지를 보내려면 해당 사용자의 WebSocketSession이 필요
 *
 * @author 김경민
 * @since 2025-10-15
 * @version 1.0.0
 */
@Slf4j
@Component
public class WebSocketSessionManager {

    /**
     * sessionId → WebSocketSession 매핑
     * ConcurrentHashMap: 여러 스레드가 동시에 접근해도 안전
     */
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    /**
     * sessionId → 연결 시작 시간 매핑
     * 세션 타임아웃 체크에 사용
     */
    private final Map<String, Instant> connectionTimes = new ConcurrentHashMap<>();

    /**
     * 새로운 WebSocket 연결을 등록합니다.
     *
     * 언제 호출되나?
     * - AudioHandler의 afterConnectionEstablished()에서 호출
     * - 클라이언트가 WebSocket 연결을 성공했을 때
     *
     * @param sessionId 대화 세션 ID
     * @param session WebSocket 연결 객체
     */
    public void registerSession(String sessionId, WebSocketSession session) {
        sessions.put(sessionId, session);
        connectionTimes.put(sessionId, Instant.now());
        log.info("WebSocket 세션 등록 완료 - sessionId: {}, wsSessionId: {}",
                sessionId, session.getId());
    }

    /**
     * sessionId로 WebSocket 연결을 조회합니다.
     *
     * 언제 사용하나?
     * - 특정 사용자에게 메시지를 보낼 때
     * - 예: AI 응답 음성을 사용자에게 전송
     *
     * @param sessionId 대화 세션 ID
     * @return WebSocketSession 객체 (없으면 null)
     */
    public WebSocketSession getSession(String sessionId) {
        WebSocketSession session = sessions.get(sessionId);
        if (session == null) {
            log.warn("세션을 찾을 수 없음 - sessionId: {}", sessionId);
        }
        return session;
    }

    /**
     * WebSocket 연결을 제거합니다.
     *
     * 언제 호출되나?
     * - AudioHandler의 afterConnectionClosed()에서 호출
     * - 클라이언트가 연결을 끊었을 때
     * - 타임아웃으로 강제 종료할 때
     *
     * @param sessionId 대화 세션 ID
     */
    public void removeSession(String sessionId) {
        WebSocketSession removed = sessions.remove(sessionId);
        connectionTimes.remove(sessionId);

        if (removed != null) {
            log.info("WebSocket 세션 제거 완료 - sessionId: {}", sessionId);
        } else {
            log.warn("제거할 세션이 없음 - sessionId: {}", sessionId);
        }
    }

    /**
     * 세션이 존재하는지 확인합니다.
     *
     * @param sessionId 대화 세션 ID
     * @return 세션 존재 여부
     */
    public boolean hasSession(String sessionId) {
        return sessions.containsKey(sessionId);
    }

    /**
     * 현재 활성화된 세션 수를 반환합니다.
     *
     * 언제 사용하나?
     * - 모니터링 대시보드
     * - 동시 접속자 제한 체크
     *
     * @return 현재 접속 중인 사용자 수
     */
    public int getActiveSessionCount() {
        return sessions.size();
    }

    /**
     * 세션의 연결 시간을 조회합니다.
     *
     * @param sessionId 대화 세션 ID
     * @return 연결 시작 시간
     */
    public Instant getConnectionTime(String sessionId) {
        return connectionTimes.get(sessionId);
    }

    /**
     * 모든 세션을 제거합니다.
     *
     * 언제 사용하나?
     * - 서버 종료 시 정리
     * - 긴급 상황에서 모든 연결 종료
     */
    public void clearAll() {
        int count = sessions.size();
        sessions.clear();
        connectionTimes.clear();
        log.info("모든 WebSocket 세션 제거 완료 - 제거된 세션 수: {}", count);
    }
}