package com.aid.train.backend.domain.verification.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 이메일 인증 요청 DTO입니다.
 * 이메일 인증 코드 발송 및 검증 시 사용됩니다.
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "이메일 인증 요청 DTO")
public class EmailVerificationRequestDto {

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    @Schema(description = "인증할 이메일", example = "user@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    /**
     * 테스트용 생성자입니다.
     *
     * @param email 이메일
     */
    public EmailVerificationRequestDto(String email) {
        this.email = email;
    }
}
