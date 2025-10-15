package com.aid.train.backend.websocket.dto.server;

import com.aid.train.backend.websocket.dto.common.BaseWsMessage;
import com.aid.train.backend.websocket.model.MessageType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

/**
 * 오류 통지 응답.
 *
 * 클래스 개요:
 * 네트워크, 권한, 타임아웃 등 오류를 클라이언트가 식별하고 복구 절차를 수행할 수 있도록 전달한다.
 *
 * 관련 클래스:
 * - SessionCloseMessage, PongMessage
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
public class ErrorMessage extends BaseWsMessage {
    /** 에러 코드. 예: "WS-401", "ASR-TIMEOUT" */
    private String code;
    /** 사용자 노출 메시지 */
    private String message;
    /** 디버그 상세(선택) */
    private String detail;

    @Builder.Default
    private MessageType type = MessageType.ERROR;
}
