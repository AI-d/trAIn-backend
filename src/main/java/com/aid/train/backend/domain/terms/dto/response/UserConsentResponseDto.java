package com.aid.train.backend.domain.terms.dto.response;

import com.aid.train.backend.domain.terms.entity.UserConsent;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 사용자 약관 동의 조회 응답 DTO입니다.
 * <p>
 * 사용자가 동의한 약관 목록을 조회할 때 반환되는 정보를 담습니다.
 * 마이페이지에서 사용자의 약관 동의 현황을 표시할 때 사용됩니다.
 * </p>
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Schema(description = "사용자 약관 동의 조회 응답")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserConsentResponseDto {

    /**
     * 약관 ID
     * <p>
     * 약관의 고유 식별자입니다.
     * </p>
     */
    @Schema(description = "약관 ID", example = "1")
    private Long termsId;

    /**
     * 약관 제목
     * <p>
     * 약관의 제목으로, 사용자에게 표시됩니다.
     * 예: "서비스 이용약관 (2025.01.01 시행)"
     * </p>
     */
    @Schema(description = "약관 제목", example = "서비스 이용약관 (2025.01.01 시행)")
    private String title;

    /**
     * 약관 유형
     * <p>
     * TERMS: 이용약관
     * PRIVACY: 개인정보처리방침
     * MARKETING: 마케팅 수신 동의
     * </p>
     */
    @Schema(description = "약관 유형 (TERMS, PRIVACY, MARKETING)", example = "TERMS")
    private String type;

    /**
     * 약관 버전
     * <p>
     * 사용자가 동의한 약관의 버전입니다.
     * </p>
     */
    @Schema(description = "동의한 약관 버전", example = "1.0")
    private String version;

    /**
     * 필수 여부
     * <p>
     * true: 필수 약관 (이용약관, 개인정보처리방침)
     * false: 선택 약관 (마케팅 수신 동의 등)
     * </p>
     */
    @Schema(description = "필수 약관 여부", example = "true")
    private Boolean required;

    /**
     * 동의 일시
     * <p>
     * 사용자가 해당 약관에 동의한 일시입니다.
     * </p>
     */
    @Schema(description = "동의 일시", example = "2025-01-01T12:34:56")
    private LocalDateTime agreedAt;

    /**
     * 엔티티로부터 DTO를 생성합니다.
     *
     * @param userConsent 사용자 약관 동의 엔티티
     * @return UserConsentResponseDto 인스턴스
     */
    public static UserConsentResponseDto from(UserConsent userConsent) {
        return UserConsentResponseDto.builder()
                .termsId(userConsent.getTerms().getId())
                .title(userConsent.getTerms().getTitle())
                .type(userConsent.getTerms().getType().name())
                .required(userConsent.getTerms().getIsRequired())
                .build();
    }
}
