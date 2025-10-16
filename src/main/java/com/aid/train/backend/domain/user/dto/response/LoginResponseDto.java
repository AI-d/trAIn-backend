package com.aid.train.backend.domain.user.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 로그인 응답 DTO입니다.
 * 로그인 성공 시 JWT 토큰과 사용자 기본 정보를 반환합니다.
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "로그인 응답")
public class LoginResponseDto {

    @Schema(description = "액세스 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;

    @Schema(description = "리프레시 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String refreshToken;

    @Schema(description = "토큰 타입", example = "Bearer")
    @Builder.Default
    private String tokenType = "Bearer";

    @Schema(description = "액세스 토큰 만료 시간 (초)", example = "3600")
    private Long expiresIn;

    @Schema(description = "사용자 ID", example = "1")
    private Long userId;

    @Schema(description = "이메일", example = "user@example.com")
    private String email;

    @Schema(description = "닉네임", example = "면접왕")
    private String nickname;

    @Schema(description = "프로필 이미지 URL", example = "https://cdn.example.com/profile/123.jpg")
    private String profileImageUrl;

    @Schema(description = "마지막 로그인 시각")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastLoginAt;
}
