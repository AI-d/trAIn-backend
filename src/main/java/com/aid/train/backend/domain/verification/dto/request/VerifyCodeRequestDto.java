package com.aid.train.backend.domain.verification.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 이메일 인증 코드 검증 요청 DTO입니다.
 * 발송된 인증 코드를 검증할 때 사용됩니다.
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "이메일 인증 코드 검증 요청 DTO")
public class VerifyCodeRequestDto {

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    @Schema(description = "인증할 이메일", example = "user@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotBlank(message = "인증 코드는 필수입니다.")
    @Pattern(regexp = "^[0-9]{6}$", message = "인증 코드는 6자리 숫자입니다.")
    @Schema(description = "인증 코드 (6자리 숫자)", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;

    /**
     * 테스트용 생성자입니다.
     *
     * @param email 이메일
     * @param code  인증 코드
     */
    public VerifyCodeRequestDto(String email, String code) {
        this.email = email;
        this.code = code;
    }
}
