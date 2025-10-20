package com.aid.train.backend.domain.user.repository;

import com.aid.train.backend.domain.user.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * RefreshToken 엔티티 Repository
 * JWT 리프레시 토큰 관리 핵심 기능
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    // ===== 토큰 관리 핵심 기능 =====

    /**
     * 토큰 값으로 조회
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * 사용자별 모든 토큰 조회 (멀티 디바이스)
     */
    List<RefreshToken> findByUserId(Long userId);

    /**
     * 유효한 토큰 조회 (만료되지 않은 토큰)
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.token = :token AND rt.expiryDate > :now")
    Optional<RefreshToken> findValidToken(@Param("token") String token, @Param("now") LocalDateTime now);

    // ===== 토큰 삭제 =====

    /**
     * 특정 토큰 삭제 (로그아웃)
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.token = :token")
    int deleteByToken(@Param("token") String token);

    /**
     * 사용자의 모든 토큰 삭제 (전체 로그아웃, 계정 탈퇴)
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.user.id = :userId")
    int deleteByUserId(@Param("userId") Long userId);

    /**
     * 특정 시간 이전에 만료된 토큰을 모두 삭제합니다. (벌크 삭제)
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiryDate < :threshold")
    int deleteByExpiryDateBefore(@Param("threshold") LocalDateTime threshold);

    // ===== 멀티 디바이스 관리 =====

    /**
     * 사용자별 토큰 개수 조회
     */
    long countByUserId(Long userId);

    /**
     * 사용자별 오래된 토큰 삭제 (디바이스 제한용)
     * 최신 N개만 유지하고 나머지 삭제
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.user.id = :userId " +
            "AND rt.id NOT IN (SELECT rt2.id FROM RefreshToken rt2 WHERE rt2.user.id = :userId " +
            "ORDER BY rt2.createdAt DESC LIMIT :keepCount)")
    int deleteOldTokensKeepRecent(@Param("userId") Long userId, @Param("keepCount") int keepCount);
}