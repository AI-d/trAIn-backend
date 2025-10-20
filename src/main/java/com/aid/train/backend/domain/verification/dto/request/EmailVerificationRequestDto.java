package com.aid.train.backend.domain.verification.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 이메일 인증 요청 DTO입니다.
 * <p>
 * 로컬 회원가입 후 이메일로 발송된 6자리 인증 코드를 검증할 때 사용됩니다.
 * EMAIL_VERIFICATION_TOKEN(15분 유효)과 6자리 코드를 모두 검증하며,
 * 둘 다 맞으면 emailVerified=true로 변경됩니다.
 * </p>
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Schema(description = "이메일 인증 요청")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailVerificationRequestDto {

    /**
     * 이메일 주소
     * <p>
     * 인증할 이메일 주소입니다.
     * 회원가입 시 입력한 이메일과 동일해야 합니다.
     * </p>
     */
    @Schema(description = "이메일 주소", example = "user@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    private String email;

    /**
     * 6자리 인증 코드
     * <p>
     * 이메일로 발송된 6자리 숫자 인증 코드입니다.
     * 15분간 유효하며, 5회 이상 실패 시 일시적으로 차단됩니다.
     * </p>
     */
    @Schema(description = "6자리 인증 코드", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "인증 코드는 필수입니다")
    @Pattern(regexp = "^\\d{6}$", message = "인증 코드는 6자리 숫자여야 합니다")
    private String verificationCode;

    /**
     * 이메일 인증 토큰 (15분 유효)
     * <p>
     * 회원가입 시 생성된 EMAIL_VERIFICATION_TOKEN입니다.
     * 이 토큰과 인증 코드를 함께 검증하여 보안을 강화합니다.
     * </p>
     */
    @Schema(description = "이메일 인증 토큰 (15분 유효)", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "이메일 인증 토큰은 필수입니다")
    private String emailVerificationToken;
}
