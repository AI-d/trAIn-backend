package com.aid.train.backend.domain.verification.controller;

import com.aid.train.backend.domain.verification.dto.request.EmailVerificationRequestDto;
import com.aid.train.backend.domain.verification.dto.request.VerifyCodeRequestDto;
import com.aid.train.backend.domain.verification.dto.response.EmailVerificationResponseDto;
import com.aid.train.backend.domain.verification.service.EmailVerificationService;
import com.aid.train.backend.global.response.ApiResponse;
import com.aid.train.backend.global.util.LogMaskingUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 이메일 인증 API 컨트롤러입니다.
 * 이메일 인증 코드 발송 및 검증 엔드포인트를 제공합니다.
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Tag(name = "02. 이메일 인증", description = "이메일 인증 코드 발송 및 검증 API")
@RestController
@RequestMapping("/api/v1/auth/email")
@RequiredArgsConstructor
@Validated
@Slf4j
public class EmailVerificationController {

    private final EmailVerificationService emailVerificationService;

    /**
     * 이메일 인증 코드를 발송합니다.
     *
     * @param request 이메일 인증 요청 DTO
     * @return 이메일 인증 응답 DTO
     */
    @Operation(summary = "인증 코드 발송", description = "이메일로 6자리 인증 코드를 발송합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "인증 코드 발송 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (검증 실패)",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "이미 가입된 이메일",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    @PostMapping("/send")
    public ApiResponse<EmailVerificationResponseDto> sendVerificationCode(
            @Valid @RequestBody EmailVerificationRequestDto request
    ) {
        log.info("[API] POST /api/v1/auth/email/send - 이메일: {}", LogMaskingUtil.maskEmail(request.getEmail()));
        EmailVerificationResponseDto response = emailVerificationService.sendVerificationCode(request);
        return ApiResponse.ok(response, "인증 코드가 발송되었습니다.");
    }

    /**
     * 이메일 인증 코드를 검증합니다.
     *
     * @param request 인증 코드 검증 요청 DTO
     * @return 이메일 인증 응답 DTO
     */
    @Operation(summary = "인증 코드 검증", description = "발송된 인증 코드를 검증합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "인증 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 또는 유효하지 않은 인증 코드",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "410",
                    description = "만료된 인증 코드",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    @PostMapping("/verify")
    public ApiResponse<EmailVerificationResponseDto> verifyCode(
            @Valid @RequestBody VerifyCodeRequestDto request
    ) {
        log.info("[API] POST /api/v1/auth/email/verify - 이메일: {}", LogMaskingUtil.maskEmail(request.getEmail()));
        EmailVerificationResponseDto response = emailVerificationService.verifyCode(request);
        return ApiResponse.ok(response, "이메일 인증에 성공했습니다.");
    }
}
