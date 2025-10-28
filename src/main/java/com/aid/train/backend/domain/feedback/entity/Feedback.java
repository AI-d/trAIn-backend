package com.aid.train.backend.domain.feedback.entity;

import com.aid.train.backend.domain.session.entity.DialogueSession;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 피드백 엔티티
 * <p>
 * 대화 세션이 완료된 후 AI가 생성한 피드백과 개선안을 저장합니다.
 * DialogueSession과 1:1 관계를 가지며, 하나의 세션에는 하나의 피드백만 존재합니다.
 * <p>
 * 주요 기능:
 * - AI 분석 결과 저장 (점수, 개선점)
 * - 3가지 스타일의 개선안 저장 (간결/공손/따뜻)
 * - 사용자가 선택/수정한 최종안 저장
 * - 피드백 생성 및 수정 이력 관리
 * <p>
 * 점수 체계 (균등 배분):
 * - 발화속도: 0-25점
 * - 추임새: 0-25점
 * - 공손도: 0-25점
 * - 명료성: 0-25점
 * - 총합: 0-100점
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Entity
@Table(name = "feedbacks", indexes = {
        @Index(name = "idx_feedback_session_id", columnList = "session_id"),
        @Index(name = "idx_feedback_created_at", columnList = "created_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(exclude = {"dialogueSession"})
@EntityListeners(AuditingEntityListener.class)
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 연관된 대화 세션 (1:1 관계)
     * 하나의 세션에는 하나의 피드백만 존재
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false, unique = true)
    private DialogueSession dialogueSession;

    /**
     * 전체 점수 (0-100)
     * 발화속도 + 추임새 + 공손도 + 명료성의 합계
     */
    @Column(name = "total_score", nullable = false)
    private Integer totalScore;

    /**
     * 발화 속도 점수 (0-25) - 균등 배분으로 수정
     */
    @Column(name = "speech_rate_score", nullable = false)
    private Integer speechRateScore;

    /**
     * 추임새 점수 (0-25) - 균등 배분으로 수정
     * "음...", "그..." 등의 불필요한 표현 빈도 평가
     */
    @Column(name = "filler_words_score", nullable = false)
    private Integer fillerWordsScore;

    /**
     * 공손도 점수 (0-25)
     * 상대방에 대한 예의와 존중 표현 평가
     */
    @Column(name = "politeness_score", nullable = false)
    private Integer politenessScore;

    /**
     * 명료성 점수 (0-25)
     * 의사 전달의 명확성과 구체성 평가
     */
    @Column(name = "clarity_score", nullable = false)
    private Integer clarityScore;

    /**
     * 주요 개선 포인트 (JSON 형태로 저장)
     * 예: [{"type": "filler_words", "description": "추임새 줄이기", "count": 5}]
     */
    @Column(name = "improvement_points", columnDefinition = "JSON")
    private String improvementPoints;

    /**
     * AI가 분석한 사용자의 원본 발화
     * 피드백 대상이 되는 실제 사용자 발언
     */
    @Column(name = "original_transcript", columnDefinition = "TEXT")
    private String originalTranscript;

    /**
     * 개선안 A - 간결하고 명료한 스타일
     */
    @Column(name = "alternative_a", columnDefinition = "TEXT")
    private String alternativeA;

    /**
     * 개선안 B - 공손하고 정중한 스타일
     */
    @Column(name = "alternative_b", columnDefinition = "TEXT")
    private String alternativeB;

    /**
     * 개선안 C - 따뜻하고 친근한 스타일
     */
    @Column(name = "alternative_c", columnDefinition = "TEXT")
    private String alternativeC;

    /**
     * 사용자가 선택한 최종 개선안
     * A, B, C 중 하나를 선택하거나 직접 수정한 결과
     */
    @Column(name = "final_choice", columnDefinition = "TEXT")
    private String finalChoice;

    /**
     * 선택된 개선안 타입
     * A, B, C, CUSTOM 중 하나
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "chosen_alternative", length = 10)
    private ChosenAlternative chosenAlternative;

    /**
     * AI 피드백 생성에 사용된 프롬프트 (디버깅용)
     */
    @Column(name = "ai_prompt", columnDefinition = "TEXT")
    private String aiPrompt;

    /**
     * AI의 원본 응답 (디버깅용)
     */
    @Column(name = "ai_raw_response", columnDefinition = "TEXT")
    private String aiRawResponse;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 선택된 개선안 타입 열거형
     */
    public enum ChosenAlternative {
        A("간결한 스타일"),
        B("공손한 스타일"),
        C("따뜻한 스타일"),
        CUSTOM("사용자 수정");

        private final String description;

        ChosenAlternative(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Feedback 엔티티 빌더 생성자.
     * 필수 필드를 초기화합니다.
     */
    @Builder
    public Feedback(DialogueSession dialogueSession, Integer totalScore,
                    Integer speechRateScore, Integer fillerWordsScore,
                    Integer politenessScore, Integer clarityScore,
                    String improvementPoints, String originalTranscript,
                    String alternativeA, String alternativeB, String alternativeC,
                    String aiPrompt, String aiRawResponse) {
        // 필수 값 검증은 @NotNull, @Column(nullable=false) 및 validateScores()에서 처리
        this.dialogueSession = dialogueSession;
        this.totalScore = totalScore;
        this.speechRateScore = speechRateScore;
        this.fillerWordsScore = fillerWordsScore;
        this.politenessScore = politenessScore;
        this.clarityScore = clarityScore;
        this.improvementPoints = improvementPoints;
        this.originalTranscript = originalTranscript;
        this.alternativeA = alternativeA;
        this.alternativeB = alternativeB;
        this.alternativeC = alternativeC;
        this.aiPrompt = aiPrompt;
        this.aiRawResponse = aiRawResponse;
    }

    /**
     * 사용자가 AI 제안 개선안(A, B, C)을 선택합니다.
     * <p>
     * 선택된 타입({@code chosenAlternative})을 설정하고,
     * 해당 개선안 내용을 {@code finalChoice}에 복사합니다.
     * </p>
     *
     * @param alternative 선택된 개선안 타입 (A, B, C 중 하나)
     * @throws IllegalArgumentException CUSTOM 또는 유효하지 않은 타입 선택 시
     */
    public void chooseAlternative(ChosenAlternative alternative) {
        this.chosenAlternative = alternative;

        switch (alternative) {
            case A -> this.finalChoice = this.alternativeA;
            case B -> this.finalChoice = this.alternativeB;
            case C -> this.finalChoice = this.alternativeC;
            default -> throw new IllegalArgumentException("유효하지 않은 개선안 타입입니다: " + alternative);
        }
    }

    /**
     * 사용자가 개선안을 직접 수정/작성합니다.
     * <p>
     * {@code chosenAlternative}를 {@link ChosenAlternative#CUSTOM}으로 설정하고,
     * 입력된 텍스트(앞뒤 공백 제거)를 {@code finalChoice}에 저장합니다.
     * </p>
     *
     * @param customText 사용자 정의 개선안 (null 또는 공백 불가)
     * @throws IllegalArgumentException customText가 비어있거나 null인 경우
     */
    public void setCustomChoice(String customText) {
        if (customText == null || customText.trim().isEmpty()) {
            throw new IllegalArgumentException("사용자 정의 개선안은 내용이 필요합니다.");
        }
        this.chosenAlternative = ChosenAlternative.CUSTOM;
        this.finalChoice = customText.trim();
    }

    /**
     * 엔티티 저장/업데이트 전 점수 유효성 검증
     * <p>
     * 각 점수 범위(0-25) 및 총점 범위(0-100)를 확인합니다.
     * 세부 점수 합계와 총점이 정확히 일치하는지 검증합니다.
     * </p>
     *
     * @throws IllegalArgumentException 점수 범위 또는 합계 불일치 시
     */
    @PrePersist
    @PreUpdate
    private void validateScores() {
        // 각 점수 범위 검증 (0-25)
        if (speechRateScore == null || speechRateScore < 0 || speechRateScore > 25) {
            throw new IllegalArgumentException("발화속도 점수 범위 오류: " + speechRateScore);
        }
        if (fillerWordsScore == null || fillerWordsScore < 0 || fillerWordsScore > 25) {
            throw new IllegalArgumentException("추임새 점수 범위 오류: " + fillerWordsScore);
        }
        if (politenessScore == null || politenessScore < 0 || politenessScore > 25) {
            throw new IllegalArgumentException("공손도 점수 범위 오류: " + politenessScore);
        }
        if (clarityScore == null || clarityScore < 0 || clarityScore > 25) {
            throw new IllegalArgumentException("명료성 점수 범위 오류: " + clarityScore);
        }

        // 총점 범위 검증 (0-100)
        if (totalScore == null || totalScore < 0 || totalScore > 100) {
            throw new IllegalArgumentException("전체 점수 범위 오류: " + totalScore);
        }

        // 점수 합계 검증
        int calculatedTotal = speechRateScore + fillerWordsScore + politenessScore + clarityScore;

        if (totalScore != calculatedTotal) {
            throw new IllegalArgumentException(
                    String.format("점수 합계 불일치: Total=%d, Calculated=%d (정확히 일치해야 함)",
                            totalScore, calculatedTotal));
        }
    }

    /**
     * 사용자가 개선안 선택을 완료했는지 확인합니다.
     *
     * @return 사용자가 최종 선택을 완료했으면 true
     */
    public boolean isChoiceComplete() {
        return chosenAlternative != null && finalChoice != null && !finalChoice.trim().isEmpty();
    }

    /**
     * 총점을 기준으로 A-F 등급을 반환합니다.
     *
     * @return 점수에 따른 등급 (A, B, C, D, F)
     */
    public String getScoreGrade() {
        if (totalScore >= 90) return "A";
        if (totalScore >= 80) return "B";
        if (totalScore >= 70) return "C";
        if (totalScore >= 60) return "D";
        return "F";
    }
}