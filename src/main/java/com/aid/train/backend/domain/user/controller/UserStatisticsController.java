package com.aid.train.backend.domain.user.controller;

import com.aid.train.backend.domain.user.dto.response.UserStatisticsResponseDto;
import com.aid.train.backend.domain.user.service.UserStatisticsService;
import com.aid.train.backend.global.common.response.ApiResponse;
import com.aid.train.backend.global.security.annotation.CurrentUserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 사용자 통계 API 컨트롤러입니다.
 * 면접 연습 통계 정보 조회 엔드포인트를 제공합니다.
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Tag(name = "05. 사용자 통계", description = "면접 연습 통계 조회 API")
@RestController
@RequestMapping("/api/v1/users/statistics")
@RequiredArgsConstructor
@Slf4j
public class UserStatisticsController {

    private final UserStatisticsService userStatisticsService;

    /**
     * 사용자의 면접 연습 통계를 조회합니다.
     *
     * @param userId 현재 로그인한 사용자 ID
     * @return 사용자 통계 응답 DTO
     */
    @Operation(
            summary = "내 통계 조회",
            description = "현재 로그인한 사용자의 면접 연습 통계(총 대화 수, 평균 점수, 우수 평가 수 등)를 조회합니다."
    )
    @GetMapping("/me")
    public ApiResponse<UserStatisticsResponseDto> getMyStatistics(
            @Parameter(hidden = true) @CurrentUserId Long userId
    ) {
        log.info("[API] GET /api/v1/users/statistics/me - 사용자 ID: {}", userId);
        UserStatisticsResponseDto response = userStatisticsService.getUserStatistics(userId);
        return ApiResponse.ok(response);
    }
}
