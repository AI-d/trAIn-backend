package com.aid.train.backend.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 로컬 로그인 요청 DTO입니다.
 * <p>
 * 이메일/비밀번호 기반 로그인 시 사용됩니다.
 * 로그인 성공 시 ACCESS_TOKEN(15분)과 REFRESH_TOKEN(14일)을 발급받습니다.
 * 이메일 인증이 완료되지 않은 경우 로그인할 수 없습니다.
 * </p>
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Schema(description = "로컬 로그인 요청")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequestDto {

    /**
     * 이메일 주소 (로그인 ID)
     * <p>
     * 회원가입 시 사용한 이메일 주소를 입력합니다.
     * 표준 이메일 형식을 따라야 합니다.
     * </p>
     */
    @Schema(description = "이메일 주소", example = "user@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    private String email;

    /**
     * 비밀번호
     * <p>
     * 회원가입 시 설정한 비밀번호를 입력합니다.
     * BCrypt로 해시화된 비밀번호와 비교하여 검증합니다.
     * </p>
     */
    @Schema(description = "비밀번호", example = "Password123!", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "비밀번호는 필수입니다")
    private String password;
}
