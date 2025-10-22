package com.aid.train.backend.domain.verification.entity;

import com.aid.train.backend.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 이메일 인증 엔티티
 * <p>
 * 로컬 회원가입 시 이메일 인증을 위한 토큰과 OTP 코드를 저장합니다.
 * JWT 토큰 + 6자리 OTP 코드의 이중 인증으로 보안을 강화합니다.
 * <p>
 * 보안 전략:
 * - JWT 토큰: 세션 식별 및 요청 검증
 * - 6자리 OTP: 사용자 입력 인증
 * - 이중 검증으로 브루트포스 공격 방지
 * - 사용 후 즉시 삭제 (원타임)
 * - 스케줄러로 만료 토큰 정리
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Entity
@Table(
        name = "email_verifications",
        indexes = {
                @Index(name = "idx_ev_token", columnList = "verification_token"),
                @Index(name = "idx_ev_user", columnList = "user_id"),
                @Index(name = "idx_ev_email", columnList = "email"),
                @Index(name = "idx_ev_email_code", columnList = "email, code"),
                @Index(name = "idx_ev_expiry", columnList = "expiry_date")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class EmailVerification {

    /**
     * 이메일 인증 고유 ID (PK)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 인증 대상 사용자
     * N:1 관계
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 인증 대상 이메일
     */
    @Column(nullable = false, length = 100)
    private String email;

    /**
     * 인증 토큰 (JWT)
     * <p>
     * JWT Claims:
     * - type: "EMAIL_VERIFICATION"
     * - email: 인증할 이메일
     * - userId: 사용자 ID
     * - exp: 만료 시간 (15분)
     */
    @Column(name = "verification_token", nullable = false, unique = true, length = 500)
    private String verificationToken;

    /**
     * 6자리 OTP 인증 코드
     * 이메일로 발송되는 숫자 코드 (예: 123456)
     */
    @Column(nullable = false, length = 6)
    private String code;

    /**
     * 토큰 만료 일시
     * JWT expiration과 동일 (15분)
     */
    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;

    /**
     * 인증 완료 여부
     */
    @Column(name = "is_verified", nullable = false)
    @Builder.Default
    private Boolean isVerified = false;

    /**
     * 인증 완료 일시
     */
    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

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
     * 이메일 인증 완료 처리
     */
    public void verify() {
        this.isVerified = true;
        this.verifiedAt = LocalDateTime.now();
    }

    /**
     * 6자리 OTP 코드 생성
     *
     * @return 6자리 랜덤 숫자 문자열
     */
    public static String generateOtpCode() {
        return String.format("%06d", (int) (Math.random() * 1000000));
    }
}