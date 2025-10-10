package com.aid.train.backend.repository;

import com.aid.train.backend.entity.*;
import com.aid.train.backend.repository.dialogueSession.DialogueSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DialogueSessionRepository 단위 테스트
 *
 * @author 김경민
 * @since 2025-10-08
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class DialogueSessionRepositoryTest {

    @Autowired
    private DialogueSessionRepository dialogueSessionRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User testUser;
    private Scenario testScenario;
    private DialogueSession testSession;

    @BeforeEach
    void setUp() {
        // 테스트용 User 생성
        testUser = User.builder()
                .email("test@example.com")
                .password("testPassword123") // LOCAL 계정에 필수 비밀번호 추가
                .name("테스트 사용자")
                .primaryProvider(Provider.LOCAL)
                .emailVerified(true)
                .status(UserStatus.ACTIVE)
                .build();
        entityManager.persistAndFlush(testUser);

        // 테스트용 Scenario 생성
        testScenario = Scenario.builder()
                .title("테스트 시나리오")
                .description("테스트용 시나리오입니다")
                .prompt("안녕하세요. 테스트 시나리오입니다.")
                .voice(Scenario.Voice.ONYX)
                .difficulty(Scenario.Difficulty.EASY)
                .category(Scenario.Category.WORK)
                .build();
        entityManager.persistAndFlush(testScenario);

        // 테스트용 DialogueSession 생성
        testSession = DialogueSession.builder()
                .user(testUser)
                .scenario(testScenario)
                .status(SessionStatus.ONGOING)
                .startedAt(LocalDateTime.now().minusMinutes(10))
                .build();
        entityManager.persistAndFlush(testSession);
    }

    @Test
    @DisplayName("세션 ID로 세션을 조회할 수 있다")
    void findBySessionId() {
        // when
        Optional<DialogueSession> found = dialogueSessionRepository.findBySessionId(testSession.getSessionId());

        // then
        assertTrue(found.isPresent());
        assertEquals(testSession.getSessionId(), found.get().getSessionId());
        assertEquals(testUser.getId(), found.get().getUser().getId());
        assertEquals(testScenario.getId(), found.get().getScenario().getId());
    }

    @Test
    @DisplayName("존재하지 않는 세션 ID로 조회하면 빈 Optional을 반환한다")
    void findBySessionId_NotFound() {
        // when
        Optional<DialogueSession> found = dialogueSessionRepository.findBySessionId("non-existent-session-id");

        // then
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("사용자 ID로 세션 목록을 최신순으로 조회할 수 있다")
    void findByUserIdOrderByStartedAtDesc() {
        // given - 추가 세션 생성
        DialogueSession olderSession = DialogueSession.builder()
                .user(testUser)
                .scenario(testScenario)
                .status(SessionStatus.COMPLETED)
                .startedAt(LocalDateTime.now().minusHours(1))
                .build();
        entityManager.persistAndFlush(olderSession);

        DialogueSession newerSession = DialogueSession.builder()
                .user(testUser)
                .scenario(testScenario)
                .status(SessionStatus.ONGOING)
                .startedAt(LocalDateTime.now().minusMinutes(5))
                .build();
        entityManager.persistAndFlush(newerSession);

        // when
        List<DialogueSession> sessions = dialogueSessionRepository.findByUserIdOrderByStartedAtDesc(testUser.getId());

        // then
        assertEquals(3, sessions.size());
        // 최신순으로 정렬되어 있는지 확인
        assertTrue(sessions.get(0).getStartedAt().isAfter(sessions.get(1).getStartedAt()));
        assertTrue(sessions.get(1).getStartedAt().isAfter(sessions.get(2).getStartedAt()));
    }

    @Test
    @DisplayName("사용자 ID로 페이징된 세션 목록을 조회할 수 있다")
    void findByUserId_WithPaging() {
        // given - 추가 세션들 생성
        for (int i = 0; i < 5; i++) {
            DialogueSession session = DialogueSession.builder()
                    .user(testUser)
                    .scenario(testScenario)
                    .status(SessionStatus.COMPLETED)
                    .startedAt(LocalDateTime.now().minusMinutes(i))
                    .build();
            entityManager.persistAndFlush(session);
        }

        // when
        Page<DialogueSession> page = dialogueSessionRepository.findByUserId(
                testUser.getId(), PageRequest.of(0, 3));

        // then
        assertEquals(3, page.getSize());
        assertEquals(6, page.getTotalElements()); // 기존 1개 + 추가 5개
        assertTrue(page.hasContent());
    }

    @Test
    @DisplayName("사용자 ID와 상태로 세션을 조회할 수 있다")
    void findByUserIdAndStatus() {
        // given - 다른 상태의 세션 추가 생성
        DialogueSession completedSession = DialogueSession.builder()
                .user(testUser)
                .scenario(testScenario)
                .status(SessionStatus.COMPLETED)
                .startedAt(LocalDateTime.now().minusMinutes(20))
                .build();
        entityManager.persistAndFlush(completedSession);

        DialogueSession failedSession = DialogueSession.builder()
                .user(testUser)
                .scenario(testScenario)
                .status(SessionStatus.FAILED)
                .startedAt(LocalDateTime.now().minusMinutes(30))
                .build();
        entityManager.persistAndFlush(failedSession);

        // when
        List<DialogueSession> inProgressSessions = dialogueSessionRepository
                .findByUserIdAndStatus(testUser.getId(), SessionStatus.ONGOING);
        List<DialogueSession> completedSessions = dialogueSessionRepository
                .findByUserIdAndStatus(testUser.getId(), SessionStatus.COMPLETED);
        List<DialogueSession> failedSessions = dialogueSessionRepository
                .findByUserIdAndStatus(testUser.getId(), SessionStatus.FAILED);

        // then
        assertEquals(1, inProgressSessions.size());
        assertEquals(1, completedSessions.size());
        assertEquals(1, failedSessions.size());
    }

    @Test
    @DisplayName("시나리오 ID로 세션 목록을 조회할 수 있다")
    void findByScenarioId() {
        // given - 다른 시나리오와 세션 생성
        Scenario anotherScenario = Scenario.builder()
                .title("다른 시나리오")
                .description("다른 테스트 시나리오")
                .prompt("다른 프롬프트")
                .voice(Scenario.Voice.ECHO)
                .difficulty(Scenario.Difficulty.MEDIUM)
                .category(Scenario.Category.RELATIONSHIP)
                .build();
        entityManager.persistAndFlush(anotherScenario);

        DialogueSession sessionWithAnotherScenario = DialogueSession.builder()
                .user(testUser)
                .scenario(anotherScenario)
                .status(SessionStatus.ONGOING)
                .startedAt(LocalDateTime.now())
                .build();
        entityManager.persistAndFlush(sessionWithAnotherScenario);

        // when
        List<DialogueSession> sessionsForTestScenario = dialogueSessionRepository
                .findByScenarioId(testScenario.getId());
        List<DialogueSession> sessionsForAnotherScenario = dialogueSessionRepository
                .findByScenarioId(anotherScenario.getId());

        // then
        assertEquals(1, sessionsForTestScenario.size());
        assertEquals(1, sessionsForAnotherScenario.size());
        assertEquals(testScenario.getId(), sessionsForTestScenario.get(0).getScenario().getId());
        assertEquals(anotherScenario.getId(), sessionsForAnotherScenario.get(0).getScenario().getId());
    }

    @Test
    @DisplayName("특정 기간 내 특정 상태의 세션을 조회할 수 있다")
    void findByStatusAndStartedAtBetween() {
        // given
        LocalDateTime start = LocalDateTime.now().minusHours(2);
        LocalDateTime end = LocalDateTime.now().minusMinutes(30);

        // 기간 내 완료된 세션
        DialogueSession sessionInRange = DialogueSession.builder()
                .user(testUser)
                .scenario(testScenario)
                .status(SessionStatus.COMPLETED)
                .startedAt(LocalDateTime.now().minusHours(1))
                .build();
        entityManager.persistAndFlush(sessionInRange);

        // 기간 밖 완료된 세션
        DialogueSession sessionOutOfRange = DialogueSession.builder()
                .user(testUser)
                .scenario(testScenario)
                .status(SessionStatus.COMPLETED)
                .startedAt(LocalDateTime.now().minusMinutes(5))
                .build();
        entityManager.persistAndFlush(sessionOutOfRange);

        // when
        List<DialogueSession> sessionsInRange = dialogueSessionRepository
                .findByStatusAndStartedAtBetween(SessionStatus.COMPLETED, start, end);

        // then
        assertEquals(1, sessionsInRange.size());
        assertEquals(sessionInRange.getSessionId(), sessionsInRange.get(0).getSessionId());
    }

    @Test
    @DisplayName("사용자 ID와 상태로 세션 개수를 조회할 수 있다")
    void countByUserIdAndStatus() {
        // given - 추가 세션들 생성
        for (int i = 0; i < 3; i++) {
            DialogueSession session = DialogueSession.builder()
                    .user(testUser)
                    .scenario(testScenario)
                    .status(SessionStatus.COMPLETED)
                    .startedAt(LocalDateTime.now().minusMinutes(i))
                    .build();
            entityManager.persistAndFlush(session);
        }

        // when
        long inProgressCount = dialogueSessionRepository.countByUserIdAndStatus(testUser.getId(), SessionStatus.ONGOING);
        long completedCount = dialogueSessionRepository.countByUserIdAndStatus(testUser.getId(), SessionStatus.COMPLETED);

        // then
        assertEquals(1, inProgressCount); // setUp에서 생성한 1개
        assertEquals(3, completedCount); // 위에서 생성한 3개
    }

    @Test
    @DisplayName("Fetch Join을 사용하여 User와 Scenario를 함께 조회할 수 있다")
    void findWithUserAndScenarioBySessionId() {
        // when
        Optional<DialogueSession> found = dialogueSessionRepository
                .findWithUserAndScenarioBySessionId(testSession.getSessionId());

        // then
        assertTrue(found.isPresent());
        assertNotNull(found.get().getUser());
        assertNotNull(found.get().getScenario());
        assertEquals(testUser.getEmail(), found.get().getUser().getEmail());
        assertEquals(testScenario.getTitle(), found.get().getScenario().getTitle());
    }

    @Test
    @DisplayName("세션 완료 기능이 정상 작동한다")
    void completeSession() {
        // given
        assertEquals(SessionStatus.ONGOING, testSession.getStatus());
        assertNull(testSession.getEndedAt());

        // when
        testSession.complete();
        entityManager.persistAndFlush(testSession);

        // then
        assertEquals(SessionStatus.COMPLETED, testSession.getStatus());
        assertNotNull(testSession.getEndedAt());
    }

    @Test
    @DisplayName("세션 실패 기능이 정상 작동한다")
    void failSession() {
        // given
        assertEquals(SessionStatus.ONGOING, testSession.getStatus());
        assertNull(testSession.getEndedAt());

        // when
        testSession.fail();
        entityManager.persistAndFlush(testSession);

        // then
        assertEquals(SessionStatus.FAILED, testSession.getStatus());
        assertNotNull(testSession.getEndedAt());
    }

    @Test
    @DisplayName("Janus Room ID로 세션을 조회할 수 있다")
    void findByJanusRoomId() {
        // given
        Long janusRoomId = 12345L;
        testSession.setJanusInfo(janusRoomId, 1001L, 1002L);
        entityManager.persistAndFlush(testSession);

        // when
        Optional<DialogueSession> found = dialogueSessionRepository.findByJanusRoomId(janusRoomId);

        // then
        assertTrue(found.isPresent());
        assertEquals(janusRoomId, found.get().getJanusRoomId());
    }

    @Test
    @DisplayName("AI Realtime Session ID로 세션을 조회할 수 있다")
    void findByAiRealtimeSessionId() {
        // given
        String aiSessionId = "ai-session-12345";
        testSession.setAiRealtimeSessionId(aiSessionId);
        entityManager.persistAndFlush(testSession);

        // when
        Optional<DialogueSession> found = dialogueSessionRepository.findByAiRealtimeSessionId(aiSessionId);

        // then
        assertTrue(found.isPresent());
        assertEquals(aiSessionId, found.get().getAiRealtimeSessionId());
    }
}