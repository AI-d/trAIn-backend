package com.aid.train.backend.websocket.dto.client;

import com.aid.train.backend.domain.scenario.entity.Scenario;
import com.aid.train.backend.websocket.dto.common.AudioFormat;
import com.aid.train.backend.websocket.dto.server.RealtimeSession;
import com.aid.train.backend.websocket.model.MessageType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

/**
 * 세션 초기화 요청.
 *
 * 클래스 개요:
 * 시나리오, 언어, 보이스, 입력 오디오 포맷 등 초기 파이프라인 구성을 서버에 전달한다.
 *
 * 필요성:
 * - 대화 시작 직후 서버가 GPT/RTC/세션 상태를 올바르게 초기화할 근거가 된다.
 *
 * 사용 예시:
 * - 연결 직후 단 한 번 전송한다.
 *
 * 관련 클래스:
 * - AudioDataMessage, AiTranscriptMessage, AiResponseMessage
 *
 * author, since, version: 하단 태그 참조
 *
 * @author 김경민
 * @since 2025-10-15
 * @version 1.0.0
 */
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SessionInitMessage {

    @Builder.Default
    private String type = "session.update";

    private RealtimeSession session;

    private AudioFormat audioFormat;


    // gpt 프롬프트 생성 편의 메소드
    public static SessionInitMessage makePrompt(RealtimeSession session, AudioFormat audioFormat) {
        return SessionInitMessage.builder()
                .session(session)
                .audioFormat(audioFormat)
                .build();
    }
}