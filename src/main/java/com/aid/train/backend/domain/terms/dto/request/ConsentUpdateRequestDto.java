package com.aid.train.backend.domain.terms.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 약관 동의 변경 요청 DTO입니다.
 * <p>
 * 마케팅 수신 동의 등 선택 약관에 대한 동의를 변경할 때 사용됩니다.
 * 필수 약관(이용약관, 개인정보처리방침)은 변경할 수 없습니다.
 * </p>
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Schema(description = "약관 동의 변경 요청")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsentUpdateRequestDto {

    /**
     * 약관 동의 목록
     * <p>
     * 변경할 약관 동의 정보 목록입니다.
     * 최소 1개 이상의 약관 동의 정보가 포함되어야 합니다.
     * </p>
     */
    @Schema(description = "변경할 약관 동의 목록 (최소 1개 이상)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "약관 동의 목록은 필수입니다")
    @Size(min = 1, message = "최소 1개 이상의 약관 동의 정보가 필요합니다")
    @Valid
    private List<ConsentRequestDto> consents;

    /**
     * 필수 약관이 포함되어 있는지 확인합니다.
     *
     * @return 필수 약관이 포함되어 있으면 true, 아니면 false
     */
    public boolean containsRequiredConsent() {
        return consents != null && consents.stream().anyMatch(ConsentRequestDto::isRequired);
    }
}
