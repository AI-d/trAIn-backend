package com.aid.train.backend.repository;

import com.aid.train.backend.entity.Speaker;
import com.aid.train.backend.entity.Transcript;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Transcript Repository
 *
 * 발화 내역 데이터에 접근하기 위한 Repository 인터페이스
 *
 * @author 김경민
 * @since 2025-10-08
 * @version 1.0
 */
public interface TranscriptRepository extends JpaRepository<Transcript, Long> {

    /**
     * 특정 세션의 모든 발화 내역을 시간순으로 조회합니다.
     *
     * @param sessionId 세션 ID
     * @return 발화 내역 목록 (시간순)
     */
    List<Transcript> findByDialogueSessionIdOrderByTimestampAsc(Long sessionId);

    /**
     * 특정 세션의 특정 발화자 발화만 조회합니다.
     *
     * @param sessionId 세션 ID
     * @param speaker 발화자 (USER 또는 AI)
     * @return 발화 내역 목록 (시간순)
     */
    List<Transcript> findByDialogueSessionIdAndSpeakerOrderByTimestampAsc(
            Long sessionId, Speaker speaker);

    /**
     * 특정 세션의 발화 개수를 조회합니다.
     *
     * @param sessionId 세션 ID
     * @return 발화 개수
     */
    long countByDialogueSessionId(Long sessionId);

    /**
     * 특정 세션의 사용자 발화 개수를 조회합니다.
     *
     * @param sessionId 세션 ID
     * @param speaker 발화자
     * @return 발화 개수
     */
    long countByDialogueSessionIdAndSpeaker(Long sessionId, Speaker speaker);

    /**
     * 특정 세션의 평균 발화 길이를 계산합니다 (글자 수).
     *
     * @param sessionId 세션 ID
     * @param speaker 발화자
     * @return 평균 글자 수, 데이터가 없으면 null
     */
    @Query("SELECT AVG(LENGTH(t.content)) " +
            "FROM Transcript t " +
            "WHERE t.dialogueSession.id = :sessionId " +
            "AND t.speaker = :speaker")
    Double findAverageContentLengthBySessionAndSpeaker(
            @Param("sessionId") Long sessionId,
            @Param("speaker") Speaker speaker);

    /**
     * 특정 세션에서 신뢰도가 낮은 발화를 조회합니다.
     *
     * @param sessionId 세션 ID
     * @param threshold 신뢰도 임계값 (예: 0.7)
     * @return 낮은 신뢰도 발화 목록
     */
    @Query("SELECT t FROM Transcript t " +
            "WHERE t.dialogueSession.id = :sessionId " +
            "AND t.confidenceScore < :threshold " +
            "ORDER BY t.confidenceScore ASC")
    List<Transcript> findLowConfidenceTranscripts(
            @Param("sessionId") Long sessionId,
            @Param("threshold") Float threshold);

    /**
     * 특정 세션의 전체 발화 시간을 계산합니다 (밀리초).
     *
     * @param sessionId 세션 ID
     * @return 전체 발화 시간 (밀리초)
     */
    @Query("SELECT SUM(t.endTimeMs - t.startTimeMs) " +
            "FROM Transcript t " +
            "WHERE t.dialogueSession.id = :sessionId " +
            "AND t.startTimeMs IS NOT NULL " +
            "AND t.endTimeMs IS NOT NULL")
    Long calculateTotalSpeakingTime(@Param("sessionId") Long sessionId);
}