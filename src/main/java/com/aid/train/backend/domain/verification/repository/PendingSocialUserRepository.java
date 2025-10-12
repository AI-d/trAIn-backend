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
 * PendingSocialUser 엔티티의 Repository 인터페이스입니다.
 * 소셜 로그인 임시 사용자의 조회, 저장, 삭제 등의 데이터 접근 기능을 제공합니다.
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Repository
public interface PendingSocialUserRepository extends JpaRepository<PendingSocialUser, Long> {

    /**
     * 임시 토큰으로 대기 중인 소셜 사용자를 조회합니다.
     *
     * @param tempToken 임시 토큰
     * @return 임시 소셜 사용자 Optional
     */
    Optional<PendingSocialUser> findByTempToken(String tempToken);

    /**
     * 제공자와 제공자 ID로 대기 중인 소셜 사용자를 조회합니다.
     *
     * @param provider   소셜 제공자
     * @param providerId 제공자의 사용자 고유 ID
     * @return 임시 소셜 사용자 Optional
     */
    Optional<PendingSocialUser> findByProviderAndProviderId(Provider provider, String providerId);

    /**
     * 제공자와 제공자 ID로 대기 중인 소셜 사용자 존재 여부를 확인합니다.
     *
     * @param provider   소셜 제공자
     * @param providerId 제공자의 사용자 고유 ID
     * @return 존재하면 true, 아니면 false
     */
    boolean existsByProviderAndProviderId(Provider provider, String providerId);

    /**
     * 특정 기간 이전에 생성된 임시 사용자를 벌크 삭제합니다.
     * 스케줄러에서 주기적으로 실행 (1시간 경과 데이터 삭제)
     *
     * @param createdAt 기준 생성 일시
     * @return 삭제된 임시 사용자 개수
     * @Modifying을 사용하여 단일 DELETE 쿼리로 실행
     */
    @Modifying
    @Query("DELETE FROM PendingSocialUser psu WHERE psu.createdAt < :createdAt")
    int bulkDeleteOldPendingUsers(@Param("createdAt") LocalDateTime createdAt);

    /**
     * 완료된 임시 사용자를 벌크 삭제합니다.
     *
     * @return 삭제된 임시 사용자 개수
     */
    @Modifying
    @Query("DELETE FROM PendingSocialUser psu WHERE psu.isCompleted = true")
    int bulkDeleteCompletedUsers();

    /**
     * 특정 기간 이전에 생성된 임시 사용자를 삭제합니다.
     * 스케줄러에서 주기적으로 실행 (1시간 경과 데이터 삭제)
     *
     * @param createdAt 기준 생성 일시
     * @return 삭제된 임시 사용자 개수
     * @deprecated bulkDeleteOldPendingUsers() 사용을 권장합니다 (성능 향상)
     */
    @Deprecated
    long deleteByCreatedAtBefore(LocalDateTime createdAt);

    /**
     * 완료된 임시 사용자를 삭제합니다.
     *
     * @param isCompleted 완료 여부
     * @return 삭제된 임시 사용자 개수
     * @deprecated bulkDeleteCompletedUsers() 사용을 권장합니다 (성능 향상)
     */
    @Deprecated
    long deleteByIsCompleted(Boolean isCompleted);

    /**
     * 임시 토큰 존재 여부를 확인합니다.
     *
     * @param tempToken 임시 토큰
     * @return 존재하면 true, 아니면 false
     */
    boolean existsByTempToken(String tempToken);
}