package com.aid.train.backend.domain.terms.controller;

import com.aid.train.backend.domain.terms.dto.request.ConsentUpdateRequestDto;
import com.aid.train.backend.domain.terms.dto.response.TermsResponseDto;
import com.aid.train.backend.domain.terms.dto.response.UserConsentResponseDto;
import com.aid.train.backend.domain.terms.service.TermsService;
import com.aid.train.backend.global.response.ApiResponse;
import com.aid.train.backend.global.security.dto.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Terms", description = "약관 조회 및 동의 관리 API")
@RestController
@RequestMapping("/api/v1/terms")
@RequiredArgsConstructor
public class TermsController {

    private final TermsService termsService;

    @Operation(summary = "활성 약관 목록 조회", description = "회원가입 등에 필요한 현재 활성화된 약관 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<TermsResponseDto>>> getActiveTerms() {
        List<TermsResponseDto> activeTerms = termsService.getActiveTerms();
        return ResponseEntity.ok(ApiResponse.success("활성 약관 목록 조회에 성공했습니다.", activeTerms));
    }

    @Operation(summary = "내 약관 동의 내역 조회", description = "현재 로그인한 사용자의 약관 동의 내역을 조회합니다.")
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/consent")
    public ResponseEntity<ApiResponse<List<UserConsentResponseDto>>> getMyConsents(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<UserConsentResponseDto> userConsents = termsService.getUserConsents(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success("약관 동의 내역 조회에 성공했습니다.", userConsents));
    }

    @Operation(summary = "약관 동의 변경", description = "마케팅 수신 동의 등 선택 약관에 대한 동의 상태를 변경합니다.")
    @SecurityRequirement(name = "Bearer Authentication")
    @PutMapping("/consent")
    public ResponseEntity<ApiResponse<String>> updateMyConsents(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ConsentUpdateRequestDto requestDto) {
        termsService.updateConsents(userDetails.getUserId(), requestDto);
        return ResponseEntity.ok(ApiResponse.success("약관 동의 상태가 변경되었습니다.", null));
    }
}