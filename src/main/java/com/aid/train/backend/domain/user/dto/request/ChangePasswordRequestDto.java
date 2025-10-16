package com.aid.train.backend.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 비밀번호 변경 요청 DTO입니다.
 * 기존 비밀번호 확인 후 새 비밀번호로 변경 시 사용됩니다.
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "비밀번호 변경 요청 DTO")
public class ChangePasswordRequestDto {

    @NotBlank(message = "현재 비밀번호는 필수입니다.")
    @Schema(description = "현재 비밀번호", example = "oldPassword123!", requiredMode = Schema.RequiredMode.REQUIRED)
    private String currentPassword;

    @NotBlank(message = "새 비밀번호는 필수입니다.")
    @Pattern(regexp = "^(?=\\S+$).{8,20}$", message = "비밀번호는 8~20자이며 공백을 포함할 수 없습니다.")
    @Schema(description = "새 비밀번호 (8~20자, 공백 불가)", example = "newPassword123!", requiredMode = Schema.RequiredMode.REQUIRED)
    private String newPassword;

    /**
     * 테스트용 생성자입니다.
     *
     * @param currentPassword 현재 비밀번호
     * @param newPassword     새 비밀번호
     */
    public ChangePasswordRequestDto(String currentPassword, String newPassword) {
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
    }
}
