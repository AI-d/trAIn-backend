package com.aid.train.backend.domain.feedback.controller;

import com.aid.train.backend.domain.feedback.dto.request.FeedbackChoiceRequest;
import com.aid.train.backend.domain.feedback.dto.response.FeedbackHistoryResponse;
import com.aid.train.backend.domain.feedback.dto.response.FeedbackResponse;
import com.aid.train.backend.domain.feedback.dto.response.FeedbackStatsResponse;
import com.aid.train.backend.domain.feedback.service.FeedbackService;
import com.aid.train.backend.global.exception.TrainException;
import com.aid.train.backend.global.exception.enums.ErrorCode;
import com.aid.train.backend.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 피드백 관리 컨트롤러
 * <p>
 * 역할:
 * - 피드백 생성 (AI가 분석 완료 후 호출)
 * - 개선안 선택 (사용자가 A, B, C 또는 직접 수정)
 * - 피드백 조회 및 히스토리 관리
 * - 통계 데이터 제공
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/feedbacks")
@RequiredArgsConstructor
@Tag(name = "Feedback", description = "AI 분석 기반 대화 피드백 및 개선안 관리 API")
public class FeedbackController {

    private final FeedbackService feedbackService;

    /**
     * AI를 통해 자동으로 피드백을 생성합니다.
     * <p>
     * 대화 세션이 완료된 후 호출하는 API입니다.
     * AI가 전체 대화를 분석하여 자동으로 점수와 개선안을 생성합니다.
     *
     * @param sessionId 세션 ID
     * @return 생성된 피드백 정보
     */
    @Operation(
            summary = "AI 자동 피드백 생성",
            description = "ChatGPT 4.0이 대화를 분석하여 자동으로 피드백을 생성합니다. " +
                    "발화속도, 추임새, 공손도, 명료성을 점수화하고 3가지 스타일의 개선안을 제공합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "AI 피드백 생성 성공",
                    content = @Content(schema = @Schema(implementation = FeedbackResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (세션 미완료, 이미 피드백 존재 등)",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "세션을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "AI API 호출 실패 또는 서버 오류",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    @PostMapping("/sessions/{sessionId}")
    public ResponseEntity<?> feedbackWithAI(
            @Parameter(description = "피드백을 생성할 세션 ID", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable String sessionId
    ) {
        log.info("AI 자동 피드백 생성 요청 - sessionId: {}", sessionId);

        try {
            FeedbackResponse response = feedbackService.generateFeedbackWithAI(sessionId);

            log.info("AI 자동 피드백 생성 완료 - feedbackId: {}, sessionId: {}, totalScore: {}",
                    response.id(), sessionId, response.totalScore());

            return ResponseEntity.ok(
                    ApiResponse.success("AI 피드백이 생성되었습니다.", response)
            );

        } catch (TrainException e) {
            log.error("AI 피드백 생성 실패 (TrainException) - sessionId: {}, errorCode: {}",
                    sessionId, e.getErrorCode(), e);

            return ResponseEntity.badRequest().body(
                    ApiResponse.error(e.getErrorCode())
            );

        } catch (Exception e) {
            log.error("AI 피드백 생성 실패 (Exception) - sessionId: {}", sessionId, e);

            return ResponseEntity.internalServerError().body(
                    ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR, "AI 피드백 생성 중 오류가 발생했습니다.")
            );
        }
    }

    /**
     * 특정 세션의 피드백을 조회합니다.
     *
     * @param sessionId 세션 ID
     * @return 피드백 상세 정보
     */
    @Operation(
            summary = "피드백 조회",
            description = "특정 세션의 피드백 상세 정보를 조회합니다. " +
                    "점수, 개선안, 사용자 선택 결과를 모두 포함합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "피드백 조회 성공",
                    content = @Content(schema = @Schema(implementation = FeedbackResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "피드백을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    @GetMapping("/{sessionId}")
    public ResponseEntity<?> getFeedback(
            @Parameter(description = "조회할 세션 ID", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable String sessionId
    ) {
        log.info("피드백 조회 요청 - sessionId: {}", sessionId);

        try {
            FeedbackResponse response = feedbackService.getFeedback(sessionId);

            return ResponseEntity.ok(
                    ApiResponse.success("피드백 조회 성공", response)
            );

        } catch (TrainException e) {
            log.error("피드백 조회 실패 (TrainException) - sessionId: {}, errorCode: {}",
                    sessionId, e.getErrorCode(), e);

            return ResponseEntity.badRequest().body(
                    ApiResponse.error(e.getErrorCode())
            );

        } catch (Exception e) {
            log.error("피드백 조회 실패 (Exception) - sessionId: {}", sessionId, e);

            return ResponseEntity.internalServerError().body(
                    ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR, "피드백 조회 중 오류가 발생했습니다.")
            );
        }
    }

    /**
     * 사용자가 개선안을 선택합니다.
     * <p>
     * A, B, C 중 하나를 선택하거나 직접 수정할 수 있습니다.
     *
     * @param sessionId 세션 ID
     * @param request   선택 요청
     * @return 업데이트된 피드백 정보
     */
    @Operation(
            summary = "개선안 선택",
            description = "사용자가 AI가 제시한 개선안 중 하나를 선택하거나 직접 수정합니다. " +
                    "A(간결), B(공손), C(따뜻), CUSTOM(사용자 작성) 중 선택 가능합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "개선안 선택 성공",
                    content = @Content(schema = @Schema(implementation = FeedbackResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (CUSTOM 선택 시 내용 누락 등)",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "피드백을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    @PutMapping("/{sessionId}/choice")
    public ResponseEntity<?> chooseAlternative(
            @Parameter(description = "선택할 피드백의 세션 ID", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable String sessionId,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "선택한 개선안 정보",
                    required = true,
                    content = @Content(schema = @Schema(implementation = FeedbackChoiceRequest.class))
            )
            @Valid @RequestBody FeedbackChoiceRequest request
    ) {
        log.info("개선안 선택 요청 - sessionId: {}, choice: {}",
                sessionId, request.chosenAlternative());

        try {
            FeedbackResponse response = feedbackService.chooseAlternative(sessionId, request);

            log.info("개선안 선택 완료 - sessionId: {}, choice: {}",
                    sessionId, request.chosenAlternative());

            return ResponseEntity.ok(
                    ApiResponse.success("개선안이 선택되었습니다.", response)
            );

        } catch (TrainException e) {
            log.error("개선안 선택 실패 (TrainException) - sessionId: {}, errorCode: {}",
                    sessionId, e.getErrorCode(), e);

            return ResponseEntity.badRequest().body(
                    ApiResponse.error(e.getErrorCode())
            );

        } catch (Exception e) {
            log.error("개선안 선택 실패 (Exception) - sessionId: {}", sessionId, e);

            return ResponseEntity.internalServerError().body(
                    ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR, "개선안 선택 중 오류가 발생했습니다.")
            );
        }
    }

    /**
     * 특정 사용자의 피드백 히스토리를 조회합니다 (페이징).
     *
     * @param userId   사용자 ID
     * @param pageable 페이징 정보
     * @return 피드백 히스토리 목록
     */
    @Operation(
            summary = "피드백 히스토리 조회",
            description = "특정 사용자의 피드백 히스토리를 페이징하여 조회합니다. " +
                    "학습 기록과 성장 추이를 확인할 수 있습니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "히스토리 조회 성공",
                    content = @Content(schema = @Schema(implementation = Page.class))
            )
    })
    @GetMapping("/users/{userId}/history")
    public ResponseEntity<?> getFeedbackHistory(
            @Parameter(description = "조회할 사용자 ID", required = true, example = "1")
            @PathVariable Long userId,

            @Parameter(description = "페이징 정보 (기본: 페이지=0, 크기=20, 정렬=생성일시 내림차순)")
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        log.info("피드백 히스토리 조회 요청 - userId: {}, page: {}, size: {}",
                userId, pageable.getPageNumber(), pageable.getPageSize());

        try {
            Page<FeedbackHistoryResponse> response = feedbackService.getFeedbackHistory(userId, pageable);

            log.info("피드백 히스토리 조회 완료 - userId: {}, totalElements: {}",
                    userId, response.getTotalElements());

            return ResponseEntity.ok(
                    ApiResponse.success("피드백 히스토리 조회 성공", response)
            );

        } catch (Exception e) {
            log.error("피드백 히스토리 조회 실패 - userId: {}", userId, e);

            return ResponseEntity.internalServerError().body(
                    ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR, "히스토리 조회 중 오류가 발생했습니다.")
            );
        }
    }

    /**
     * 특정 사용자의 전체 피드백 히스토리를 조회합니다 (페이징 없음).
     *
     * @param userId 사용자 ID
     * @return 전체 피드백 히스토리 목록
     */
    @Operation(
            summary = "전체 피드백 히스토리 조회",
            description = "특정 사용자의 모든 피드백 히스토리를 한 번에 조회합니다. " +
                    "전체 학습 기록을 분석하거나 데이터 내보내기에 사용됩니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "전체 히스토리 조회 성공",
                    content = @Content(schema = @Schema(implementation = List.class))
            )
    })
    @GetMapping("/users/{userId}/history/all")
    public ResponseEntity<?> getAllFeedbackHistory(
            @Parameter(description = "조회할 사용자 ID", required = true, example = "1")
            @PathVariable Long userId
    ) {
        log.info("전체 피드백 히스토리 조회 요청 - userId: {}", userId);

        try {
            List<FeedbackHistoryResponse> response = feedbackService.getAllFeedbackHistory(userId);

            log.info("전체 피드백 히스토리 조회 완료 - userId: {}, count: {}",
                    userId, response.size());

            return ResponseEntity.ok(
                    ApiResponse.success("전체 피드백 히스토리 조회 성공", response)
            );

        } catch (Exception e) {
            log.error("전체 피드백 히스토리 조회 실패 - userId: {}", userId, e);

            return ResponseEntity.internalServerError().body(
                    ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR, "히스토리 조회 중 오류가 발생했습니다.")
            );
        }
    }

    /**
     * 특정 사용자의 피드백 통계를 조회합니다.
     * <p>
     * 성장 그래프 및 대시보드에 사용됩니다.
     *
     * @param userId 사용자 ID
     * @return 피드백 통계 정보
     */
    @Operation(
            summary = "피드백 통계 조회",
            description = "특정 사용자의 피드백 통계를 조회합니다. " +
                    "평균 점수, 성장 추이, 등급별 분포, 최근 학습 현황 등을 포함합니다. " +
                    "성장 그래프와 대시보드에 사용됩니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "통계 조회 성공",
                    content = @Content(schema = @Schema(implementation = FeedbackStatsResponse.class))
            )
    })
    @GetMapping("/users/{userId}/stats")
    public ResponseEntity<?> getFeedbackStats(
            @Parameter(description = "통계를 조회할 사용자 ID", required = true, example = "1")
            @PathVariable Long userId
    ) {
        log.info("피드백 통계 조회 요청 - userId: {}", userId);

        try {
            FeedbackStatsResponse response = feedbackService.getFeedbackStats(userId);

            log.info("피드백 통계 조회 완료 - userId: {}, totalCount: {}, averageScore: {}",
                    userId, response.totalCount(), response.averageScore());

            return ResponseEntity.ok(
                    ApiResponse.success("피드백 통계 조회 성공", response)
            );

        } catch (Exception e) {
            log.error("피드백 통계 조회 실패 - userId: {}", userId, e);

            return ResponseEntity.internalServerError().body(
                    ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR, "통계 조회 중 오류가 발생했습니다.")
            );
        }
    }
}