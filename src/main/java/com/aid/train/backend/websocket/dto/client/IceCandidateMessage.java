package com.aid.train.backend.websocket.dto.client;

import com.aid.train.backend.websocket.model.MessageType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

/**
 * ICE 후보 전송 요청.
 *
 * 클래스 개요:
 * 후보 네트워크 경로(ICE Candidate)를 교환한다.
 *
 * 필요성:
 * - 방화벽, NAT 등 다양한 네트워크 상황에서 최적 경로를 탐색한다.
 *
 * 사용 예시:
 * - 시그널링 과정에서 ICE 이벤트가 발생할 때마다 전송한다.
 *
 * 관련 클래스:
 * - OfferMessage, AnswerMessage
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
public class IceCandidateMessage{

    @Builder.Default
    private MessageType type = MessageType.ICE_CANDIDATE;

    private IceCandidate candidate;

}

@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
class IceCandidate {

    private String candidate;

    /** ICE: 미디어 스트림 식별자 */
    private String sdpMid;
    /** ICE: m-line index */
    private Integer sdpMLineIndex;

}
