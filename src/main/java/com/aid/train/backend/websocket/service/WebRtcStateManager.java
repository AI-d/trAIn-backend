package com.aid.train.backend.websocket.service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketSession;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebRTC 연결 상태를 관리하는 매니저
 *
 * 역할:
 * - sessionId별 WebRTC 연결 상태 추적
 * - 연결/해제 시간 기록
 * - 오디오 송수신 통계 수집
 *
 * 왜 필요한가?
 * - 음성이 제대로 전달되는지 확인
 * - 연결이 끊겼는지 감지
 * - 디버깅 및 모니터링용
 *
 * @author 김경민
 * @since 2025-10-15
 * @version 1.0.0
 */
@Slf4j
@Component
public class WebRtcStateManager {

    /**
     * OpenAI API Key (환경변수에서 주입)
     */
    @Value("${spring.ai.openai.api-key}")
    private String openAiApiKey;

    /**
     * WebRTC 연결 상태 정의
     */
    @Getter
    public enum State {
        DISCONNECTED("연결 안됨"),
        CONNECTING("연결 중"),
        CONNECTED("연결됨"),
        FAILED("연결 실패");

        private final String description;

        State(String description) {
            this.description = description;
        }
    }

    /**
     * WebRTC 상태 정보를 담는 클래스
     */
    @Getter
    public static class StateInfo {
        private State state;
        private Instant connectedAt;
        private Instant disconnectedAt;
        private long audioPacketsSent;
        private long audioPacketsReceived;
        private long totalBytesSent;
        private long totalBytesReceived;

        public StateInfo() {
            this.state = State.DISCONNECTED;
            this.audioPacketsSent = 0;
            this.audioPacketsReceived = 0;
            this.totalBytesSent = 0;
            this.totalBytesReceived = 0;
        }

        public void setState(State state) {
            this.state = state;
            if (state == State.CONNECTED) {
                this.connectedAt = Instant.now();
            } else if (state == State.DISCONNECTED || state == State.FAILED) {
                this.disconnectedAt = Instant.now();
            }
        }

        public void incrementAudioPacketsSent() {
            this.audioPacketsSent++;
        }

        public void incrementAudioPacketsReceived() {
            this.audioPacketsReceived++;
        }

        public void addBytesSent(long bytes) {
            this.totalBytesSent += bytes;
        }

        public void addBytesReceived(long bytes) {
            this.totalBytesReceived += bytes;
        }

        public Long getConnectionDurationSeconds() {
            if (connectedAt == null) {
                return null;
            }
            Instant end = disconnectedAt != null ? disconnectedAt : Instant.now();
            return end.getEpochSecond() - connectedAt.getEpochSecond();
        }
    }

    /**
     * sessionId -> WebRTC 상태 정보 매핑
     */
    private final Map<String, StateInfo> states = new ConcurrentHashMap<>();

    /**
     * WebRTC 연결을 초기화합니다.
     *
     * 언제 호출되나?
     * - SessionCoordinator의 initializeSession()
     * - WebSocket 연결 성공 후 WebRTC 준비 시
     *
     * @param sessionId 대화 세션 ID
     */
    public void initializeState(String sessionId) {
        StateInfo stateInfo = new StateInfo();
        stateInfo.setState(State.CONNECTING);
        states.put(sessionId, stateInfo);
        log.info("WebRTC 상태 초기화 - sessionId: {}, state: CONNECTING", sessionId);
    }

    /**
     * WebRTC 연결 상태를 업데이트합니다.
     *
     * 언제 호출되나?
     * - AudioHandler의 handleBinaryMessage()
     * - WebRTC 연결 성공 시
     * - WebRTC 연결 실패 시
     * - WebRTC 연결 끊김 감지 시
     *
     * @param sessionId 대화 세션 ID
     * @param state 새로운 상태
     */
    public void updateState(String sessionId, State state) {
        StateInfo stateInfo = states.get(sessionId);

        if (stateInfo == null) {
            log.warn("상태 정보 없음, 새로 생성 - sessionId: {}", sessionId);
            stateInfo = new StateInfo();
            states.put(sessionId, stateInfo);
        }

        State oldState = stateInfo.getState();
        stateInfo.setState(state);

        log.info("WebRTC 상태 변경 - sessionId: {}, {} -> {}",
                sessionId, oldState, state);
    }

