package com.aid.train.backend.domain.scenario.repository;

import com.aid.train.backend.domain.scenario.entity.Scenario;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScenarioRepository extends JpaRepository<Scenario, Long> {

    /**
     *
     * 전체 시나리오 목록을 조회합니다.
     * @return List<Scenario>
     */
    @Query("SELECT s FROM Scenario s WHERE s.status <> 'DELETED'")
    List<Scenario> findAll();

    /**
     * 단일 시나리오를 조회합니다.
     * @return Optional<Scenario>
     */
    @Query("SELECT s FROM Scenario s WHERE s.status <> 'DELETED' AND s.id =:scenarioId")
    Optional<Scenario> findById(@Param("scenarioId") Long scenarioId);

    /**
     * 사용자가 생성한 전체 시나리오 목록을 조회합니다.
     * @param id
     * @return List<Scenario>
     */
    @Query("SELECT s FROM Scenario s JOIN FETCH s.owner WHERE s.status <> 'DELETED' AND s.owner.id = :ownerId")
    List<Scenario> findAllByUserId(@Param("ownerId") Long id);

    /**
     * 기본 시나리오 목록을 조회합니다.
     * @return List<Scenario>
     */
    @Query("SELECT s FROM Scenario s WHERE s.status <> 'DELETED' AND s.isDefault = true")
    List<Scenario> findDefaultAll();

    /**
     * 사용자가 생성한 시나리오를 삭제합니다.
     * @param userId userId
     * @param scenarioId scenarioId
     * @return int - 1이면 삭제, 0 이면 실패, 성공 여부를 반환
     */
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(" UPDATE Scenario s SET s.status = 'DELETED' WHERE s.owner.id = :userId AND s.id = :scenarioId AND s.isDefault = false")
    int deleteByAndUserId(@Param("userId") Long userId, @Param("scenarioId") Long scenarioId);

    /**
     * 사용자가 생성한 시나리오를 조회합니다.
     * @param userId
     * @param scenarioId
     * @return Optional<Scenario>
     */
    @Query("""
        SELECT s 
        FROM Scenario s 
        JOIN FETCH s.owner 
        WHERE s.status <> 'DELETED' 
        AND s.owner.id = :userId 
        AND s.id = :scenarioId
        AND s.isDefault = false
    """)
    Optional<Scenario> findByAndUserId(@Param("userId") Long userId, @Param("scenarioId") Long scenarioId);

    /**
     * 기본 시나리오의 존재 여부를 확인합니다.
     * @return boolean - 있으면 true, 없으면 false
     */
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Scenario s WHERE s.isDefault = true")
    boolean existDefaultScenario();
}
