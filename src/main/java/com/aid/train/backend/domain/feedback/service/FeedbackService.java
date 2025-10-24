package com.aid.train.backend.domain.feedback.service;

import com.aid.train.backend.domain.feedback.dto.request.FeedbackChoiceRequest;
import com.aid.train.backend.domain.feedback.dto.request.FeedbackCreateRequest;
import com.aid.train.backend.domain.feedback.dto.response.FeedbackHistoryResponse;
import com.aid.train.backend.domain.feedback.dto.response.FeedbackResponse;
import com.aid.train.backend.domain.feedback.dto.response.FeedbackStatsResponse;
import com.aid.train.backend.domain.feedback.entity.Feedback;
import com.aid.train.backend.domain.feedback.repository.FeedbackRepository;
import com.aid.train.backend.domain.session.entity.DialogueSession;
import com.aid.train.backend.domain.session.service.DialogueSessionService;
import com.aid.train.backend.global.exception.TrainException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.aid.train.backend.global.exception.enums.ErrorCode.*;

/**
 * 피드백 서비스 (확장 버전)
 * <p>
 * TJ님 요구사항 반영:
 * - 기존 기능: 호환성 유지
 * - 새로운 기능: 종합 대화 분석 포함
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final DialogueSessionService dialogueSessionService;
    private final FeedbackPromptService feedbackPromptService;

    /**
     * AI 종합 피드백 생성 (TJ님이 원하는 기능)
     * <p>
     * 전체 대화 흐름 + 개별 문장 분석 포함
     */
    public FeedbackResponse generateFeedbackWithAI(String sessionId) {
        log.info("AI 종합 피드백 생성 시작 - sessionId: {}", sessionId);

        // 1. 세션 조회 및 검증
        DialogueSession session = dialogueSessionService.getSessionWithUserAndScenario(sessionId);

        // 세션이 완료되었는지 확인
        if (!session.getStatus().isCompleted()) {
            log.error("완료되지 않은 세션에 대한 피드백 생성 시도 - sessionId: {}, status: {}",
                    sessionId, session.getStatus());
            throw new TrainException(SESSION_INVALID_STATUS);
        }

        // 2. 중복 피드백 확인
        if (feedbackRepository.existsByDialogueSessionSessionId(sessionId)) {
            log.error("이미 피드백이 존재하는 세션 - sessionId: {}", sessionId);
            throw new TrainException(FEEDBACK_ALREADY_EXISTS);
        }

        try {
            // 2. AI 1회 호출로 모든 분석 생성
            FeedbackResponse comprehensiveFeedback = feedbackPromptService.generateComprehensiveFeedback(session);

            // 3. DB 저장용 객체 생성 (AI 호출 없이)
            FeedbackCreateRequest basicRequest = FeedbackCreateRequest.builder()
                    .sessionId(sessionId)
                    .totalScore(comprehensiveFeedback.totalScore())
                    .speechRateScore(comprehensiveFeedback.speechRateScore())
                    .fillerWordsScore(comprehensiveFeedback.fillerWordsScore())
                    .politenessScore(comprehensiveFeedback.politenessScore())
                    .clarityScore(comprehensiveFeedback.clarityScore())
                    .improvementPoints(comprehensiveFeedback.improvementPoints())
                    .originalTranscript(comprehensiveFeedback.originalTranscript())
                    .alternativeA(comprehensiveFeedback.alternativeA())
                    .alternativeB(comprehensiveFeedback.alternativeB())
                    .alternativeC(comprehensiveFeedback.alternativeC())
                    .build();

            // 4. DB 저장
            Feedback savedFeedback = createFeedbackFromRequest(basicRequest, session);

            // 5. 저장된 ID 포함해서 응답 생성
            return FeedbackResponse.withComprehensiveAnalysis(
                    savedFeedback,
                    comprehensiveFeedback.overallAnalysis(),
                    comprehensiveFeedback.sentenceAnalyses(),
                    comprehensiveFeedback.conversationImprovement()
            );
        } catch (Exception e) {
            log.error("AI 피드백 생성 실패 - sessionId: {}", sessionId, e);
            throw new TrainException(AI_ANALYSIS_FAILED, "AI 피드백 생성에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * 피드백 조회 (기본 정보만, 종합 분석 제외)
     */
    @Transactional(readOnly = true)
    public FeedbackResponse getFeedback(String sessionId) {
        log.info("피드백 조회 - sessionId: {}", sessionId);

        Feedback feedback = feedbackRepository.findByDialogueSessionSessionId(sessionId)
                .orElseThrow(() -> {
                    log.error("피드백을 찾을 수 없음 - sessionId: {}", sessionId);
                    return new TrainException(FEEDBACK_NOT_FOUND);
                });

        log.info("피드백 조회 완료 - sessionId: {}, feedbackId: {}", sessionId, feedback.getId());

        return FeedbackResponse.from(feedback);
    }

    /**
     * 개선안 선택
     */
    public FeedbackResponse chooseAlternative(String sessionId, FeedbackChoiceRequest request) {
        log.info("개선안 선택 - sessionId: {}, alternative: {}", sessionId, request.chosenAlternative());

        Feedback feedback = feedbackRepository.findByDialogueSessionSessionId(sessionId)
                .orElseThrow(() -> {
                    log.error("피드백을 찾을 수 없음 - sessionId: {}", sessionId);
                    return new TrainException(FEEDBACK_NOT_FOUND);
                });

        // 개선안 선택 처리
        if (request.chosenAlternative() == Feedback.ChosenAlternative.CUSTOM) {
            // 사용자 직접 작성
            if (request.finalChoice() == null || request.finalChoice().trim().isEmpty()) {
                throw new TrainException(INVALID_INPUT_VALUE, "사용자 정의 개선안은 내용이 필요합니다.");
            }
            feedback.setCustomChoice(request.finalChoice());
        } else {
            // A, B, C 중 선택
            feedback.chooseAlternative(request.chosenAlternative());
        }

        Feedback savedFeedback = feedbackRepository.save(feedback);

        log.info("개선안 선택 완료 - sessionId: {}, alternative: {}",
                sessionId, request.chosenAlternative());

        return FeedbackResponse.from(savedFeedback);
    }

    /**
     * 사용자 피드백 히스토리 조회 (페이징)
     */
    @Transactional(readOnly = true)
    public Page<FeedbackHistoryResponse> getFeedbackHistory(Long userId, Pageable pageable) {
        log.info("피드백 히스토리 조회 (페이징) - userId: {}, page: {}, size: {}",
                userId, pageable.getPageNumber(), pageable.getPageSize());

        Page<Feedback> feedbacks = feedbackRepository.findByUserId(userId, pageable);

        return feedbacks.map(FeedbackHistoryResponse::from);
    }

    /**
     * 사용자 전체 피드백 히스토리 조회
     */
    @Transactional(readOnly = true)
    public List<FeedbackHistoryResponse> getAllFeedbackHistory(Long userId) {
        log.info("전체 피드백 히스토리 조회 - userId: {}", userId);

        List<Feedback> feedbacks = feedbackRepository.findByUserIdOrderByCreatedAtDesc(userId);

        return feedbacks.stream()
                .map(FeedbackHistoryResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 사용자 피드백 통계 조회
     */
    @Transactional(readOnly = true)
    public FeedbackStatsResponse getFeedbackStats(Long userId) {
        log.info("피드백 통계 조회 - userId: {}", userId);

        // 기본 통계 계산
        long totalCount = feedbackRepository.countByUserId(userId);
        if (totalCount == 0) {
            return createEmptyStats();
        }

        Double averageScore = feedbackRepository.findAverageScoreByUserId(userId);
        Object[] detailedAverages = feedbackRepository.findDetailedScoreAveragesByUserId(userId);

        // 최근 통계
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        LocalDateTime monthAgo = LocalDateTime.now().minusDays(30);

        List<Feedback> recentWeekFeedbacks = feedbackRepository.findByUserIdAndDateRange(userId, weekAgo, LocalDateTime.now());
        List<Feedback> recentMonthFeedbacks = feedbackRepository.findByUserIdAndDateRange(userId, monthAgo, LocalDateTime.now());

        // 완료율 통계
        List<Feedback> allFeedbacks = feedbackRepository.findByUserIdOrderByCreatedAtDesc(userId);
        long completedCount = allFeedbacks.stream()
                .filter(Feedback::isChoiceComplete)
                .count();

        // 등급별 분포 계산
        FeedbackStatsResponse.GradeDistribution gradeDistribution = calculateGradeDistribution(allFeedbacks);

        // 월별 추이 계산 (최근 6개월)
        List<FeedbackStatsResponse.MonthlyScore> monthlyScores = calculateMonthlyScores(allFeedbacks);

        // 최고/최저 점수
        int maxScore = allFeedbacks.stream()
                .mapToInt(Feedback::getTotalScore)
                .max()
                .orElse(0);

        int minScore = allFeedbacks.stream()
                .mapToInt(Feedback::getTotalScore)
                .min()
                .orElse(0);

        return FeedbackStatsResponse.of(
                totalCount,
                averageScore != null ? Math.round(averageScore * 10.0) / 10.0 : 0.0,
                detailedAverages[0] != null ? Math.round(((Number) detailedAverages[0]).doubleValue() * 10.0) / 10.0 : 0.0,
                detailedAverages[1] != null ? Math.round(((Number) detailedAverages[1]).doubleValue() * 10.0) / 10.0 : 0.0,
                detailedAverages[2] != null ? Math.round(((Number) detailedAverages[2]).doubleValue() * 10.0) / 10.0 : 0.0,
                detailedAverages[3] != null ? Math.round(((Number) detailedAverages[3]).doubleValue() * 10.0) / 10.0 : 0.0,
                maxScore,
                minScore,
                (long) recentWeekFeedbacks.size(),
                (long) recentMonthFeedbacks.size(),
                completedCount,
                totalCount - completedCount,
                gradeDistribution,
                monthlyScores
        );
    }

    // ========================================
    // Private Helper Methods
    // ========================================

    private Feedback createFeedbackFromRequest(FeedbackCreateRequest request, DialogueSession session) {
        Feedback feedback = Feedback.builder()
                .dialogueSession(session)
                .totalScore(request.totalScore())
                .speechRateScore(request.speechRateScore())
                .fillerWordsScore(request.fillerWordsScore())
                .politenessScore(request.politenessScore())
                .clarityScore(request.clarityScore())
                .improvementPoints(request.improvementPoints())
                .originalTranscript(request.originalTranscript())
                .alternativeA(request.alternativeA())
                .alternativeB(request.alternativeB())
                .alternativeC(request.alternativeC())
                .aiPrompt(request.aiPrompt())
                .aiRawResponse(request.aiRawResponse())
                .build();

        return feedbackRepository.save(feedback);
    }

    private FeedbackStatsResponse createEmptyStats() {
        return FeedbackStatsResponse.of(
                0L, 0.0, 0.0, 0.0, 0.0, 0.0, 0, 0, 0L, 0L, 0L, 0L,
                FeedbackStatsResponse.GradeDistribution.builder()
                        .gradeA(0L).gradeB(0L).gradeC(0L).gradeD(0L).gradeF(0L).build(),
                new ArrayList<>()
        );
    }

    private FeedbackStatsResponse.GradeDistribution calculateGradeDistribution(List<Feedback> feedbacks) {
        long gradeA = feedbacks.stream().filter(f -> f.getTotalScore() >= 90).count();
        long gradeB = feedbacks.stream().filter(f -> f.getTotalScore() >= 80 && f.getTotalScore() < 90).count();
        long gradeC = feedbacks.stream().filter(f -> f.getTotalScore() >= 70 && f.getTotalScore() < 80).count();
        long gradeD = feedbacks.stream().filter(f -> f.getTotalScore() >= 60 && f.getTotalScore() < 70).count();
        long gradeF = feedbacks.stream().filter(f -> f.getTotalScore() < 60).count();

        return FeedbackStatsResponse.GradeDistribution.builder()
                .gradeA(gradeA)
                .gradeB(gradeB)
                .gradeC(gradeC)
                .gradeD(gradeD)
                .gradeF(gradeF)
                .build();
    }

    private List<FeedbackStatsResponse.MonthlyScore> calculateMonthlyScores(List<Feedback> feedbacks) {
        // 최근 6개월 월별 통계 계산
        List<FeedbackStatsResponse.MonthlyScore> monthlyScores = new ArrayList<>();

        LocalDateTime now = LocalDateTime.now();
        for (int i = 5; i >= 0; i--) {
            LocalDateTime monthStart = now.minusMonths(i).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime monthEnd = monthStart.plusMonths(1).minusSeconds(1);

            List<Feedback> monthFeedbacks = feedbacks.stream()
                    .filter(f -> f.getCreatedAt().isAfter(monthStart) && f.getCreatedAt().isBefore(monthEnd))
                    .toList();

            if (!monthFeedbacks.isEmpty()) {
                double avgScore = monthFeedbacks.stream()
                        .mapToInt(Feedback::getTotalScore)
                        .average()
                        .orElse(0.0);

                monthlyScores.add(FeedbackStatsResponse.MonthlyScore.builder()
                        .yearMonth(String.format("%04d-%02d", monthStart.getYear(), monthStart.getMonthValue()))
                        .averageScore(Math.round(avgScore * 10.0) / 10.0)
                        .count((long) monthFeedbacks.size())
                        .build());
            }
        }

        return monthlyScores;
    }
}