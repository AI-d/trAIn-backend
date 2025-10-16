package com.aid.train.backend.websocket.dto.client;

import com.aid.train.backend.websocket.model.MessageType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

/**
 * SDP Offer 전송 요청.
 *
 * 클래스 개요:
 * 브라우저가 생성한 SDP Offer를 서버(시그널링 브로커)로 전달한다.
 *
 * 필요성:
 * - WebRTC P2P 연결 수립을 위해 필수 초기 신호이다.
 *
 * 사용 예시:
 * - 클라이언트가 Offer 생성 직후 본 메시지를 전송한다.
 *
 * @author 김경민
 * @since 2025-10-15
 * @version 1.0.0
 */
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OfferMessage {
    /** SDP 본문(Offer) */
    private String sdp;

    @Builder.Default
    private MessageType type = MessageType.OFFER;

}

