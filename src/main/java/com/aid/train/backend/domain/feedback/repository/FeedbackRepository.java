package com.aid.train.backend.domain.feedback.repository;

import com.aid.train.backend.domain.feedback.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Feedback 엔티티의 Repository 인터페이스입니다.
 * AI 대화 피드백의 조회, 저장, 삭제 등의 데이터 접근 기능을 제공합니다.
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    /**
     * 대화 세션 ID로 피드백을 조회합니다.
     *
     * @param dialogueSessionId 대화 세션 ID
     * @return 피드백 Optional
     */
    Optional<Feedback> findByDialogueSessionId(Long dialogueSessionId);

    /**
     * 특정 기간에 생성된 피드백을 조회합니다.
     *
     * @param startDate 시작 일시
     * @param endDate   종료 일시
     * @return 피드백 목록
     */
    List<Feedback> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 대화 세션 ID 존재 여부를 확인합니다.
     *
     * @param dialogueSessionId 대화 세션 ID
     * @return 존재하면 true, 아니면 false
     */
    boolean existsByDialogueSessionId(Long dialogueSessionId);

    /**
     * 특정 기간 이전에 생성된 피드백을 삭제합니다.
     * 데이터 정리용 (필요 시)
     *
     * @param createdAt 기준 생성 일시
     * @return 삭제된 피드백 개수
     */
    long deleteByCreatedAtBefore(LocalDateTime createdAt);
}