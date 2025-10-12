package com.aid.train.backend.domain.user.repository;

import com.aid.train.backend.domain.user.entity.RefreshToken;
import com.aid.train.backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * RefreshToken 엔티티의 Repository 인터페이스입니다.
 * JWT 리프레시 토큰의 조회, 저장, 삭제 등의 데이터 접근 기능을 제공합니다.
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /**
     * 토큰 문자열로 리프레시 토큰을 조회합니다.
     *
     * @param token 토큰 문자열
     * @return 리프레시 토큰 Optional
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * 특정 사용자의 모든 리프레시 토큰을 조회합니다.
     * 최신 생성순으로 정렬
     *
     * @param user 사용자
     * @return 리프레시 토큰 목록
     */
    List<RefreshToken> findByUserOrderByCreatedAtDesc(User user);

    /**
     * 특정 사용자의 활성화된 리프레시 토큰만 조회합니다.
     * 최신 생성순으로 정렬
     *
     * @param user    사용자
     * @param revoked 폐기 여부
     * @return 리프레시 토큰 목록
     */
    List<RefreshToken> findByUserAndRevokedOrderByCreatedAtDesc(User user, Boolean revoked);

    /**
     * 특정 사용자의 만료 임박 토큰을 조회합니다.
     * 만료 일시가 빠른 순으로 정렬
     *
     * @param user 사용자
     * @return 리프레시 토큰 목록
     */
    List<RefreshToken> findByUserOrderByExpiryDateAsc(User user);

    /**
     * 특정 사용자의 특정 디바이스 리프레시 토큰을 조회합니다.
     *
     * @param user     사용자
     * @param deviceId 디바이스 ID
     * @return 리프레시 토큰 Optional
     */
    Optional<RefreshToken> findByUserAndDeviceId(User user, String deviceId);

    /**
     * 특정 사용자의 모든 리프레시 토큰을 삭제합니다.
     * 로그아웃 또는 회원 탈퇴 시 사용
     *
     * @param user 사용자
     */
    void deleteByUser(User user);

    /**
     * 특정 사용자의 폐기된 토큰을 삭제합니다.
     * 토큰 정리용
     *
     * @param user    사용자
     * @param revoked 폐기 여부
     * @return 삭제된 토큰 개수
     */
    long deleteByUserAndRevoked(User user, Boolean revoked);

    /**
     * 만료된 리프레시 토큰을 벌크 삭제합니다.
     * 스케줄러에서 주기적으로 실행 (만료 후 14일 지난 토큰)
     *
     * @param now 기준 일시
     * @return 삭제된 토큰 개수
     * @Modifying을 사용하여 단일 DELETE 쿼리로 실행
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiryDate < :now")
    int bulkDeleteExpiredTokens(@Param("now") LocalDateTime now);

    /**
     * 폐기되었거나 만료된 토큰을 벌크 삭제합니다.
     * 스케줄러에서 주기적으로 실행
     *
     * @param now 기준 일시
     * @return 삭제된 토큰 개수
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.revoked = true OR rt.expiryDate < :now")
    int bulkDeleteInvalidTokens(@Param("now") LocalDateTime now);

    /**
     * 만료된 리프레시 토큰을 삭제합니다.
     * 스케줄러에서 주기적으로 실행 (만료 후 14일 지난 토큰)
     *
     * @param expiryDate 기준 만료 일시
     * @return 삭제된 토큰 개수
     * @deprecated bulkDeleteExpiredTokens() 사용을 권장합니다 (성능 향상)
     */
    @Deprecated
    long deleteByExpiryDateBefore(LocalDateTime expiryDate);

    /**
     * 토큰 존재 여부를 확인합니다.
     *
     * @param token 토큰 문자열
     * @return 존재하면 true, 아니면 false
     */
    boolean existsByToken(String token);
}