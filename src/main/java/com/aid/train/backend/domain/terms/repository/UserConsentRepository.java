package com.aid.train.backend.domain.terms.repository;

import com.aid.train.backend.domain.terms.entity.UserConsent;
import com.aid.train.backend.domain.terms.enums.TermsType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * UserConsent 엔티티 Repository
 * 사용자 약관 동의 관리 핵심 기능만 제공
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Repository
public interface UserConsentRepository extends JpaRepository<UserConsent, Long> {

    // ===== 동의 현황 조회 =====

    /**
     * 사용자별 모든 동의 이력 조회
     */
    List<UserConsent> findByUserId(Long userId);

    /**
     * 사용자별 특정 약관 동의 조회
     */
    Optional<UserConsent> findByUserIdAndTermsId(Long userId, Long termsId);

    /**
     * 사용자의 현재 유효한 동의 조회 (동의 상태인 것만)
     */
    @Query("SELECT uc FROM UserConsent uc WHERE uc.user.id = :userId AND uc.isAgreed = true")
    List<UserConsent> findCurrentAgreedConsents(@Param("userId") Long userId);

    /**
     * 사용자별 특정 약관 타입의 최신 동의 조회
     */
    @Query("SELECT uc FROM UserConsent uc JOIN uc.terms t WHERE uc.user.id = :userId AND t.type = :termsType " +
            "ORDER BY uc.consentedAt DESC LIMIT 1")
    Optional<UserConsent> findLatestConsentByUserAndType(@Param("userId") Long userId,
                                                         @Param("termsType") TermsType termsType);

    // ===== 필수 약관 동의 확인 =====

    /**
     * 필수 약관 모두 동의했는지 확인
     */
    @Query("SELECT COUNT(DISTINCT t.type) = " +
            "(SELECT COUNT(DISTINCT t2.type) FROM Terms t2 WHERE t2.isActive = true AND t2.isRequired = true) " +
            "FROM UserConsent uc JOIN uc.terms t WHERE uc.user.id = :userId AND uc.isAgreed = true " +
            "AND t.isActive = true AND t.isRequired = true")
    boolean hasAllRequiredConsents(@Param("userId") Long userId);

    // ===== 마케팅 동의 관리 =====

    /**
     * 마케팅 수신 동의한 사용자 ID 목록 조회
     */
    @Query("SELECT DISTINCT uc.user.id FROM UserConsent uc JOIN uc.terms t " +
            "WHERE t.type = 'MARKETING_CONSENT' AND uc.isAgreed = true " +
            "AND uc.id = (SELECT MAX(uc2.id) FROM UserConsent uc2 JOIN uc2.terms t2 " +
            "WHERE uc2.user.id = uc.user.id AND t2.type = 'MARKETING_CONSENT')")
    List<Long> findUsersWithMarketingConsent();

    // ===== 동의 정리 =====

    /**
     * 사용자의 모든 동의 정보 삭제 (계정 탈퇴시)
     */
    @Modifying
    @Query("DELETE FROM UserConsent uc WHERE uc.user.id = :userId")
    int deleteByUserId(@Param("userId") Long userId);

    /**
     * 오래된 동의 이력 삭제 (법적 보관 기간 경과 후)
     */
    @Modifying
    @Query("DELETE FROM UserConsent uc WHERE uc.consentedAt <= :before")
    int deleteOldConsents(@Param("before") LocalDateTime before);

    // ===== 존재 여부 확인 =====

    /**
     * 사용자의 특정 약관 동의 여부 확인
     */
    boolean existsByUserIdAndTermsId(Long userId, Long termsId);
}