package com.aid.train.backend.domain.terms.controller;

import com.aid.train.backend.domain.terms.dto.request.UserConsentRequestDto;
import com.aid.train.backend.domain.terms.dto.response.UserConsentResponseDto;
import com.aid.train.backend.domain.terms.service.UserConsentService;
import com.aid.train.backend.global.common.response.ApiResponse;
import com.aid.train.backend.global.security.annotation.CurrentUserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 사용자 약관 동의 API 컨트롤러입니다.
 * 약관 동의 및 철회 엔드포인트를 제공합니다.
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Tag(name = "07. 약관 동의", description = "사용자 약관 동의 관리 API")
@RestController
@RequestMapping("/api/v1/consents")
@RequiredArgsConstructor
@Slf4j
public class UserConsentController {

    private final UserConsentService userConsentService;

    /**
     * 현재 사용자의 모든 약관 동의 내역을 조회합니다.
     *
     * @param userId 현재 로그인한 사용자 ID
     * @return 약관 동의 내역 목록
     */
    @Operation(summary = "내 약관 동의 내역 조회", description = "현재 로그인한 사용자의 모든 약관 동의 내역을 조회합니다.")
    @GetMapping("/me")
    public ApiResponse<List<UserConsentResponseDto>> getMyConsents(
            @Parameter(hidden = true) @CurrentUserId Long userId
    ) {
        log.info("[API] GET /api/v1/consents/me - 사용자 ID: {}", userId);
        List<UserConsentResponseDto> response = userConsentService.getUserConsents(userId);
        return ApiResponse.ok(response);
    }

    /**
     * 약관에 동의하거나 철회합니다.
     *
     * @param userId  현재 로그인한 사용자 ID
     * @param request 약관 동의 요청 DTO
     * @return 약관 동의 응답 DTO
     */
    @Operation(
            summary = "약관 동의/철회",
            description = "특정 약관에 동의하거나 철회합니다. 필수 약관은 철회할 수 없습니다."
    )
    @PostMapping
    public ApiResponse<UserConsentResponseDto> updateConsent(
            @Parameter(hidden = true) @CurrentUserId Long userId,
            @Valid @RequestBody UserConsentRequestDto request
    ) {
        log.info("[API] POST /api/v1/consents - 사용자 ID: {}, 약관 타입: {}, 동의 여부: {}",
                userId, request.getTermsType(), request.getIsAgreed());
        UserConsentResponseDto response = userConsentService.updateConsent(userId, request);
        
        String message = request.getIsAgreed() ? "약관에 동의했습니다." : "약관 동의를 철회했습니다.";
        return ApiResponse.ok(response, message);
    }
}
