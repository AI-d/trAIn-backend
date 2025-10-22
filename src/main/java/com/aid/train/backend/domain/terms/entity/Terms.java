package com.aid.train.backend.domain.terms.entity;

import com.aid.train.backend.domain.terms.enums.TermsType;
import jakarta.persistence.*;
import lombok.*;

/**
 * 약관 본문 엔티티 클래스입니다.
 * 서비스 이용약관, 개인정보 처리방침, 마케팅 수신 동의 등의 약관을 저장합니다.
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Entity
@Table(
        name = "terms",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_terms_type_version",
                        columnNames = {"type", "version"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_terms_type_active",
                        columnList = "type, is_active"
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Terms {

    /**
     * 약관 고유 ID (자동 생성)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 약관 종류 (TERMS_OF_SERVICE, PRIVACY_POLICY, MARKETING_CONSENT)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TermsType type;

    /**
     * 약관 버전 (예: 1.0, 1.1, 2.0)
     */
    @Column(nullable = false, length = 20)
    private String version;

    /**
     * 약관 제목
     */
    @Column(nullable = false, length = 200)
    private String title;

    /**
     * 약관 내용 (전문)
     * Markdown 또는 HTML 형식으로 저장 가능
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * 필수 동의 여부
     * true: 필수 약관 (동의하지 않으면 가입 불가)
     * false: 선택 약관 (거부 가능)
     */
    @Column(name = "is_required", nullable = false)
    @Builder.Default
    private Boolean isRequired = true;

    /**
     * 약관 활성화 여부
     * 최신 버전만 true로 설정
     * 이전 버전은 false (참조용으로 보관)
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    /**
     * 약관을 비활성화합니다.
     * 새 버전 약관 등록 시 이전 버전 비활성화에 사용
     */
    public void deactivate() {
        this.isActive = false;
    }

    /**
     * 약관을 활성화합니다.
     */
    public void activate() {
        this.isActive = true;
    }

    /**
     * 약관 내용을 업데이트합니다.
     * 주의: 약관 변경 시 기존 약관은 비활성화하고 새 버전 생성 권장
     *
     * @param title   업데이트할 제목
     * @param content 업데이트할 내용
     */
    public void updateContent(String title, String content) {
        this.title = title;
        this.content = content;
    }

    /**
     * 약관 엔티티를 생성하는 정적 팩토리 메서드입니다.
     * 초기 데이터 생성 및 새 약관 버전 등록 시 사용합니다.
     *
     * @param type    약관 타입
     * @param title   약관 제목
     * @param content 약관 내용
     * @param version 약관 버전
     * @return 생성된 Terms 엔티티
     */
    public static Terms createTerms(TermsType type, String title, String content, String version) {
        return Terms.builder()
                .type(type)
                .title(title)
                .content(content)
                .version(version)
                .isRequired(type != TermsType.MARKETING_CONSENT) // 마케팅 동의는 선택, 나머지는 필수
                .isActive(true)
                .build();
    }
}