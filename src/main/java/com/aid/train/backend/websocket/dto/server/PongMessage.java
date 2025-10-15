package com.aid.train.backend.websocket.dto.server;

import com.aid.train.backend.websocket.dto.common.BaseWsMessage;
import com.aid.train.backend.websocket.model.MessageType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

/**
 * 핑-퐁 헬스체크 응답.
 *
 * 클래스 개요:
 * 연결 유효성 확인과 왕복 지연 측정에 사용한다.
 *
 * 관련 클래스:
 * - ErrorMessage
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
public class PongMessage extends BaseWsMessage {
    /** 클라이언트 ping 페이로드 에코 */
    private String echo;

    @Builder.Default
    private MessageType type = MessageType.PONG;
}
