package com.aid.train.backend.websocket.dto.server;

import com.aid.train.backend.websocket.model.MessageType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

/**
 * SDP Answer 전송 요청.
 *
 * 클래스 개요:
 * 원격 피어의 Offer에 대한 Answer를 전달한다.
 *
 * 필요성:
 * - 세션 설명 교환을 통해 미디어 경로 협상을 마무리한다.
 *
 * 사용 예시:
 * - Offer 수신 후 Answer 생성 시 본 메시지를 전송한다.
 *
 * 관련 클래스:
 * - OfferMessage, IceCandidateMessage
 *
 * @author 김경민
 * @since 2025-10-15
 * @version 1.0.0
 */
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AnswerMessage{
    /** SDP 본문(Answer) */
    private String sdp;

    @Builder.Default
    private MessageType type = MessageType.ANSWER;
}
