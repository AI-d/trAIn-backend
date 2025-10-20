package com.aid.train.backend.domain.terms.dto.response;

import com.aid.train.backend.domain.terms.entity.Terms;
import com.aid.train.backend.domain.terms.enums.TermsType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 약관 조회 응답 DTO입니다.
 * <p>
 * 약관 목록 조회 또는 상세 조회 시 반환되는 정보를 담습니다.
 * 관리자 페이지에서도 사용되며, 사용자에게는 현재 시점의 최신 약관만 노출됩니다.
 * </p>
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Schema(description = "약관 조회 응답")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TermsResponseDto {

    /**
     * 약관 ID
     * <p>
     * 약관의 고유 식별자입니다.
     * </p>
     */
    @Schema(description = "약관 ID", example = "1")
    private Long termsId;

    /**
     * 약관 유형
     * <p>
     * TERMS: 이용약관
     * PRIVACY: 개인정보처리방침
     * MARKETING: 마케팅 수신 동의
     * </p>
     */
    @Schema(description = "약관 유형 (TERMS, PRIVACY, MARKETING)", example = "TERMS")
    private TermsType type;

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
     * 약관 내용
     * <p>
     * 약관의 상세 내용으로, HTML 형식으로 저장됩니다.
     * 프론트엔드에서 렌더링하여 표시합니다.
     * </p>
     */
    @Schema(description = "약관 내용 (HTML)", example = "<h1>제1조 (목적)</h1><p>이 약관은...")
    private String content;

    /**
     * 약관 버전
     * <p>
     * 약관의 버전 정보입니다.
     * 형식: "1.0", "2.1" 등
     * </p>
     */
    @Schema(description = "약관 버전", example = "1.0")
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
     * 시행 일자
     * <p>
     * 해당 약관이 시행된 일시입니다.
     * </p>
     */
    @Schema(description = "시행 일자", example = "2025-01-01T00:00:00")
    private LocalDateTime effectiveDate;

    /**
     * 엔티티로부터 DTO를 생성합니다.
     *
     * @param terms 약관 엔티티
     * @return TermsResponseDto 인스턴스
     */
    public static TermsResponseDto from(Terms terms) {
        return TermsResponseDto.builder()
                .termsId(terms.getId())
                .type(terms.getType())
                .title(terms.getTitle())
                .content(terms.getContent())
                .version(terms.getVersion())
                .required(terms.getIsRequired())
                .build();
    }
}
