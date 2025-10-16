package com.aid.train.backend.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 토큰 갱신 요청 DTO입니다.
 * RefreshToken을 사용한 AccessToken 재발급 시 사용됩니다.
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "토큰 갱신 요청 DTO")
public class RefreshTokenRequestDto {

    @NotBlank(message = "리프레시 토큰은 필수입니다.")
    @Size(max = 4096, message = "리프레시 토큰은 4096자를 초과할 수 없습니다.")
    @Pattern(regexp = "^(?=\\S+$).+$", message = "리프레시 토큰은 공백을 포함할 수 없습니다.")
    @Schema(description = "리프레시 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...", requiredMode = Schema.RequiredMode.REQUIRED)
    private String refreshToken;

    /**
     * 테스트용 생성자입니다.
     *
     * @param refreshToken 리프레시 토큰
     */
    public RefreshTokenRequestDto(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
