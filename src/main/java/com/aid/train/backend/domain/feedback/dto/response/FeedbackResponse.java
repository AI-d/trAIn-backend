package com.aid.train.backend.domain.feedback.dto.response;

import com.aid.train.backend.domain.feedback.entity.Feedback;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 피드백 상세 응답 DTO
 * <p>
 * 단일 피드백의 상세 정보를 클라이언트에게 전달합니다.
 * AI 분석 결과, 점수, 3가지 스타일의 개선안, 사용자 선택 결과를 모두 포함합니다.
 * </p>
 * <p>
 * 사용 시나리오:
 * - 피드백 생성 후 즉시 반환
 * - 특정 세션의 피드백 상세 조회
 * - 개선안 선택 후 업데이트된 정보 반환
 * </p>
 *
 * @author 왕택준
 * @since 1.0.0
 * @see com.aid.train.backend.domain.feedback.controller.FeedbackController#getFeedback(String)
 * @see com.aid.train.backend.domain.feedback.controller.FeedbackController#createFeedback(com.aid.train.backend.domain.feedback.dto.request.FeedbackCreateRequest)
 */
@Schema(description = "피드백 상세 정보")
@Builder
public record FeedbackResponse(
        /**
         * 피드백 고유 ID
         * <p>
         * 데이터베이스에서 생성된 피드백의 고유 식별자입니다.
         * </p>
         */
        @Schema(description = "피드백 ID", example = "1")
        Long id,

        /**
         * 연관된 세션 ID
         * <p>
         * 이 피드백이 생성된 대화 세션의 고유 식별자입니다.
         * </p>
         */
        @Schema(description = "세션 ID", example = "550e8400-e29b-41d4-a716-446655440000")
        String sessionId,

        /**
         * 시나리오 ID
         * <p>
         * 대화가 진행된 시나리오의 고유 식별자입니다.
         * </p>
         */
        @Schema(description = "시나리오 ID", example = "1")
        Long scenarioId,

        /**
         * 시나리오 제목
         * <p>
         * 대화가 진행된 시나리오의 제목입니다.
         * 피드백 컨텍스트를 이해하는 데 도움이 됩니다.
         * </p>
         */
        @Schema(description = "시나리오 제목", example = "상사에게 휴가 요청하기")
        String scenarioTitle,

        /**
         * 전체 점수 (0-100)
         * <p>
         * AI가 분석한 종합 점수입니다.
         * 발화속도, 추임새, 공손도, 명료성의 합계입니다.
         * </p>
         */
        @Schema(description = "전체 점수 (0-100)", example = "72", minimum = "0", maximum = "100")
        Integer totalScore,

        /**
         * 점수 등급 (A-F)
         * <p>
         * 총점을 기준으로 한 등급입니다.
         * A(90+), B(80-89), C(70-79), D(60-69), F(60미만)
         * </p>
         */
        @Schema(description = "점수 등급", example = "C", allowableValues = {"A", "B", "C", "D", "F"})
        String scoreGrade,

        /**
         * 발화속도 점수 (0-30)
         * <p>
         * 적절한 말하기 속도 평가 점수입니다.
         * 너무 빠르거나 느리지 않은 적절한 속도를 유지했는지 분석합니다.
         * </p>
         */
        @Schema(description = "발화속도 점수 (0-30)", example = "23", minimum = "0", maximum = "30")
        Integer speechRateScore,

        /**
         * 추임새 점수 (0-20)
         * <p>
         * "음...", "그..." 등 불필요한 표현의 빈도 평가 점수입니다.
         * 점수가 높을수록 추임새가 적어 좋은 것입니다.
         * </p>
         */
        @Schema(description = "추임새 점수 (0-20) - 높을수록 좋음", example = "12", minimum = "0", maximum = "20")
        Integer fillerWordsScore,

        /**
         * 공손도 점수 (0-25)
         * <p>
         * 상대방에 대한 예의와 존중 표현 평가 점수입니다.
         * 상황에 맞는 높임말과 정중한 표현을 사용했는지 분석합니다.
         * </p>
         */
        @Schema(description = "공손도 점수 (0-25)", example = "21", minimum = "0", maximum = "25")
        Integer politenessScore,

        /**
         * 명료성 점수 (0-25)
         * <p>
         * 의사 전달의 명확성과 구체성 평가 점수입니다.
         * 듣는 사람이 이해하기 쉽게 말했는지 분석합니다.
         * </p>
         */
        @Schema(description = "명료성 점수 (0-25)", example = "16", minimum = "0", maximum = "25")
        Integer clarityScore,

        /**
         * 개선점 목록 (JSON)
         * <p>
         * AI가 분석한 구체적인 개선 포인트들입니다.
         * JSON 배열 형태로 저장되며, 각 개선점은 유형, 설명, 제안사항을 포함합니다.
         * </p>
         */
        @Schema(description = "개선점 목록 (JSON)", example = "[{\"type\":\"filler_words\",\"description\":\"추임새 줄이기\",\"count\":5}]")
        String improvementPoints,

        /**
         * 원본 발화 내용
         * <p>
         * 사용자가 실제로 말한 내용 중 피드백 대상이 되는 주요 발언입니다.
         * </p>
         */
        @Schema(description = "원본 발화 내용", example = "팀장님, 내일... 음... 휴가 가능하신가요?")
        String originalTranscript,

        /**
         * 개선안 A - 간결한 스타일
         * <p>
         * 불필요한 표현을 제거하고 핵심만 전달하는 간결한 스타일의 개선안입니다.
         * </p>
         */
        @Schema(description = "개선안 A (간결한 스타일)", example = "팀장님, 내일 휴가 가능한가요?")
        String alternativeA,

        /**
         * 개선안 B - 공손한 스타일
         * <p>
         * 정중한 표현과 예의를 강조한 공손한 스타일의 개선안입니다.
         * </p>
         */
        @Schema(description = "개선안 B (공손한 스타일)", example = "팀장님, 내일 하루 휴가를 사용해도 될까요?")
        String alternativeB,

        /**
         * 개선안 C - 따뜻한 스타일
         * <p>
         * 친근하고 감정적 교감을 포함한 따뜻한 스타일의 개선안입니다.
         * </p>
         */
        @Schema(description = "개선안 C (따뜻한 스타일)", example = "팀장님, 내일 개인적인 일로 휴가를 쓰고 싶은데 괜찮을까요?")
        String alternativeC,

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
         * 최종 선택된 개선안 내용
         * <p>
         * 사용자가 최종적으로 선택하거나 직접 작성한 개선안입니다.
         * 선택하지 않은 경우 null입니다.
         * </p>
         */
        @Schema(description = "최종 선택된 개선안 내용", example = "팀장님, 내일 휴가 가능한가요?")
        String finalChoice,

        /**
         * 선택 완료 여부
         * <p>
         * 사용자가 개선안 선택을 완료했는지 여부입니다.
         * true인 경우 학습이 완전히 완료된 상태입니다.
         * </p>
         */
        @Schema(description = "선택 완료 여부", example = "true")
        Boolean isChoiceComplete,

        /**
         * 피드백 생성 시간
         * <p>
         * AI가 피드백을 생성한 시간입니다.
         * </p>
         */
        @Schema(description = "피드백 생성 시간", example = "2025-10-24T14:30:00")
        LocalDateTime createdAt,

        /**
         * 피드백 수정 시간
         * <p>
         * 피드백이 마지막으로 수정된 시간입니다.
         * 사용자가 개선안을 선택하면 업데이트됩니다.
         * </p>
         */
        @Schema(description = "피드백 수정 시간", example = "2025-10-24T14:35:00")
        LocalDateTime updatedAt
) {
    /**
     * Feedback 엔티티로부터 FeedbackResponse를 생성합니다.
     *
     * @param feedback 피드백 엔티티
     * @return 완전한 피드백 응답 객체
     */
    public static FeedbackResponse from(Feedback feedback) {
        return FeedbackResponse.builder()
                .id(feedback.getId())
                .sessionId(feedback.getDialogueSession().getSessionId())
                .scenarioId(feedback.getDialogueSession().getScenario().getId())
                .scenarioTitle(feedback.getDialogueSession().getScenario().getTitle())
                .totalScore(feedback.getTotalScore())
                .scoreGrade(feedback.getScoreGrade())
                .speechRateScore(feedback.getSpeechRateScore())
                .fillerWordsScore(feedback.getFillerWordsScore())
                .politenessScore(feedback.getPolitenessScore())
                .clarityScore(feedback.getClarityScore())
                .improvementPoints(feedback.getImprovementPoints())
                .originalTranscript(feedback.getOriginalTranscript())
                .alternativeA(feedback.getAlternativeA())
                .alternativeB(feedback.getAlternativeB())
                .alternativeC(feedback.getAlternativeC())
                .chosenAlternative(feedback.getChosenAlternative())
                .finalChoice(feedback.getFinalChoice())
                .isChoiceComplete(feedback.isChoiceComplete())
                .createdAt(feedback.getCreatedAt())
                .updatedAt(feedback.getUpdatedAt())
                .build();
    }

    /**
     * 점수만 포함한 간단한 응답을 생성합니다.
     * <p>
     * 히스토리 목록이나 통계에서 상세한 개선안 정보 없이
     * 점수 정보만 필요한 경우 사용합니다.
     * </p>
     *
     * @param feedback 피드백 엔티티
     * @return 점수 정보만 포함한 응답 객체
     */
    public static FeedbackResponse fromWithScoreOnly(Feedback feedback) {
        return FeedbackResponse.builder()
                .id(feedback.getId())
                .sessionId(feedback.getDialogueSession().getSessionId())
                .totalScore(feedback.getTotalScore())
                .scoreGrade(feedback.getScoreGrade())
                .speechRateScore(feedback.getSpeechRateScore())
                .fillerWordsScore(feedback.getFillerWordsScore())
                .politenessScore(feedback.getPolitenessScore())
                .clarityScore(feedback.getClarityScore())
                .createdAt(feedback.getCreatedAt())
                .build();
    }

}