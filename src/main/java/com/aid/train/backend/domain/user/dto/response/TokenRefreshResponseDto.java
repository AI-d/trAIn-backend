package com.aid.train.backend.domain.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 토큰 갱신 응답 DTO입니다.
 * <p>
 * Access Token 갱신 시 반환되는 정보를 담습니다.
 * Axios Interceptor가 401 에러를 감지하면 자동으로 토큰 갱신을 요청하며,
 * 새로운 15분짜리 Access Token을 발급받습니다.
 * 사용자는 이 과정을 전혀 느끼지 못합니다.
 * </p>
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Schema(description = "토큰 갱신 응답")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenRefreshResponseDto {

    /**
     * 새로운 Access Token (15분 유효)
     * <p>
     * 갱신된 Access Token입니다.
     * API 요청 시 Authorization 헤더에 포함하여 사용합니다.
     * 형식: "Bearer {accessToken}"
     * </p>
     */
    @Schema(description = "새로운 Access Token (15분 유효)", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;

    /**
     * 새로운 Refresh Token (7일 유효, 선택적)
     * <p>
     * Refresh Token이 만료 임박 시에만 새로 발급됩니다.
     * 클라이언트는 이 값을 받으면 기존 Refresh Token을 교체해야 합니다.
     * </p>
     */
    @Schema(description = "새로운 Refresh Token (7일 유효, 선택적)", example = "eyJhbGciOi34zI1Niasd34CI6IkpXVCJ9...")
    private String refreshToken;

}
