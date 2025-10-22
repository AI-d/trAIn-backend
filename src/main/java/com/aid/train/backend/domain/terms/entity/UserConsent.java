package com.aid.train.backend.domain.terms.entity;

import com.aid.train.backend.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 약관 동의 이력 엔티티 클래스입니다.
 * 각 사용자가 동의한 약관 내역을 저장합니다.
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
     */
    @Column(name = "consented_at", nullable = false)
    private LocalDateTime consentedAt;

    /**
     * 동의 철회 일시
     * 마케팅 동의 철회 시 사용
     */
    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

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
     * @param isAgreed 동의 여부
     */
    public void updateConsent(Boolean isAgreed) {
        this.isAgreed = isAgreed;
        this.consentedAt = LocalDateTime.now();

        if (!isAgreed) {
            this.revokedAt = LocalDateTime.now();
        } else {
            this.revokedAt = null;
        }
    }
}