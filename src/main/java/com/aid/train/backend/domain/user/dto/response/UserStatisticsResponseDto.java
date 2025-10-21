package com.aid.train.backend.domain.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자 통계 응답 DTO입니다.
 * 사용자의 면접 연습 통계 정보를 반환합니다.
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "사용자 통계 응답")
public class UserStatisticsResponseDto {

    @Schema(description = "사용자 ID", example = "1")
    private Long userId;

    @Schema(description = "총 면접 연습 횟수", example = "15")
    private Long totalInterviews;

    @Schema(description = "평균 점수", example = "85.5")
    private Double averageScore;

    @Schema(description = "최고 점수", example = "95.0")
    private Double highestScore;

    @Schema(description = "최저 점수", example = "70.0")
    private Double lowestScore;

    @Schema(description = "우수 성적 횟수 (80점 이상)", example = "10")
    private Long excellentCount;

    @Schema(description = "우수 성적 비율 (%)", example = "66.7")
    private Double excellentRate;

    @Schema(description = "완료한 시나리오 수", example = "8")
    private Long completedScenarios;

    /**
     * 통계 정보로부터 DTO를 생성합니다.
     *
     * @param userId 사용자 ID
     * @param totalInterviews 총 면접 횟수
     * @param averageScore 평균 점수
     * @param highestScore 최고 점수
     * @param lowestScore 최저 점수
     * @param excellentCount 우수 성적 횟수
     * @param completedScenarios 완료 시나리오 수
     * @return UserStatisticsResponseDto
     */
    public static UserStatisticsResponseDto of(
            Long userId,
            Long totalInterviews,
            Double averageScore,
            Double highestScore,
            Double lowestScore,
            Long excellentCount,
            Long completedScenarios
    ) {
        // 우수 성적 비율 계산
        Double excellentRate = totalInterviews > 0
                ? (excellentCount.doubleValue() / totalInterviews.doubleValue()) * 100
                : 0.0;

        return UserStatisticsResponseDto.builder()
                .userId(userId)
                .totalInterviews(totalInterviews)
                .averageScore(Math.round(averageScore * 10) / 10.0)  // 소수점 1자리 반올림
                .highestScore(highestScore)
                .lowestScore(lowestScore)
                .excellentCount(excellentCount)
                .excellentRate(Math.round(excellentRate * 10) / 10.0)  // 소수점 1자리 반올림
                .completedScenarios(completedScenarios)
                .build();
    }
}
