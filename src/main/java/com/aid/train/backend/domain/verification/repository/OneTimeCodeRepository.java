package com.aid.train.backend.domain.verification.repository;

import com.aid.train.backend.domain.verification.entity.OneTimeCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 일회용 코드 엔티티에 대한 데이터 접근 인터페이스입니다.
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Repository
public interface OneTimeCodeRepository extends JpaRepository<OneTimeCode, Long> {

    /**
     * 코드로 일회용 코드 엔티티를 조회합니다.
     *
     * @param code 조회할 코드
     * @return 일회용 코드 엔티티 (Optional)
     */
    Optional<OneTimeCode> findByCode(String code);

    /**
     * 특정 사용자의 모든 일회용 코드를 삭제합니다.
     * 새로운 코드 생성 전에 기존 코드들을 정리하기 위해 사용됩니다.
     *
     * @param userId 사용자 ID
     */
    void deleteByUserId(String userId);

    /**
     * 만료된 일회용 코드들을 삭제합니다.
     * 스케줄러에 의해 주기적으로 호출되어 DB를 정리합니다.
     *
     * @param now 현재 시간
     * @return 삭제된 코드 수
     */
    @Modifying
    @Query("DELETE FROM OneTimeCode o WHERE o.expiryDate < :now")
    int deleteByExpiryDateBefore(@Param("now") LocalDateTime now);

    /**
     * 만료되지 않은 유효한 코드를 조회합니다.
     * 추가적인 검증이 필요한 경우 사용할 수 있습니다.
     *
     * @param code 조회할 코드
     * @param now  현재 시간
     * @return 유효한 일회용 코드 엔티티 (Optional)
     */
    @Query("SELECT o FROM OneTimeCode o WHERE o.code = :code AND o.expiryDate > :now AND o.used = false")
    Optional<OneTimeCode> findValidCode(@Param("code") String code, @Param("now") LocalDateTime now);
}