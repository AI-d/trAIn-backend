package com.aid.train.backend.domain.verification.repository;

import com.aid.train.backend.domain.user.enums.Provider;
import com.aid.train.backend.domain.verification.entity.PendingSocialUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * PendingSocialUser 엔티티 Repository
 * 소셜 회원가입 완료 대기 관리 핵심 기능
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Repository
public interface PendingSocialUserRepository extends JpaRepository<PendingSocialUser, Long> {

    // ===== 소셜 회원가입 완료 핵심 기능 =====

    /**
     * 대기 토큰으로 조회
     */
    Optional<PendingSocialUser> findByPendingToken(String pendingToken);

    /**
     * 소셜 제공자와 제공자 ID로 조회 (중복 방지)
     */
    Optional<PendingSocialUser> findByProviderAndProviderId(Provider provider, String providerId);

    /**
     * 유효한 미사용 토큰 조회 (만료되지 않고 미사용)
     */
    @Query("SELECT psu FROM PendingSocialUser psu WHERE psu.pendingToken = :token " +
            "AND psu.expiryDate > :now AND psu.used = false")
    Optional<PendingSocialUser> findValidToken(@Param("token") String token, @Param("now") LocalDateTime now);

    // ===== 중복 방지 =====

    /**
     * 소셜 계정 대기 토큰 존재 여부 확인
     */
    boolean existsByProviderAndProviderId(Provider provider, String providerId);

    /**
     * 유효한 미사용 토큰 존재 여부 확인 (중복 방지)
     */
    @Query("SELECT COUNT(psu) > 0 FROM PendingSocialUser psu WHERE psu.provider = :provider " +
            "AND psu.providerId = :providerId AND psu.expiryDate > :now AND psu.used = false")
    boolean hasValidUnusedToken(@Param("provider") Provider provider, @Param("providerId") String providerId,
                                @Param("now") LocalDateTime now);

    // ===== 토큰 정리 =====

    /**
     * 특정 시간 이전에 만료되었거나, 이미 사용된 임시 소셜 사용자 정보를 모두 삭제합니다. (벌크 삭제)
     */
    @Modifying
    @Query("DELETE FROM PendingSocialUser psu WHERE psu.expiryDate < :threshold OR psu.used = true")
    int deleteExpiredOrUsedTokens(@Param("threshold") LocalDateTime threshold);
    
    // ===== 재요청 제한 =====

    /**
     * 최근 N분 내 요청 토큰 개수 (스팸 방지)
     */
    @Query("SELECT COUNT(psu) FROM PendingSocialUser psu WHERE psu.provider = :provider " +
            "AND psu.providerId = :providerId AND psu.createdAt > :after")
    long countRecentTokens(@Param("provider") Provider provider, @Param("providerId") String providerId,
                           @Param("after") LocalDateTime after);
}