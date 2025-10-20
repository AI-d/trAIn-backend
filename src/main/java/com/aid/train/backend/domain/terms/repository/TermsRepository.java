package com.aid.train.backend.domain.terms.repository;

import com.aid.train.backend.domain.terms.entity.Terms;
import com.aid.train.backend.domain.terms.enums.TermsType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Terms 엔티티 Repository
 * 약관 관리 핵심 기능만 제공
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Repository
public interface TermsRepository extends JpaRepository<Terms, Long> {

    // ===== 활성 약관 조회 (회원가입/동의 시 사용) =====

    /**
     * 특정 타입의 활성 약관 조회
     */
    Optional<Terms> findByTypeAndIsActive(TermsType type, Boolean isActive);

    /**
     * 모든 활성 약관 조회 (회원가입 시 동의할 약관 목록)
     */
    @Query("SELECT t FROM Terms t WHERE t.isActive = true ORDER BY t.type")
    List<Terms> findActiveTerms();

    /**
     * 필수 약관만 조회 (필수 동의 확인용)
     */
    @Query("SELECT t FROM Terms t WHERE t.isActive = true AND t.isRequired = true ORDER BY t.type")
    List<Terms> findActiveRequiredTerms();

    // ===== 약관 버전 관리 =====

    /**
     * 특정 타입의 특정 버전 약관 조회
     */
    Optional<Terms> findByTypeAndVersion(TermsType type, String version);

    /**
     * 특정 타입의 모든 버전 조회 (최신순)
     */
    @Query("SELECT t FROM Terms t WHERE t.type = :type ORDER BY t.id DESC")
    List<Terms> findAllVersionsByType(@Param("type") TermsType type);

    // ===== 약관 활성화 관리 =====

    /**
     * 특정 타입의 다른 모든 버전 비활성화 (새 버전 활성화 시)
     */
    @Modifying
    @Query("UPDATE Terms t SET t.isActive = false WHERE t.type = :type AND t.id != :excludeId")
    int deactivateOtherVersions(@Param("type") TermsType type, @Param("excludeId") Long excludeId);

    // ===== 존재 여부 확인 =====

    /**
     * 특정 타입의 활성 약관 존재 여부 확인
     */
    @Query("SELECT COUNT(t) > 0 FROM Terms t WHERE t.type = :type AND t.isActive = true")
    boolean hasActiveTerms(@Param("type") TermsType type);

    /**
     * 특정 타입과 버전의 약관 존재 여부 확인 (중복 생성 방지)
     */
    boolean existsByTypeAndVersion(TermsType type, String version);
}