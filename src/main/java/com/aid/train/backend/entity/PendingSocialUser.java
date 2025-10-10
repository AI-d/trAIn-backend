package com.aid.train.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 소셜 로그인 임시 저장 엔티티
 *
 * @author 왕택준
 * @since 2025-10-08
 */
@Entity
@Table(name = "pending_social_users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PendingSocialUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "temp_token", nullable = false, unique = true, length = 100)
    private String tempToken;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Provider provider;

    @Column(name = "provider_id", nullable = false, length = 100)
    private String providerId;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "is_completed", nullable = false)
    @Builder.Default
    private Boolean isCompleted = false;

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }

    public void complete() {
        this.isCompleted = true;
    }
}
