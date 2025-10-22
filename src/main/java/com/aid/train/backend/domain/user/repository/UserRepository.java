package com.aid.train.backend.domain.user.repository;

import com.aid.train.backend.domain.user.entity.User;
import com.aid.train.backend.domain.user.enums.Provider;
import com.aid.train.backend.domain.user.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * User 엔티티 Repository
 * 핵심 사용자 관리 기능만 제공
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // ===== 로그인/회원가입 핵심 기능 =====

    /**
     * 이메일과 제공자로 사용자 조회
     * 로그인, 중복 체크용
     */
    Optional<User> findByEmailAndPrimaryProvider(String email, Provider primaryProvider);

    /**
     * 활성 사용자 조회 (탈퇴하지 않은 사용자)
     */
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.primaryProvider = :primaryProvider AND u.deletedAt IS NULL")
    Optional<User> findActiveUser(@Param("email") String email, @Param("primaryProvider") Provider primaryProvider);

    /**
     * 로그인 가능한 사용자 조회 (ACTIVE, INACTIVE만)
     */
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.primaryProvider = :primaryProvider " +
            "AND u.status IN ('ACTIVE', 'INACTIVE') AND u.deletedAt IS NULL")
    Optional<User> findLoginableUser(@Param("email") String email, @Param("primaryProvider") Provider primaryProvider);

    // ===== 이메일 인증 관련 =====

    /**
     * 미인증 로컬 사용자 조회 (이메일 인증 대상)
     */
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.primaryProvider = 'LOCAL' " +
            "AND u.emailVerified = false AND u.deletedAt IS NULL")
    Optional<User> findUnverifiedLocalUser(@Param("email") String email);

    // ===== 중복 체크 =====

    /**
     * 이메일 중복 체크 (제공자별)
     */
    boolean existsByEmailAndPrimaryProvider(String email, Provider primaryProvider);

    // ===== 스케줄러용 (휴면 전환) =====

    /**
     * 1년 미접속 사용자를 휴면 상태로 전환합니다. (벌크 업데이트)
     */
    @Modifying
    @Query("UPDATE User u SET u.status = 'INACTIVE' WHERE u.lastLoginAt < :threshold " +
            "AND u.status = 'ACTIVE' AND u.deletedAt IS NULL")
    int convertToInactiveStatus(@Param("threshold") LocalDateTime threshold);

    /**
     * 탈퇴 후 일정 기간이 지난 사용자를 DB에서 완전히 삭제합니다. (벌크 삭제)
     */
    @Modifying
    @Query("DELETE FROM User u WHERE u.status = :status AND u.deletedAt < :threshold")
    int deleteWithdrawnUsersBefore(@Param("status") UserStatus status, @Param("threshold") LocalDateTime threshold);
}