package com.aid.train.backend.domain.verification.dto.response;

import com.aid.train.backend.domain.user.enums.Provider;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 소셜 로그인 콜백 응답 DTO입니다.
 * <p>
 * OAuth2 소셜 로그인(Google, Kakao, Naver) 콜백 처리 후 반환되는 정보를 담습니다.
 * 기존 회원인 경우 즉시 토큰을 발급하고, 신규 회원인 경우 추가 정보 입력이 필요합니다.
 * </p>
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Schema(description = "소셜 로그인 콜백 응답")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SocialCallbackResponseDto {

    /**
     * 신규 사용자 여부
     * <p>
     * true: 신규 사용자 - 추가 정보 입력 필요
     * false: 기존 사용자 - 즉시 로그인
     * </p>
     */
    @Schema(description = "신규 사용자 여부", example = "false")
    private Boolean isNewUser;

    /**
     * Access Token (기존 회원만, 15분 유효)
     * <p>
     * 기존 회원인 경우 즉시 발급되는 Access Token입니다.
     * 신규 회원인 경우 null입니다.
     * </p>
     */
    @Schema(description = "Access Token (기존 회원만, 15분 유효)", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;

    /**
     * Refresh Token (기존 회원만, 14일 유효)
     * <p>
     * 기존 회원인 경우 즉시 발급되는 Refresh Token입니다.
     * 신규 회원인 경우 null입니다.
     * </p>
     */
    @Schema(description = "Refresh Token (기존 회원만, 14일 유효)", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String refreshToken;

    /**
     * 소셜 회원가입 대기 토큰 (신규 회원만, 15분 유효)
     * <p>
     * 신규 회원인 경우 발급되는 SOCIAL_SIGNUP_PENDING_TOKEN입니다.
     * 이 토큰으로 추가 정보 입력 후 회원가입을 완료합니다.
     * 기존 회원인 경우 null입니다.
     * </p>
     */
    @Schema(description = "소셜 회원가입 대기 토큰 (신규 회원만, 15분 유효)", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String socialSignupPendingToken;

    /**
     * 이메일 주소
     * <p>
     * 소셜 제공자에서 받아온 이메일 주소입니다.
     * 신규 회원인 경우 추가 정보 입력 페이지에 표시됩니다.
     * </p>
     */
    @Schema(description = "이메일 주소", example = "user@example.com")
    private String email;

    /**
     * 사용자 이름
     * <p>
     * 소셜 제공자에서 받아온 사용자 이름입니다.
     * 신규 회원인 경우 추가 정보 입력 페이지에 표시됩니다.
     * </p>
     */
    @Schema(description = "사용자 이름", example = "홍길동")
    private String name;

    /**
     * 소셜 제공자
     * <p>
     * 로그인에 사용한 소셜 제공자입니다.
     * GOOGLE, KAKAO, NAVER 중 하나입니다.
     * </p>
     */
    @Schema(description = "소셜 제공자", example = "GOOGLE")
    private Provider provider;
}
