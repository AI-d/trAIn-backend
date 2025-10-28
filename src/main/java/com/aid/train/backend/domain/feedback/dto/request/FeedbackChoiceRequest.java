package com.aid.train.backend.domain.feedback.dto.request;

import com.aid.train.backend.domain.feedback.entity.Feedback;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Builder;

/**
 * 피드백 개선안 선택 요청 DTO
 * <p>
 * 사용자가 AI가 제시한 개선안 중 하나를 선택하거나 직접 수정할 때 사용합니다.
 * AI가 제공한 3가지 스타일(간결/공손/따뜻) 중 선택하거나, 사용자가 직접 작성할 수 있습니다.
 * </p>
 * <p>
 * 선택 옵션:
 * - A: 간결하고 명료한 스타일
 * - B: 공손하고 정중한 스타일
 * - C: 따뜻하고 친근한 스타일
 * - CUSTOM: 사용자가 직접 작성한 개선안
 * </p>
 *
 * @author 왕택준
 * @since 1.0.0
 * @see com.aid.train.backend.domain.feedback.controller.FeedbackController#choosealternative(String, FeedbackChoiceRequest)
 * @see com.aid.train.backend.domain.feedback.entity.Feedback.ChosenAlternative
 */
@Schema(description = "피드백 개선안 선택 요청")
@Builder
public record FeedbackChoiceRequest(
        /**
         * 선택한 개선안 타입
         * <p>
         * 사용자가 선택한 개선안의 유형을 나타냅니다.
         * A, B, C 중 하나를 선택하거나 CUSTOM으로 직접 작성할 수 있습니다.
         * </p>
         */
        @Schema(description = "선택한 개선안 타입", example = "A", allowableValues = {"A", "B", "C", "CUSTOM"}, requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "선택한 개선안 타입은 필수입니다.")
        Feedback.ChosenAlternative chosenAlternative,

        /**
         * 최종 선택된 개선안 내용
         * <p>
         * chosenAlternative가 CUSTOM인 경우에만 필수입니다.
         * A, B, C를 선택한 경우 해당 개선안이 자동으로 설정됩니다.
         * 사용자가 직접 작성하거나 기존 개선안을 수정한 최종 버전입니다.
         * </p>
         */
        @Schema(description = "최종 선택된 개선안 내용 (CUSTOM인 경우 필수)", example = "팀장님, 내일 휴가를 사용하고 싶습니다.")
        @Size(max = 2000, message = "최종 선택안은 2000자 이하여야 합니다.")
        String finalChoice
) {
    /**
     * 요청 데이터의 유효성을 검증합니다.
     * <p>
     * CUSTOM 선택 시 finalChoice가 필수인지 확인합니다.
     * A, B, C 선택 시에는 finalChoice가 없어도 됩니다.
     * </p>
     *
     * @return 유효한 요청이면 true, 그렇지 않으면 false
     */
    public boolean isValid() {
        if (chosenAlternative == Feedback.ChosenAlternative.CUSTOM) {
            return finalChoice != null && !finalChoice.trim().isEmpty();
        }
        return true;
    }
}