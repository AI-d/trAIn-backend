package com.aid.train.backend.domain.user.service;

import com.aid.train.backend.domain.user.dto.response.UserStatisticsResponseDto;
import com.aid.train.backend.domain.user.entity.User;
import com.aid.train.backend.domain.user.repository.UserRepository;
import com.aid.train.backend.domain.history.entity.History;
import com.aid.train.backend.domain.history.repository.HistoryRepository;
import com.aid.train.backend.global.exception.TrainException;
import com.aid.train.backend.global.exception.enums.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

/**
 * 사용자 통계 서비스입니다.
 * 사용자의 면접 연습 통계 정보를 제공합니다.
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class UserStatisticsService {

    private final UserRepository userRepository;
    private final HistoryRepository historyRepository;

    /**
     * 사용자 통계 정보를 조회합니다.
     *
     * @param userId 사용자 ID
     * @return 사용자 통계 응답 DTO
     * @throws TrainException 사용자를 찾을 수 없는 경우
     */
    public UserStatisticsResponseDto getUserStatistics(Long userId) {
        log.info("[통계 조회] 사용자 통계 조회 시작 - 사용자 ID: {}", userId);

        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("[통계 조회] 사용자를 찾을 수 없음 - ID: {}", userId);
                    return new TrainException(ErrorCode.USER_NOT_FOUND);
                });

        // 2. 프로젝션을 사용한 기본 통계 조회 (단일 쿼리)
        HistoryRepository.UserStatistics stats = historyRepository.getUserStatistics(user);

        // 3. 전체 히스토리 조회 (최고/최저 점수 계산용)
        List<History> histories = historyRepository.findByUserOrderByCompletedAtDesc(user);

        // 4. 최고/최저 점수 계산
        Integer highestScore = histories.stream()
                .map(History::getTotalScore)
                .max(Comparator.naturalOrder())
                .orElse(0);

        Integer lowestScore = histories.stream()
                .map(History::getTotalScore)
                .min(Comparator.naturalOrder())
                .orElse(0);

        // 5. 세부 평균 점수 계산
        Double averageAnswerScore = histories.stream()
                .mapToInt(History::getAnswerScore)
                .average()
                .orElse(0.0);

        Double averagePresentationScore = histories.stream()
                .mapToInt(History::getPresentationScore)
                .average()
                .orElse(0.0);

        Double averageLanguageScore = histories.stream()
                .mapToInt(History::getLanguageScore)
                .average()
                .orElse(0.0);

        log.info("[통계 조회] 사용자 통계 조회 완료 - 사용자 ID: {}, 총 대화: {}, 평균 점수: {}",
                userId, stats.getTotalCount(), stats.getAverageScore());

        return UserStatisticsResponseDto.of(
                stats.getTotalCount(),
                stats.getAverageScore(),
                stats.getExcellentCount(),
                highestScore,
                lowestScore,
                averageAnswerScore,
                averagePresentationScore,
                averageLanguageScore
        );
    }
}
