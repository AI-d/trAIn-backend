package com.aid.train.backend.domain.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Access Token 갱신 성공 시 클라이언트에게 반환되는 응답 DTO입니다.
 * <p>
 * Refresh Token Rotation(RTR) 정책에 따라 새로운 Access Token과
 * (항상) 새로운 Refresh Token이 발급됩니다.
 * 클라이언트(Axios Interceptor)는 이 응답을 받아 Access Token을 교체하고
 * 실패했던 원래 요청을 재시도합니다. 브라우저는 응답 헤더의 Set-Cookie를 통해
 * Refresh Token을 자동으로 갱신합니다.
 * </p>
 * <p>
 * **중요**: 실제 HTTP 응답 시 `refreshToken` 필드는 `null`로 설정되어 반환됩니다.
 * 실제 새로운 RefreshToken 값은 보안을 위해 HttpOnly 쿠키로만 전달(갱신)됩니다.
 * </p>
 *
 * @author 왕택준
 * @since 1.0.0
 * @see com.aid.train.backend.domain.user.controller.UserController#refresh(jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse)
 */
@Schema(description = "토큰 갱신 응답")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenRefreshResponseDto {

    /**
     * 새로 발급된 Access Token (15분 유효)
     * <p>
     * 클라이언트(React)는 이 값을 받아 기존 Access Token을 교체하고
     * 메모리(State)에 저장하여 다음 API 요청부터 사용해야 합니다.
     * 형식: "Bearer {accessToken}"
     * </p>
     */
    @Schema(description = "새로운 Access Token (15분 유효)", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String accessToken;

    /**
     * 새로 발급된 Refresh Token (14일 유효)
     * <p>
     * **주의:** 이 필드는 실제 API 응답 시에는 항상 `null`입니다.
     * 실제 값은 HttpOnly 쿠키로만 전달(갱신)됩니다.
     * </p>
     * <p>
     * -- SETTER -- <br>
     * 컨트롤러에서 RefreshToken 값을 null로 설정하기 위해 사용됩니다.
     * </p>
     *
     * @param refreshToken 컨트롤러에서 null 값을 전달받습니다.
     */
    @Setter // refreshToken 필드에만 Setter 적용
    @Schema(description = "새로운 Refresh Token (14일 유효, 응답 시 항상 null)", example = "null", nullable = true)
    private String refreshToken;
}