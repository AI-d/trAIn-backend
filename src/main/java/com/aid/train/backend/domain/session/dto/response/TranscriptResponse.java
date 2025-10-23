package com.aid.train.backend.domain.session.dto.response;

import com.aid.train.backend.domain.session.entity.Transcript;
import com.aid.train.backend.domain.session.enums.Speaker;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * Transcript 응답 DTO
 *
 * @author 김경민
 * @since 2025-10-23
 * @version 1.0.0
 */
@Builder
@Schema(description = "발화 내역 응답")
public record TranscriptResponse(
        @Schema(description = "발화 ID", example = "1")
        Long id,

        @Schema(description = "발화자", example = "USER")
        Speaker speaker,

        @Schema(description = "발화 내용", example = "안녕하세요")
        String content,

        @Schema(description = "발화 시간", example = "2025-10-23T14:30:00")
        LocalDateTime timestamp,

        @Schema(description = "시작 시간 (밀리초)", example = "1000")
        Long startTimeMs,

        @Schema(description = "종료 시간 (밀리초)", example = "3000")
        Long endTimeMs,

        @Schema(description = "신뢰도 점수", example = "0.95")
        Float confidenceScore
) {
    public static TranscriptResponse from(Transcript transcript) {
        return TranscriptResponse.builder()
                .id(transcript.getId())
                .speaker(transcript.getSpeaker())
                .content(transcript.getContent())
                .timestamp(transcript.getTimestamp())
                .startTimeMs(transcript.getStartTimeMs())
                .endTimeMs(transcript.getEndTimeMs())
                .confidenceScore(transcript.getConfidenceScore())
                .build();
    }
}
