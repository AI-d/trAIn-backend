package com.aid.train.backend.domain.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 로그인 성공 시 클라이언트에게 반환되는 응답 DTO입니다.
 * <p>
 * 로컬 로그인, 소셜 가입 완료 후 로그인 처리 시 사용됩니다.
 * AccessToken은 API 요청 시 Bearer 헤더에 사용되며,
 * RefreshToken은 재발급 요청 외에는 클라이언트가 직접 사용할 필요가 없습니다.
 * </p>
 * <p>
 * **중요**: 실제 HTTP 응답 시 `refreshToken` 필드는 `null`로 설정되어 반환됩니다.
 * 실제 RefreshToken 값은 보안을 위해 HttpOnly 쿠키로만 전달됩니다.
 * </p>
 *
 * @author 왕택준
 * @since 1.0.0
 * @see com.aid.train.backend.domain.user.controller.UserController#login(com.aid.train.backend.domain.user.dto.request.LoginRequestDto, jakarta.servlet.http.HttpServletResponse)
 * @see com.aid.train.backend.domain.verification.controller.VerificationController#completeSocialSignup(com.aid.train.backend.domain.user.dto.request.SocialSignupCompleteRequestDto, jakarta.servlet.http.HttpServletResponse)
 */
@Schema(description = "로그인 성공 응답")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDto {

    /**
     * 로그인한 사용자의 고유 ID
     */
    @Schema(description = "사용자 ID", example = "1")
    private Long userId;

    /**
     * 로그인한 사용자의 이메일 주소
     */
    @Schema(description = "사용자 이메일", example = "user@example.com")
    private String email;

    /**
     * 로그인한 사용자의 이름
     */
    @Schema(description = "사용자 이름", example = "홍길동")
    private String name;

    /**
     * 발급된 Access Token (15분 유효)
     * <p>
     * API 요청 시 `Authorization: Bearer {accessToken}` 헤더에 사용됩니다.
     * 클라이언트(React)는 이 값을 메모리(State)에 저장하여 관리해야 합니다.
     * </p>
     */
    @Schema(description = "Access Token (15분 유효)", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String accessToken;

    /**
     * 발급된 Refresh Token (14일 유효)
     * <p>
     * **주의:** 이 필드는 실제 API 응답 시에는 항상 `null`입니다.
     * 실제 값은 HttpOnly 쿠키로만 전달됩니다.
     * </p>
     * <p>
     * -- SETTER -- <br>
     * 컨트롤러에서 RefreshToken 값을 null로 설정하기 위해 사용됩니다.
     * </p>
     * @param refreshToken 컨트롤러에서 null 값을 전달받습니다.
     */
    @Setter // refreshToken 필드에만 Setter 적용
    @Schema(description = "Refresh Token (14일 유효, 응답 시 항상 null)", example = "null", nullable = true)
    private String refreshToken;
}