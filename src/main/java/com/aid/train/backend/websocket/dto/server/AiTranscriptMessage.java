package com.aid.train.backend.websocket.dto.server;

import com.aid.train.backend.websocket.dto.common.BaseWsMessage;
import com.aid.train.backend.websocket.model.MessageType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

/**
 * 사용자 발화의 STT 결과 응답.
 *
 * 클래스 개요:
 * 부분/최종 인식 텍스트와 타이밍 정보를 전달한다.
 *
 * 필요성:
 * - UI 자막 표시와 최종 세그먼트 저장 트리거에 필요하다.
 *
 * 사용 예시:
 * - finalSegment=false로 중간 결과를 스트리밍하고,
 *   finalSegment=true에서 Transcript 저장을 트리거한다.
 *
 * 관련 클래스:
 * - AudioDataMessage, FeedbackMessage
 *
 * @author 김경민
 * @since 2025-10-15
 * @version 1.0.0
 */
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AiTranscriptMessage extends BaseWsMessage {
    /** 인식 텍스트 */
    private String transcript;

    /** true면 최종 세그먼트, false면 중간 업데이트 */
    private Boolean finalSegment;

    /** 인식 신뢰도(가능 시) */
    private Double confidence;

    /** 발화 시작/종료 시각(ms) */
    private Long startMs;
    private Long endMs;

    @Builder.Default
    private MessageType type = MessageType.AI_TRANSCRIPT;
}

