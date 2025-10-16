package com.aid.train.backend.domain.terms.dto.response;

import com.aid.train.backend.domain.terms.entity.Terms;
import com.aid.train.backend.domain.terms.enums.TermsType;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 약관 응답 DTO입니다.
 * 약관 정보를 반환합니다.
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "약관 응답")
public class TermsResponseDto {

    @Schema(description = "약관 ID", example = "1")
    private Long termsId;

    @Schema(description = "약관 타입", example = "TERMS_OF_SERVICE")
    private TermsType type;

    @Schema(description = "약관 제목", example = "[필수] 서비스 이용약관")
    private String title;

    @Schema(description = "약관 내용")
    private String content;

    @Schema(description = "약관 버전", example = "1.0")
    private String version;

    @Schema(description = "필수 동의 여부", example = "true")
    private Boolean isRequired;

    @Schema(description = "활성화 여부", example = "true")
    private Boolean isActive;

    @Schema(description = "생성 일시")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "수정 일시")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    /**
     * Terms 엔티티를 DTO로 변환합니다.
     *
     * @param terms Terms 엔티티
     * @return TermsResponseDto
     */
    public static TermsResponseDto from(Terms terms) {
        return TermsResponseDto.builder()
                .termsId(terms.getId())
                .type(terms.getType())
                .title(terms.getTitle())
                .content(terms.getContent())
                .version(terms.getVersion())
                .isRequired(terms.getType().isRequired())
                .isActive(terms.getIsActive())
                .createdAt(terms.getCreatedAt())
                .updatedAt(terms.getUpdatedAt())
                .build();
    }
}
