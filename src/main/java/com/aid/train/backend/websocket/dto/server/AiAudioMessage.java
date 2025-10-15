package com.aid.train.backend.websocket.dto.server;

import com.aid.train.backend.websocket.dto.common.AudioFormat;
import com.aid.train.backend.websocket.dto.common.BaseWsMessage;
import com.aid.train.backend.websocket.model.MessageType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

/**
 * AI의 오디오 응답(TTS 등).
 *
 * 클래스 개요:
 * 모델이 생성한 음성을 Base64 바이트로 전송한다.
 *
 * 필요성:
 * - 클라이언트 측 실시간 재생을 위해 필요하다.
 *
 * 사용 예시:
 * - 텍스트 응답과 별도로 또는 함께 전송할 수 있다.
 *
 * 관련 클래스:
 * - AiResponseMessage, AudioFormat
 *
 * @author 김경민
 * @since 2025-10-15
 * @version 1.0.0
 */
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AiAudioMessage extends BaseWsMessage {
    /** 출력 보이스 프리셋(선택) */
    private String voice;

    /** 출력 오디오 포맷 */
    private AudioFormat format;

    /** 출력 오디오 데이터(Base64) */
    private byte[] audio;

    @Builder.Default
    private MessageType type = MessageType.AI_AUDIO;
}
