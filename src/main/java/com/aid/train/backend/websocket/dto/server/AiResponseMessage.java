package com.aid.train.backend.websocket.dto.server;

import com.aid.train.backend.websocket.model.MessageType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

/**
 * AI의 텍스트 응답.
 *
 * 클래스 개요:
 * 모델이 생성한 답변을 텍스트로 전달한다.
 *
 * 필요성:
 * - 채팅 UI와 로그 저장에 사용한다.
 *
 * 사용 예시:
 * - STT 결과에 따른 답변 텍스트를 연속 또는 구간 단위로 보낸다.
 *
 * 관련 클래스:
 * - AiAudioMessage, AiTranscriptMessage
 *
 * @author 김경민
 * @since 2025-10-15
 * @version 1.0.0
 */
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AiResponseMessage {
    /** 생성 텍스트 */
    private String transcript;

    /** 최종 세ग먼트 여부 */
    private Boolean finalSegment;

    /** 처리 지연(ms, 선택) */
    private Long latencyMs;

    @Builder.Default
    private MessageType type = MessageType.AI_RESPONSE;

    /** 세션 식별자 */
    private String sessionId;
}
