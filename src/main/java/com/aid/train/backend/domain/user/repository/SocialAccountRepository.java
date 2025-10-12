package com.aid.train.backend.domain.user.repository;

import com.aid.train.backend.domain.user.entity.SocialAccount;
import com.aid.train.backend.domain.user.entity.User;
import com.aid.train.backend.domain.user.enums.Provider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * SocialAccount 엔티티의 Repository 인터페이스입니다.
 * 소셜 계정 연동 정보의 조회, 저장, 삭제 등의 데이터 접근 기능을 제공합니다.
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Repository
public interface SocialAccountRepository extends JpaRepository<SocialAccount, Long> {

    /**
     * 제공자와 제공자 ID로 소셜 계정을 조회합니다.
     * 소셜 로그인 시 기존 연동 확인용
     *
     * @param provider   소셜 제공자
     * @param providerId 제공자의 사용자 고유 ID
     * @return 소셜 계정 Optional
     */
    Optional<SocialAccount> findByProviderAndProviderId(Provider provider, String providerId);

    /**
     * 특정 사용자의 모든 소셜 계정을 조회합니다.
     * 최근 로그인순으로 정렬
     *
     * @param user 사용자
     * @return 소셜 계정 목록
     */
    List<SocialAccount> findByUserOrderByLastLoginAtDesc(User user);

    /**
     * 특정 사용자의 연동된 소셜 계정만 조회합니다.
     * 최근 로그인순으로 정렬
     *
     * @param user        사용자
     * @param isConnected 연동 상태
     * @return 소셜 계정 목록
     */
    List<SocialAccount> findByUserAndIsConnectedOrderByLastLoginAtDesc(User user, Boolean isConnected);

    /**
     * 특정 사용자의 특정 제공자 소셜 계정을 조회합니다.
     *
     * @param user     사용자
     * @param provider 소셜 제공자
     * @return 소셜 계정 Optional
     */
    Optional<SocialAccount> findByUserAndProvider(User user, Provider provider);

    /**
     * 제공자와 제공자 ID로 소셜 계정 존재 여부를 확인합니다.
     *
     * @param provider   소셜 제공자
     * @param providerId 제공자의 사용자 고유 ID
     * @return 존재하면 true, 아니면 false
     */
    boolean existsByProviderAndProviderId(Provider provider, String providerId);

    /**
     * 특정 사용자의 모든 소셜 계정을 삭제합니다.
     * 회원 탈퇴 시 사용
     *
     * @param user 사용자
     */
    void deleteByUser(User user);
}