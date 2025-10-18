package com.aid.train.backend.domain.session.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 세션 응답 DTO
 *
 * @author 김경민
 * @since 2025-10-15
 */
@Builder
public record SessionResponse(
        String sessionId,           // UUID
        Long userId,
        Long scenarioId,
        String scenarioTitle,
        String status,              // ONGOING, COMPLETED, FAILED
        LocalDateTime startedAt,
        LocalDateTime endedAt,
        Integer audioDurationSeconds
) {
}