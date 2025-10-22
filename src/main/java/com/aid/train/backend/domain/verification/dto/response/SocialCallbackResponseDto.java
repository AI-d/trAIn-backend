package com.aid.train.backend.domain.verification.dto.response;

import com.aid.train.backend.domain.user.enums.Provider;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

/**
 * OAuth2 소셜 로그인 처리 결과를 담는 응답 DTO입니다.
 * <p>
 * AuthService.processOAuth2User() 메서드에서 반환되며,
 * OAuth2AuthenticationSuccessHandler가 이 정보를 바탕으로 리다이렉트 URL을 결정합니다.
 * </p>
 *
 * <p>
 * **변경사항 (일회용 코드 방식 도입):**
 * - 기존 회원: accessToken, refreshToken 대신 oneTimeCode 필드 사용
 * - 신규 회원: 기존과 동일하게 socialSignupPendingToken 사용
 * </p>
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Schema(description = "소셜 로그인 콜백 처리 결과")
@Getter
@Builder
public class SocialCallbackResponseDto {

    /**
     * 신규 사용자 여부
     * <p>
     * true: 신규 회원 (추가 정보 입력 필요)
     * false: 기존 회원 (즉시 로그인 처리)
     * </p>
     */
    @Schema(description = "신규 사용자 여부", example = "false")
    private Boolean isNewUser;

    /**
     * 기존 회원용 일회용 코드 (1분 유효)
     * <p>
     * **isNewUser = false인 경우에만 값이 존재합니다.**
     * 프론트엔드에서 이 코드를 받아 /api/v1/users/token/exchange API를 호출하여
     * AccessToken으로 교환해야 합니다.
     * </p>
     */
    @Schema(description = "기존 회원용 일회용 코드 (1분 유효)", example = "a1b2c3d4e5f6g7h8", nullable = true)
    private String oneTimeCode;

    /**
     * 신규 회원용 소셜 회원가입 대기 토큰 (15분 유효)
     * <p>
     * **isNewUser = true인 경우에만 값이 존재합니다.**
     * 프론트엔드에서 이 토큰과 함께 추가 정보를 입력하여
     * /api/v1/verification/social/complete API를 호출해야 합니다.
     * </p>
     */
    @Schema(description = "신규 회원용 소셜 회원가입 대기 토큰 (15분 유효)", example = "eyJhbGciOiJIUzI1NiJ9...", nullable = true)
    private String socialSignupPendingToken;

    /**
     * 사용자 이메일 주소
     */
    @Schema(description = "사용자 이메일", example = "user@example.com")
    private String email;

    /**
     * 사용자 이름
     */
    @Schema(description = "사용자 이름", example = "홍길동")
    private String name;

    /**
     * 소셜 로그인 제공자
     */
    @Schema(description = "소셜 로그인 제공자", example = "GOOGLE")
    private Provider provider;

    // 기존 필드들 제거됨: accessToken, refreshToken
    // → 기존 회원은 oneTimeCode 사용
    // → 신규 회원은 소셜 회원가입 완료 후 일반 로그인 응답(LoginResponseDto) 사용
}