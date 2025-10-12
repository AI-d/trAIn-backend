package com.aid.train.backend.domain.verification.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 이메일 인증 OTP 엔티티 클래스입니다.
 * 로컬 회원가입 시 이메일 인증을 위한 6자리 OTP 코드를 저장하고 관리합니다.
 *
 * <p>
 * 주요 기능:
 * <ul>
 *   <li>6자리 숫자 OTP 코드 발급 및 저장</li>
 *   <li>OTP 만료 시간 관리 (발급 후 10분)</li>
 *   <li>재전송 횟수 제한 (최대 3회)</li>
 *   <li>인증 완료 상태 관리</li>
 * </ul>
 * </p>
 *
 * <p>
 * 인증 방식:
 * 6자리 숫자 OTP 코드를 생성하여 이메일로 전송합니다.
 * 사용자가 코드를 입력하면 검증하고 인증을 완료합니다.
 * URL에 민감 정보를 포함하지 않아 보안이 강화됩니다.
 * </p>
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Entity
@Table(name = "email_verifications")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class EmailVerification {

    /**
     * 이메일 인증 고유 ID (자동 생성)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 인증할 이메일 주소
     */
    @Column(nullable = false, length = 100)
    private String email;

    /**
     * 인증 코드 (6자리 숫자)
     * 이메일로 전송되어 사용자가 직접 입력
     */
    @Column(nullable = false, length = 6)
    private String code;

    /**
     * 토큰 만료 일시
     * 기본값: 생성 시점 + 10분
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
     * 토큰 생성 일시
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * 재전송 횟수
     * 스팸 방지를 위해 최대 3회로 제한
     */
    @Column(name = "resend_count", nullable = false)
    @Builder.Default
    private Integer resendCount = 0;

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
     * 토큰이 만료되었는지 확인합니다.
     *
     * @return 만료되었으면 true, 아니면 false
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }

    /**
     * 인증을 완료 처리합니다.
     */
    public void verify() {
        this.isVerified = true;
        this.verifiedAt = LocalDateTime.now();
    }

    /**
     * 재전송이 가능한지 확인합니다.
     * 최대 3회까지만 재전송 가능
     *
     * @return 재전송 가능하면 true, 아니면 false
     */
    public boolean canResend() {
        return this.resendCount < 3;
    }

    /**
     * 재전송 횟수를 증가시킵니다.
     *
     * @throws IllegalStateException 재전송 한도를 초과한 경우
     */
    public void incrementResendCount() {
        if (!canResend()) {
            throw new IllegalStateException("재전송 한도를 초과했습니다.");
        }
        this.resendCount++;
    }

    /**
     * 인증 코드와 만료 시간을 갱신합니다.
     * 재전송 시 사용
     *
     * @param newCode 새로운 6자리 인증 코드
     */
    public void refreshCode(String newCode) {
        this.code = newCode;
        this.expiryDate = LocalDateTime.now().plusMinutes(10);
        incrementResendCount();
    }

    /**
     * 인증 코드가 일치하는지 확인합니다.
     *
     * @param code 확인할 인증 코드
     * @return 일치하면 true, 아니면 false
     */
    public boolean matchesCode(String code) {
        return this.code.equals(code);
    }
}