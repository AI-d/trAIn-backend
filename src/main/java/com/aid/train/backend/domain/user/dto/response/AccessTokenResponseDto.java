package com.aid.train.backend.domain.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 일회용 코드-토큰 교환 API 성공 시 반환되는 응답 DTO입니다.
 * <p>
 * 소셜 로그인 (기존 회원) 시 일회용 코드를 AccessToken으로 교환하는 과정에서 사용됩니다.
 * 이 토큰은 15분간 유효하며, Authorization 헤더에 Bearer 토큰으로 사용됩니다.
 * </p>
 * 
 * <p>
 * **주의사항:**
 * - 이 API는 기존 회원의 소셜 로그인에만 사용됩니다.
 * - 신규 회원은 소셜 회원가입 완료 후 일반 로그인 응답(LoginResponseDto)을 받습니다.
 * - RefreshToken은 이 응답에 포함되지 않으며, 별도 API(/refresh)를 통해 관리됩니다.
 * </p>
 * 
 * <p>
 * **사용 흐름:**
 * 1. 일회용 코드 → AccessToken 교환 (이 DTO 사용)
 * 2. AccessToken으로 API 호출
 * 3. AccessToken 만료 시 → /refresh API로 갱신
 * </p>
 *
 * @author 왕택준
 * @since 1.0.0
 * @see com.aid.train.backend.domain.user.controller.UserController#exchangeToken(com.aid.train.backend.domain.user.dto.request.ExchangeCodeRequestDto)
 */
@Schema(description = "AccessToken 교환 성공 응답 DTO")
public record AccessTokenResponseDto(
        
        /**
         * 새로 발급된 Access Token (15분 유효)
         * <p>
         * JWT 형태의 토큰으로, API 요청 시 Authorization 헤더에 포함하여 사용합니다.
         * 형식: `Authorization: Bearer {accessToken}`
         * </p>
         * 
         * <p>
         * **토큰 관리 방법:**
         * - 클라이언트(React)는 이 토큰을 메모리(State)에 저장해야 합니다.
         * - localStorage나 sessionStorage 저장은 XSS 공격에 취약하므로 권장하지 않습니다.
         * - 15분 후 만료되면 Axios Interceptor가 자동으로 /refresh API를 호출하여 갱신합니다.
         * </p>
         */
        @Schema(
                description = "새로 발급된 Access Token (15분 유효)", 
                example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkhvbmcgR2lsZG9uZyIsImlhdCI6MTUxNjIzOTAyMn0.keH6T3x1z7mmhKL1T3r09-pgM1il5MMUrC9XSTF6eho",
                required = true
        )
        String accessToken
) {
}