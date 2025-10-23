package com.aid.train.backend.domain.verification.dto.request;

import com.aid.train.backend.global.util.LogMaskingUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 인증 코드 재발송 요청 DTO입니다.
 * <p>
 * 이메일 인증 코드가 만료되었거나 받지 못한 경우 새로운 인증 코드를 재발송할 때 사용됩니다.
 * 새로운 6자리 코드와 EMAIL_VERIFICATION_TOKEN(15분 유효)을 생성하여 이메일로 발송합니다.
 * </p>
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Schema(description = "인증 코드 재발송 요청")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailResendRequestDto {

    /**
     * 이메일 주소
     * <p>
     * 인증 코드를 재발송할 이메일 주소입니다.
     * 회원가입 시 입력한 이메일이어야 하며, 이미 인증된 이메일은 재발송할 수 없습니다.
     * </p>
     */
    @Schema(description = "이메일 주소", example = "user@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    private String email;

    /**
     * 로그 출력 시 민감정보(이메일)를 마스킹합니다.
     * 디버깅 중 toString() 호출로 인해 정보가 유출되지 않도록 보호합니다.
     */
    @Override
    public String toString() {
        return "EmailResendRequestDto(" +
                "email=" + LogMaskingUtil.maskEmail(email) +
                ")";
    }

}
