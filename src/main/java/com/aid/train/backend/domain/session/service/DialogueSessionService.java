package com.aid.train.backend.domain.session.service;

import com.aid.train.backend.domain.scenario.entity.Scenario;
import com.aid.train.backend.domain.scenario.repository.ScenarioRepository;
import com.aid.train.backend.domain.session.entity.DialogueSession;
import com.aid.train.backend.domain.session.enums.SessionStatus;
import com.aid.train.backend.domain.session.repository.DialogueSessionRepository;
import com.aid.train.backend.domain.user.entity.User;
import com.aid.train.backend.domain.user.repository.UserRepository;
import com.aid.train.backend.global.exception.TrainException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static com.aid.train.backend.global.exception.enums.ErrorCode.*;

/**
 * 대화 세션 비즈니스 로직 서비스
 *
 * 역할:
 * - DialogueSession 생성 및 관리
 * - 세션 상태 변경
 * - 세션 조회
 *
 * @author 김경민
 * @since 2025-10-15
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class DialogueSessionService {

    private final DialogueSessionRepository dialogueSessionRepository;
    private final UserRepository userRepository;
    private final ScenarioRepository scenarioRepository;

    /**
     * 새로운 대화 세션을 생성합니다.
     *
     * 처리 순서:
     * 1. User와 Scenario 조회 (없으면 예외)
     * 2. DialogueSession 엔티티 생성 (UUID sessionId 자동 생성)
     * 3. DB 저장
     * 4. 생성된 세션 반환
     *
     * @param userId 사용자 ID
     * @param scenarioId 시나리오 ID
     * @return 생성된 DialogueSession
     * @throws TrainException User 또는 Scenario를 찾을 수 없을 때
     */
    public DialogueSession createSession(Long userId, Long scenarioId) {
        log.info("DialogueSession 생성 시작 - userId: {}, scenarioId: {}",
                userId, scenarioId);

        // 1. User 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("사용자를 찾을 수 없음 - userId: {}", userId);
                    return new TrainException(USER_NOT_FOUND);
                });

        // 2. Scenario 조회
        Scenario scenario = scenarioRepository.findById(scenarioId)
                .orElseThrow(() -> {
                    log.error("시나리오를 찾을 수 없음 - scenarioId: {}", scenarioId);
                    return new TrainException(SCENARIO_NOT_FOUND);
                });

        // 3. DialogueSession 생성
        // Builder 패턴 사용 - UUID sessionId는 @Builder에서 자동 생성됨
        DialogueSession session = DialogueSession.builder()
                .user(user)
                .scenario(scenario)
                .status(SessionStatus.ONGOING)
                .startedAt(LocalDateTime.now())
                .build();

        // 4. DB 저장
        DialogueSession savedSession = dialogueSessionRepository.save(session);
        dialogueSessionRepository.flush(); // 즉시 DB 반영

        log.info("DialogueSession 생성 완료 - sessionId: {}, userId: {}, scenarioId: {}",
                savedSession.getSessionId(), userId, scenarioId);

        return savedSession;
    }

    /**
     * sessionId로 세션을 조회합니다.
     *
     * @param sessionId 세션 ID (UUID)
     * @return DialogueSession
     * @throws TrainException 세션을 찾을 수 없을 때
     */
    @Transactional(readOnly = true)
    public DialogueSession getSession(String sessionId) {
        return dialogueSessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> {
                    log.error("세션을 찾을 수 없음 - sessionId: {}", sessionId);
                    return new TrainException(SESSION_NOT_FOUND);
                });
    }

    /**
     * sessionId로 세션을 조회합니다. (User, Scenario Fetch Join)
     * N+1 문제 방지
     *
     * @param sessionId 세션 ID (UUID)
     * @return DialogueSession (User, Scenario 포함)
     * @throws TrainException 세션을 찾을 수 없을 때
     */
    @Transactional(readOnly = true)
    public DialogueSession getSessionWithUserAndScenario(String sessionId) {
        return dialogueSessionRepository.findWithUserAndScenarioBySessionId(sessionId)
                .orElseThrow(() -> {
                    log.error("세션을 찾을 수 없음 - sessionId: {}", sessionId);
                    return new TrainException(SESSION_NOT_FOUND);
                });
    }

    /**
     * 세션을 정상 종료 처리합니다.
     *
     * @param sessionId 세션 ID (UUID)
     */
    public void completeSession(String sessionId) {
        log.info("세션 정상 종료 처리 - sessionId: {}", sessionId);

        DialogueSession session = getSession(sessionId);
        session.complete(); // status = COMPLETED, endedAt = now()

        log.info("세션 정상 종료 완료 - sessionId: {}", sessionId);
    }

    /**
     * 세션을 실패 처리합니다.
     *
     * @param sessionId 세션 ID (UUID)
     */
    public void failSession(String sessionId) {
        log.info("세션 실패 처리 - sessionId: {}", sessionId);

        DialogueSession session = getSession(sessionId);
        session.fail(); // status = FAILED, endedAt = now()

        log.info("세션 실패 처리 완료 - sessionId: {}", sessionId);
    }

    /**
     * 세션에 오디오 정보를 저장합니다.
     *
     * @param sessionId 세션 ID
     * @param audioUrl 녹음 파일 URL
     * @param durationSeconds 녹음 길이 (초)
     */
    public void saveAudioInfo(String sessionId, String audioUrl, Integer durationSeconds) {
        log.info("오디오 정보 저장 - sessionId: {}, audioUrl: {}, duration: {}초",
                sessionId, audioUrl, durationSeconds);

        DialogueSession session = getSession(sessionId);
        session.setAudioInfo(audioUrl, durationSeconds);

        log.info("오디오 정보 저장 완료 - sessionId: {}", sessionId);
    }

    /**
     * 세션에 실시간 메트릭을 저장합니다.
     *
     * @param sessionId 세션 ID
     * @param metricsJson 메트릭 JSON
     */
    public void saveRealtimeMetrics(String sessionId, String metricsJson) {
        log.info("실시간 메트릭 저장 - sessionId: {}", sessionId);

        DialogueSession session = getSession(sessionId);
        session.setRealtimeMetrics(metricsJson);

        log.info("실시간 메트릭 저장 완료 - sessionId: {}", sessionId);
    }


}