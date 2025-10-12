package com.aid.train.backend.domain.user.repository;

import com.aid.train.backend.domain.user.entity.User;
import com.aid.train.backend.domain.user.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * User 엔티티의 Repository 인터페이스입니다.
 * 사용자 조회, 저장, 삭제 등의 데이터 접근 기능을 제공합니다.
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 이메일로 사용자를 조회합니다.
     *
     * @param email 조회할 이메일 주소
     * @return 사용자 Optional
     */
    Optional<User> findByEmail(String email);

    /**
     * 이메일로 사용자 존재 여부를 확인합니다.
     *
     * @param email 확인할 이메일 주소
     * @return 존재하면 true, 아니면 false
     */
    boolean existsByEmail(String email);

    /**
     * 특정 상태의 사용자 목록을 조회합니다.
     *
     * @param status 사용자 상태
     * @return 사용자 목록
     */
    List<User> findByStatus(UserStatus status);

    /**
     * 특정 기간 동안 업데이트되지 않은 활성 사용자를 조회합니다.
     * 휴면 계정 전환용 (1년 미접속)
     *
     * @param status    사용자 상태
     * @param updatedAt 기준 일시
     * @return 사용자 목록
     */
    List<User> findByStatusAndUpdatedAtBefore(UserStatus status, LocalDateTime updatedAt);

    /**
     * 이메일 인증이 완료되지 않은 사용자를 조회합니다.
     *
     * @param emailVerified 이메일 인증 여부
     * @return 사용자 목록
     */
    List<User> findByEmailVerified(Boolean emailVerified);

    /**
     * 특정 기간 이전에 탈퇴한 사용자를 조회합니다.
     * 개인정보 완전 삭제용 (탈퇴 후 30일)
     *
     * @param status    사용자 상태
     * @param deletedAt 기준 일시
     * @return 사용자 목록
     */
    List<User> findByStatusAndDeletedAtBefore(UserStatus status, LocalDateTime deletedAt);
}