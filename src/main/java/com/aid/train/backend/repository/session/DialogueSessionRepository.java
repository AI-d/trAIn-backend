package com.aid.train.backend.repository.session;

import com.aid.train.backend.domain.session.entity.DialogueSession;
import com.aid.train.backend.domain.session.enums.SessionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * DialogueSession Repository
 *
 * 대화 세션 데이터에 접근하기 위한 Repository 인터페이스
 *
 * @author 김경민
 * @since 2025-10-08
 * @version 1.0
 */
public interface DialogueSessionRepository extends JpaRepository<DialogueSession, Long> {
    /**
     * session_id로 세션을 조회합니다.
     *
     * 외부 API에서 세션을 식별할 때 사용합니다.
     *
     * @param sessionId UUID 형태의 세션 ID
     * @return 세션 (없으면 Optional.empty())
     */
    Optional<DialogueSession> findBySessionId(String sessionId);

    /**
     * 특정 사용자의 세션 목록을 조회합니다 (최신순).
     *
     * @param userId 사용자 ID
     * @return 세션 목록 (최신순 정렬)
     */
    List<DialogueSession> findByUserIdOrderByStartedAtDesc(Long userId);

    /**
     * 특정 사용자의 세션 목록을 페이징하여 조회합니다.
     *
     * @param userId 사용자 ID
     * @param pageable 페이징 정보
     * @return 페이징된 세션 목록
     */
    Page<DialogueSession> findByUserId(Long userId, Pageable pageable);

    /**
     * 특정 사용자의 특정 상태 세션을 조회합니다.
     *
     * @param userId 사용자 ID
     * @param status 세션 상태
     * @return 세션 목록
     */
    List<DialogueSession> findByUserIdAndStatus(Long userId, SessionStatus status);

    /**
     * 특정 시나리오의 세션 목록을 조회합니다.
     *
     * @param scenarioId 시나리오 ID
     * @return 세션 목록
     */
    List<DialogueSession> findByScenarioId(Long scenarioId);

    /**
     * 특정 기간 내 완료된 세션을 조회합니다.
     *
     * @param status 세션 상태
     * @param start 시작 시간
     * @param end 종료 시간
     * @return 세션 목록
     */
    List<DialogueSession> findByStatusAndStartedAtBetween(
            SessionStatus status, LocalDateTime start, LocalDateTime end);

    /**
     * 특정 사용자의 완료된 세션 개수를 조회합니다.
     *
     * @param userId 사용자 ID
     * @param status 세션 상태
     * @return 세션 개수
     */
    long countByUserIdAndStatus(Long userId, SessionStatus status);

    /**
     * 특정 사용자와 시나리오의 완료된 세션을 조회합니다 (최신순).
     *
     * @param userId 사용자 ID
     * @param scenarioId 시나리오 ID
     * @param status 세션 상태
     * @return 세션 목록 (최신순)
     */
    List<DialogueSession> findByUserIdAndScenarioIdAndStatusOrderByStartedAtDesc(
            Long userId, Long scenarioId, SessionStatus status);

    /**
     * Fetch Join을 사용하여 User와 Scenario를 함께 조회합니다.
     * N+1 문제를 방지합니다.
     *
     * @param sessionId UUID 형태의 세션 ID
     * @return 세션 (User, Scenario 포함)
     */
    @Query("SELECT s FROM DialogueSession s " +
            "JOIN FETCH s.user " +
            "JOIN FETCH s.scenario " +
            "WHERE s.sessionId = :sessionId")
    Optional<DialogueSession> findWithUserAndScenarioBySessionId(@Param("sessionId") String sessionId);

    /**
     * Fetch Join을 사용하여 Transcript까지 함께 조회합니다.
     *
     * @param sessionId UUID 형태의 세션 ID
     * @return 세션 (Transcript 목록 포함)
     */
    @Query("SELECT DISTINCT s FROM DialogueSession s " +
            "LEFT JOIN FETCH s.transcripts " +
            "WHERE s.sessionId = :sessionId")
    Optional<DialogueSession> findWithTranscriptsBySessionId(@Param("sessionId") String sessionId);

    /**
     * 특정 사용자의 세션 목록을 User와 Scenario를 함께 조회합니다.
     *
     * @param userId 사용자 ID
     * @return 세션 목록
     */
    @Query("SELECT s FROM DialogueSession s " +
            "JOIN FETCH s.user " +
            "JOIN FETCH s.scenario " +
            "WHERE s.user.id = :userId " +
            "ORDER BY s.startedAt DESC")
    List<DialogueSession> findByUserIdWithUserAndScenario(@Param("userId") Long userId);

    /**
     * 완료된 세션의 평균 소요 시간을 계산합니다 (초 단위).
     *
     * @return 평균 소요 시간 (초), 데이터가 없으면 null
     */
    @Query(value = "SELECT AVG(TIMESTAMPDIFF(SECOND, started_at, ended_at)) " +
            "FROM dialogue_session " +
            "WHERE status = 'COMPLETED' AND ended_at IS NOT NULL",
            nativeQuery = true)
    Double findAverageSessionDuration();

    /**
     * 특정 사용자의 완료된 세션 평균 소요 시간을 계산합니다 (초 단위).
     *
     * @param userId 사용자 ID
     * @return 평균 소요 시간 (초), 데이터가 없으면 null
     */
    @Query(value = "SELECT AVG(TIMESTAMPDIFF(SECOND, started_at, ended_at)) " +
            "FROM dialogue_session " +
            "WHERE user_id = :userId " +
            "AND status = 'COMPLETED' AND ended_at IS NOT NULL",
            nativeQuery = true)
    Double findAverageSessionDurationByUserId(@Param("userId") Long userId);

    /**
     * Janus Room ID로 세션을 조회합니다.
     *
     * @param janusRoomId Janus Room ID
     * @return 세션
     */
    Optional<DialogueSession> findByJanusRoomId(Long janusRoomId);

    /**
     * AI Realtime Session ID로 세션을 조회합니다.
     *
     * @param aiRealtimeSessionId AI Realtime Session ID
     * @return 세션
     */
    Optional<DialogueSession> findByAiRealtimeSessionId(String aiRealtimeSessionId);
}
