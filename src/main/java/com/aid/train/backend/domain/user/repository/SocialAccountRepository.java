package com.aid.train.backend.domain.user.repository;

import com.aid.train.backend.domain.user.entity.SocialAccount;
import com.aid.train.backend.domain.user.enums.Provider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * SocialAccount 엔티티 Repository
 * 소셜 계정 연동 관리 핵심 기능
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Repository
public interface SocialAccountRepository extends JpaRepository<SocialAccount, Long> {

    // ===== 소셜 로그인 핵심 기능 =====

    /**
     * 소셜 제공자와 제공자 ID로 조회
     * 소셜 로그인 시 기존 계정 확인용
     */
    Optional<SocialAccount> findByProviderAndProviderId(Provider provider, String providerId);

    /**
     * 사용자별 모든 연동된 소셜 계정 조회
     */
    @Query("SELECT sa FROM SocialAccount sa WHERE sa.user.id = :userId AND sa.isConnected = true")
    List<SocialAccount> findConnectedAccountsByUserId(@Param("userId") Long userId);

    /**
     * 사용자별 특정 제공자 소셜 계정 조회
     */
    @Query("SELECT sa FROM SocialAccount sa WHERE sa.user.id = :userId AND sa.provider = :provider AND sa.isConnected = true")
    Optional<SocialAccount> findConnectedAccountByUserAndProvider(@Param("userId") Long userId, @Param("provider") Provider provider);

    // ===== 연동 관리 =====

    /**
     * 특정 제공자 연동 여부 확인
     */
    @Query("SELECT COUNT(sa) > 0 FROM SocialAccount sa WHERE sa.user.id = :userId AND sa.provider = :provider AND sa.isConnected = true")
    boolean hasConnectedProvider(@Param("userId") Long userId, @Param("provider") Provider provider);

    /**
     * 사용자의 연동된 소셜 계정 개수
     */
    @Query("SELECT COUNT(sa) FROM SocialAccount sa WHERE sa.user.id = :userId AND sa.isConnected = true")
    long countConnectedAccountsByUserId(@Param("userId") Long userId);

    // ===== 계정 정리 =====

    /**
     * 사용자의 모든 소셜 계정 연동 해제 (계정 탈퇴시)
     */
    @Modifying
    @Query("UPDATE SocialAccount sa SET sa.isConnected = false WHERE sa.user.id = :userId")
    int disconnectAllAccountsByUserId(@Param("userId") Long userId);

    /**
     * 사용자의 모든 소셜 계정 삭제 (물리 삭제)
     */
    @Modifying
    @Query("DELETE FROM SocialAccount sa WHERE sa.user.id = :userId")
    int deleteByUserId(@Param("userId") Long userId);

    // ===== 존재 여부 확인 =====

    /**
     * 소셜 계정 존재 여부 확인 (연동 상태 무관)
     */
    boolean existsByProviderAndProviderId(Provider provider, String providerId);
}