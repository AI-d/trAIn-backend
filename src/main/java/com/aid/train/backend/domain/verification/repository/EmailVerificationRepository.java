package com.aid.train.backend.domain.verification.repository;

import com.aid.train.backend.domain.verification.entity.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * EmailVerification 엔티티의 Repository 인터페이스입니다.
 * 이메일 인증 OTP의 조회, 저장, 삭제 등의 데이터 접근 기능을 제공합니다.
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Repository
public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {

    /**
     * 이메일과 인증 코드로 인증 정보를 조회합니다.
     *
     * @param email 이메일 주소
     * @param code  6자리 인증 코드
     * @return 이메일 인증 Optional
     */
    Optional<EmailVerification> findByEmailAndCode(String email, String code);

    /**
     * 특정 이메일의 가장 최근 인증 정보를 조회합니다.
     *
     * @param email 이메일 주소
     * @return 이메일 인증 Optional
     */
    Optional<EmailVerification> findTopByEmailOrderByCreatedAtDesc(String email);

    /**
     * 특정 이메일의 모든 인증 정보를 조회합니다.
     *
     * @param email 이메일 주소
     * @return 이메일 인증 목록
     */
    List<EmailVerification> findByEmail(String email);

    /**
     * 특정 이메일의 인증되지 않은 정보를 조회합니다.
     *
     * @param email      이메일 주소
     * @param isVerified 인증 여부
     * @return 이메일 인증 목록
     */
    List<EmailVerification> findByEmailAndIsVerified(String email, Boolean isVerified);

    /**
     * 특정 기간 이전에 생성된 인증 정보를 벌크 삭제합니다.
     * 스케줄러에서 주기적으로 실행 (1시간 경과 데이터 삭제)
     *
     * @param createdAt 기준 생성 일시
     * @return 삭제된 인증 정보 개수
     * @Modifying을 사용하여 단일 DELETE 쿼리로 실행
     */
    @Modifying
    @Query("DELETE FROM EmailVerification ev WHERE ev.createdAt < :createdAt")
    int bulkDeleteOldVerifications(@Param("createdAt") LocalDateTime createdAt);

    /**
     * 특정 기간 이전에 생성된 인증 정보를 삭제합니다.
     * 스케줄러에서 주기적으로 실행 (1시간 경과 데이터 삭제)
     *
     * @param createdAt 기준 생성 일시
     * @return 삭제된 인증 정보 개수
     * @deprecated bulkDeleteOldVerifications() 사용을 권장합니다 (성능 향상)
     */
    @Deprecated
    long deleteByCreatedAtBefore(LocalDateTime createdAt);

    /**
     * 특정 이메일의 인증 정보를 모두 삭제합니다.
     *
     * @param email 이메일 주소
     */
    void deleteByEmail(String email);
}