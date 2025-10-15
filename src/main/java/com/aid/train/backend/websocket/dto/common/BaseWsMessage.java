package com.aid.train.backend.websocket.dto.common;

import com.aid.train.backend.websocket.model.MessageType;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import lombok.*;

/**
 * 모든 WebSocket 메시지의 공통 상위 클래스.
 *
 * 클래스 개요:
 * 공통 메타데이터(type, sessionId, messageId, sequence, timestamp, version)를 제공하여
 * 라우팅, 로깅, 트레이싱, 재시도 기준을 표준화한다.
 *
 * 필요성:
 * - 여러 DTO에서 동일한 메타를 반복 정의하지 않도록 한다.
 * - 서버와 클라이언트 사이의 공통 규약을 강제하여 디버깅과 운영 안정성을 높인다.
 *
 * 사용 예시:
 * - 모든 구체 DTO가 본 클래스를 상속한다.
 *
 * @author 김경민
 * @since 2025-10-15
 * @version 1.0.0
 */
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // null 필드 직렬화 제외
public class BaseWsMessage {
    /** 메시지 유형. 라우팅 분기 기준 */
    private MessageType type;

    /** 세션 식별자. DialogueSession 등과 1:1 매핑되는 키 */
    private String sessionId;

    /** 메시지 고유 식별자(UUID 권장) */
    private String messageId;

    /** 세션 내 전송 순서 보장을 위한 증가값(0부터 가산) */
    private Long sequence;

    /** 전송 시각(UTC) */
    private Instant timestamp;

    /** 메시지 규약 버전(예: "ws.v1") */
    private String version;
}
