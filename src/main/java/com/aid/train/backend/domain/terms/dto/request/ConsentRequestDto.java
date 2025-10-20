package com.aid.train.backend.domain.terms.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 약관 동의 요청 DTO입니다.
 * <p>
 * 회원가입 시 또는 마케팅 수신 동의 변경 시 사용됩니다.
 * 필수 약관(이용약관, 개인정보처리방침)은 반드시 동의해야 합니다.
 * </p>
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Schema(description = "약관 동의 요청")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsentRequestDto {

    /**
     * 약관 ID
     * <p>
     * 약관의 고유 식별자입니다.
     * 필수 약관: 1(이용약관), 2(개인정보처리방침)
     * 선택 약관: 3(마케팅 수신 동의)
     * </p>
     */
    @Schema(description = "약관 ID (1:이용약관, 2:개인정보처리방침, 3:마케팅수신동의)", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "약관 ID는 필수입니다")
    private Long termsId;

    /**
     * 약관 버전
     * <p>
     * 약관의 버전 정보입니다.
     * 서버에서 최신 버전과 비교하여 동의 여부를 확인합니다.
     * </p>
     */
    @Schema(description = "약관 버전", example = "1.0", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "약관 버전은 필수입니다")
    private String version;

    /**
     * 동의 여부
     * <p>
     * true: 동의, false: 동의하지 않음
     * 필수 약관은 반드시 true여야 합니다.
     * </p>
     */
    @Schema(description = "동의 여부", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "동의 여부는 필수입니다")
    private Boolean agreed;

    /**
     * 필수 약관인지 확인합니다.
     *
     * @return 필수 약관이면 true, 선택 약관이면 false
     */
    public boolean isRequired() {
        return termsId != null && (termsId == 1L || termsId == 2L);
    }
}