    /**
     * 현재 WebRTC 상태를 조회합니다.
     *
     * @param sessionId 대화 세션 ID
     * @return 현재 상태
     */
    public State getState(String sessionId) {
        StateInfo stateInfo = states.get(sessionId);
        return stateInfo != null ? stateInfo.getState() : State.DISCONNECTED;
    }

    /**
     * WebRTC가 연결되어 있는지 확인합니다.
     *
     * 언제 사용하나?
     * - AudioHandler에서 음성 전송 전 확인
     * - SessionCoordinator에서 세션 유효성 검증
     *
     * @param sessionId 대화 세션 ID
     * @return 연결 여부
     */
    public boolean isConnected(String sessionId) {
        return getState(sessionId) == State.CONNECTED;
    }

    /**
     * 오디오 패킷 전송 통계를 업데이트합니다.
     *
     * 언제 호출되나?
     * - AudioHandler에서 음성 데이터 전송 시
     *
     * @param sessionId 대화 세션 ID
     * @param bytes 전송한 바이트 수
     */
    public void recordAudioSent(String sessionId, long bytes) {
        StateInfo stateInfo = states.get(sessionId);
        if (stateInfo != null) {
            stateInfo.incrementAudioPacketsSent();
            stateInfo.addBytesSent(bytes);
            log.debug("오디오 전송 기록 - sessionId: {}, bytes: {}", sessionId, bytes);
        }
    }

    /**
     * 오디오 패킷 수신 통계를 업데이트합니다.
     *
     * 언제 호출되나?
     * - AudioHandler에서 음성 데이터 수신 시
     *
     * @param sessionId 대화 세션 ID
     * @param bytes 수신한 바이트 수
     */
    public void recordAudioReceived(String sessionId, long bytes) {
        StateInfo stateInfo = states.get(sessionId);
        if (stateInfo != null) {
            stateInfo.incrementAudioPacketsReceived();
            stateInfo.addBytesReceived(bytes);
            log.debug("오디오 수신 기록 - sessionId: {}, bytes: {}", sessionId, bytes);
        }
    }

    /**
     * 상태 정보를 조회합니다.
     *
     * 언제 사용하나?
     * - 모니터링 대시보드
     * - 디버깅
     * - 통계 수집
     *
     * @param sessionId 대화 세션 ID
     * @return 상태 정보
     */
    public StateInfo getStateInfo(String sessionId) {
        return states.get(sessionId);
    }

    /**
     * WebRTC 상태를 제거합니다.
     *
     * 언제 호출되나?
     * - SessionCoordinator의 terminateSession()
     * - 대화 종료 시
     *
     * @param sessionId 대화 세션 ID
     */
    public void removeState(String sessionId) {
        StateInfo removed = states.remove(sessionId);

        if (removed != null) {
            log.info("WebRTC 상태 제거 - sessionId: {}, 연결 시간: {}초, " +
                            "전송: {}패킷/{}bytes, 수신: {}패킷/{}bytes",
                    sessionId,
                    removed.getConnectionDurationSeconds(),
                    removed.getAudioPacketsSent(),
                    removed.getTotalBytesSent(),
                    removed.getAudioPacketsReceived(),
                    removed.getTotalBytesReceived());
        }
    }

    /**
     * 현재 추적 중인 상태 수를 반환합니다.
     *
     * @return 상태 수
     */
    public int getStateCount() {
        return states.size();
    }

    /**
     * 모든 상태를 제거합니다.
     *
     * 언제 사용하나?
     * - 서버 종료 시
     */
    public void clearAll() {
        int count = states.size();
        states.clear();
        log.info("모든 WebRTC 상태 제거 완료 - 제거된 상태 수: {}", count);
    }


}