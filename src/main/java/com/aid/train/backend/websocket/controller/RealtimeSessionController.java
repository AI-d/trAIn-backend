package com.aid.train.backend.websocket.controller;

import com.aid.train.backend.domain.scenario.dto.response.ScenarioResponseDto;
import com.aid.train.backend.domain.scenario.entity.Scenario;
import com.aid.train.backend.domain.scenario.service.ScenarioService;
import com.aid.train.backend.domain.session.entity.DialogueSession;
import com.aid.train.backend.domain.session.enums.SessionStatus;
import com.aid.train.backend.domain.session.service.DialogueSessionService;
import com.aid.train.backend.domain.user.dto.response.UserProfileResponseDto;
import com.aid.train.backend.domain.user.service.UserService;
import com.aid.train.backend.global.exception.TrainException;
import com.aid.train.backend.global.response.ApiResponse;
import com.aid.train.backend.websocket.dto.client.RealtimeSessionRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static com.aid.train.backend.global.exception.enums.ErrorCode.SESSION_ALREADY_COMPLETED;

@Slf4j
@RestController
@RequestMapping("/api/v1/realtime")
@RequiredArgsConstructor
public class RealtimeSessionController {

    @Value("${spring.ai.openai.api-key}")
    private String openAiApiKey;

    private final RestTemplate restTemplate;
    private final DialogueSessionService dialogueSessionService;
    private final ScenarioService scenarioService;

    /**
     * Ephemeral Key 발급 (시나리오 프롬프트 포함)
     *
     * P2P 연결 전 필요한 작업:
     * - DialogueSession 검증
     * - 시나리오 프롬프트 생성
     * - OpenAI Ephemeral Key 발급
     */
    @PostMapping("/session")
    public ResponseEntity<?> createEphemeralSession(
            @RequestBody RealtimeSessionRequest req
    ) {
        log.info("Ephemeral Key 발급 요청 - sessionId: {}", req.sessionId());

        // 1. 세션 존재 확인
        DialogueSession session = dialogueSessionService.getSessionWithUserAndScenario(req.sessionId());

        // 2. 세션 상태 확인
        if(session.getStatus() != SessionStatus.ONGOING) {
            throw new TrainException(SESSION_ALREADY_COMPLETED);
        }

        // 3. 프롬프트 생성
        Scenario scenario = session.getScenario();
        String instructions = scenarioService.createPrompt(scenario);
        log.debug("프롬프트 생성 완료 - scenarioId: {}, title: {}", scenario.getId(), scenario.getTitle());

        String url = "https://api.openai.com/v1/realtime/sessions";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(openAiApiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> request = Map.of (
                "model", req.model(),
                "voice", req.voice(),
                "instruction", instructions,
                "turn_detection", Map.of(), // 매뉴얼 모드 사용
                "input_audio_transcription", Map.of("model", req.sttModel())
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
        log.info("임시 세션 생성 성공: {}", response.getBody());

        return ResponseEntity.ok().body(ApiResponse.success("webRtc 연결을 위한 키 발급에 성공했습니다.", response.getBody()));
    }

}
