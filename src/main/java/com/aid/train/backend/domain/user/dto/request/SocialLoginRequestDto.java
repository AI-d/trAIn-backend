package com.aid.train.backend.domain.user.dto.request;

import com.aid.train.backend.domain.user.enums.Provider;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 소셜 로그인 요청 DTO입니다.
 * 소셜 OAuth 로그인 시 사용됩니다.
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "소셜 로그인 요청 DTO")
public class SocialLoginRequestDto {

    @NotNull(message = "소셜 제공자는 필수입니다.")
    @Schema(description = "소셜 제공자 (GOOGLE, KAKAO, NAVER)", example = "GOOGLE", requiredMode = Schema.RequiredMode.REQUIRED)
    private Provider provider;

    @NotBlank(message = "인가 코드는 필수입니다.")
    @Schema(description = "OAuth 인가 코드", example = "4/0AX4XfWh...", requiredMode = Schema.RequiredMode.REQUIRED)
    private String authorizationCode;

    @Schema(description = "리다이렉트 URI", example = "http://localhost:3000/auth/callback")
    private String redirectUri;

    @Schema(description = "디바이스 ID (선택)", example = "device-uuid-12345")
    private String deviceId;

    /**
     * 테스트용 생성자입니다.
     */
    public SocialLoginRequestDto(Provider provider, String authorizationCode, String redirectUri) {
        this.provider = provider;
        this.authorizationCode = authorizationCode;
        this.redirectUri = redirectUri;
    }

    /**
     * 테스트용 생성자입니다.
     */
    public SocialLoginRequestDto(Provider provider, String authorizationCode, String redirectUri, String deviceId) {
        this.provider = provider;
        this.authorizationCode = authorizationCode;
        this.redirectUri = redirectUri;
        this.deviceId = deviceId;
    }
}
