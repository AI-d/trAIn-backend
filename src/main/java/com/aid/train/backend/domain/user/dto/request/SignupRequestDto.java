package com.aid.train.backend.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 회원가입 요청 DTO입니다.
 * 이메일 기반 회원가입 시 사용됩니다.
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "회원가입 요청 DTO")
public class SignupRequestDto {

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    @Schema(description = "이메일", example = "user@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Pattern(regexp = "^(?=\\S+$).{8,20}$", message = "비밀번호는 8~20자이며 공백을 포함할 수 없습니다.")
    @Schema(description = "비밀번호 (8~20자, 공백 불가, 영문+숫자+특수문자 권장)", example = "password123!", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

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

    @NotBlank(message = "이메일 인증 코드는 필수입니다.")
    @Pattern(regexp = "^[0-9]{6}$", message = "인증 코드는 6자리 숫자입니다.")
    @Schema(description = "이메일 인증 코드 (6자리 숫자)", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
    private String verificationCode;

    /**
     * 테스트용 생성자입니다.
     */
    public SignupRequestDto(
            String email,
            String password,
            String nickname,
            Boolean agreeTermsOfService,
            Boolean agreePrivacyPolicy,
            Boolean agreeMarketingConsent,
            String verificationCode
    ) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.agreeTermsOfService = agreeTermsOfService;
        this.agreePrivacyPolicy = agreePrivacyPolicy;
        this.agreeMarketingConsent = agreeMarketingConsent;
        this.verificationCode = verificationCode;
    }
}
