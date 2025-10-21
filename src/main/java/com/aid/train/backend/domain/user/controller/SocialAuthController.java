package com.aid.train.backend.domain.user.controller;

import com.aid.train.backend.domain.user.dto.request.SocialLoginRequestDto;
import com.aid.train.backend.domain.user.dto.request.SocialSignupRequestDto;
import com.aid.train.backend.domain.user.dto.response.SignupResponseDto;
import com.aid.train.backend.domain.user.dto.response.SocialLoginResponseDto;
import com.aid.train.backend.domain.user.service.SocialAuthService;
import com.aid.train.backend.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * 소셜 로그인 API 컨트롤러입니다.
 * OAuth 기반 소셜 로그인 및 회원가입 엔드포인트를 제공합니다.
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Tag(name = "03. 소셜 로그인", description = "OAuth 기반 소셜 로그인 및 회원가입 API")
@RestController
@RequestMapping("/api/v1/auth/social")
@RequiredArgsConstructor
@Slf4j
public class SocialAuthController {

    private final SocialAuthService socialAuthService;

    /**
     * 소셜 로그인을 처리합니다.
     * 기존 사용자는 로그인, 신규 사용자는 임시 토큰을 발급합니다.
     *
     * @param request 소셜 로그인 요청 DTO
     * @return 소셜 로그인 응답 DTO
     */
    @Operation(
            summary = "소셜 로그인",
            description = "OAuth 인증 코드로 소셜 로그인을 처리합니다. " +
                    "기존 사용자는 토큰을 발급하고, 신규 사용자는 임시 토큰을 반환합니다."
    )
    @PostMapping("/login")
    public ApiResponse<SocialLoginResponseDto> socialLogin(
            @Valid @RequestBody SocialLoginRequestDto request
    ) {
        log.info("[API] POST /api/v1/auth/social/login - 제공자: {}", request.getProvider());
        SocialLoginResponseDto response = socialAuthService.socialLogin(request);
        
        if (response.getIsNewUser()) {
            return ApiResponse.ok(response, "신규 사용자입니다. 추가 정보를 입력해주세요.");
        } else {
            return ApiResponse.ok(response, "소셜 로그인에 성공했습니다.");
        }
    }

    /**
     * 소셜 회원가입을 처리합니다.
     * 임시 토큰으로 회원가입을 완료하고 정식 토큰을 발급합니다.
     *
     * @param request 소셜 회원가입 요청 DTO
     * @return 회원가입 응답 DTO
     */
    @Operation(
            summary = "소셜 회원가입",
            description = "임시 토큰으로 소셜 회원가입을 완료하고 정식 토큰을 발급합니다."
    )
    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<SignupResponseDto> socialSignup(
            @Valid @RequestBody SocialSignupRequestDto request
    ) {
        log.info("[API] POST /api/v1/auth/social/signup");
        SignupResponseDto response = socialAuthService.socialSignup(request);
        return ApiResponse.created(response, "소셜 회원가입이 완료되었습니다.");
    }
}
