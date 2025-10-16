package com.aid.train.backend.domain.terms.controller;

import com.aid.train.backend.domain.terms.dto.response.TermsResponseDto;
import com.aid.train.backend.domain.terms.enums.TermsType;
import com.aid.train.backend.domain.terms.service.TermsService;
import com.aid.train.backend.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 약관 관리 API 컨트롤러입니다.
 * 약관 조회 엔드포인트를 제공합니다.
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Tag(name = "06. 약관", description = "서비스 약관 조회 API")
@RestController
@RequestMapping("/api/v1/terms")
@RequiredArgsConstructor
@Slf4j
public class TermsController {

    private final TermsService termsService;

    /**
     * 모든 활성화된 약관을 조회합니다.
     *
     * @return 약관 목록
     */
    @Operation(
            summary = "전체 약관 조회",
            description = "현재 활성화된 모든 약관을 조회합니다. (서비스 이용약관, 개인정보 처리방침, 마케팅 수신 동의)"
    )
    @GetMapping
    public ApiResponse<List<TermsResponseDto>> getAllTerms() {
        log.info("[API] GET /api/v1/terms");
        List<TermsResponseDto> response = termsService.getAllActiveTerms();
        return ApiResponse.ok(response);
    }

    /**
     * 특정 약관을 ID로 조회합니다.
     *
     * @param termsId 약관 ID
     * @return 약관 상세 정보
     */
    @Operation(summary = "약관 상세 조회", description = "약관 ID로 특정 약관의 상세 정보를 조회합니다.")
    @GetMapping("/{termsId}")
    public ApiResponse<TermsResponseDto> getTermsById(
            @Parameter(description = "약관 ID", example = "1") @PathVariable Long termsId
    ) {
        log.info("[API] GET /api/v1/terms/{} - 약관 ID: {}", termsId, termsId);
        TermsResponseDto response = termsService.getTermsById(termsId);
        return ApiResponse.ok(response);
    }

    /**
     * 특정 타입의 최신 약관을 조회합니다.
     *
     * @param type 약관 타입
     * @return 최신 약관 정보
     */
    @Operation(
            summary = "타입별 최신 약관 조회",
            description = "약관 타입(TERMS_OF_SERVICE, PRIVACY_POLICY, MARKETING_CONSENT)으로 최신 약관을 조회합니다."
    )
    @GetMapping("/type/{type}")
    public ApiResponse<TermsResponseDto> getTermsByType(
            @Parameter(
                    description = "약관 타입",
                    example = "TERMS_OF_SERVICE"
            ) @PathVariable TermsType type
    ) {
        log.info("[API] GET /api/v1/terms/type/{} - 타입: {}", type, type);
        TermsResponseDto response = termsService.getLatestTermsByType(type);
        return ApiResponse.ok(response);
    }

    /**
     * 필수 약관 목록을 조회합니다.
     *
     * @return 필수 약관 목록
     */
    @Operation(
            summary = "필수 약관 조회",
            description = "회원가입 시 필수로 동의해야 하는 약관 목록을 조회합니다."
    )
    @GetMapping("/required")
    public ApiResponse<List<TermsResponseDto>> getRequiredTerms() {
        log.info("[API] GET /api/v1/terms/required");
        List<TermsResponseDto> response = termsService.getRequiredTerms();
        return ApiResponse.ok(response);
    }

    /**
     * 선택 약관 목록을 조회합니다.
     *
     * @return 선택 약관 목록
     */
    @Operation(
            summary = "선택 약관 조회",
            description = "회원가입 시 선택적으로 동의할 수 있는 약관 목록을 조회합니다."
    )
    @GetMapping("/optional")
    public ApiResponse<List<TermsResponseDto>> getOptionalTerms() {
        log.info("[API] GET /api/v1/terms/optional");
        List<TermsResponseDto> response = termsService.getOptionalTerms();
        return ApiResponse.ok(response);
    }
}
