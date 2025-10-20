package com.aid.train.backend.domain.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 로그인 응답 DTO입니다.
 * <p>
 * 로컬 로그인 또는 소셜 로그인 성공 시 반환되는 정보를 담습니다.
 * ACCESS_TOKEN(15분 유효)과 REFRESH_TOKEN(14일 유효)을 발급하며,
 * HttpOnly 쿠키로 저장됩니다.
 * </p>
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Schema(description = "로그인 응답")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponseDto {

    /**
     * 사용자 ID
     * <p>
     * 로그인한 사용자의 고유 식별자입니다.
     * </p>
     */
    @Schema(description = "사용자 ID", example = "1")
    private Long userId;

    /**
     * 이메일 주소
     * <p>
     * 로그인한 사용자의 이메일 주소입니다.
     * </p>
     */
    @Schema(description = "이메일 주소", example = "user@example.com")
    private String email;

    /**
     * 사용자 이름
     * <p>
     * 로그인한 사용자의 이름입니다.
     * </p>
     */
    @Schema(description = "사용자 이름", example = "홍길동")
    private String name;

    /**
     * Access Token (15분 유효)
     * <p>
     * API 요청 시 Authorization 헤더에 포함하여 사용합니다.
     * 형식: "Bearer {accessToken}"
     * 15분 후 만료되며, Refresh Token으로 자동 갱신됩니다.
     * </p>
     */
    @Schema(description = "Access Token (15분 유효)", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;

    /**
     * Refresh Token (14일 유효)
     * <p>
     * Access Token 갱신 시 사용됩니다.
     * HttpOnly 쿠키로 저장되어 XSS 공격으로부터 보호됩니다.
     * 14일 후 만료되며, 만료 시 재로그인이 필요합니다.
     * </p>
     */
    @Schema(description = "Refresh Token (14일 유효)", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String refreshToken;
}
