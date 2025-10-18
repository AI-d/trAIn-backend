package com.aid.train.backend.domain.session.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

/**
 * 세션 생성 요청 DTO
 *
 * @author 김경민
 * @since 2025-10-15
 */
@Builder
public record CreateSessionRequest(
        @NotNull(message = "사용자 ID는 필수입니다.")
        Long userId,

        @NotNull(message = "시나리오 ID는 필수입니다.")
        Long scenarioId
) {
}