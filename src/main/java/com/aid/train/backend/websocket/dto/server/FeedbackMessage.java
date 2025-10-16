package com.aid.train.backend.websocket.dto.server;

import com.aid.train.backend.websocket.model.MessageType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

/**
 * 실시간 발화/대화 품질 피드백.
 *
 * 클래스 개요:
 * 속도, 추임새, 음량, 감정 등의 지표와 코칭 문구를 제공한다.
 *
 * 필요성:
 * - 사용자 자기교정과 교육 효과 측정을 위해 즉시 피드백이 필요하다.
 *
 * 사용 예시:
 * - 발화 윈도우 단위로 주기적으로 전송한다.
 *
 * 관련 클래스:
 * - AiTranscriptMessage, AudioDataMessage
 *
 * @author 김경민
 * @since 2025-10-15
 * @version 1.0.0
 */
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FeedbackMessage{
    /** 분당 단어수(또는 자수 환산 지표) */
    private Integer wpm;

    /** 평균 볼륨(dBFS 유사 추정치) */
    private Double avgVolumeDb;

    /** 추임새 개수 */
    private Integer fillerCount;

    /** 감정 레이블. 예: "neutral", "happy", "sad" */
    private String emotion;

    /** 정규화 점수(0~1 또는 0~100) */
    private Double score;

    /** 개선 코칭 문구 */
    private String tip;

    /** 파형 시각화 샘플(선택) */
    private Double[] waveform;

    /** 파형 창 크기(ms, 선택) */
    private Integer windowMs;

    @Builder.Default
    private MessageType type = MessageType.FEEDBACK; // 필요 시 SPEED/FILLER/EMOTION 타입 사용 가능

    /** 세션 식별자 */
    private String sessionId;
}
