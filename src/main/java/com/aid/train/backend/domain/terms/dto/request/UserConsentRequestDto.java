package com.aid.train.backend.domain.terms.dto.request;

import com.aid.train.backend.domain.terms.enums.TermsType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 약관 동의 요청 DTO입니다.
 * 약관 동의 또는 철회 시 사용됩니다.
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "약관 동의 요청 DTO")
public class UserConsentRequestDto {

    @NotNull(message = "약관 타입은 필수입니다.")
    @Schema(description = "약관 타입", example = "MARKETING_CONSENT", requiredMode = Schema.RequiredMode.REQUIRED)
    private TermsType termsType;

    @NotNull(message = "동의 여부는 필수입니다.")
    @Schema(description = "동의 여부", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean isAgreed;

    /**
     * 테스트용 생성자입니다.
     *
     * @param termsType 약관 타입
     * @param isAgreed  동의 여부
     */
    public UserConsentRequestDto(TermsType termsType, Boolean isAgreed) {
        this.termsType = termsType;
        this.isAgreed = isAgreed;
    }
}
