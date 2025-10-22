package com.aid.train.backend.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * JWT 리프레시 토큰 엔티티
 * <p>
 * Access Token 갱신을 위한 Refresh Token을 저장합니다.
 * HttpOnly 쿠키로 전달되며, 만료 시 재로그인이 필요합니다.
 * <p>
 * 주요 특징:
 * - 유효기간: 14일
 * - 로그아웃 시 DB에서 삭제
 * - 사용자당 여러 토큰 가능 (멀티 디바이스)
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Entity
@Table(
        name = "refresh_tokens",
        indexes = {
                @Index(name = "idx_rt_user", columnList = "user_id"),
                @Index(name = "idx_rt_token", columnList = "token"),
                @Index(name = "idx_rt_expiry", columnList = "expiry_date")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class RefreshToken {

    /**
     * 리프레시 토큰 고유 ID (PK)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 토큰 소유 사용자
     * N:1 관계
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 리프레시 토큰 값 (JWT)
     * 고유값이며 HttpOnly 쿠키로 전달
     */
    @Column(nullable = false, unique = true, length = 500)
    private String token;

    /**
     * 토큰 만료 일시
     * 발급 시점 + 14일
     */
    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;

    /**
     * 토큰 발급 일시
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 토큰 만료 여부 확인
     * <p>
     * 만료된 토큰은 사용 시 즉시 삭제되며,
     * 스케줄러를 통해 주기적으로 정리됩니다.
     *
     * @return 만료되었으면 true
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }
}