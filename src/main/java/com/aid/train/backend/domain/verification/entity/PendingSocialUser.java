package com.aid.train.backend.domain.verification.entity;

import com.aid.train.backend.domain.user.enums.Provider;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 소셜 회원가입 완료 대기 엔티티
 * <p>
 * 소셜 로그인 신규 사용자의 회원가입 완료를 위한 임시 저장소입니다.
 * 추가 정보 입력 및 약관 동의 후 User 엔티티로 전환됩니다.
 * <p>
 * 추가 정보:
 * - 닉네임 (소셜에서 받은 이름을 기본값으로 수정 가능)
 * - 생년월일 (필수)
 * - 직업 정보 (필수)
 * - 약관 동의 (필수/선택)
 * <p>
 * 보안 전략:
 * - JWT로 정보 전달 (stateless)
 * - DB 저장으로 무효화 가능 (원타임 토큰)
 * - 이중 검증 (JWT 검증 + DB 조회)
 * - 사용 후 즉시 삭제
 * - 스케줄러로 만료 토큰 정리
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Entity
@Table(
        name = "pending_social_users",
        indexes = {
                @Index(name = "idx_psu_token", columnList = "pending_token"),
                @Index(name = "idx_psu_provider", columnList = "provider, provider_id"),
                @Index(name = "idx_psu_expiry", columnList = "expiry_date")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class PendingSocialUser {

    /**
     * 임시 데이터 고유 ID (PK)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 회원가입 완료 대기 토큰 (JWT)
     * <p>
     * JWT Claims:
     * - type: "SOCIAL_SIGNUP_PENDING"
     * - provider: "GOOGLE" | "KAKAO" | "NAVER"
     * - providerId: 소셜 플랫폼 ID
     * - email: 소셜 이메일
     * - name: 소셜 이름
     * - exp: 만료 시간 (15분)
     */
    @Column(name = "pending_token", nullable = false, unique = true, length = 500)
    private String pendingToken;

    /**
     * 소셜 제공자
     * JWT와 동일한 정보 (검증용)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Provider provider;

    /**
     * 소셜 플랫폼 고유 ID
     * JWT와 동일한 정보 (검증용)
     */
    @Column(name = "provider_id", nullable = false, length = 100)
    private String providerId;

    /**
     * 소셜 이메일
     * JWT와 동일한 정보 (검증용)
     */
    @Column(nullable = false, length = 100)
    private String email;

    /**
     * 소셜 이름
     * JWT와 동일한 정보 (검증용)
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * 토큰 만료 일시
     * JWT expiration과 동일 (15분)
     */
    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;

    /**
     * 사용 여부
     * 회원가입 완료 시 true (중복 사용 방지)
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean used = false;

    /**
     * 사용 일시
     * 회원가입 완료 시간
     */
    @Column(name = "used_at")
    private LocalDateTime usedAt;

    /**
     * 생성 일시
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 토큰 만료 여부 확인
     *
     * @return 만료되었으면 true
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }

    /**
     * 토큰 사용 처리
     * 회원가입 완료 시 호출
     */
    public void markAsUsed() {
        this.used = true;
        this.usedAt = LocalDateTime.now();
    }
}