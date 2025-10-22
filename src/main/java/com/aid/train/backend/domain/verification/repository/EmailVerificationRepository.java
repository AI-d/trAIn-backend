package com.aid.train.backend.domain.verification.repository;

import com.aid.train.backend.domain.verification.entity.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * EmailVerification 엔티티 Repository
 * 이메일 인증 핵심 기능만 제공
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Repository
public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {

    // ===== 이메일 인증 핵심 기능 =====

    /**
     * 인증 토큰으로 조회
     */
    Optional<EmailVerification> findByVerificationToken(String verificationToken);

    /**
     * 이메일과 OTP 코드로 조회 (이중 인증)
     */
    Optional<EmailVerification> findByEmailAndCode(String email, String code);

    /**
     * 사용자의 가장 최근 미인증 토큰 조회
     */
    @Query("SELECT ev FROM EmailVerification ev WHERE ev.user.id = :userId AND ev.email = :email " +
            "AND ev.isVerified = false ORDER BY ev.createdAt DESC LIMIT 1")
    Optional<EmailVerification> findLatestUnverifiedByUserAndEmail(@Param("userId") Long userId, @Param("email") String email);

    /**
     * 유효한 미인증 토큰 존재 여부 확인 (중복 발송 방지)
     */
    @Query("SELECT COUNT(ev) > 0 FROM EmailVerification ev WHERE ev.user.id = :userId AND ev.email = :email " +
            "AND ev.expiryDate > :now AND ev.isVerified = false")
    boolean hasValidUnverifiedToken(@Param("userId") Long userId, @Param("email") String email, @Param("now") LocalDateTime now);

    // ===== 인증 정리 =====

    /**
     * 사용자의 모든 이메일 인증 토큰 삭제 (계정 탈퇴시)
     */
    @Modifying
    @Query("DELETE FROM EmailVerification ev WHERE ev.user.id = :userId")
    int deleteByUserId(@Param("userId") Long userId);

    /**
     * 특정 시간 이전에 만료된 토큰을 모두 삭제합니다. (벌크 삭제)
     */
    @Modifying
    @Query("DELETE FROM EmailVerification ev WHERE ev.expiryDate < :threshold")
    int deleteByExpiryDateBefore(@Param("threshold") LocalDateTime threshold);

    /**
     * 인증 완료된 토큰 삭제 (원타임 토큰 정책)
     */
    @Modifying
    @Query("DELETE FROM EmailVerification ev WHERE ev.isVerified = true")
    int deleteVerifiedTokens();

    // ===== 재발송 제한 =====

    /**
     * 최근 N분 내 발송 토큰 개수 (재발송 제한용)
     */
    @Query("SELECT COUNT(ev) FROM EmailVerification ev WHERE ev.user.id = :userId AND ev.email = :email " +
            "AND ev.createdAt > :after")
    long countRecentTokens(@Param("userId") Long userId, @Param("email") String email, @Param("after") LocalDateTime after);
}