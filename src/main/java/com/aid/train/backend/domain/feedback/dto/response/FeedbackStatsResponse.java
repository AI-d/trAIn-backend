package com.aid.train.backend.domain.feedback.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

/**
 * 피드백 통계 응답 DTO
 * <p>
 * 사용자의 피드백 통계 정보를 제공합니다.
 * 성장 그래프, 대시보드, 학습 분석에 사용되는 종합적인 통계 데이터를 포함합니다.
 * </p>
 * <p>
 * 포함되는 주요 정보:
 * - 전체 학습 통계 (총 개수, 평균 점수, 최고/최저 점수)
 * - 항목별 평균 점수 (발화속도, 추임새, 공손도, 명료성)
 * - 기간별 학습 현황 (최근 7일, 30일)
 * - 완료율 통계 (완료/미완료 개수)
 * - 등급별 분포 (A, B, C, D, F)
 * - 월별 성장 추이 (최근 6개월)
 * </p>
 *
 * @author 왕택준
 * @since 1.0.0
 * @see com.aid.train.backend.domain.feedback.controller.FeedbackController#getFeedbackStats(Long)
 * @see com.aid.train.backend.domain.feedback.service.FeedbackService#getFeedbackStats(Long)
 */
@Schema(description = "피드백 통계 정보")
@Builder
public record FeedbackStatsResponse(
        /**
         * 총 피드백 개수
         * <p>
         * 해당 사용자가 받은 모든 피드백의 총 개수입니다.
         * 완료/미완료 구분 없이 생성된 모든 피드백을 포함합니다.
         * </p>
         */
        @Schema(description = "총 피드백 개수", example = "15", minimum = "0")
        Long totalCount,

        /**
         * 전체 평균 점수 (0-100)
         * <p>
         * 모든 피드백의 총점 평균입니다.
         * 발화속도 + 추임새 + 공손도 + 명료성의 평균 합계입니다.
         * </p>
         */
        @Schema(description = "전체 평균 점수 (0-100)", example = "75.2", minimum = "0", maximum = "100")
        Double averageScore,

        /**
         * 발화속도 평균 점수 (0-30)
         * <p>
         * 적절한 말하기 속도 항목의 평균 점수입니다.
         * 너무 빠르거나 느리지 않은 적절한 속도 유지 능력을 나타냅니다.
         * </p>
         */
        @Schema(description = "발화속도 평균 점수 (0-30)", example = "22.5", minimum = "0", maximum = "30")
        Double averageSpeechRateScore,

        /**
         * 추임새 평균 점수 (0-20)
         * <p>
         * "음...", "그..." 등 불필요한 표현 줄이기 항목의 평균 점수입니다.
         * 점수가 높을수록 추임새가 적어 좋은 것입니다.
         * </p>
         */
        @Schema(description = "추임새 평균 점수 (0-20) - 높을수록 좋음", example = "15.3", minimum = "0", maximum = "20")
        Double averageFillerWordsScore,

        /**
         * 공손도 평균 점수 (0-25)
         * <p>
         * 상대방에 대한 예의와 존중 표현 항목의 평균 점수입니다.
         * 상황에 맞는 높임말과 정중한 표현 사용 능력을 나타냅니다.
         * </p>
         */
        @Schema(description = "공손도 평균 점수 (0-25)", example = "20.1", minimum = "0", maximum = "25")
        Double averagePolitenessScore,

        /**
         * 명료성 평균 점수 (0-25)
         * <p>
         * 의사 전달의 명확성과 구체성 항목의 평균 점수입니다.
         * 듣는 사람이 이해하기 쉽게 말하는 능력을 나타냅니다.
         * </p>
         */
        @Schema(description = "명료성 평균 점수 (0-25)", example = "17.3", minimum = "0", maximum = "25")
        Double averageClarityScore,

        /**
         * 최고 점수
         * <p>
         * 지금까지 받은 피드백 중 가장 높은 총점입니다.
         * 사용자의 최고 성과를 나타냅니다.
         * </p>
         */
        @Schema(description = "최고 점수 (0-100)", example = "89", minimum = "0", maximum = "100")
        Integer maxScore,

        /**
         * 최저 점수
         * <p>
         * 지금까지 받은 피드백 중 가장 낮은 총점입니다.
         * 개선이 필요한 기준점을 나타냅니다.
         * </p>
         */
        @Schema(description = "최저 점수 (0-100)", example = "45", minimum = "0", maximum = "100")
        Integer minScore,

        /**
         * 최근 7일 피드백 개수
         * <p>
         * 지난 7일 동안 생성된 피드백의 개수입니다.
         * 최근 학습 활동량을 나타냅니다.
         * </p>
         */
        @Schema(description = "최근 7일 피드백 개수", example = "3", minimum = "0")
        Long recentWeekCount,

        /**
         * 최근 30일 피드백 개수
         * <p>
         * 지난 30일 동안 생성된 피드백의 개수입니다.
         * 한 달간 학습 활동량을 나타냅니다.
         * </p>
         */
        @Schema(description = "최근 30일 피드백 개수", example = "12", minimum = "0")
        Long recentMonthCount,

        /**
         * 완료된 피드백 개수
         * <p>
         * 사용자가 개선안 선택까지 완료한 피드백의 개수입니다.
         * 학습 완료율을 계산하는 데 사용됩니다.
         * </p>
         */
        @Schema(description = "완료된 피드백 개수", example = "12", minimum = "0")
        Long completedCount,

        /**
         * 미완료 피드백 개수
         * <p>
         * 아직 개선안 선택을 완료하지 않은 피드백의 개수입니다.
         * 미완료 학습이 있는지 확인할 수 있습니다.
         * </p>
         */
        @Schema(description = "미완료 피드백 개수", example = "3", minimum = "0")
        Long incompleteCount,

        /**
         * 점수 등급별 분포
         * <p>
         * A, B, C, D, F 등급별로 받은 피드백 개수 분포입니다.
         * 성과 분포를 시각적으로 확인할 수 있습니다.
         * </p>
         */
        @Schema(description = "점수 등급별 분포")
        GradeDistribution gradeDistribution,

        /**
         * 월별 점수 추이 (최근 6개월)
         * <p>
         * 최근 6개월간 월별 평균 점수와 학습 횟수입니다.
         * 성장 그래프를 그리는 데 사용됩니다.
         * </p>
         */
        @Schema(description = "월별 점수 추이 (최근 6개월)")
        List<MonthlyScore> monthlyScores
) {
    /**
     * 점수 등급별 분포 정보
     * <p>
     * 각 등급별로 받은 피드백 개수를 나타냅니다.
     * 등급 기준: A(90+), B(80-89), C(70-79), D(60-69), F(60미만)
     * </p>
     */
    @Schema(description = "점수 등급별 분포")
    @Builder
    public record GradeDistribution(
            /**
             * A등급 개수 (90점 이상)
             * <p>
             * 90점 이상의 우수한 성과를 받은 피드백 개수입니다.
             * </p>
             */
            @Schema(description = "A등급 개수 (90점 이상)", example = "2", minimum = "0")
            Long gradeA,

            /**
             * B등급 개수 (80-89점)
             * <p>
             * 80점 이상 89점 이하의 양호한 성과를 받은 피드백 개수입니다.
             * </p>
             */
            @Schema(description = "B등급 개수 (80-89점)", example = "5", minimum = "0")
            Long gradeB,

            /**
             * C등급 개수 (70-79점)
             * <p>
             * 70점 이상 79점 이하의 보통 성과를 받은 피드백 개수입니다.
             * </p>
             */
            @Schema(description = "C등급 개수 (70-79점)", example = "4", minimum = "0")
            Long gradeC,

            /**
             * D등급 개수 (60-69점)
             * <p>
             * 60점 이상 69점 이하의 미흡한 성과를 받은 피드백 개수입니다.
             * </p>
             */
            @Schema(description = "D등급 개수 (60-69점)", example = "3", minimum = "0")
            Long gradeD,

            /**
             * F등급 개수 (60점 미만)
             * <p>
             * 60점 미만의 부족한 성과를 받은 피드백 개수입니다.
             * </p>
             */
            @Schema(description = "F등급 개수 (60점 미만)", example = "1", minimum = "0")
            Long gradeF
    ) {}

    /**
     * 월별 점수 정보
     * <p>
     * 특정 월의 학습 통계를 나타냅니다.
     * 성장 추이 분석에 사용됩니다.
     * </p>
     */
    @Schema(description = "월별 점수 정보")
    @Builder
    public record MonthlyScore(
            /**
             * 년월 (YYYY-MM 형식)
             * <p>
             * 해당 통계가 속한 년월을 나타냅니다.
             * </p>
             */
            @Schema(description = "년월 (YYYY-MM 형식)", example = "2025-10", pattern = "^\\d{4}-\\d{2}$")
            String yearMonth,

            /**
             * 해당 월 평균 점수
             * <p>
             * 해당 월에 받은 모든 피드백의 평균 점수입니다.
             * 월별 성장 추이를 확인할 수 있습니다.
             * </p>
             */
            @Schema(description = "해당 월 평균 점수 (0-100)", example = "76.5", minimum = "0", maximum = "100")
            Double averageScore,

            /**
             * 해당 월 피드백 개수
             * <p>
             * 해당 월에 생성된 피드백의 총 개수입니다.
             * 월별 학습 활동량을 나타냅니다.
             * </p>
             */
            @Schema(description = "해당 월 피드백 개수", example = "8", minimum = "0")
            Long count
    ) {}

    /**
     * 통계 데이터로부터 응답 객체를 생성합니다.
     * <p>
     * 서비스 레이어에서 계산된 통계 데이터를 기반으로
     * 완전한 통계 응답 객체를 생성합니다.
     * </p>
     *
     * @param totalCount 총 피드백 개수
     * @param averageScore 전체 평균 점수
     * @param averageSpeechRateScore 발화속도 평균 점수
     * @param averageFillerWordsScore 추임새 평균 점수
     * @param averagePolitenessScore 공손도 평균 점수
     * @param averageClarityScore 명료성 평균 점수
     * @param maxScore 최고 점수
     * @param minScore 최저 점수
     * @param recentWeekCount 최근 7일 피드백 개수
     * @param recentMonthCount 최근 30일 피드백 개수
     * @param completedCount 완료된 피드백 개수
     * @param incompleteCount 미완료 피드백 개수
     * @param gradeDistribution 등급별 분포
     * @param monthlyScores 월별 점수 추이
     * @return 완전한 피드백 통계 응답 객체
     */
    public static FeedbackStatsResponse of(
            Long totalCount,
            Double averageScore,
            Double averageSpeechRateScore,
            Double averageFillerWordsScore,
            Double averagePolitenessScore,
            Double averageClarityScore,
            Integer maxScore,
            Integer minScore,
            Long recentWeekCount,
            Long recentMonthCount,
            Long completedCount,
            Long incompleteCount,
            GradeDistribution gradeDistribution,
            List<MonthlyScore> monthlyScores
    ) {
        return FeedbackStatsResponse.builder()
                .totalCount(totalCount)
                .averageScore(averageScore)
                .averageSpeechRateScore(averageSpeechRateScore)
                .averageFillerWordsScore(averageFillerWordsScore)
                .averagePolitenessScore(averagePolitenessScore)
                .averageClarityScore(averageClarityScore)
                .maxScore(maxScore)
                .minScore(minScore)
                .recentWeekCount(recentWeekCount)
                .recentMonthCount(recentMonthCount)
                .completedCount(completedCount)
                .incompleteCount(incompleteCount)
                .gradeDistribution(gradeDistribution)
                .monthlyScores(monthlyScores)
                .build();
    }
}