package com.aid.train.backend.domain.session.entity;

import com.aid.train.backend.domain.session.enums.Speaker;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 발화 내역 엔티티
 *
 * @author 김경민
 * @since 2025-10-08
 */
@Entity
@Table(name = "transcripts", indexes = {
        @Index(name = "idx_session_id", columnList = "session_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(exclude = {"dialogueSession"})
@EntityListeners(AuditingEntityListener.class)
public class Transcript {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private DialogueSession dialogueSession;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Speaker speaker;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "start_time_ms")
    private Long startTimeMs;

    @Column(name = "end_time_ms")
    private Long endTimeMs;

    @Column(name = "confidence_score")
    private Float confidenceScore;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public Transcript(DialogueSession dialogueSession, Speaker speaker, String content,
                      LocalDateTime timestamp, Long startTimeMs, Long endTimeMs, Float confidenceScore) {
        this.dialogueSession = dialogueSession;
        this.speaker = speaker;
        this.content = content;
        this.timestamp = timestamp;
        this.startTimeMs = startTimeMs;
        this.endTimeMs = endTimeMs;
        this.confidenceScore = confidenceScore;
    }

    protected void setDialogueSession(DialogueSession dialogueSession) {
        this.dialogueSession = dialogueSession;
    }

    public Long getDurationMs() {
        if (startTimeMs != null && endTimeMs != null) {
            return endTimeMs - startTimeMs;
        }
        return null;
    }

    public Double getDurationSeconds() {
        Long durationMs = getDurationMs();
        return durationMs != null ? durationMs / 1000.0 : null;
    }

    public Double getSpeechRate() {
        Double durationSeconds = getDurationSeconds();
        if (durationSeconds != null && durationSeconds > 0) {
            int charCount = content.replaceAll("\\s", "").length();
            return (charCount / durationSeconds) * 60;
        }
        return null;
    }

    // ========================================
    // 음성 인식 신뢰도 관련 메서드
    // @author 왕택준
    // @since 1.0.0
    // ========================================

    /**
     * 음성 인식 신뢰도 점수를 반환합니다.
     * null인 경우 기본값 0.8(80%)을 반환합니다.
     *
     * @return 신뢰도 점수 (0.0 ~ 1.0)
     */
    public Float getEffectiveConfidenceScore() {
        return confidenceScore != null ? confidenceScore : 0.8f;
    }

    /**
     * 신뢰도 수준을 문자열로 반환합니다.
     *
     * @return 신뢰도 수준 ("높음", "보통", "낮음", "측정되지 않음")
     */
    public String getConfidenceLevel() {
        if (confidenceScore == null) {
            return "측정되지 않음";
        }
        if (confidenceScore >= 0.95f) {
            return "높음";
        }
        if (confidenceScore >= 0.7f) {
            return "보통";
        }
        return "낮음";
    }

    /**
     * 해당 발화가 신뢰할 수 있는 품질인지 확인합니다.
     * null이거나 0.7 이상이면 신뢰할 수 있는 것으로 판단합니다.
     *
     * @return 신뢰할 수 있으면 true, 그렇지 않으면 false
     */
    public boolean isReliable() {
        return confidenceScore == null || confidenceScore >= 0.7f;
    }

    /**
     * 피드백 분석에 포함할 수 있는 발화인지 확인합니다.
     * 사용자 발화이면서 신뢰할 수 있고, 의미있는 내용이어야 합니다.
     *
     * @return 분석 대상이면 true, 그렇지 않으면 false
     */
    public boolean isAnalyzable() {
        return speaker == Speaker.USER
                && content != null
                && !content.trim().isEmpty()
                && content.trim().length() > 3
                && isReliable();
    }
}