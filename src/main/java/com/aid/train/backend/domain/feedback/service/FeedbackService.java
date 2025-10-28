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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.aid.train.backend.global.exception.enums.ErrorCode.*;

/**
 * 피드백 도메인의 생성/조회/통계 연산을 담당하는 서비스입니다.
 *
 * <p>특징</p>
 * <ul>
 *   <li><b>기존 호환 기능 유지</b>: 외부가 생성한 피드백 저장/조회 흐름을 보존함</li>
 *   <li><b>확장 기능 제공</b>: AI 기반 종합 분석(전체 흐름 + 문장별 분석 + 개선안)을 생성하여 저장/응답함</li>
 *   <li><b>트랜잭션 경계</b>: 클래스 레벨 {@code @Transactional}로 쓰기 기본, 조회 메서드는 {@code readOnly}로 분리</li>
 * </ul>
 *
 * <p>예외</p>
 * <ul>
 *   <li>세션 상태가 완료가 아닌 경우: {@link TrainException}({@code SESSION_INVALID_STATUS})</li>
 *   <li>이미 피드백이 존재하는 세션: {@link TrainException}({@code FEEDBACK_ALREADY_EXISTS})</li>
 *   <li>AI 분석 실패: {@link TrainException}({@code AI_ANALYSIS_FAILED})</li>
 *   <li>피드백 미존재: {@link TrainException}({@code FEEDBACK_NOT_FOUND})</li>
 * </ul>
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
     * AI를 이용해 단일 호출로 종합 피드백을 생성하고 저장한 뒤, 저장 결과에 종합 분석을 결합하여 반환합니다.
     *
     * <p>동작 순서</p>
     * <ol>
     *   <li>세션 로드 및 완료 상태 검증</li>
     *   <li>기존 피드백 존재 여부 검증</li>
     *   <li>AI 종합 분석 생성(전체/문장별/개선안)</li>
     *   <li>DB 저장을 위한 최소 필드로 {@link FeedbackCreateRequest} 구성 및 저장</li>
     *   <li>저장된 엔티티 + 분석 결과를 결합하여 {@link FeedbackResponse} 반환</li>
     * </ol>
     *
     * @param sessionId 분석 및 피드백 생성 대상 세션 ID
     * @return 저장된 피드백의 식별자·점수·개선안과 함께 종합 분석이 포함된 응답
     * @throws TrainException 세션 상태가 완료가 아니거나, 이미 피드백이 존재하거나, AI 분석에 실패한 경우 발생
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
            FeedbackResponse comprehensiveFeedback = feedbackPromptService.generateFeedbackFromAI(session);

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
     * 세션 ID로 피드백을 조회합니다(종합 분석 필드 제외).
     *
     * @param sessionId 조회 대상 세션 ID
     * @return 기본 정보만 포함한 {@link FeedbackResponse}
     * @throws TrainException 해당 세션의 피드백이 없을 때 발생({@code FEEDBACK_NOT_FOUND})
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
     * 개선안(A/B/C 또는 CUSTOM)을 선택합니다.
     *
     * <p>규칙</p>
     * <ul>
     *   <li>{@code CUSTOM} 선택 시 요청 본문의 {@code finalChoice}가 필수</li>
     *   <li>A/B/C 선택 시 해당 선택을 엔티티에 반영</li>
     * </ul>
     *
     * @param sessionId 대상 세션 ID
     * @param request   개선안 선택 요청
     * @return 선택 결과가 반영된 {@link FeedbackResponse}
     * @throws TrainException 피드백 미존재 또는 사용자 정의 내용 누락 시 발생
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
     * 사용자 피드백 히스토리를 페이지 단위로 조회합니다.
     *
     * @param userId   사용자 ID
     * @param pageable 페이지 정보(페이지 번호, 크기, 정렬)
     * @return 페이지 형태의 {@link FeedbackHistoryResponse} 컬렉션
     */
    @Transactional(readOnly = true)
    public Page<FeedbackHistoryResponse> getFeedbackHistory(Long userId, Pageable pageable) {
        log.info("피드백 히스토리 조회 (페이징) - userId: {}, page: {}, size: {}",
                userId, pageable.getPageNumber(), pageable.getPageSize());

        Page<Feedback> feedbacks = feedbackRepository.findByUserId(userId, pageable);

        return feedbacks.map(FeedbackHistoryResponse::from);
    }

    /**
     * 사용자 전체 피드백 히스토리를 내림차순(생성일)으로 조회합니다.
     *
     * @param userId 사용자 ID
     * @return 전체 히스토리 목록(최신 순)
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
     * 사용자 피드백에 대한 통계를 계산합니다.
     *
     * <p>포함 내용</p>
     * <ul>
     *   <li>총 개수, 평균 점수, 상세 항목별 평균(발화속도/추임새/공손도/명료성)</li>
     *   <li>최근 7일/30일 건수, 개선안 선택 완료 수/미완료 수</li>
     *   <li>등급 분포(A~F) 및 최근 6개월 월별 평균 추이</li>
     *   <li>최고/최저 점수</li>
     * </ul>
     *
     * @param userId 사용자 ID
     * @return 계산된 통계 응답
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
        log.info("detailedAverages: {}", Arrays.toString(detailedAverages));

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
                extractScoreAverage(detailedAverages, 0),
                extractScoreAverage(detailedAverages, 1),
                extractScoreAverage(detailedAverages, 2),
                extractScoreAverage(detailedAverages, 3),
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

    /**
     * Object 배열에서 안전하게 점수 평균을 추출합니다.
     *
     * @param detailedAverages 점수 평균 배열
     * @param index            추출할 인덱스
     * @return 추출된 점수 (실패 시 0.0)
     */
    private Double extractScoreAverage(Object[] detailedAverages, int index) {
        if (detailedAverages == null || detailedAverages.length <= index || detailedAverages[index] == null) {
            return 0.0;
        }
        try {
            return Math.round(((Number) detailedAverages[index]).doubleValue() * 10.0) / 10.0;
        } catch (ClassCastException e) {
            log.warn("점수 변환 실패 - index: {}, value: {}", index, detailedAverages[index]);
            return 0.0;
        }
    }

    // ========================================
    // Private Helper Methods
    // ========================================

    /**
     * {@link FeedbackCreateRequest}와 세션 정보를 바탕으로 {@link Feedback} 엔티티를 생성·저장합니다.
     *
     * @param request 저장에 필요한 최소 정보가 담긴 요청 DTO
     * @param session 연관될 대화 세션 엔티티
     * @return 저장된 {@link Feedback} 엔티티
     */
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

    /**
     * 데이터가 없을 때 사용할 기본 통계 응답을 생성합니다.
     *
     * @return 모든 수치가 0으로 초기화된 통계 응답
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
     * 등급 분포를 계산합니다.
     *
     * @param feedbacks 대상 피드백 목록
     * @return A·B·C·D·F의 개수로 구성된 분포 객체
     */
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

    /**
     * 최근 6개월의 월별 평균 점수와 건수를 계산합니다.
     *
     * @param feedbacks 대상 피드백 목록
     * @return 최근 6개월의 {@code yearMonth}, {@code averageScore}, {@code count} 목록
     */
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
