package com.aid.train.backend.domain.terms.repository;

import com.aid.train.backend.domain.terms.entity.Terms;
import com.aid.train.backend.domain.terms.enums.TermsType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Terms 엔티티의 Repository 인터페이스입니다.
 * 약관 본문의 조회, 저장, 삭제 등의 데이터 접근 기능을 제공합니다.
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Repository
public interface TermsRepository extends JpaRepository<Terms, Long> {

    /**
     * 특정 타입의 활성화된 약관을 조회합니다.
     * 회원가입 시 최신 약관 표시용
     *
     * @param type 약관 타입
     * @param isActive 활성화 여부
     * @return 약관 Optional
     */
    Optional<Terms> findByTypeAndIsActive(TermsType type, Boolean isActive);

    /**
     * 특정 타입의 모든 활성화된 약관을 조회합니다.
     *
     * @param type 약관 타입
     * @param isActive 활성화 여부
     * @return 약관 목록
     */
    List<Terms> findAllByTypeAndIsActive(TermsType type, Boolean isActive);

    /**
     * 모든 활성화된 약관을 조회합니다.
     * 회원가입 시 전체 약관 표시용
     *
     * @param isActive 활성화 여부
     * @return 약관 목록
     */
    List<Terms> findByIsActive(Boolean isActive);

    /**
     * 특정 타입과 버전의 약관을 조회합니다.
     *
     * @param type 약관 타입
     * @param version 약관 버전
     * @return 약관 Optional
     */
    Optional<Terms> findByTypeAndVersion(TermsType type, String version);

    /**
     * 특정 타입의 모든 약관을 조회합니다.
     * 약관 이력 조회용
     *
     * @param type 약관 타입
     * @return 약관 목록
     */
    List<Terms> findByType(TermsType type);

    /**
     * 특정 타입과 버전의 약관 존재 여부를 확인합니다.
     *
     * @param type 약관 타입
     * @param version 약관 버전
     * @return 존재하면 true, 아니면 false
     */
    boolean existsByTypeAndVersion(TermsType type, String version);
}
