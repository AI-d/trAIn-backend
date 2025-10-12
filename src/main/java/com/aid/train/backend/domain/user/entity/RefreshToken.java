package com.aid.train.backend.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * JWT 리프레시 토큰 엔티티 클래스입니다.
 * 액세스 토큰 갱신을 위한 리프레시 토큰을 저장하고 관리합니다.
 *
 * <p>
 * 주요 기능:
 * <ul>
 *   <li>리프레시 토큰 발급 및 저장</li>
 *   <li>토큰 만료 및 폐기 상태 관리</li>
 *   <li>디바이스별 토큰 관리 (다중 기기 로그인)</li>
 *   <li>보안 정보 기록 (IP, User-Agent)</li>
 * </ul>
 * </p>
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Entity
@Table(
        name = "refresh_tokens",
        indexes = {
                @Index(name = "idx_rt_user", columnList = "user_id"),
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
     * 리프레시 토큰 고유 ID (자동 생성)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 토큰 소유 사용자
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 리프레시 토큰 문자열 (고유값)
     * JWT 형식으로 저장
     */
    @Column(nullable = false, unique = true, length = 500)
    private String token;

    /**
     * 디바이스 고유 ID
     * 동일 사용자의 여러 기기 구분용
     */
    @Column(name = "device_id", length = 200)
    private String deviceId;

    /**
     * 클라이언트 User-Agent
     * 보안 및 로그 추적용
     */
    @Column(name = "user_agent", length = 500)
    private String userAgent;

    /**
     * 클라이언트 IP 주소
     * IPv4/IPv6 모두 지원 (최대 45자)
     */
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    /**
     * 토큰 만료 일시
     * 기본값: 발급 시점 + 14일
     */
    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;

    /**
     * 토큰 폐기 여부
     * true: 폐기됨 (로그아웃, 보안 위협 등)
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean revoked = false;

    /**
     * 토큰 폐기 일시
     * 보안 로그 및 문제 분석용
     */
    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    /**
     * 토큰 발급 일시
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 토큰 정보 수정 일시
     */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 토큰이 활성 상태인지 확인합니다.
     * 폐기되지 않았고 만료되지 않은 경우 활성 상태입니다.
     *
     * @return 활성 상태면 true, 아니면 false
     */
    public boolean isActive() {
        return !revoked && LocalDateTime.now().isBefore(expiryDate);
    }

    /**
     * 토큰이 만료되었는지 확인합니다.
     *
     * @return 만료되었으면 true, 아니면 false
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }

    /**
     * 토큰을 폐기합니다.
     * 로그아웃 시 또는 보안 위협 감지 시 사용
     */
    public void revoke() {
        this.revoked = true;
        this.revokedAt = LocalDateTime.now();
    }

    /**
     * 토큰을 갱신합니다.
     *
     * @param newToken   새로운 토큰 문자열
     * @param expiryDate 새로운 만료 일시
     */
    public void updateToken(String newToken, LocalDateTime expiryDate) {
        this.token = newToken;
        this.expiryDate = expiryDate;
        this.revoked = false;
    }

    /**
     * 토큰과 연관된 보안 정보를 업데이트합니다.
     *
     * @param ipAddress 클라이언트 IP 주소
     * @param userAgent 클라이언트 User-Agent
     */
    public void updateSecurityInfo(String ipAddress, String userAgent) {
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
    }
}