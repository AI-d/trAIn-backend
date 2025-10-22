package com.aid.train.backend.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 비밀번호 변경 요청 DTO입니다.
 * <p>
 * 로그인한 사용자가 자신의 비밀번호를 변경할 때 사용됩니다.
 * 현재 비밀번호를 확인한 후 새로운 비밀번호로 변경합니다.
 * 소셜 로그인 사용자는 비밀번호가 없으므로 이 기능을 사용할 수 없습니다.
 * </p>
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Schema(description = "비밀번호 변경 요청")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordChangeRequestDto {

    /**
     * 현재 비밀번호
     * <p>
     * 비밀번호 변경 전 본인 확인을 위해 현재 비밀번호를 입력받습니다.
     * BCrypt로 해시화된 비밀번호와 비교하여 검증합니다.
     * </p>
     */
    @Schema(description = "현재 비밀번호", example = "OldPassword123!", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "현재 비밀번호는 필수입니다")
    private String currentPassword;

    /**
     * 새 비밀번호
     * <p>
     * 8-20자, 영문 대소문자, 숫자, 특수문자를 모두 포함해야 합니다.
     * 현재 비밀번호와 동일하면 안 됩니다.
     * BCrypt로 해시화되어 저장됩니다.
     * </p>
     */
    @Schema(description = "새 비밀번호 (8-20자, 영문+숫자+특수문자)", example = "NewPassword123!", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "새 비밀번호는 필수입니다")
    @Pattern(
            regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[@$!%*?&#])[A-Za-z\\d@$!%*?&#]{8,20}$",
            message = "비밀번호는 8-20자의 영문, 숫자, 특수문자를 포함해야 합니다"
    )
    private String newPassword;

    /**
     * 새 비밀번호 확인
     * <p>
     * newPassword 필드와 정확히 일치해야 합니다.
     * 프론트엔드와 백엔드 양쪽에서 검증합니다.
     * </p>
     */
    @Schema(description = "새 비밀번호 확인", example = "NewPassword123!", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "새 비밀번호 확인은 필수입니다")
    private String newPasswordConfirm;

    /**
     * 새 비밀번호와 새 비밀번호 확인이 일치하는지 검증합니다.
     *
     * @return 일치하면 true, 불일치하면 false
     */
    public boolean isNewPasswordMatched() {
        return newPassword != null && newPassword.equals(newPasswordConfirm);
    }

    /**
     * 현재 비밀번호와 새 비밀번호가 동일한지 확인합니다.
     *
     * @return 동일하면 true, 다르면 false
     */
    public boolean isSameAsCurrentPassword() {
        return currentPassword != null && currentPassword.equals(newPassword);
    }
}
