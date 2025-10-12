package com.aid.train.backend.domain.history.repository;

import com.aid.train.backend.domain.history.entity.History;
import com.aid.train.backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * History 엔티티의 Repository 인터페이스입니다.
 * 대화 히스토리의 조회, 저장, 삭제 등의 데이터 접근 기능을 제공합니다.
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Repository
public interface HistoryRepository extends JpaRepository<History, Long> {

    /**
     * 사용자 통계 프로젝션 인터페이스입니다.
     * 엔티티 전체를 조회하지 않고 필요한 통계 데이터만 반환합니다.
     */
    interface UserStatistics {
        /**
         * 평균 점수를 반환합니다.
         *
         * @return 평균 점수 (0.0~100.0)
         */
        Double getAverageScore();

        /**
         * 총 대화 횟수를 반환합니다.
         *
         * @return 총 대화 횟수
         */
        Long getTotalCount();

        /**
         * 우수 성적 횟수를 반환합니다. (80점 이상)
         *
         * @return 우수 성적 횟수
         */
        Long getExcellentCount();
    }

    /**
     * 대화 세션 ID로 히스토리를 조회합니다.
     *
     * @param dialogueSessionId 대화 세션 ID
     * @return 히스토리 Optional
     */
    Optional<History> findByDialogueSessionId(Long dialogueSessionId);

    /**
     * 특정 사용자의 모든 히스토리를 조회합니다.
     * 최신순 정렬
     *
     * @param user 사용자
     * @return 히스토리 목록
     */
    List<History> findByUserOrderByCompletedAtDesc(User user);

    /**
     * 특정 사용자의 특정 기간 히스토리를 조회합니다.
     *
     * @param user      사용자
     * @param startDate 시작 일시
     * @param endDate   종료 일시
     * @return 히스토리 목록
     */
    List<History> findByUserAndCompletedAtBetween(User user, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 특정 사용자의 통계 정보를 조회합니다.
     * 프로젝션을 사용하여 필요한 데이터만 효율적으로 조회합니다.
     *
     * @param user 사용자
     * @return 사용자 통계 (평균 점수, 총 횟수, 우수 성적 횟수)
     */
    @Query("""
                SELECT 
                    COALESCE(AVG(h.totalScore), 0.0) as averageScore,
                    COUNT(h) as totalCount,
                    SUM(CASE WHEN h.totalScore >= 80 THEN 1 ELSE 0 END) as excellentCount
                FROM History h 
                WHERE h.user = :user
            """)
    UserStatistics getUserStatistics(@Param("user") User user);

    /**
     * 특정 사용자의 평균 점수를 계산합니다.
     *
     * @param user 사용자
     * @return 평균 점수 (없으면 0.0)
     * @deprecated getUserStatistics() 사용을 권장합니다.
     */
    @Deprecated
    @Query("SELECT COALESCE(AVG(h.totalScore), 0.0) FROM History h WHERE h.user = :user")
    Double calculateAverageScore(@Param("user") User user);

    /**
     * 특정 사용자의 총 대화 횟수를 조회합니다.
     *
     * @param user 사용자
     * @return 대화 횟수
     */
    long countByUser(User user);

    /**
     * 특정 사용자의 특정 점수 이상 히스토리를 조회합니다.
     * 우수 성적 통계용
     *
     * @param user     사용자
     * @param minScore 최소 점수
     * @return 히스토리 목록
     */
    List<History> findByUserAndTotalScoreGreaterThanEqual(User user, Integer minScore);

    /**
     * 특정 사용자의 최근 N개 히스토리를 조회합니다.
     *
     * @param user 사용자
     * @return 히스토리 목록 (최신순)
     */
    List<History> findTop10ByUserOrderByCompletedAtDesc(User user);

    /**
     * 대화 세션 ID 존재 여부를 확인합니다.
     *
     * @param dialogueSessionId 대화 세션 ID
     * @return 존재하면 true, 아니면 false
     */
    boolean existsByDialogueSessionId(Long dialogueSessionId);

    /**
     * 특정 기간 이전의 히스토리를 벌크 삭제합니다.
     * 데이터 보관 정책에 따른 오래된 데이터 정리용
     *
     * @param completedAt 기준 완료 일시
     * @return 삭제된 히스토리 개수
     */
    @Modifying
    @Query("DELETE FROM History h WHERE h.completedAt < :completedAt")
    int bulkDeleteOldHistories(@Param("completedAt") LocalDateTime completedAt);
}