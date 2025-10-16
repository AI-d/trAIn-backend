package com.aid.train.backend.domain.user.dto.response;

import com.aid.train.backend.domain.user.enums.Provider;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 소셜 로그인 응답 DTO입니다.
 * 소셜 로그인 성공 시 토큰과 사용자 정보를 반환합니다.
 * 신규 가입이 필요한 경우 tempToken을 반환합니다.
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "소셜 로그인 응답")
public class SocialLoginResponseDto {

    @Schema(description = "로그인 성공 여부", example = "true")
    private Boolean isSuccess;

    @Schema(description = "신규 가입 필요 여부", example = "false")
    private Boolean requiresSignup;

    @Schema(description = "임시 토큰 (신규 가입 시)", example = "temp_abc123...")
    private String tempToken;

    @Schema(description = "액세스 토큰 (기존 회원)", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;

    @Schema(description = "리프레시 토큰 (기존 회원)", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String refreshToken;

    @Schema(description = "토큰 타입", example = "Bearer")
    @Builder.Default
    private String tokenType = "Bearer";

    @Schema(description = "액세스 토큰 만료 시간 (초)", example = "3600")
    private Long expiresIn;

    @Schema(description = "사용자 ID (기존 회원)", example = "1")
    private Long userId;

    @Schema(description = "이메일", example = "user@example.com")
    private String email;

    @Schema(description = "닉네임 (기존 회원)", example = "면접왕")
    private String nickname;

    @Schema(description = "프로필 이미지 URL", example = "https://cdn.example.com/profile/123.jpg")
    private String profileImageUrl;

    @Schema(description = "소셜 제공자", example = "GOOGLE")
    private Provider provider;

    @Schema(description = "마지막 로그인 시각")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastLoginAt;

    /**
     * 기존 회원 로그인 응답을 생성합니다.
     *
     * @param accessToken 액세스 토큰
     * @param refreshToken 리프레시 토큰
     * @param expiresIn 만료 시간
     * @param userId 사용자 ID
     * @param email 이메일
     * @param nickname 닉네임
     * @param profileImageUrl 프로필 이미지 URL
     * @param provider 소셜 제공자
     * @param lastLoginAt 마지막 로그인 시각
     * @return 로그인 성공 응답 DTO
     */
    public static SocialLoginResponseDto ofExistingUser(
            String accessToken,
            String refreshToken,
            Long expiresIn,
            Long userId,
            String email,
            String nickname,
            String profileImageUrl,
            Provider provider,
            LocalDateTime lastLoginAt
    ) {
        return SocialLoginResponseDto.builder()
                .isSuccess(true)
                .requiresSignup(false)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(expiresIn)
                .userId(userId)
                .email(email)
                .nickname(nickname)
                .profileImageUrl(profileImageUrl)
                .provider(provider)
                .lastLoginAt(lastLoginAt)
                .build();
    }

    /**
     * 신규 가입 필요 응답을 생성합니다.
     *
     * @param tempToken 임시 토큰
     * @param email 이메일
     * @param profileImageUrl 프로필 이미지 URL
     * @param provider 소셜 제공자
     * @return 신규 가입 필요 응답 DTO
     */
    public static SocialLoginResponseDto ofNewUser(
            String tempToken,
            String email,
            String profileImageUrl,
            Provider provider
    ) {
        return SocialLoginResponseDto.builder()
                .isSuccess(false)
                .requiresSignup(true)
                .tempToken(tempToken)
                .email(email)
                .profileImageUrl(profileImageUrl)
                .provider(provider)
                .build();
    }
}
