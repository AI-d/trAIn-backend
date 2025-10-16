package com.aid.train.backend.websocket.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

/**
 * 오디오 포맷 및 프레임 메타데이터 값 객체.
 *
 * 클래스 개요:
 * WebSocket에서 주고받는 오디오 데이터의 포맷 정보를 정의한다.
 * sampleRate, encoding, frameDuration 등 반복 필드를 VO로 묶어 중복을 제거한다.
 *
 * 필요성:
 * - AudioDataMessage, SessionInitMessage, AiAudioMessage 등에서 동일 포맷을 사용한다.
 * - 포맷 변경 시 수정 범위를 최소화한다.
 *
 * 사용 예시:
 *   AudioFormat format = AudioFormat.builder()
 *       .sampleRate(16000).channels(1).encoding("OPUS").frameDurationMs(20).build();
 *
 * @author 김경민
 * @since 2025-10-15
 * @version 1.0.0
 */
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // null 필드 직렬화 제어
public class AudioFormat {
    /** 샘플레이트(Hz). 예: 16000, 24000 */
    private Integer sampleRate;

    /** 채널 수. 1 또는 2 */
    private Integer channels;

    /** 인코딩 포맷. 예: "OPUS", "LINEAR16" */
    private String encoding;

    /** 프레임 길이(ms). 예: 20, 40, 100 */
    private Integer frameDurationMs;
}
