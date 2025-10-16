package com.aid.train.backend.domain.user.controller;

import com.aid.train.backend.domain.user.dto.request.LoginRequestDto;
import com.aid.train.backend.domain.user.dto.request.RefreshTokenRequestDto;
import com.aid.train.backend.domain.user.dto.request.SignupRequestDto;
import com.aid.train.backend.domain.user.dto.response.LoginResponseDto;
import com.aid.train.backend.domain.user.dto.response.SignupResponseDto;
import com.aid.train.backend.domain.user.dto.response.TokenResponseDto;
import com.aid.train.backend.domain.user.service.AuthService;
import com.aid.train.backend.global.common.response.ApiResponse;
import com.aid.train.backend.global.security.annotation.CurrentUserId;
import com.aid.train.backend.global.util.LogMaskingUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 인증 관련 API 컨트롤러입니다.
 * 회원가입, 로그인, 토큰 갱신, 로그아웃 등의 엔드포인트를 제공합니다.
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Tag(name = "01. 인증", description = "회원가입, 로그인, 토큰 관리 API")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Validated
@Slf4j
public class AuthController {

    private final AuthService authService;

    /**
     * 회원가입을 처리합니다.
     *
     * @param request 회원가입 요청 DTO
     * @return 회원가입 응답 DTO
     */
    @Operation(summary = "회원가입", description = "이메일 기반 회원가입을 처리합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "회원가입 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (검증 실패)",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "이미 존재하는 이메일",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<SignupResponseDto> signup(@Valid @RequestBody SignupRequestDto request) {
        log.info("[API] POST /api/v1/auth/signup - 이메일: {}", LogMaskingUtil.maskEmail(request.getEmail()));
        SignupResponseDto response = authService.signup(request);
        return ApiResponse.created(response, "회원가입이 완료되었습니다.");
    }

    /**
     * 로그인을 처리합니다.
     *
     * @param request 로그인 요청 DTO
     * @return 로그인 응답 DTO
     */
    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "로그인 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (검증 실패)",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 (비밀번호 불일치)",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "사용자를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    @PostMapping("/login")
    public ApiResponse<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto request) {
        log.info("[API] POST /api/v1/auth/login - 이메일: {}", LogMaskingUtil.maskEmail(request.getEmail()));
        LoginResponseDto response = authService.login(request);
        return ApiResponse.ok(response, "로그인에 성공했습니다.");
    }

    /**
     * 액세스 토큰을 갱신합니다.
     *
     * @param request 토큰 갱신 요청 DTO
     * @return 새로운 토큰 응답 DTO
     */
    @Operation(summary = "토큰 갱신", description = "리프레시 토큰으로 액세스 토큰을 갱신합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "토큰 갱신 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "유효하지 않은 리프레시 토큰",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "리프레시 토큰을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    @PostMapping("/refresh")
    public ApiResponse<TokenResponseDto> refreshToken(@Valid @RequestBody RefreshTokenRequestDto request) {
        log.info("[API] POST /api/v1/auth/refresh");
        TokenResponseDto response = authService.refreshToken(request);
        return ApiResponse.ok(response, "토큰 갱신에 성공했습니다.");
    }

    /**
     * 로그아웃을 처리합니다.
     *
     * @param userId 현재 로그인한 사용자 ID
     * @return 성공 응답
     */
    @Operation(summary = "로그아웃", description = "로그아웃하고 리프레시 토큰을 폐기합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "로그아웃 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            @Parameter(hidden = true) @CurrentUserId Long userId
    ) {
        log.info("[API] POST /api/v1/auth/logout - 사용자 ID: {}", LogMaskingUtil.maskUserId(userId));
        authService.logout(userId);
        return ApiResponse.ok("로그아웃에 성공했습니다.");
    }
}
