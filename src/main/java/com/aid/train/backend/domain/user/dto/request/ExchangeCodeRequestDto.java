package com.aid.train.backend.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * 일회용 코드-토큰 교환 API 요청 DTO입니다.
 * <p>
 * 소셜 로그인 성공 후 URL 파라미터로 받은 일회용 코드를 담아 전송합니다.
 * 이 코드는 5분간 유효하며, 한번 사용 시 즉시 삭제됩니다.
 * </p>
 * 
 * <p>
 * **사용 시나리오:**
 * 1. 소셜 로그인 성공 → OAuth2AuthenticationSuccessHandler가 일회용 코드 생성
 * 2. 프론트엔드 리다이렉트: `/?code=abc123def456...`
 * 3. 프론트엔드에서 이 DTO로 `/api/v1/users/token/exchange` API 호출
 * 4. 서버에서 AccessToken 반환 → 정상 로그인 완료
 * </p>
 *
 * @author 왕택준
 * @since 1.0.0
 * @see com.aid.train.backend.domain.user.controller.UserController#exchangeToken(ExchangeCodeRequestDto)
 */
@Schema(description = "일회용 코드-토큰 교환 요청 DTO")
public record ExchangeCodeRequestDto(
        
        /**
         * URL 파라미터 'code'로 받은 일회용 코드
         * <p>
         * 소셜 로그인 성공 후 리다이렉트 URL에서 추출한 코드입니다.
         * 16자리 알파벳+숫자 조합으로 구성되며, 1분간 유효합니다.
         * </p>
         */
        @Schema(
                description = "URL 쿼리 파라미터 'code'로 받은 일회용 코드 (16자리, 1분 유효)", 
                required = true, 
                example = "a1b2c3d4e5f6g7h8",
                minLength = 16,
                maxLength = 16
        )
        @NotBlank(message = "일회용 코드는 필수입니다.")
        String code
) {
}