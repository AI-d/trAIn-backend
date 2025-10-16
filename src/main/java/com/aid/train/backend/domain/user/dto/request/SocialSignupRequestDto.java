package com.aid.train.backend.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 소셜 회원가입 요청 DTO입니다.
 * 소셜 로그인 후 추가 정보 입력 시 사용됩니다.
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "소셜 회원가입 요청 DTO")
public class SocialSignupRequestDto {

    @NotBlank(message = "임시 토큰은 필수입니다.")
    @Schema(description = "소셜 로그인 임시 토큰", example = "temp-token-abc123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String tempToken;

    @NotBlank(message = "닉네임은 필수입니다.")
    @Size(min = 2, max = 20, message = "닉네임은 2~20자 사이여야 합니다.")
    @Schema(description = "닉네임 (2~20자)", example = "홍길동", requiredMode = Schema.RequiredMode.REQUIRED)
    private String nickname;

    @NotNull(message = "서비스 이용약관 동의는 필수입니다.")
    @Schema(description = "서비스 이용약관 동의 여부", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean agreeTermsOfService;

    @NotNull(message = "개인정보 처리방침 동의는 필수입니다.")
    @Schema(description = "개인정보 처리방침 동의 여부", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean agreePrivacyPolicy;

    @Schema(description = "마케팅 수신 동의 여부 (선택)", example = "false")
    private Boolean agreeMarketingConsent = false;

    /**
     * 테스트용 생성자입니다.
     */
    public SocialSignupRequestDto(
            String tempToken,
            String nickname,
            Boolean agreeTermsOfService,
            Boolean agreePrivacyPolicy,
            Boolean agreeMarketingConsent
    ) {
        this.tempToken = tempToken;
        this.nickname = nickname;
        this.agreeTermsOfService = agreeTermsOfService;
        this.agreePrivacyPolicy = agreePrivacyPolicy;
        this.agreeMarketingConsent = agreeMarketingConsent;
    }
}
