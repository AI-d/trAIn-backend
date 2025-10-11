package com.aid.train.backend.domain.verification.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 이메일 인증 토큰 엔티티
 *
 * @author 왕택준
 * @since 2025-10-08
 */
@Entity
@Table(name = "email_verifications")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class EmailVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(nullable = false, unique = true, length = 100)
    private String token;

    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;

    @Column(name = "is_verified", nullable = false)
    @Builder.Default
    private Boolean isVerified = false;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "resend_count", nullable = false)
    @Builder.Default
    private Integer resendCount = 0;

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }

    public void verify() {
        this.isVerified = true;
        this.verifiedAt = LocalDateTime.now();
    }

    public boolean canResend() {
        return this.resendCount < 3;
    }
}
