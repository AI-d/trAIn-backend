package com.aid.train.backend.websocket.controller;

import com.aid.train.backend.global.response.ApiResponse;
import com.aid.train.backend.websocket.dto.client.RealtimeSessionRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/realtime")
@RequiredArgsConstructor
public class RealtimeSessionController {

    @Value("${spring.ai.openai.api-key}")
    private String openAiApiKey;

    private final RestTemplate restTemplate;

    @PostMapping("/session")
    public ResponseEntity<?> createEphemeralSession(
            @RequestBody RealtimeSessionRequest req
    ) {
        String url = "https://api.openai.com/v1/realtime/sessions";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(openAiApiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> request = Map.of (
                "model", req.model(),
                "voice", req.voice(),
                "input_audio_transcription", Map.of("model", req.sttModel())
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
        log.info("임시 세션 생성 성공: {}", response.getBody());

        return ResponseEntity.ok().body(ApiResponse.success("webRtc 연결을 위한 키 발급에 성공했습니다.", response.getBody()));
    }

}
