package com.aid.train.backend.domain.feedback.dto.response;

import com.aid.train.backend.domain.feedback.entity.Feedback;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 피드백 히스토리 응답 DTO
 * <p>
 * 히스토리 목록 조회 시 사용하는 간소화된 피드백 정보입니다.
 * 전체 개선안 내용은 제외하고 핵심 정보만 포함하여 목록 조회 성능을 최적화합니다.
 * </p>
 * <p>
 * 사용 시나리오:
 * - 사용자의 전체 학습 히스토리 조회
 * - 대시보드에서 최근 학습 현황 표시
 * - 성장 기록 추적 및 분석
 * </p>
 *
 * @author 왕택준
 * @since 1.0.0
 * @see com.aid.train.backend.domain.feedback.controller.FeedbackController#getFeedbackHistory(Long, org.springframework.data.domain.Pageable)
 * @see com.aid.train.backend.domain.feedback.controller.FeedbackController#getAllFeedbackHistory(Long)
 */
@Schema(description = "피드백 히스토리 정보")
@Builder
public record FeedbackHistoryResponse(
        /**
         * 피드백 고유 ID
         * <p>
         * 상세 조회 시 사용할 수 있는 피드백의 고유 식별자입니다.
         * </p>
         */
        @Schema(description = "피드백 ID", example = "1")
        Long id,

        /**
         * 연관된 세션 ID
         * <p>
         * 상세 피드백 조회나 세션 정보 확인에 사용할 수 있습니다.
         * </p>
         */
        @Schema(description = "세션 ID", example = "550e8400-e29b-41d4-a716-446655440000")
        String sessionId,

        /**
         * 시나리오 ID
         * <p>
         * 어떤 시나리오로 학습했는지 확인할 수 있는 식별자입니다.
         * </p>
         */
        @Schema(description = "시나리오 ID", example = "1")
        Long scenarioId,

        /**
         * 시나리오 제목
         * <p>
         * 학습한 상황을 한눈에 파악할 수 있는 제목입니다.
         * </p>
         */
        @Schema(description = "시나리오 제목", example = "상사에게 휴가 요청하기")
        String scenarioTitle,

        /**
         * 시나리오 카테고리
         * <p>
         * 학습 분야를 분류하는 카테고리입니다.
         * WORK, RELATIONSHIP, FAMILY, FRIEND 중 하나입니다.
         * </p>
         */
        @Schema(description = "시나리오 카테고리", example = "WORK", allowableValues = {"WORK", "RELATIONSHIP", "FAMILY", "FRIEND"})
        String scenarioCategory,

        /**
         * 전체 점수 (0-100)
         * <p>
         * 해당 학습에서 받은 종합 점수입니다.
         * 성장 추이를 확인하는 데 사용됩니다.
         * </p>
         */
        @Schema(description = "전체 점수 (0-100)", example = "72", minimum = "0", maximum = "100")
        Integer totalScore,

        /**
         * 점수 등급 (A-F)
         * <p>
         * 점수를 등급으로 표현한 것입니다.
         * 시각적으로 성과를 확인하기 쉽습니다.
         * </p>
         */
        @Schema(description = "점수 등급", example = "C", allowableValues = {"A", "B", "C", "D", "F"})
        String scoreGrade,

        /**
         * 원본 발화 미리보기 (100자 제한)
         * <p>
         * 사용자가 실제로 말한 내용의 일부입니다.
         * 100자를 초과하는 경우 "..."으로 생략됩니다.
         * </p>
         */
        @Schema(description = "원본 발화 미리보기 (100자 제한)", example = "팀장님, 내일... 음... 휴가 가능하신가요?")
        String originalTranscriptPreview,

        /**
         * 사용자가 선택한 개선안 타입
         * <p>
         * 사용자가 최종적으로 선택한 개선안의 유형입니다.
         * 선택하지 않은 경우 null입니다.
         * </p>
         */
        @Schema(description = "선택된 개선안 타입", example = "A", allowableValues = {"A", "B", "C", "CUSTOM"})
        Feedback.ChosenAlternative chosenAlternative,

        /**
         * 학습 완료 여부
         * <p>
         * 사용자가 개선안 선택까지 완료했는지 여부입니다.
         * false인 경우 아직 미완료된 학습입니다.
         * </p>
         */
        @Schema(description = "학습 완료 여부", example = "true")
        Boolean isChoiceComplete,

        /**
         * 대화 진행 시간 (초)
         * <p>
         * 해당 세션에서 실제 대화가 진행된 시간입니다.
         * 학습 참여도를 측정하는 지표로 활용됩니다.
         * </p>
         */
        @Schema(description = "대화 진행 시간 (초)", example = "180", minimum = "0")
        Integer sessionDurationSeconds,

        /**
         * 피드백 생성 시간
         * <p>
         * 해당 학습이 완료된 시점입니다.
         * 학습 기록을 시간순으로 정렬하는 데 사용됩니다.
         * </p>
         */
        @Schema(description = "피드백 생성 시간", example = "2025-10-24T14:30:00")
        LocalDateTime createdAt
) {
    /**
     * Feedback 엔티티로부터 FeedbackHistoryResponse를 생성합니다.
     * <p>
     * 원본 발화가 100자를 초과하는 경우 자동으로 생략됩니다.
     * </p>
     *
     * @param feedback 피드백 엔티티
     * @return 히스토리용 간소화된 응답 객체
     */
    public static FeedbackHistoryResponse from(Feedback feedback) {
        String originalPreview = feedback.getOriginalTranscript();
        if (originalPreview != null && originalPreview.length() > 100) {
            originalPreview = originalPreview.substring(0, 100) + "...";
        }

        return FeedbackHistoryResponse.builder()
                .id(feedback.getId())
                .sessionId(feedback.getDialogueSession().getSessionId())
                .scenarioId(feedback.getDialogueSession().getScenario().getId())
                .scenarioTitle(feedback.getDialogueSession().getScenario().getTitle())
                .scenarioCategory(feedback.getDialogueSession().getScenario().getCategory().name())
                .totalScore(feedback.getTotalScore())
                .scoreGrade(feedback.getScoreGrade())
                .originalTranscriptPreview(originalPreview)
                .chosenAlternative(feedback.getChosenAlternative())
                .isChoiceComplete(feedback.isChoiceComplete())
                .sessionDurationSeconds(feedback.getDialogueSession().getAudioDurationSeconds())
                .createdAt(feedback.getCreatedAt())
                .build();
    }
}