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
import java.util.concurrent.atomic.AtomicBoolean;

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

    // GPT 응답 진행 중 상태 추적 (response.done 받기 전까지 true)
    private final Map<String, AtomicBoolean> gptResponseInProgress = new ConcurrentHashMap<>();


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

            // 응답 진행 상태 초기화
            gptResponseInProgress.put(sessionId, new AtomicBoolean(false));

            // 1. WebSocket 세션 등록
            wsSessionManager.registerSession(sessionId, wsSession);

            // 2. DB에서 DialogueSession 조회 및 매핑
            dialogueSessionMapper.mapSession(sessionId);
            DialogueSession dialogueSession = dialogueSessionMapper.getDialogueSession(sessionId);

            // 3. 시나리오 정보 추출
            Scenario scenario = scenarioRepository.findById(scenarioId).orElseThrow();
            log.info("시나리오 조회 성공: {}", scenario.getTitle());

            String instructions =
                    """
                    당신은 역할극 대화 파트너입니다. 사용자의 대화 연습을 도와주는 것이 목적입니다.
                    다음 시나리오 정보를 기반으로 대화를 진행하세요.
                    
                    **CRITICAL: You MUST speak in Korean language ONLY.**
                
                    - 난이도: %s
                    - 카테고리: %s
                    - 대화 주제: %s
                    - 시나리오 설명: %s
                
                    아래의 지침을 따르세요:
                    1. **반드시 한국어로만 대화하세요. 절대 영어를 사용하지 마세요.**
                    2. 사용자의 실력을 고려해 난이도에 맞는 어휘와 문장을 사용하세요.
                    3. 카테고리에 맞는 상황 설정과 맥락을 유지하세요.
                    4. 대화가 자연스럽게 이어지도록 짧은 문장으로 응답하세요.
                    5. 언어: %s (반드시 이 언어로만 대화)
                            
                    **중요: 대화 시작 시 먼저 한국어로 인사하고 주제에 맞는 첫 질문을 해주세요.**
                    **예시: "안녕하세요! 오늘 날씨 어때요?" 또는 "안녕하세요! 무엇을 도와드릴까요?"**
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

           // gptSession 객체 생성
            GptSession initialGptSession = GptSession.builder()
                    .sessionId(sessionId)
                    .isReady(new AtomicBoolean(false))
                    .build();

            gptSessionManager.registerGptSession(sessionId, initialGptSession);

            // 4. GPT Realtime API 연결
            gptSessionManager.createGptSession(
                    sessionId,
                    message,
                    this::handleGptResponse

            ).thenAccept((v) -> {
                log.info("GPT WebSocket 연결 성공 - sessionId: {}", sessionId);

                GptSession gSession = gptSessionManager.getGptSession(sessionId);
                if (gSession != null) {
                    gSession.setWebSocketSession(v);
                    gSession.setReady(true);

                    // 대기 중이던 오디오 큐 전송
                    Queue<byte[]> queue = gSession.getAudioQueue();
                    if(queue.size() > 0) {
                        log.info("대기 중이던 오디오 청크 전송 시작 - count: {}", queue.size());
                        while (!queue.isEmpty()) {
                            byte[] chunk = queue.poll();
                            gptSessionManager.sendAudioToGpt(sessionId, chunk);
                        }
                        log.info("대기 중이던 오디오 청크 전송 완료");
                    }
                }
            })
            .exceptionally(ex -> {
               log.error("GPT 세션 생성 실패", ex);
                terminateSession(sessionId);
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
            GptSession gptSession = gptSessionManager.getGptSession(sessionId);
            if (gptSession == null) {
                log.error("GPT 세션 없음 - sessionId: {}", sessionId);
                return;
            }

            // 3. GPT 세션 상태 확인
            if (gptSession.getIsReady().get() == false) {
                log.warn("GPT 세션이 아직 준비되지 않음 - 큐에 추가 - sessionId: {}", sessionId);
                return;
            }

            // 4. WebSocket 연결 상태 확인 추가
            if (gptSession.getWebSocketSession() == null ||
                    !gptSession.getWebSocketSession().isOpen()) {
                log.error("GPT WebSocket이 닫혀있음 - sessionId: {}", sessionId);
                log.error("세션 상태 - ready: {}, session null: {}",
                        gptSession.getIsReady(),
                        gptSession.getWebSocketSession() == null);
                return;
            }

            // 5. gpt에 음성 전송
            gptSessionManager.sendAudioToGpt(sessionId, audioData);

            // 6. 통계 기록
            webRtcStateManager.recordAudioReceived(sessionId, audioData.length);

            log.debug("음성 GPT 전송 완료 - sessionId: {}, 크기: {} bytes",
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

            log.debug("GPT 응답 수신 - sessionId: {}, message length: {}", sessionId, jsonResponse.length());

            // 1. JSON 파싱
            JsonObject json = JsonParser.parseString(jsonResponse).getAsJsonObject();
            String type = json.get("type").getAsString();

            switch (type) {
                case "session.create":
                    log.info("GPT 세션 생성됨 - sessionId: {}", sessionId);
                    break;

                case "session.updated":
                    log.info("GPT 세션 업데이트 됨 - sessionId: {}", sessionId);
                    requestInitialGptResponse(sessionId);
                    break;

                case "response.created":
                    log.info("GPT 응답 생성 시작 - sessionId: {}", sessionId);
                    // 응답 진행 중 플래그 설정
                    AtomicBoolean inProgress = gptResponseInProgress.get(sessionId);
                    if (inProgress != null) {
                        inProgress.set(true);
                        log.info("응답 진행 중 플래그 설정 - sessionId: {}", sessionId);
                    }
                    break;

                case "response.audio.delta":
                    log.debug("오디오 델타 수신 - sessionId: {}", sessionId);
                    if (json.has("delta")) {
                    String base64Audio = json.get("delta").getAsString();
                    byte[] audioData = Base64.getDecoder().decode(base64Audio);

                    WebSocketSession wsSession = wsSessionManager.getSession(sessionId);
                    if (wsSession != null && wsSession.isOpen()) {
                        wsSession.sendMessage(new BinaryMessage(audioData));
                        webRtcStateManager.recordAudioSent(sessionId, audioData.length);
                        log.debug("오디오 클라이언트 전송 - {} bytes", audioData.length);
                    }
                    break;
                }
                case "response.audio_transcript.delta" :
                    if (json.has("delta")) {
                        String transcript = json.get("delta").getAsString();
                        log.info("음성 텍스트: {}", transcript);
                    }
                    break;

                case "response.done":
                    log.info("GPT 응답 완료 - sessionId: {}", sessionId);

                    // 핵심: 응답 완료 플래그 해제
                    AtomicBoolean isFinished = gptResponseInProgress.get(sessionId);
                    if (isFinished != null) {
                        isFinished.set(false);
                        log.info("응답 진행 중 플래그 해제 - sessionId: {}", sessionId);
                    }
                    break;

                case "error":
                    String errorMsg = json.has("error") ?
                            json.get("error").toString() : "Unknown error";
                    log.error("GPT 에러 발생 - sessionId: {}, error: {}", sessionId, errorMsg);

                    // 에러 발생 시 응답 진행 플래그 해제
                    AtomicBoolean flag = gptResponseInProgress.get(sessionId);
                    if (flag != null) {
                        flag.set(false);
                    }
                    break;

                case "input_audio_buffer.speech_started":
                    log.info("사용자 음성 감지 시작 - sessionId: {}", sessionId);
                    break;

                case "input_audio_buffer.speech_stopped":
                    log.info("사용자 음성 감지 종료 - sessionId: {}", sessionId);
                    break;

                default:
                    log.debug("기타 GPT 이벤트: {} - sessionId: {}", type, sessionId);
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

    public void queueAudio(String sessionId, byte[] audioData) {
        GptSession gptSession = gptSessionManager.getGptSession(sessionId);
        if (gptSession == null) {
            log.warn("GPT 세션 없음, 오디오 큐에 저장 불가 - sessionId: {}", sessionId);
            return;
        }

        gptSession.getAudioQueue().offer(audioData);
        log.debug("오디오 큐에 저장 - sessionId: {}, 큐 크기: {}", sessionId, gptSession.getAudioQueue().size());
    }

    public void requestInitialGptResponse(String sessionId) {
        try {
            GptSession gptSession = gptSessionManager.getGptSession(sessionId);
            if(gptSession == null) {
                log.error("gpt 세션 없음 - sessionId: {}", sessionId);
                return;
            }

            log.info("gpt 초기 응답 요청 - sessionId: {}", sessionId);
            String responseCreate = """
                    {
                        "type": "response.create",
                         "response": {
                            "modalities": ["audio", "text"],
                            "instructions": "사용자에게 한국어로 인사하고 대화를 시작해주세요."
                         }
                    }
                    """;

            gptSessionManager.sendToGpt(sessionId, responseCreate);

        } catch(Exception e) {

        }
    }

}