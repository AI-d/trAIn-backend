package com.aid.train.backend.domain.feedback.repository;

import com.aid.train.backend.domain.feedback.entity.Feedback;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Feedback Repository
 *
 * 피드백 데이터에 접근하기 위한 Repository 인터페이스
 * 옵션 2 (단순 조회 모델) 기반으로 구현됨
 *
 * @author 왕택준
 * @since 1.0.0
 */
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    /**
     * 특정 세션의 피드백을 조회합니다.
     *
     * @param sessionId 대화 세션 ID
     * @return 피드백 (없으면 Optional.empty())
     */
    @Query("SELECT f FROM Feedback f " +
            "JOIN FETCH f.dialogueSession s " +
            "WHERE s.sessionId = :sessionId")
    Optional<Feedback> findByDialogueSessionSessionId(@Param("sessionId") String sessionId);

    /**
     * 특정 세션의 피드백 존재 여부를 확인합니다.
     *
     * @param sessionId 대화 세션 ID
     * @return 피드백이 존재하면 true
     */
    @Query("SELECT COUNT(f) > 0 FROM Feedback f " +
            "JOIN f.dialogueSession s " +
            "WHERE s.sessionId = :sessionId")
    boolean existsByDialogueSessionSessionId(@Param("sessionId") String sessionId);

    /**
     * 특정 사용자의 모든 피드백을 조회합니다 (최신순).
     * 히스토리 조회에 사용
     *
     * @param userId 사용자 ID
     * @return 피드백 목록 (최신순)
     */
    @Query("SELECT f FROM Feedback f " +
            "JOIN FETCH f.dialogueSession s " +
            "JOIN FETCH s.scenario sc " +
            "WHERE s.user.id = :userId " +
            "ORDER BY f.createdAt DESC")
    List<Feedback> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);

    /**
     * 특정 사용자의 피드백을 페이징하여 조회합니다.
     *
     * @param userId 사용자 ID
     * @param pageable 페이징 정보
     * @return 페이징된 피드백 목록
     */
    @Query("SELECT f FROM Feedback f " +
            "JOIN FETCH f.dialogueSession s " +
            "JOIN FETCH s.scenario sc " +
            "WHERE s.user.id = :userId")
    Page<Feedback> findByUserId(@Param("userId") Long userId, Pageable pageable);

    /**
     * 특정 사용자의 피드백 개수를 조회합니다.
     * 구독제 제한 로직에 사용 (향후 구현)
     *
     * @param userId 사용자 ID
     * @return 피드백 개수
     */
    @Query("SELECT COUNT(f) FROM Feedback f " +
            "JOIN f.dialogueSession s " +
            "WHERE s.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);

    /**
     * 특정 사용자의 특정 시나리오 피드백을 조회합니다.
     *
     * @param userId 사용자 ID
     * @param scenarioId 시나리오 ID
     * @return 피드백 목록 (최신순)
     */
    @Query("SELECT f FROM Feedback f " +
            "JOIN FETCH f.dialogueSession s " +
            "JOIN FETCH s.scenario sc " +
            "WHERE s.user.id = :userId " +
            "AND s.scenario.id = :scenarioId " +
            "ORDER BY f.createdAt DESC")
    List<Feedback> findByUserIdAndScenarioId(@Param("userId") Long userId,
                                             @Param("scenarioId") Long scenarioId);

    /**
     * 특정 기간 내 생성된 피드백을 조회합니다.
     *
     * @param userId 사용자 ID
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @return 피드백 목록
     */
    @Query("SELECT f FROM Feedback f " +
            "JOIN FETCH f.dialogueSession s " +
            "WHERE s.user.id = :userId " +
            "AND f.createdAt BETWEEN :startDate AND :endDate " +
            "ORDER BY f.createdAt DESC")
    List<Feedback> findByUserIdAndDateRange(@Param("userId") Long userId,
                                            @Param("startDate") LocalDateTime startDate,
                                            @Param("endDate") LocalDateTime endDate);

    /**
     * 특정 사용자의 평균 점수를 계산합니다.
     * 성장 그래프 기능에 사용
     *
     * @param userId 사용자 ID
     * @return 평균 점수 (데이터가 없으면 null)
     */
    @Query("SELECT AVG(f.totalScore) FROM Feedback f " +
            "JOIN f.dialogueSession s " +
            "WHERE s.user.id = :userId")
    Double findAverageScoreByUserId(@Param("userId") Long userId);

    /**
     * 특정 사용자의 특정 기간 평균 점수를 계산합니다.
     *
     * @param userId 사용자 ID
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @return 평균 점수 (데이터가 없으면 null)
     */
    @Query("SELECT AVG(f.totalScore) FROM Feedback f " +
            "JOIN f.dialogueSession s " +
            "WHERE s.user.id = :userId " +
            "AND f.createdAt BETWEEN :startDate AND :endDate")
    Double findAverageScoreByUserIdAndDateRange(@Param("userId") Long userId,
                                                @Param("startDate") LocalDateTime startDate,
                                                @Param("endDate") LocalDateTime endDate);

    /**
     * 특정 사용자의 점수별 세부 통계를 조회합니다.
     *
     * @param userId 사용자 ID
     * @return 점수별 평균 [발화속도, 추임새, 공손도, 명료성]
     */
    @Query("SELECT " +
            "AVG(f.speechRateScore), " +
            "AVG(f.fillerWordsScore), " +
            "AVG(f.politenessScore), " +
            "AVG(f.clarityScore) " +
            "FROM Feedback f " +
            "JOIN f.dialogueSession s " +
            "WHERE s.user.id = :userId")
    Object[] findDetailedScoreAveragesByUserId(@Param("userId") Long userId);

    /**
     * 최근 N개의 피드백을 조회합니다.
     * 최근 학습 현황 표시용
     *
     * @param userId 사용자 ID
     * @param limit 조회할 개수
     * @return 최근 피드백 목록
     */
    @Query("SELECT f FROM Feedback f " +
            "JOIN FETCH f.dialogueSession s " +
            "JOIN FETCH s.scenario sc " +
            "WHERE s.user.id = :userId " +
            "ORDER BY f.createdAt DESC " +
            "LIMIT :limit")
    List<Feedback> findRecentFeedbacksByUserId(@Param("userId") Long userId,
                                               @Param("limit") int limit);

    /**
     * 특정 점수 이상의 피드백을 조회합니다.
     * 성취도 분석용
     *
     * @param userId 사용자 ID
     * @param minScore 최소 점수
     * @return 조건에 맞는 피드백 목록
     */
    @Query("SELECT f FROM Feedback f " +
            "JOIN FETCH f.dialogueSession s " +
            "WHERE s.user.id = :userId " +
            "AND f.totalScore >= :minScore " +
            "ORDER BY f.totalScore DESC")
    List<Feedback> findByUserIdAndScoreGreaterThanEqual(@Param("userId") Long userId,
                                                        @Param("minScore") Integer minScore);

    /**
     * 최종 선택이 완료되지 않은 피드백을 조회합니다.
     * 미완료 피드백 정리용
     *
     * @param userId 사용자 ID
     * @return 미완료 피드백 목록
     */
    @Query("SELECT f FROM Feedback f " +
            "JOIN FETCH f.dialogueSession s " +
            "WHERE s.user.id = :userId " +
            "AND (f.chosenAlternative IS NULL OR f.finalChoice IS NULL)")
    List<Feedback> findIncompleteByUserId(@Param("userId") Long userId);

    /**
     * DialogueSession ID로 피드백을 조회합니다.
     * 내부 로직용
     *
     * @param sessionId DialogueSession의 내부 ID (Long)
     * @return 피드백
     */
    Optional<Feedback> findByDialogueSessionId(Long sessionId);

    /**
     * 시나리오별 평균 점수를 조회합니다.
     * 시나리오 난이도 분석용
     *
     * @param scenarioId 시나리오 ID
     * @return 평균 점수
     */
    @Query("SELECT AVG(f.totalScore) FROM Feedback f " +
            "JOIN f.dialogueSession s " +
            "WHERE s.scenario.id = :scenarioId")
    Double findAverageScoreByScenarioId(@Param("scenarioId") Long scenarioId);
}