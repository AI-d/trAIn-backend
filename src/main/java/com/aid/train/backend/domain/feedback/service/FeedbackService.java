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
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.aid.train.backend.global.exception.enums.ErrorCode.*;

/**
 * 피드백 비즈니스 로직 서비스
 *
 * 역할:
 * - 피드백 생성 및 관리
 * - 개선안 선택 처리
 * - 피드백 히스토리 조회
 * - 통계 데이터 계산
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
     * AI를 통해 자동으로 피드백을 생성합니다.
     *
     * 처리 순서:
     * 1. 세션 조회 및 완료 상태 확인
     * 2. 기존 피드백 중복 확인
     * 3. AI에게 대화 분석 요청
     * 4. AI 응답을 파싱하여 피드백 생성
     *
     * @param sessionId 세션 ID
     * @return 생성된 피드백 응답
     * @throws TrainException 세션을 찾을 수 없거나 이미 피드백이 존재하는 경우
     */
    public FeedbackResponse generateFeedbackWithAI(String sessionId) {
        log.info("AI 자동 피드백 생성 시작 - sessionId: {}", sessionId);

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

        // 3. AI를 통한 피드백 생성
        FeedbackCreateRequest aiRequest = feedbackPromptService.generateFeedbackFromAI(session);

        // 4. 기존 createFeedback 메서드 재사용
        return createFeedback(aiRequest);
    }

    /**
     * 새로운 피드백을 생성합니다.
     *
     * 처리 순서:
     * 1. 세션 존재 여부 및 완료 상태 확인
     * 2. 기존 피드백 중복 확인
     * 3. 점수 유효성 검증
     * 4. 피드백 엔티티 생성 및 저장
     *
     * @param request 피드백 생성 요청
     * @return 생성된 피드백 응답
     * @throws TrainException 세션을 찾을 수 없거나 이미 피드백이 존재하는 경우
     */
    public FeedbackResponse createFeedback(FeedbackCreateRequest request) {
        log.info("피드백 생성 시작 - sessionId: {}", request.sessionId());

        // 1. 세션 조회 및 검증
        DialogueSession session = dialogueSessionService.getSessionWithUserAndScenario(request.sessionId());

        // 세션이 완료되었는지 확인
        if (!session.getStatus().isCompleted()) {
            log.error("완료되지 않은 세션에 대한 피드백 생성 시도 - sessionId: {}, status: {}",
                    request.sessionId(), session.getStatus());
            throw new TrainException(SESSION_INVALID_STATUS);
        }

        // 2. 중복 피드백 확인
        if (feedbackRepository.existsByDialogueSessionSessionId(request.sessionId())) {
            log.error("이미 피드백이 존재하는 세션 - sessionId: {}", request.sessionId());
            throw new TrainException(FEEDBACK_ALREADY_EXISTS);
        }

        // 3. 점수 합계 검증
        int calculatedTotal = request.speechRateScore() + request.fillerWordsScore()
                + request.politenessScore() + request.clarityScore();
        if (!request.totalScore().equals(calculatedTotal)) {
            log.error("점수 합계 불일치 - expected: {}, actual: {}", request.totalScore(), calculatedTotal);
            throw new TrainException(INVALID_INPUT_VALUE, "점수 합계가 올바르지 않습니다.");
        }

        // 4. 피드백 엔티티 생성
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

        // 5. DB 저장
        Feedback savedFeedback = feedbackRepository.save(feedback);
        feedbackRepository.flush(); // 즉시 DB 반영

        log.info("피드백 생성 완료 - feedbackId: {}, sessionId: {}, totalScore: {}",
                savedFeedback.getId(), request.sessionId(), request.totalScore());

        return FeedbackResponse.from(savedFeedback);
    }

    /**
     * 사용자가 개선안을 선택합니다.
     *
     * @param sessionId 세션 ID
     * @param request 선택 요청
     * @return 업데이트된 피드백 응답
     * @throws TrainException 피드백을 찾을 수 없거나 요청이 유효하지 않은 경우
     */
    public FeedbackResponse choosealternative(String sessionId, FeedbackChoiceRequest request) {
        log.info("개선안 선택 시작 - sessionId: {}, choice: {}", sessionId, request.chosenAlternative());

        // 1. 피드백 조회
        Feedback feedback = getFeedbackBySessionId(sessionId);

        // 2. 요청 유효성 검증
        if (!request.isValid()) {
            log.error("유효하지 않은 선택 요청 - sessionId: {}, choice: {}", sessionId, request.chosenAlternative());
            throw new TrainException(INVALID_INPUT_VALUE, "CUSTOM 선택 시 최종안은 필수입니다.");
        }

        // 3. 개선안 설정
        if (request.chosenAlternative() == Feedback.ChosenAlternative.CUSTOM) {
            feedback.setCustomChoice(request.finalChoice());
        } else {
            feedback.choosealternative(request.chosenAlternative());
        }

        log.info("개선안 선택 완료 - sessionId: {}, choice: {}", sessionId, request.chosenAlternative());

        return FeedbackResponse.from(feedback);
    }

    /**
     * 특정 세션의 피드백을 조회합니다.
     *
     * @param sessionId 세션 ID
     * @return 피드백 응답
     * @throws TrainException 피드백을 찾을 수 없는 경우
     */
    @Transactional(readOnly = true)
    public FeedbackResponse getFeedback(String sessionId) {
        log.info("피드백 조회 - sessionId: {}", sessionId);

        Feedback feedback = getFeedbackBySessionId(sessionId);
        return FeedbackResponse.from(feedback);
    }

    /**
     * 특정 사용자의 피드백 히스토리를 조회합니다.
     *
     * @param userId 사용자 ID
     * @param pageable 페이징 정보
     * @return 피드백 히스토리 목록
     */
    @Transactional(readOnly = true)
    public Page<FeedbackHistoryResponse> getFeedbackHistory(Long userId, Pageable pageable) {
        log.info("피드백 히스토리 조회 - userId: {}, page: {}, size: {}",
                userId, pageable.getPageNumber(), pageable.getPageSize());

        Page<Feedback> feedbacks = feedbackRepository.findByUserId(userId, pageable);

        return feedbacks.map(FeedbackHistoryResponse::from);
    }

    /**
     * 특정 사용자의 피드백 히스토리를 전체 조회합니다 (페이징 없음).
     *
     * @param userId 사용자 ID
     * @return 피드백 히스토리 목록
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
     * 특정 사용자의 피드백 통계를 조회합니다.
     *
     * @param userId 사용자 ID
     * @return 피드백 통계 응답
     */
    @Transactional(readOnly = true)
    public FeedbackStatsResponse getFeedbackStats(Long userId) {
        log.info("피드백 통계 조회 - userId: {}", userId);

        // 기본 통계 조회
        Long totalCount = feedbackRepository.countByUserId(userId);
        if (totalCount == 0) {
            return createEmptyStats();
        }

        Double averageScore = feedbackRepository.findAverageScoreByUserId(userId);
        Object[] detailedAverages = feedbackRepository.findDetailedScoreAveragesByUserId(userId);

        // 최고/최저 점수 계산
        List<Feedback> allFeedbacks = feedbackRepository.findByUserIdOrderByCreatedAtDesc(userId);
        Integer maxScore = allFeedbacks.stream()
                .mapToInt(Feedback::getTotalScore)
                .max()
                .orElse(0);
        Integer minScore = allFeedbacks.stream()
                .mapToInt(Feedback::getTotalScore)
                .min()
                .orElse(0);

        // 최근 기간별 통계
        LocalDateTime weekAgo = LocalDateTime.now().minusWeeks(1);
        LocalDateTime monthAgo = LocalDateTime.now().minusMonths(1);

        Long recentWeekCount = (long) feedbackRepository.findByUserIdAndDateRange(userId, weekAgo, LocalDateTime.now()).size();
        Long recentMonthCount = (long) feedbackRepository.findByUserIdAndDateRange(userId, monthAgo, LocalDateTime.now()).size();

        // 완료/미완료 통계
        Long incompleteCount = (long) feedbackRepository.findIncompleteByUserId(userId).size();
        Long completedCount = totalCount - incompleteCount;

        // 등급별 분포 계산
        FeedbackStatsResponse.GradeDistribution gradeDistribution = calculateGradeDistribution(allFeedbacks);

        // 월별 점수 추이 계산 (최근 6개월)
        List<FeedbackStatsResponse.MonthlyScore> monthlyScores = calculateMonthlyScores(userId);

        return FeedbackStatsResponse.of(
                totalCount,
                averageScore,
                (Double) detailedAverages[0], // speechRateScore
                (Double) detailedAverages[1], // fillerWordsScore
                (Double) detailedAverages[2], // politenessScore
                (Double) detailedAverages[3], // clarityScore
                maxScore,
                minScore,
                recentWeekCount,
                recentMonthCount,
                completedCount,
                incompleteCount,
                gradeDistribution,
                monthlyScores
        );
    }

    /**
     * sessionId로 피드백을 조회합니다 (내부용).
     *
     * @param sessionId 세션 ID
     * @return 피드백 엔티티
     * @throws TrainException 피드백을 찾을 수 없는 경우
     */
    private Feedback getFeedbackBySessionId(String sessionId) {
        return feedbackRepository.findByDialogueSessionSessionId(sessionId)
                .orElseThrow(() -> {
                    log.error("피드백을 찾을 수 없음 - sessionId: {}", sessionId);
                    return new TrainException(FEEDBACK_NOT_FOUND);
                });
    }

    /**
     * 빈 통계 응답을 생성합니다.
     */
    private FeedbackStatsResponse createEmptyStats() {
        return FeedbackStatsResponse.of(
                0L, 0.0, 0.0, 0.0, 0.0, 0.0, 0, 0, 0L, 0L, 0L, 0L,
                FeedbackStatsResponse.GradeDistribution.builder()
                        .gradeA(0L).gradeB(0L).gradeC(0L).gradeD(0L).gradeF(0L).build(),
                new ArrayList<>()
        );
    }

    /**
     * 등급별 분포를 계산합니다.
     */
    private FeedbackStatsResponse.GradeDistribution calculateGradeDistribution(List<Feedback> feedbacks) {
        long gradeA = feedbacks.stream().mapToInt(Feedback::getTotalScore).filter(score -> score >= 90).count();
        long gradeB = feedbacks.stream().mapToInt(Feedback::getTotalScore).filter(score -> score >= 80 && score < 90).count();
        long gradeC = feedbacks.stream().mapToInt(Feedback::getTotalScore).filter(score -> score >= 70 && score < 80).count();
        long gradeD = feedbacks.stream().mapToInt(Feedback::getTotalScore).filter(score -> score >= 60 && score < 70).count();
        long gradeF = feedbacks.stream().mapToInt(Feedback::getTotalScore).filter(score -> score < 60).count();

        return FeedbackStatsResponse.GradeDistribution.builder()
                .gradeA(gradeA)
                .gradeB(gradeB)
                .gradeC(gradeC)
                .gradeD(gradeD)
                .gradeF(gradeF)
                .build();
    }

    /**
     * 월별 점수 추이를 계산합니다 (최근 6개월).
     */
    private List<FeedbackStatsResponse.MonthlyScore> calculateMonthlyScores(Long userId) {
        List<FeedbackStatsResponse.MonthlyScore> monthlyScores = new ArrayList<>();

        for (int i = 5; i >= 0; i--) {
            YearMonth targetMonth = YearMonth.now().minusMonths(i);
            LocalDateTime startOfMonth = targetMonth.atDay(1).atStartOfDay();
            LocalDateTime endOfMonth = targetMonth.atEndOfMonth().atTime(23, 59, 59);

            List<Feedback> monthlyFeedbacks = feedbackRepository.findByUserIdAndDateRange(
                    userId, startOfMonth, endOfMonth);

            if (!monthlyFeedbacks.isEmpty()) {
                double average = monthlyFeedbacks.stream()
                        .mapToInt(Feedback::getTotalScore)
                        .average()
                        .orElse(0.0);

                monthlyScores.add(FeedbackStatsResponse.MonthlyScore.builder()
                        .yearMonth(targetMonth.toString())
                        .averageScore(average)
                        .count((long) monthlyFeedbacks.size())
                        .build());
            } else {
                monthlyScores.add(FeedbackStatsResponse.MonthlyScore.builder()
                        .yearMonth(targetMonth.toString())
                        .averageScore(0.0)
                        .count(0L)
                        .build());
            }
        }

        return monthlyScores;
    }
}