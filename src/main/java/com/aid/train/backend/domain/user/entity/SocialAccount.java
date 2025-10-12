package com.aid.train.backend.domain.user.entity;

import com.aid.train.backend.domain.user.enums.Provider;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 소셜 계정 연동 엔티티 클래스입니다.
 * 카카오, 구글, 네이버 등 소셜 로그인 연동 정보를 저장합니다.
 *
 * <p>
 * 주요 기능:
 * <ul>
 *   <li>소셜 제공자별 고유 ID 관리</li>
 *   <li>소셜 계정의 이메일/이름 별도 저장</li>
 *   <li>연동 상태 관리 (연동/해제)</li>
 *   <li>마지막 로그인 시간 추적</li>
 * </ul>
 * </p>
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Entity
@Table(name = "social_accounts",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_provider_provider_id",
                columnNames = {"provider", "provider_id"}
        ))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class SocialAccount {

    /**
     * 소셜 계정 고유 ID (자동 생성)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 연동된 사용자
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @Setter
    private User user;

    /**
     * 소셜 로그인 제공자 (KAKAO, GOOGLE, NAVER)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Provider provider;

    /**
     * 소셜 제공자의 고유 사용자 ID
     * 각 제공자별로 고유한 식별자
     */
    @Column(name = "provider_id", nullable = false, length = 100)
    private String providerId;

    /**
     * 소셜 계정의 이메일 주소
     * 사용자 엔티티의 email과 다를 수 있음
     */
    @Column(name = "social_email", nullable = false, length = 100)
    private String socialEmail;

    /**
     * 소셜 계정의 이름
     * 사용자 엔티티의 name과 다를 수 있음
     */
    @Column(name = "social_name", nullable = false, length = 100)
    private String socialName;

    /**
     * 연동 상태
     * true: 연동됨, false: 연동 해제됨
     */
    @Column(name = "is_connected", nullable = false)
    @Builder.Default
    private Boolean isConnected = true;

    /**
     * 마지막 소셜 로그인 일시
     */
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    /**
     * 소셜 계정 연동 일시
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 소셜 계정 정보 수정 일시
     */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 마지막 로그인 시간을 현재 시간으로 업데이트합니다.
     */
    public void updateLastLogin() {
        this.lastLoginAt = LocalDateTime.now();
    }

    /**
     * 소셜 계정 연동을 해제합니다.
     */
    public void disconnect() {
        this.isConnected = false;
    }

    /**
     * 소셜 계정 연동을 재활성화합니다.
     */
    public void reconnect() {
        this.isConnected = true;
    }

    /**
     * 소셜 계정 정보를 업데이트합니다.
     *
     * @param socialEmail 업데이트할 소셜 이메일
     * @param socialName  업데이트할 소셜 이름
     */
    public void updateSocialInfo(String socialEmail, String socialName) {
        this.socialEmail = socialEmail;
        this.socialName = socialName;
    }
}