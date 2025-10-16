package com.aid.train.backend.websocket.dto.client;

import com.aid.train.backend.websocket.dto.common.AudioFormat;
import com.aid.train.backend.websocket.model.MessageType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

/**
 * 오디오 프레임 업스트림 요청.
 *
 * 클래스 개요:
 * 실시간 음성을 프레임 단위로 서버에 전송한다.
 *
 * 필요성:
 * - STT, 실시간 분석, 응답 생성을 위한 입력 파이프라인이다.
 *
 * 사용 예시:
 * - 대화 중 프레임 간격에 맞춰 반복 전송한다.
 *
 * 관련 클래스:
 * - SessionInitMessage, AiTranscriptMessage, FeedbackMessage
 *
 * @author 김경민
 * @since 2025-10-15
 * @version 1.0.0
 */
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AudioDataMessage {
    /** 입력 프레임 포맷 */
    private AudioFormat format;

    /** Base64 직렬화된 오디오 프레임 */
    private byte[] audio;

    @Builder.Default
    private MessageType type = MessageType.AUDIO_DATA;
}
