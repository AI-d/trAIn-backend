package com.aid.train.backend.domain.user.entity;

import com.aid.train.backend.domain.user.enums.Provider;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 소셜 계정 연동 엔티티
 * <p>
 * 카카오, 구글, 네이버 등 소셜 로그인 연동 정보를 저장합니다.
 * 하나의 User에 여러 소셜 계정을 연동할 수 있습니다.
 * <p>
 * 주요 기능:
 * - 소셜 계정 연동/해제
 * - 소셜 로그인 이력 관리
 * - 소셜 계정 정보 동기화
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Entity
@Table(
        name = "social_accounts",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_provider_provider_id",
                columnNames = {"provider", "provider_id"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class SocialAccount {

    /**
     * 소셜 계정 고유 ID (PK)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 연동된 사용자
     * N:1 관계
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @Setter
    private User user;

    /**
     * 소셜 제공자
     * GOOGLE, KAKAO, NAVER
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Provider provider;

    /**
     * 소셜 플랫폼 고유 사용자 ID
     * 각 플랫폼에서 발급하는 사용자 식별자
     * 예: Google의 sub, Kakao의 id
     */
    @Column(name = "provider_id", nullable = false, length = 100)
    private String providerId;

    /**
     * 소셜 계정 이메일
     * 소셜 플랫폼에서 받은 이메일 주소
     * User의 email과 다를 수 있음
     */
    @Column(name = "social_email", nullable = false, length = 100)
    private String socialEmail;

    /**
     * 소셜 계정 이름 (원본 보관용)
     * 소셜 플랫폼에서 받은 이름을 그대로 저장
     * User의 nickname과 다를 수 있음 (User는 닉네임 변경 가능)
     */
    @Column(name = "social_name", nullable = false, length = 100)
    private String socialName;

    /**
     * 연동 상태
     * true: 연동 중, false: 연동 해제
     */
    @Column(name = "is_connected", nullable = false)
    @Builder.Default
    private Boolean isConnected = true;

    /**
     * 마지막 소셜 로그인 일시
     * 휴면 계정 관리를 위해 필요
     */
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    /**
     * 마지막 로그인 시간 업데이트
     */
    public void updateLastLogin() {
        this.lastLoginAt = LocalDateTime.now();
    }

    /**
     * 소셜 계정 연동 해제
     */
    public void disconnect() {
        this.isConnected = false;
    }

    /**
     * 소셜 계정 재연동
     */
    public void reconnect() {
        this.isConnected = true;
    }

    /**
     * 소셜 계정 정보 업데이트
     *
     * @param socialEmail 소셜 이메일
     * @param socialName  소셜 이름
     */
    public void updateSocialInfo(String socialEmail, String socialName) {
        this.socialEmail = socialEmail;
        this.socialName = socialName;
    }
}