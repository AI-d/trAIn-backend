package com.aid.train.backend.domain.feedback.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Builder;

/**
 * 피드백 생성 요청 DTO
 * <p>
 * AI가 대화 분석을 완료한 후 피드백을 저장할 때 사용하는 요청 객체입니다.
 * ChatGPT 4.0이 분석한 점수와 3가지 스타일의 개선안을 포함합니다.
 * </p>
 * <p>
 * 점수 체계:
 * - 전체 점수 (0-100): 발화속도 + 추임새 + 공손도 + 명료성의 합계
 * - 발화속도 (0-30): 적절한 말하기 속도 평가
 * - 추임새 (0-20): "음...", "그..." 등 불필요한 표현 빈도 (적을수록 좋음)
 * - 공손도 (0-25): 상대방에 대한 예의와 존중 표현
 * - 명료성 (0-25): 의사 전달의 명확성과 구체성
 * </p>
 *
 * @author 왕택준
 * @see com.aid.train.backend.domain.feedback.controller.FeedbackController#createFeedback(FeedbackCreateRequest)
 * @see com.aid.train.backend.domain.feedback.service.FeedbackPromptService#generateFeedbackFromAI(com.aid.train.backend.domain.session.entity.DialogueSession)
 * @since 1.0.0
 */
@Schema(description = "피드백 생성 요청")
@Builder
public record FeedbackCreateRequest(
        /**
         * 대화 세션 ID
         * <p>
         * 피드백을 생성할 대상 세션의 고유 식별자입니다.
         * 해당 세션은 완료 상태여야 하며, 이미 피드백이 존재하면 안 됩니다.
         * </p>
         */
        @Schema(description = "세션 ID", example = "550e8400-e29b-41d4-a716-446655440000", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "세션 ID는 필수입니다.")
        String sessionId,

        /**
         * 전체 점수 (0-100)
         * <p>
         * 발화속도, 추임새, 공손도, 명료성 점수의 합계입니다.
         * AI가 대화를 종합적으로 분석한 결과입니다.
         * </p>
         */
        @Schema(description = "전체 점수 (0-100)", example = "72", minimum = "0", maximum = "100", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "전체 점수는 필수입니다.")
        @Min(value = 0, message = "전체 점수는 0 이상이어야 합니다.")
        @Max(value = 100, message = "전체 점수는 100 이하여야 합니다.")
        Integer totalScore,

        /**
         * 발화속도 점수 (0-30)
         * <p>
         * 너무 빠르거나 느리지 않은 적절한 말하기 속도를 평가합니다.
         * 상황에 맞는 말하기 속도를 유지했는지 분석합니다.
         * </p>
         */
        @Schema(description = "발화속도 점수 (0-30)", example = "23", minimum = "0", maximum = "25", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "발화속도 점수는 필수입니다.")
        @Min(value = 0, message = "발화속도 점수는 0 이상이어야 합니다.")
        @Max(value = 30, message = "발화속도 점수는 25 이하여야 합니다.")
        Integer speechRateScore,

        /**
         * 추임새 점수 (0-20)
         * <p>
         * "음...", "그...", "아..." 등 불필요한 표현의 빈도를 평가합니다.
         * 점수가 높을수록 추임새가 적어 좋은 것입니다.
         * </p>
         */
        @Schema(description = "추임새 점수 (0-25) - 높을수록 좋음", example = "12", minimum = "0", maximum = "25", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "추임새 점수는 필수입니다.")
        @Min(value = 0, message = "추임새 점수는 0 이상이어야 합니다.")
        @Max(value = 20, message = "추임새 점수는 25 이하여야 합니다.")
        Integer fillerWordsScore,

        /**
         * 공손도 점수 (0-25)
         * <p>
         * 상대방에 대한 예의와 존중이 표현되었는지 평가합니다.
         * 상황에 맞는 높임말과 정중한 표현을 사용했는지 분석합니다.
         * </p>
         */
        @Schema(description = "공손도 점수 (0-25)", example = "21", minimum = "0", maximum = "25", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "공손도 점수는 필수입니다.")
        @Min(value = 0, message = "공손도 점수는 0 이상이어야 합니다.")
        @Max(value = 25, message = "공손도 점수는 25 이하여야 합니다.")
        Integer politenessScore,

        /**
         * 명료성 점수 (0-25)
         * <p>
         * 의사 전달의 명확성과 구체성을 평가합니다.
         * 듣는 사람이 이해하기 쉽게 말했는지 분석합니다.
         * </p>
         */
        @Schema(description = "명료성 점수 (0-25)", example = "16", minimum = "0", maximum = "25", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "명료성 점수는 필수입니다.")
        @Min(value = 0, message = "명료성 점수는 0 이상이어야 합니다.")
        @Max(value = 25, message = "명료성 점수는 25 이하여야 합니다.")
        Integer clarityScore,

        /**
         * 개선점 목록 (JSON 형태)
         * <p>
         * AI가 분석한 구체적인 개선 포인트들을 JSON 배열로 저장합니다.
         * 각 개선점은 유형, 설명, 개수, 제안사항을 포함합니다.
         * </p>
         */
        @Schema(description = "개선점 목록 (JSON)", example = "[{\"type\":\"filler_words\",\"description\":\"추임새 줄이기\",\"count\":5,\"suggestion\":\"말하기 전에 잠시 생각하는 시간을 가져보세요\"}]")
        @Size(max = 5000, message = "개선점은 5000자 이하여야 합니다.")
        String improvementPoints,

        /**
         * 원본 발화 내용
         * <p>
         * 사용자가 실제로 말한 내용 중 피드백 대상이 되는 주요 발언입니다.
         * AI가 분석하여 개선이 필요하다고 판단한 실제 발화입니다.
         * </p>
         */
        @Schema(description = "원본 발화 내용", example = "팀장님, 내일... 음... 휴가 가능하신가요?", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "원본 발화는 필수입니다.")
        @Size(max = 2000, message = "원본 발화는 2000자 이하여야 합니다.")
        String originalTranscript,

        /**
         * 개선안 A - 간결하고 명료한 스타일
         * <p>
         * 불필요한 표현을 제거하고 핵심만 전달하는 간결한 스타일의 개선안입니다.
         * 비즈니스 상황에서 효율적인 커뮤니케이션을 위한 제안입니다.
         * </p>
         */
        @Schema(description = "개선안 A (간결한 스타일)", example = "팀장님, 내일 휴가 가능한가요?", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "개선안 A는 필수입니다.")
        @Size(max = 2000, message = "개선안 A는 2000자 이하여야 합니다.")
        String alternativeA,

        /**
         * 개선안 B - 공손하고 정중한 스타일
         * <p>
         * 정중한 표현과 예의를 강조한 공손한 스타일의 개선안입니다.
         * 격식있는 상황이나 상급자와의 대화에 적합한 제안입니다.
         * </p>
         */
        @Schema(description = "개선안 B (공손한 스타일)", example = "팀장님, 내일 하루 휴가를 사용해도 될까요?", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "개선안 B는 필수입니다.")
        @Size(max = 2000, message = "개선안 B는 2000자 이하여야 합니다.")
        String alternativeB,

        /**
         * 개선안 C - 따뜻하고 친근한 스타일
         * <p>
         * 친근하고 감정적 교감을 포함한 따뜻한 스타일의 개선안입니다.
         * 동료나 친밀한 관계에서 사용하기 좋은 제안입니다.
         * </p>
         */
        @Schema(description = "개선안 C (따뜻한 스타일)", example = "팀장님, 내일 개인적인 일로 휴가를 쓰고 싶은데 괜찮을까요?", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "개선안 C는 필수입니다.")
        @Size(max = 2000, message = "개선안 C는 2000자 이하여야 합니다.")
        String alternativeC,

        /**
         * AI 프롬프트 (디버깅용)
         * <p>
         * ChatGPT에게 전달한 원본 프롬프트입니다.
         * 피드백 품질 개선을 위한 디버깅 및 분석 목적으로 저장됩니다.
         * </p>
         */
        @Schema(description = "AI에게 전달한 프롬프트 (디버깅용)", example = "당신은 대화 훈련 전문 AI 코치입니다...")
        @Size(max = 10000, message = "AI 프롬프트는 10000자 이하여야 합니다.")
        String aiPrompt,

        /**
         * AI 원본 응답 (디버깅용)
         * <p>
         * ChatGPT가 반환한 원본 응답입니다.
         * JSON 파싱 오류나 응답 품질 분석을 위한 디버깅 목적으로 저장됩니다.
         * </p>
         */
        @Schema(description = "AI의 원본 응답 (디버깅용)", example = "```json\\n{\\\"totalScore\\\": 72, ...}\\n```")
        @Size(max = 20000, message = "AI 원본 응답은 20000자 이하여야 합니다.")
        String aiRawResponse
) {
}