package com.aid.train.backend.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 로그인 요청 DTO입니다.
 * 이메일과 비밀번호를 사용한 일반 로그인 요청 시 사용됩니다.
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "로그인 요청 DTO")
public class LoginRequestDto {

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    @Schema(description = "이메일", example = "user@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Pattern(regexp = "^(?=\\S+$).{8,20}$", message = "비밀번호는 8~20자이며 공백을 포함할 수 없습니다.")
    @Schema(description = "비밀번호 (8~20자, 공백 불가)", example = "password123!", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    @Schema(description = "디바이스 ID (선택)", example = "device-uuid-12345")
    private String deviceId;

    /**
     * 테스트용 생성자입니다.
     *
     * @param email    이메일
     * @param password 비밀번호
     */
    public LoginRequestDto(String email, String password) {
        this.email = email;
        this.password = password;
    }

    /**
     * 테스트용 생성자입니다.
     *
     * @param email    이메일
     * @param password 비밀번호
     * @param deviceId 디바이스 ID
     */
    public LoginRequestDto(String email, String password, String deviceId) {
        this.email = email;
        this.password = password;
        this.deviceId = deviceId;
    }
}
