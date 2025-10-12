package com.aid.train.backend.domain.feedback.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * AI 대화 피드백 엔티티 클래스입니다.
 * 각 대화 세션에 대한 AI의 상세 분석 및 피드백을 저장합니다.
 *
 * <p>
 * 주요 기능:
 * <ul>
 *   <li>대화 세션당 1개의 피드백 생성</li>
 *   <li>음성 녹음 파일 URL 저장</li>
 *   <li>대화 전문(transcript) 저장</li>
 *   <li>다차원 평가 점수 저장 (JSON)</li>
 *   <li>개선 사항 제안 저장 (JSON)</li>
 * </ul>
 * </p>
 *
 * <p>
 * JSON 구조 예시:
 * <pre>
 * scores: {
 *   "pronunciation": 85,
 *   "fluency": 78,
 *   "grammar": 92,
 *   "vocabulary": 88
 * }
 *
 * improvements: [
 *   {"category": "발음", "detail": "th 발음 개선 필요"},
 *   {"category": "유창성", "detail": "더 자연스러운 말하기 연습"}
 * ]
 * </pre>
 * </p>
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Entity
@Table(name = "feedback")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Feedback {

    /**
     * 피드백 고유 ID (자동 생성)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 피드백 대상 대화 세션 ID (고유값)
     * DialogueSession 엔티티와 1:1 관계
     */
    @Column(name = "dialogue_session_id", nullable = false, unique = true)
    private Long dialogueSessionId;

    /**
     * 녹음된 음성 파일 URL
     * S3 또는 CDN 경로
     */
    @Column(name = "audio_url", length = 500)
    private String audioUrl;

    /**
     * 대화 전문 (텍스트)
     * STT(Speech-to-Text) 결과
     */
    @Column(columnDefinition = "TEXT")
    private String transcript;

    /**
     * 다차원 평가 점수 (JSON 형식)
     * 발음, 유창성, 문법, 어휘력 등의 세부 점수
     *
     * <p>예시: {"pronunciation": 85, "fluency": 78, "grammar": 92}</p>
     */
    @Column(columnDefinition = "JSON")
    private String scores;

    /**
     * 개선 사항 제안 (JSON 배열 형식)
     * AI가 분석한 개선이 필요한 부분과 구체적인 조언
     *
     * <p>예시: [{"category": "발음", "detail": "th 발음 개선"}]</p>
     */
    @Column(columnDefinition = "JSON")
    private String improvements;

    /**
     * 피드백 생성 일시
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 피드백 수정 일시
     */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 음성 파일 URL을 업데이트합니다.
     *
     * @param audioUrl 업데이트할 음성 파일 URL
     */
    public void updateAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }

    /**
     * 대화 전문을 업데이트합니다.
     *
     * @param transcript 업데이트할 대화 전문
     */
    public void updateTranscript(String transcript) {
        this.transcript = transcript;
    }

    /**
     * 평가 점수를 업데이트합니다.
     *
     * @param scores 업데이트할 점수 (JSON 형식)
     */
    public void updateScores(String scores) {
        this.scores = scores;
    }

    /**
     * 개선 사항을 업데이트합니다.
     *
     * @param improvements 업데이트할 개선 사항 (JSON 형식)
     */
    public void updateImprovements(String improvements) {
        this.improvements = improvements;
    }

    /**
     * 피드백이 완전한지 확인합니다.
     * 모든 필수 필드가 채워져 있는지 확인
     *
     * @return 완전하면 true, 아니면 false
     */
    public boolean isComplete() {
        return this.transcript != null
                && this.scores != null
                && this.improvements != null;
    }
}