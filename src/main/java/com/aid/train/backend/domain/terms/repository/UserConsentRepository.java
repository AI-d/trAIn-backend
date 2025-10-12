package com.aid.train.backend.domain.terms.repository;

import com.aid.train.backend.domain.terms.entity.Terms;
import com.aid.train.backend.domain.terms.entity.UserConsent;
import com.aid.train.backend.domain.terms.enums.TermsType;
import com.aid.train.backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * UserConsent 엔티티의 Repository 인터페이스입니다.
 * 사용자 약관 동의 이력의 조회, 저장, 삭제 등의 데이터 접근 기능을 제공합니다.
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Repository
public interface UserConsentRepository extends JpaRepository<UserConsent, Long> {

    /**
     * 약관별 동의 통계 프로젝션 인터페이스입니다.
     * 약관 타입별 동의/철회 현황을 집계합니다.
     */
    interface ConsentStatistics {
        /**
         * 약관 타입을 반환합니다.
         *
         * @return 약관 타입
         */
        TermsType getTermsType();

        /**
         * 총 동의 횟수를 반환합니다.
         *
         * @return 총 동의 횟수
         */
        Long getTotalCount();

        /**
         * 철회된 동의 횟수를 반환합니다.
         *
         * @return 철회 횟수
         */
        Long getRevokedCount();

        /**
         * 현재 유효한 동의 횟수를 반환합니다.
         *
         * @return 유효 동의 횟수
         */
        Long getActiveCount();
    }

    /**
     * 특정 사용자의 특정 약관 동의 내역을 조회합니다.
     *
     * @param user  사용자
     * @param terms 약관
     * @return 약관 동의 Optional
     */
    Optional<UserConsent> findByUserAndTerms(User user, Terms terms);

    /**
     * 특정 사용자의 모든 약관 동의 내역을 조회합니다.
     *
     * @param user 사용자
     * @return 약관 동의 목록
     */
    List<UserConsent> findByUser(User user);

    /**
     * 특정 사용자의 동의한 약관 목록만 조회합니다.
     *
     * @param user     사용자
     * @param isAgreed 동의 여부
     * @return 약관 동의 목록
     */
    List<UserConsent> findByUserAndIsAgreed(User user, Boolean isAgreed);

    /**
     * 특정 약관에 동의한 모든 사용자를 조회합니다.
     * 약관 변경 시 재동의 알림용
     *
     * @param terms    약관
     * @param isAgreed 동의 여부
     * @return 약관 동의 목록
     */
    List<UserConsent> findByTermsAndIsAgreed(Terms terms, Boolean isAgreed);

    /**
     * 특정 사용자가 특정 약관에 동의했는지 확인합니다.
     *
     * @param user     사용자
     * @param terms    약관
     * @param isAgreed 동의 여부
     * @return 동의했으면 true, 아니면 false
     */
    boolean existsByUserAndTermsAndIsAgreed(User user, Terms terms, Boolean isAgreed);

    /**
     * 약관 타입별 동의 통계를 조회합니다.
     * 프로젝션을 사용하여 집계 데이터만 효율적으로 조회합니다.
     *
     * @param types 조회할 약관 타입 목록
     * @return 약관별 통계 목록
     */
    @Query("""
                SELECT 
                    uc.terms.type as termsType,
                    COUNT(uc) as totalCount,
                    SUM(CASE WHEN uc.revokedAt IS NOT NULL THEN 1 ELSE 0 END) as revokedCount,
                    SUM(CASE WHEN uc.isAgreed = true AND uc.revokedAt IS NULL THEN 1 ELSE 0 END) as activeCount
                FROM UserConsent uc
                WHERE uc.terms.type IN :types
                GROUP BY uc.terms.type
            """)
    List<ConsentStatistics> getConsentStatisticsByTypes(@Param("types") List<TermsType> types);

    /**
     * 모든 약관 타입의 동의 통계를 조회합니다.
     *
     * @return 약관별 통계 목록
     */
    @Query("""
                SELECT 
                    uc.terms.type as termsType,
                    COUNT(uc) as totalCount,
                    SUM(CASE WHEN uc.revokedAt IS NOT NULL THEN 1 ELSE 0 END) as revokedCount,
                    SUM(CASE WHEN uc.isAgreed = true AND uc.revokedAt IS NULL THEN 1 ELSE 0 END) as activeCount
                FROM UserConsent uc
                GROUP BY uc.terms.type
            """)
    List<ConsentStatistics> getAllConsentStatistics();

    /**
     * 특정 사용자의 모든 약관 동의 내역을 삭제합니다.
     * 회원 탈퇴 시 사용
     *
     * @param user 사용자
     */
    void deleteByUser(User user);
}