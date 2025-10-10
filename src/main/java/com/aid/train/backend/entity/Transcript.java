package com.aid.train.backend.entity;

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
}