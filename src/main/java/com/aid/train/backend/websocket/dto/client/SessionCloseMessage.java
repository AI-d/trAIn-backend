package com.aid.train.backend.websocket.dto.client;

import com.aid.train.backend.websocket.dto.common.BaseWsMessage;
import com.aid.train.backend.websocket.model.MessageType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

/**
 * 세션 정상 종료 의사 전달.
 *
 * 클래스 개요:
 * 클라이언트 측 정상 종료를 서버에 알린다.
 *
 * 필요성:
 * - 서버가 GPT/WebRTC/세션 관련 자원을 안전하게 정리할 근거가 된다.
 *
 * 사용 예시:
 * - 정상 종료 경로에서 전송하고, 이후 실제 WebSocket close 프레임을 보낸다.
 *
 * 관련 클래스:
 * - ErrorMessage, PongMessage
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
public class SessionCloseMessage extends BaseWsMessage {
    /** 종료 사유(선택) */
    private String reason;

    @Builder.Default
    private MessageType type = MessageType.SESSION_CLOSE;
}
