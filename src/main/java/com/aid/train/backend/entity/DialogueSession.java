package com.aid.train.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
/**
 * 대화 세션 엔티티
 *
 * @author 김경민
 * @since 2025-10-08
 */
@Entity
@Table(name = "dialogue_sessions", indexes = {
        @Index(name = "idx_user_id", columnList = "user_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(exclude = {"user", "scenario", "transcripts"})
@EntityListeners(AuditingEntityListener.class)
public class DialogueSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", nullable = false, unique = true)
    private String sessionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scenario_id", nullable = false)
    private Scenario scenario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SessionStatus status;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @OneToMany(mappedBy = "dialogueSession", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Transcript> transcripts = new ArrayList<>();

    @Column(name = "audio_url")
    private String audioUrl;

    @Column(name = "audio_duration_seconds")
    private Integer audioDurationSeconds;

    @Column(name = "realtime_metrics", columnDefinition = "JSON")
    private String realtimeMetrics;

    // Janus & GPT Realtime 운영키 (MVP, nullable)
    @Column(name = "janus_room_id")
    private Long janusRoomId;

    @Column(name = "janus_user_feed_id")
    private Long janusUserFeedId;

    @Column(name = "janus_bot_feed_id")
    private Long janusBotFeedId;

    @Column(name = "ai_realtime_session_id", length = 128)
    private String aiRealtimeSessionId;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public DialogueSession(User user, Scenario scenario, SessionStatus status, LocalDateTime startedAt) {
        this.sessionId = UUID.randomUUID().toString();
        this.user = user;
        this.scenario = scenario;
        this.status = status;
        this.startedAt = startedAt;
    }

    public void complete() {
        this.status = SessionStatus.COMPLETED;
        this.endedAt = LocalDateTime.now();
    }

    public void fail() {
        this.status = SessionStatus.FAILED;
        this.endedAt = LocalDateTime.now();
    }

    public void addTranscript(Transcript transcript) {
        transcripts.add(transcript);
        transcript.setDialogueSession(this);
    }

    public void setAudioInfo(String audioUrl, Integer durationSeconds) {
        this.audioUrl = audioUrl;
        this.audioDurationSeconds = durationSeconds;
    }

    public void setRealtimeMetrics(String metricsJson) {
        this.realtimeMetrics = metricsJson;
    }

    public void setJanusInfo(Long roomId, Long userFeedId, Long botFeedId) {
        this.janusRoomId = roomId;
        this.janusUserFeedId = userFeedId;
        this.janusBotFeedId = botFeedId;
    }

    public void setAiRealtimeSessionId(String sessionId) {
        this.aiRealtimeSessionId = sessionId;
    }
}
