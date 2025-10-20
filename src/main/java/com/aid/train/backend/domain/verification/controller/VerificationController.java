package com.aid.train.backend.domain.verification.controller;

import com.aid.train.backend.domain.user.dto.request.SocialSignupCompleteRequestDto;
import com.aid.train.backend.domain.user.dto.response.LoginResponseDto;
import com.aid.train.backend.domain.user.service.AuthService;
import com.aid.train.backend.domain.verification.dto.request.EmailResendRequestDto;
import com.aid.train.backend.domain.verification.dto.request.EmailVerificationRequestDto;
import com.aid.train.backend.domain.verification.dto.response.EmailVerificationResponseDto;
import com.aid.train.backend.domain.verification.service.VerificationService;
import com.aid.train.backend.global.response.ApiResponse;
import com.aid.train.backend.global.util.CookieUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Verification", description = "이메일 인증 및 소셜 회원가입 완료 API")
@RestController
@RequestMapping("/api/v1/verification")
@RequiredArgsConstructor
public class VerificationController {

    private final VerificationService verificationService;
    private final AuthService authService;
    private final CookieUtil cookieUtil;

    @Operation(summary = "이메일 인증 확인", description = "회원가입 후 이메일로 받은 6자리 코드와 인증 토큰을 검증합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "인증 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "인증 코드 불일치"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "410", description = "인증 세션 만료")
    })
    @PostMapping("/email")
    public ResponseEntity<ApiResponse<EmailVerificationResponseDto>> verifyEmail(@Valid @RequestBody EmailVerificationRequestDto requestDto) {
        EmailVerificationResponseDto response = verificationService.verifyEmail(requestDto);
        return ResponseEntity.ok(ApiResponse.success("이메일 인증이 완료되었습니다.", response));
    }

    @Operation(summary = "이메일 인증 코드 재발송", description = "만료된 이메일 인증 코드를 재발송합니다.")
    @PostMapping("/email/resend")
    public ResponseEntity<ApiResponse<String>> resendVerificationEmail(@Valid @RequestBody EmailResendRequestDto requestDto) {
        verificationService.resendVerificationEmail(requestDto);
        return ResponseEntity.ok(ApiResponse.success("인증 이메일이 재발송되었습니다.", null));
    }

    // 참고: /api/v1/verification/social/callback 엔드포인트는 SecurityConfig와 OAuth2SuccessHandler가 처리하므로
    // 컨트롤러에 별도로 정의할 필요가 없습니다.

    @Operation(summary = "소셜 회원가입 완료", description = "소셜 로그인 후 신규 사용자가 추가 정보를 입력하여 회원가입을 완료합니다.")
    @PostMapping("/social/complete")
    public ResponseEntity<ApiResponse<LoginResponseDto>> completeSocialSignup(
            @Valid @RequestBody SocialSignupCompleteRequestDto requestDto,
            HttpServletResponse response) {

        LoginResponseDto loginResponse = authService.completeSocialSignup(requestDto);
        cookieUtil.addRefreshTokenCookie(response, loginResponse.getRefreshToken());

        return ResponseEntity.ok(ApiResponse.success("소셜 회원가입 및 로그인이 완료되었습니다.", loginResponse));
    }
}