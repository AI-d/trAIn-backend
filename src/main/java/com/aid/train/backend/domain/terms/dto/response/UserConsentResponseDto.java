package com.aid.train.backend.domain.terms.dto.response;

import com.aid.train.backend.domain.terms.entity.UserConsent;
import com.aid.train.backend.domain.terms.enums.TermsType;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 사용자 약관 동의 응답 DTO입니다.
 * 사용자의 약관 동의 이력을 반환합니다.
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "사용자 약관 동의 응답")
public class UserConsentResponseDto {

    @Schema(description = "약관 동의 ID", example = "1")
    private Long consentId;

    @Schema(description = "약관 ID", example = "1")
    private Long termsId;

    @Schema(description = "약관 타입", example = "TERMS_OF_SERVICE")
    private TermsType termsType;

    @Schema(description = "약관 제목", example = "[필수] 서비스 이용약관")
    private String termsTitle;

    @Schema(description = "약관 버전", example = "1.0")
    private String termsVersion;

    @Schema(description = "동의 여부", example = "true")
    private Boolean isAgreed;

    @Schema(description = "필수 동의 여부", example = "true")
    private Boolean isRequired;

    @Schema(description = "동의 일시")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime agreedAt;

    @Schema(description = "철회 일시")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime revokedAt;

    /**
     * UserConsent 엔티티를 DTO로 변환합니다.
     *
     * @param userConsent UserConsent 엔티티
     * @return UserConsentResponseDto
     */
    public static UserConsentResponseDto from(UserConsent userConsent) {
        return UserConsentResponseDto.builder()
                .consentId(userConsent.getId())
                .termsId(userConsent.getTerms().getId())
                .termsType(userConsent.getTerms().getType())
                .termsTitle(userConsent.getTerms().getTitle())
                .termsVersion(userConsent.getTerms().getVersion())
                .isAgreed(userConsent.getIsAgreed())
                .isRequired(userConsent.getTerms().getType().isRequired())
                .agreedAt(userConsent.getAgreedAt())
                .revokedAt(userConsent.getRevokedAt())
                .build();
    }
}
