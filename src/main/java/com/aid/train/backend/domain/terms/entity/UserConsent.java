package com.aid.train.backend.domain.terms.entity;

import com.aid.train.backend.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 약관 동의 이력 엔티티 클래스입니다.
 * 각 사용자가 동의한 약관 내역과 법적 증거 정보를 저장합니다.
 *
 * <p>
 * 주요 기능:
 * <ul>
 *   <li>사용자별 약관 동의 내역 관리</li>
 *   <li>동의 시점의 IP 주소 및 User-Agent 기록 (법적 증거)</li>
 *   <li>동의 철회 및 재동의 관리</li>
 * </ul>
 * </p>
 *
 * <p>
 * 법적 요구사항:
 * 개인정보보호법에 따라 약관 동의 시 IP 주소, User-Agent, 동의 시각을
 * 최소 3년간 보관해야 합니다.
 * </p>
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Entity
@Table(
        name = "user_consents",
        indexes = {
                @Index(
                        name = "idx_uc_user_terms",
                        columnList = "user_id, terms_id"
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class UserConsent {

    /**
     * 사용자 약관 동의 고유 ID (자동 생성)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 동의한 사용자
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 동의한 약관
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "terms_id", nullable = false)
    private Terms terms;

    /**
     * 동의 여부
     * true: 동의함, false: 동의 철회
     */
    @Column(name = "is_agreed", nullable = false)
    @Builder.Default
    private Boolean isAgreed = true;

    /**
     * 동의 일시
     * 법적 증거로 사용
     */
    @Column(name = "consented_at", nullable = false)
    private LocalDateTime consentedAt;

    /**
     * 동의 철회 일시
     * 법적 증거 및 마케팅 분석용
     */
    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    /**
     * 동의 당시 클라이언트 IP 주소
     * 법적 증거로 사용 (IPv4/IPv6 지원)
     */
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    /**
     * 동의 당시 클라이언트 User-Agent
     * 법적 증거로 사용
     */
    @Column(name = "user_agent", length = 500)
    private String userAgent;

    /**
     * 레코드 생성 일시
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 레코드 수정 일시
     */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 동의를 철회합니다.
     * 마케팅 수신 동의 등 선택 약관에 사용
     */
    public void withdraw() {
        this.isAgreed = false;
        this.revokedAt = LocalDateTime.now();
    }

    /**
     * 동의를 다시 활성화합니다.
     * 철회 후 재동의 시 사용
     */
    public void consent() {
        this.isAgreed = true;
        this.consentedAt = LocalDateTime.now();
        this.revokedAt = null;
    }

    /**
     * 약관 동의 정보를 업데이트합니다.
     *
     * @param isAgreed  동의 여부
     * @param ipAddress 클라이언트 IP 주소
     * @param userAgent 클라이언트 User-Agent
     */
    public void updateConsent(Boolean isAgreed, String ipAddress, String userAgent) {
        this.isAgreed = isAgreed;
        this.consentedAt = LocalDateTime.now();
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
    }
}