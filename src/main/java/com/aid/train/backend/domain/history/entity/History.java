package com.aid.train.backend.domain.history.entity;

import com.aid.train.backend.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 대화 히스토리 엔티티 클래스입니다.
 * 사용자의 대화 세션 완료 내역 및 성장 통계를 저장합니다.
 *
 * <p>
 * 주요 기능:
 * <ul>
 *   <li>대화 세션 완료 내역 기록</li>
 *   <li>AI 평가 점수 저장</li>
 *   <li>사용자 성장 통계 추적</li>
 *   <li>시나리오별 학습 이력 관리</li>
 * </ul>
 * </p>
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Entity
@Table(name = "history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class History {

    /**
     * 히스토리 고유 ID (자동 생성)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 대화 세션을 완료한 사용자
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 완료된 대화 세션 ID (고유값)
     * DialogueSession 엔티티와 1:1 관계
     */
    @Column(name = "dialogue_session_id", nullable = false, unique = true)
    private Long dialogueSessionId;

    /**
     * AI 평가 종합 점수 (0~100)
     * 발음, 유창성, 정확성 등의 평균 점수
     */
    @Column(name = "total_score", nullable = false)
    private Integer totalScore;

    /**
     * 대화 세션 완료 일시
     */
    @Column(name = "completed_at", nullable = false)
    private LocalDateTime completedAt;

    /**
     * 대화 시나리오 제목
     * 통계 및 히스토리 조회 편의를 위해 역정규화
     */
    @Column(name = "scenario_title", length = 120)
    private String scenarioTitle;

    /**
     * 레코드 생성 일시
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 레코드 수정 일시
     */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 점수를 업데이트합니다.
     *
     * @param totalScore 업데이트할 종합 점수 (0~100)
     * @throws IllegalArgumentException 점수가 범위를 벗어나는 경우
     */
    public void updateScore(Integer totalScore) {
        if (totalScore != null && (totalScore < 0 || totalScore > 100)) {
            throw new IllegalArgumentException("점수는 0~100 사이여야 합니다.");
        }
        this.totalScore = totalScore;
    }

    /**
     * 점수가 우수한지 확인합니다.
     * 80점 이상을 우수로 판단
     *
     * @return 우수하면 true, 아니면 false
     */
    public boolean isExcellent() {
        return this.totalScore >= 80;
    }

    /**
     * 점수가 양호한지 확인합니다.
     * 60점 이상 80점 미만을 양호로 판단
     *
     * @return 양호하면 true, 아니면 false
     */
    public boolean isGood() {
        return this.totalScore >= 60 && this.totalScore < 80;
    }

    /**
     * 점수가 개선 필요한지 확인합니다.
     * 60점 미만을 개선 필요로 판단
     *
     * @return 개선 필요하면 true, 아니면 false
     */
    public boolean needsImprovement() {
        return this.totalScore < 60;
    }
}