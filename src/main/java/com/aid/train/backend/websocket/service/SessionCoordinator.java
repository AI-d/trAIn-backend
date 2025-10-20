package com.aid.train.backend.websocket.service;

import com.aid.train.backend.domain.scenario.entity.Scenario;
import com.aid.train.backend.domain.scenario.repository.ScenarioRepository;
import com.aid.train.backend.domain.session.entity.DialogueSession;
import com.aid.train.backend.websocket.dto.client.SessionInitMessage;
import com.aid.train.backend.websocket.dto.common.AudioFormat;
import com.aid.train.backend.websocket.dto.server.RealtimeSession;
import com.aid.train.backend.websocket.dto.server.GptSession;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Base64;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 모든 세션 관련 Manager를 통합 조정하는 코디네이터
 *
 * 역할:
 * - WebSocketSessionManager, DialogueSessionMapper, GptSessionManager, WebRtcStateManager를 통합 관리
 * - 세션 생명주기 전체를 조율
 * - 데이터 라우팅 (사용자 음성 -> GPT, GPT 응답 -> 사용자)
 *
 * 왜 필요한가?
 * - 4개의 Manager가 따로 놀면 안 됨
 * - 사용자 접속/종료 시 모든 Manager를 한 번에 처리
 * - 음성 데이터 흐름을 중앙에서 제어
 *
 * @author 김경민
 * @since 2025-10-15
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SessionCoordinator {

    private final WebSocketSessionManager wsSessionManager;
    private final DialogueSessionMapper dialogueSessionMapper;
    private final GptSessionManager gptSessionManager;
    private final WebRtcStateManager webRtcStateManager;

    private final ScenarioRepository scenarioRepository;

    private final ObjectMapper objectMapper;

    private final Map<String, GptSession> gptSessionMap = new ConcurrentHashMap<>();

    /**
     * 세션 전체를 초기화합니다.
     *
     * 언제 호출되나?
     * - AudioHandler의 afterConnectionEstablished()
     * - 클라이언트가 WebSocket 연결 성공했을 때
     *
     * 무엇을 하나?
     * 1. WebSocket 세션 등록
     * 2. DB의 DialogueSession 조회 및 매핑
     * 3. GPT Realtime API 연결
     * 4. WebRTC 상태 초기화
     *
     * @param sessionId 대화 세션 ID
     * @param wsSession WebSocket 연결 객체
     */
    public void initializeSession(String sessionId, WebSocketSession wsSession, Long scenarioId) {
        try {
            log.info("세션 초기화 시작 - sessionId: {}", sessionId);
            log.info("시나리오 id: {}", scenarioId);

            // 1. WebSocket 세션 등록
            wsSessionManager.registerSession(sessionId, wsSession);

            // 2. DB에서 DialogueSession 조회 및 매핑
            dialogueSessionMapper.mapSession(sessionId);
            DialogueSession dialogueSession = dialogueSessionMapper.getDialogueSession(sessionId);

            // 3. 시나리오 정보 추출
            /*String prompt = dialogueSession.getScenario().getPrompt();
            String voice = dialogueSession.getScenario().getVoice().name().toLowerCase();*/
            Scenario scenario = scenarioRepository.findById(scenarioId).orElseThrow();
            log.info("시나리오 조회 성공: {}", scenario.getTitle());

            String instructions =
                    """
                    당신은 역할극 대화 파트너입니다. 사용자의 대화 연습을 도와주는 것이 목적입니다.
                    다음 시나리오 정보를 기반으로 대화를 진행하세요.
                
                    - 난이도: %s
                    - 카테고리: %s
                    - 대화 주제: %s
                    - 시나리오 설명: %s
                
                    아래의 지침을 따르세요:
                    1. 사용자의 실력을 고려해 난이도에 맞는 어휘와 문장을 사용하세요.
                    2. 카테고리에 맞는 상황 설정과 맥락을 유지하세요.
                    3. 대화가 자연스럽게 이어지도록 짧은 문장으로 응답하세요.
                    4. 반드시 한국어(%s)로 대화하세요.
                    """.formatted(
                            scenario.getDifficulty(),
                            scenario.getCategory(),
                            scenario.getTitle(),
                            scenario.getDescription(),
                            scenario.getLocale()
                    );

            AudioFormat audioFormat = AudioFormat.builder()
                    .sampleRate(48000)   // 서버에서 기본값
                    .channels(1)     // mono
                    .encoding("pcm16")   // PCM 16-bit
                    .build();

            RealtimeSession session = RealtimeSession.builder()
                    .model("gpt-4o-realtime-preview-2025-10-15")
                    .instructions(instructions)
                    .voice(scenario.getVoice().name().toLowerCase())
                    .build();

            SessionInitMessage message = SessionInitMessage.makePrompt(session);

            GptSession gptSession = GptSession.builder()
                    .sessionId(sessionId)
                    .build();

            gptSessionMap.put(sessionId, gptSession);

            // 4. GPT Realtime API 연결
            gptSessionManager.createGptSession(
                    sessionId,
                    message,
                    this::handleGptResponse

            ).thenAccept((v) -> {
                GptSession gSession = gptSessionMap.get(sessionId);
                if (gSession != null) {
                    gSession .setReady(true);
                    Queue<byte[]> queue = gSession.getAudioQueue();
                    while (!queue.isEmpty()) {
                        byte[] chunk = queue.poll();
                        gptSessionManager.sendAudioToGpt(sessionId, chunk);
                    }
                }
            })
            .exceptionally(ex -> {
               log.error("GPT 세션 생성 실패", ex);
               return null;
            });



            // 5. WebRTC 상태 초기화
            webRtcStateManager.initializeState(sessionId);

            log.info("세션 초기화 완료 - sessionId: {}", sessionId);

        } catch (Exception e) {
            log.error("세션 초기화 실패 - sessionId: {}", sessionId, e);
            terminateSession(sessionId);
            throw new RuntimeException("세션 초기화 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 사용자 음성을 GPT로 라우팅합니다.
     *
     * 언제 호출되나?
     * - AudioHandler의 handleBinaryMessage()
     * - 사용자가 마이크로 음성 입력했을 때
     *
     * 무엇을 하나?
     * 1. WebRTC 연결 상태 확인
     * 2. GPT 세션 존재 확인
     * 3. GPT에 음성 데이터 전송
     * 4. 통계 기록
     *
     * @param sessionId 대화 세션 ID
     * @param audioData 음성 데이터 (바이너리)
     */
    public void routeAudioToGpt(String sessionId, byte[] audioData) {
        try {
            // 1. WebRTC 연결 확인
            if (!webRtcStateManager.isConnected(sessionId)) {
                log.warn("WebRTC 연결 안됨 - sessionId: {}", sessionId);
                return;
            }

            // 2. GPT 세션 확인
            GptSession gptSession = gptSessionMap.get(sessionId);
            if (gptSession == null) {
                log.error("GPT 세션 없음 - sessionId: {}", sessionId);
                return;
            }

            // 3. ready 상태 확인
            gptSessionManager.sendAudioToGpt(sessionId, audioData);

            // 4. 통계 기록
            webRtcStateManager.recordAudioReceived(sessionId, audioData.length);

            log.debug("음성 라우팅 완료 - sessionId: {}, 크기: {} bytes",
                    sessionId, audioData.length);

        } catch (Exception e) {
            log.error("음성 라우팅 실패 - sessionId: {}", sessionId, e);
        }
    }

    /**
     * GPT 응답을 사용자에게 라우팅합니다.
     *
     * 언제 호출되나?
     * - GptSessionManager의 GptResponseHandler 콜백
     * - GPT로부터 응답을 받았을 때
     *
     * 무엇을 하나?
     * 1. JSON 파싱하여 타입 확인
     * 2. audio.delta 타입이면 음성 데이터 추출
     * 3. Base64 디코딩
     * 4. WebSocket으로 사용자에게 전송
     * 5. 통계 기록
     *
     * @param sessionId 대화 세션 ID
     * @param jsonResponse GPT 응답 JSON
     */
    private void handleGptResponse(String sessionId, String jsonResponse) {
        try {
            // 1. JSON 파싱
            JsonObject json = JsonParser.parseString(jsonResponse).getAsJsonObject();
            String type = json.get("type").getAsString();

            // 2. audio.delta 타입 확인 (음성 응답)
            if ("audio.delta".equals(type)) {
                // 3. Base64 음성 데이터 추출
                String base64Audio = json.get("audio").getAsString();
                byte[] audioData = Base64.getDecoder().decode(base64Audio);

                // 4. 사용자에게 전송
                routeAudioToUser(sessionId, audioData);

                log.debug("GPT 응답 처리 완료 - sessionId: {}, 크기: {} bytes",
                        sessionId, audioData.length);
            } else {
                // "error"를 포함한 모든 응답의 전체 내용을 로그로 남깁니다.
                log.debug("GPT 응답 수신 (전체) - sessionId: {}, payload: {}", sessionId, jsonResponse);
            }

        } catch (Exception e) {
            log.error("GPT 응답 처리 실패 - sessionId: {}", sessionId, e);
        }
    }

    /**
     * AI 음성을 사용자에게 전송합니다.
     *
     * @param sessionId 대화 세션 ID
     * @param audioData 음성 데이터
     */
    private void routeAudioToUser(String sessionId, byte[] audioData) {
        try {
            // 1. WebSocket 세션 조회
            WebSocketSession wsSession = wsSessionManager.getSession(sessionId);

            if (wsSession == null || !wsSession.isOpen()) {
                log.error("WebSocket 세션 없음 - sessionId: {}", sessionId);
                return;
            }

            // 2. 바이너리 메시지로 전송
            wsSession.sendMessage(new BinaryMessage(audioData));

            // 3. 통계 기록
            webRtcStateManager.recordAudioSent(sessionId, audioData.length);

            log.debug("AI 음성 전송 완료 - sessionId: {}, 크기: {} bytes",
                    sessionId, audioData.length);

        } catch (Exception e) {
            log.error("AI 음성 전송 실패 - sessionId: {}", sessionId, e);
        }
    }

    /**
     * 세션을 종료하고 모든 리소스를 정리합니다.
     *
     * 언제 호출되나?
     * - AudioHandler의 afterConnectionClosed()
     * - 클라이언트가 연결을 끊었을 때
     * - 타임아웃으로 강제 종료할 때
     *
     * 무엇을 하나?
     * 1. GPT 세션 종료
     * 2. WebRTC 상태 제거
     * 3. DB 매핑 제거
     * 4. WebSocket 세션 제거
     *
     * @param sessionId 대화 세션 ID
     */
    public void terminateSession(String sessionId) {
        try {
            log.info("세션 종료 시작 - sessionId: {}", sessionId);

            // 1. GPT 세션 종료
            if (gptSessionManager.hasGptSession(sessionId)) {
                gptSessionManager.closeGptSession(sessionId);
            }

            // 2. WebRTC 상태 제거
            webRtcStateManager.removeState(sessionId);

            // 3. DB 매핑 제거
            dialogueSessionMapper.unmapSession(sessionId);

            // 4. WebSocket 세션 제거
            wsSessionManager.removeSession(sessionId);

            log.info("세션 종료 완료 - sessionId: {}", sessionId);

        } catch (Exception e) {
            log.error("세션 종료 실패 - sessionId: {}", sessionId, e);
        }
    }

    /**
     * 세션이 유효한지 확인합니다.
     *
     * @param sessionId 대화 세션 ID
     * @return 유효 여부
     */
    public boolean isSessionValid(String sessionId) {
        return wsSessionManager.hasSession(sessionId)
                && dialogueSessionMapper.hasDialogueSession(sessionId)
                && gptSessionManager.hasGptSession(sessionId);
    }

    /**
     * 현재 활성화된 세션 수를 반환합니다.
     *
     * @return 활성 세션 수
     */
    public int getActiveSessionCount() {
        return wsSessionManager.getActiveSessionCount();
    }

    /**
     * 모든 세션을 종료합니다.
     *
     * 언제 사용하나?
     * - 서버 종료 시
     */
    public void terminateAllSessions() {
        log.info("모든 세션 종료 시작");

        gptSessionManager.closeAll();
        webRtcStateManager.clearAll();
        dialogueSessionMapper.clearCache();
        wsSessionManager.clearAll();

        log.info("모든 세션 종료 완료");
    }

    public boolean hasGptSession(String sessionId) {
        return gptSessionMap.containsKey(sessionId);
    }

    public void queueAudio(String sessionId, byte[] audioData) {
        GptSession gptSession = gptSessionMap.get(sessionId);
        if (gptSession == null) {
            log.warn("GPT 세션 없음, 오디오 큐에 저장 불가 - sessionId: {}", sessionId);
            return;
        }

        gptSession.getAudioQueue().offer(audioData);
        log.debug("오디오 큐에 저장 - sessionId: {}, 큐 크기: {}", sessionId, gptSession.getAudioQueue().size());
    }
}