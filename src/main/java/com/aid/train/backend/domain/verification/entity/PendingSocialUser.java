package com.aid.train.backend.domain.verification.entity;

import com.aid.train.backend.domain.user.enums.Provider;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 소셜 로그인 임시 저장 엔티티 클래스입니다.
 * 소셜 로그인 후 추가 정보 입력 대기 중인 사용자 정보를 임시 저장합니다.
 *
 * <p>
 * 사용 시나리오:
 * <ol>
 *   <li>사용자가 소셜 로그인 시도</li>
 *   <li>소셜 제공자로부터 사용자 정보 수신</li>
 *   <li>추가 정보 필요 시 PendingSocialUser에 임시 저장</li>
 *   <li>사용자가 추가 정보 입력 (약관 동의 등)</li>
 *   <li>User 및 SocialAccount 생성 후 PendingSocialUser 삭제</li>
 * </ol>
 * </p>
 *
 * <p>
 * 보안:
 * 임시 토큰을 통해 추가 정보 입력 과정을 추적하고,
 * 10분 이내 미완료 시 자동 삭제됩니다.
 * </p>
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Entity
@Table(
        name = "pending_social_users",
        indexes = {
                @Index(
                        name = "idx_psu_provider_id",
                        columnList = "provider, provider_id"
                ),
                @Index(
                        name = "idx_psu_expiry",
                        columnList = "expiry_date"
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PendingSocialUser {

    /**
     * 대기 사용자 고유 ID (자동 생성)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 임시 토큰 (UUID 형식, 고유값)
     * 추가 정보 입력 과정 추적용
     */
    @Column(name = "temp_token", nullable = false, unique = true, length = 100)
    private String tempToken;

    /**
     * 소셜 제공자 (KAKAO, GOOGLE, NAVER)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Provider provider;

    /**
     * 소셜 제공자의 고유 사용자 ID
     */
    @Column(name = "provider_id", nullable = false, length = 100)
    private String providerId;

    /**
     * 소셜 로그인으로 받은 이메일 주소
     */
    @Column(nullable = false, length = 100)
    private String email;

    /**
     * 소셜 로그인으로 받은 이름
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * 임시 데이터 만료 일시
     * 기본값: 생성 시점 + 10분
     * 10분 내에 추가 정보를 입력하지 않으면 삭제됨
     */
    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;

    /**
     * 임시 데이터 생성 일시
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * 회원가입 완료 여부
     * 추가 정보 입력 완료 시 true로 변경
     */
    @Column(name = "is_completed", nullable = false)
    @Builder.Default
    private Boolean isCompleted = false;

    /**
     * 엔티티 생성 시 자동으로 현재 시간과 만료 시간을 설정합니다.
     */
    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.expiryDate == null) {
            this.expiryDate = this.createdAt.plusMinutes(10);
        }
    }

    /**
     * 임시 데이터가 만료되었는지 확인합니다.
     *
     * @return 만료되었으면 true, 아니면 false
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }

    /**
     * 회원가입을 완료 처리합니다.
     * User 및 SocialAccount 생성 후 호출
     */
    public void complete() {
        this.isCompleted = true;
    }

    /**
     * 사용자 이름을 업데이트합니다.
     * 추가 정보 입력 시 사용
     *
     * @param name 업데이트할 이름
     */
    public void updateName(String name) {
        this.name = name;
    }

    /**
     * 임시 토큰이 일치하는지 확인합니다.
     *
     * @param token 확인할 토큰
     * @return 일치하면 true, 아니면 false
     */
    public boolean matchesToken(String token) {
        return this.tempToken.equals(token);
    }

    /**
     * 회원가입이 완료 가능한 상태인지 확인합니다.
     * 만료되지 않았고 아직 완료되지 않은 경우 가능
     *
     * @return 완료 가능하면 true, 아니면 false
     */
    public boolean canComplete() {
        return !isExpired() && !isCompleted;
    }
}