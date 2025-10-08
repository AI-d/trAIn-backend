package com.aid.train.backend.repository;

import com.aid.train.backend.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TranscriptRepository 단위 테스트
 *
 * @author 김경민
 * @since 2025-10-08
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class TranscriptRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TranscriptRepository transcriptRepository;

    private DialogueSession testSession;
    private User testUser;
    private Scenario testScenario;

    @BeforeEach
    void setUp() {
        // 테스트용 사용자 생성
        testUser = User.builder()
                .email("test@example.com")
                .name("testUser")
                .primaryProvider(Provider.LOCAL)
                .password("testPassword")
                .status(UserStatus.ACTIVE)
                .build();
        entityManager.persistAndFlush(testUser);

        // 테스트용 시나리오 생성
        testScenario = Scenario.builder()
                .title("테스트 시나리오")
                .description("테스트용 시나리오입니다")
                .prompt("테스트 프롬프트")
                .voice(Scenario.Voice.ONYX)
                .difficulty(Scenario.Difficulty.EASY)
                .category(Scenario.Category.WORK)
                .build();
        entityManager.persistAndFlush(testScenario);

        // 테스트용 대화 세션 생성
        testSession = DialogueSession.builder()
                .user(testUser)
                .scenario(testScenario)
                .status(SessionStatus.ONGOING)
                .startedAt(LocalDateTime.now())
                .build();
        entityManager.persistAndFlush(testSession);
    }

    @Test
    @DisplayName("세션 ID로 발화 내역을 시간순으로 조회할 수 있다")
    void findByDialogueSessionIdOrderByTimestampAsc() {
        // Given
        LocalDateTime now = LocalDateTime.now();

        Transcript transcript1 = Transcript.builder()
                .dialogueSession(testSession)
                .speaker(Speaker.USER)
                .content("안녕하세요")
                .timestamp(now.plusMinutes(1))
                .build();

        Transcript transcript2 = Transcript.builder()
                .dialogueSession(testSession)
                .speaker(Speaker.AI)
                .content("안녕하세요! 도움이 필요하시나요?")
                .timestamp(now.plusMinutes(2))
                .build();

        Transcript transcript3 = Transcript.builder()
                .dialogueSession(testSession)
                .speaker(Speaker.USER)
                .content("네, 도움이 필요합니다")
                .timestamp(now.plusMinutes(3))
                .build();

        entityManager.persist(transcript1);
        entityManager.persist(transcript2);
        entityManager.persist(transcript3);
        entityManager.flush();

        // When
        List<Transcript> results = transcriptRepository.findByDialogueSessionIdOrderByTimestampAsc(testSession.getId());

        // Then
        assertEquals(3, results.size());
        assertEquals("안녕하세요", results.get(0).getContent());
        assertEquals("안녕하세요! 도움이 필요하시나요?", results.get(1).getContent());
        assertEquals("네, 도움이 필요합니다", results.get(2).getContent());
    }

    @Test
    @DisplayName("특정 세션의 특정 발화자 발화만 조회할 수 있다")
    void findByDialogueSessionIdAndSpeakerOrderByTimestampAsc() {
        // Given
        LocalDateTime now = LocalDateTime.now();

        Transcript userTranscript1 = Transcript.builder()
                .dialogueSession(testSession)
                .speaker(Speaker.USER)
                .content("첫 번째 사용자 발화")
                .timestamp(now.plusMinutes(1))
                .build();

        Transcript aiTranscript = Transcript.builder()
                .dialogueSession(testSession)
                .speaker(Speaker.AI)
                .content("AI 응답")
                .timestamp(now.plusMinutes(2))
                .build();

        Transcript userTranscript2 = Transcript.builder()
                .dialogueSession(testSession)
                .speaker(Speaker.USER)
                .content("두 번째 사용자 발화")
                .timestamp(now.plusMinutes(3))
                .build();

        entityManager.persist(userTranscript1);
        entityManager.persist(aiTranscript);
        entityManager.persist(userTranscript2);
        entityManager.flush();

        // When
        List<Transcript> userTranscripts = transcriptRepository.findByDialogueSessionIdAndSpeakerOrderByTimestampAsc(
                testSession.getId(), Speaker.USER);

        // Then
        assertEquals(2, userTranscripts.size());
        assertEquals("첫 번째 사용자 발화", userTranscripts.get(0).getContent());
        assertEquals("두 번째 사용자 발화", userTranscripts.get(1).getContent());
        assertTrue(userTranscripts.stream().allMatch(t -> t.getSpeaker() == Speaker.USER));
    }

    @Test
    @DisplayName("특정 세션의 발화 개수를 조회할 수 있다")
    void countByDialogueSessionId() {
        // Given
        for (int i = 0; i < 5; i++) {
            Transcript transcript = Transcript.builder()
                    .dialogueSession(testSession)
                    .speaker(i % 2 == 0 ? Speaker.USER : Speaker.AI)
                    .content("발화 내용 " + i)
                    .timestamp(LocalDateTime.now().plusMinutes(i))
                    .build();
            entityManager.persist(transcript);
        }
        entityManager.flush();

        // When
        long count = transcriptRepository.countByDialogueSessionId(testSession.getId());

        // Then
        assertEquals(5, count);
    }

    @Test
    @DisplayName("특정 세션의 특정 발화자 발화 개수를 조회할 수 있다")
    void countByDialogueSessionIdAndSpeaker() {
        // Given
        for (int i = 0; i < 6; i++) {
            Transcript transcript = Transcript.builder()
                    .dialogueSession(testSession)
                    .speaker(i % 3 == 0 ? Speaker.USER : Speaker.AI) // USER: 2개, AI: 4개
                    .content("발화 내용 " + i)
                    .timestamp(LocalDateTime.now().plusMinutes(i))
                    .build();
            entityManager.persist(transcript);
        }
        entityManager.flush();

        // When
        long userCount = transcriptRepository.countByDialogueSessionIdAndSpeaker(testSession.getId(), Speaker.USER);
        long aiCount = transcriptRepository.countByDialogueSessionIdAndSpeaker(testSession.getId(), Speaker.AI);

        // Then
        assertEquals(2, userCount);
        assertEquals(4, aiCount);
    }

    @Test
    @DisplayName("특정 세션의 평균 발화 길이를 계산할 수 있다")
    void findAverageContentLengthBySessionAndSpeaker() {
        // Given
        Transcript transcript1 = Transcript.builder()
                .dialogueSession(testSession)
                .speaker(Speaker.USER)
                .content("안녕") // 2글자
                .timestamp(LocalDateTime.now())
                .build();

        Transcript transcript2 = Transcript.builder()
                .dialogueSession(testSession)
                .speaker(Speaker.USER)
                .content("안녕하세요") // 5글자
                .timestamp(LocalDateTime.now().plusMinutes(1))
                .build();

        Transcript transcript3 = Transcript.builder()
                .dialogueSession(testSession)
                .speaker(Speaker.USER)
                .content("반갑습니다") // 5글자
                .timestamp(LocalDateTime.now().plusMinutes(2))
                .build();

        entityManager.persist(transcript1);
        entityManager.persist(transcript2);
        entityManager.persist(transcript3);
        entityManager.flush();

        // When
        Double averageLength = transcriptRepository.findAverageContentLengthBySessionAndSpeaker(
                testSession.getId(), Speaker.USER);

        // Then
        assertNotNull(averageLength);
        assertEquals(4.0, averageLength, 0.01); // (2+5+5)/3 = 4.0
    }

    @Test
    @DisplayName("특정 세션에서 신뢰도가 낮은 발화를 조회할 수 있다")
    void findLowConfidenceTranscripts() {
        // Given
        Transcript highConfidence = Transcript.builder()
                .dialogueSession(testSession)
                .speaker(Speaker.USER)
                .content("높은 신뢰도")
                .timestamp(LocalDateTime.now())
                .confidenceScore(0.95f)
                .build();

        Transcript lowConfidence1 = Transcript.builder()
                .dialogueSession(testSession)
                .speaker(Speaker.USER)
                .content("낮은 신뢰도 1")
                .timestamp(LocalDateTime.now().plusMinutes(1))
                .confidenceScore(0.3f)
                .build();

        Transcript lowConfidence2 = Transcript.builder()
                .dialogueSession(testSession)
                .speaker(Speaker.USER)
                .content("낮은 신뢰도 2")
                .timestamp(LocalDateTime.now().plusMinutes(2))
                .confidenceScore(0.5f)
                .build();

        entityManager.persist(highConfidence);
        entityManager.persist(lowConfidence1);
        entityManager.persist(lowConfidence2);
        entityManager.flush();

        // When
        List<Transcript> lowConfidenceTranscripts = transcriptRepository.findLowConfidenceTranscripts(
                testSession.getId(), 0.7f);

        // Then
        assertEquals(2, lowConfidenceTranscripts.size());
        assertEquals("낮은 신뢰도 1", lowConfidenceTranscripts.get(0).getContent()); // 신뢰도 낮은 순
        assertEquals("낮은 신뢰도 2", lowConfidenceTranscripts.get(1).getContent());
    }

    @Test
    @DisplayName("특정 세션의 전체 발화 시간을 계산할 수 있다")
    void calculateTotalSpeakingTime() {
        // Given
        Transcript transcript1 = Transcript.builder()
                .dialogueSession(testSession)
                .speaker(Speaker.USER)
                .content("첫 번째 발화")
                .timestamp(LocalDateTime.now())
                .startTimeMs(1000L)
                .endTimeMs(3000L) // 2초
                .build();

        Transcript transcript2 = Transcript.builder()
                .dialogueSession(testSession)
                .speaker(Speaker.AI)
                .content("두 번째 발화")
                .timestamp(LocalDateTime.now().plusMinutes(1))
                .startTimeMs(5000L)
                .endTimeMs(8000L) // 3초
                .build();

        Transcript transcript3 = Transcript.builder()
                .dialogueSession(testSession)
                .speaker(Speaker.USER)
                .content("시간 정보 없음")
                .timestamp(LocalDateTime.now().plusMinutes(2))
                .build();

        entityManager.persist(transcript1);
        entityManager.persist(transcript2);
        entityManager.persist(transcript3);
        entityManager.flush();

        // When
        Long totalTime = transcriptRepository.calculateTotalSpeakingTime(testSession.getId());

        // Then
        assertNotNull(totalTime);
        assertEquals(5000L, totalTime); // 2000 + 3000 = 5000ms
    }

    @Test
    @DisplayName("존재하지 않는 세션 ID로 조회하면 빈 결과를 반환한다")
    void findByNonExistentSessionId() {
        // When
        List<Transcript> results = transcriptRepository.findByDialogueSessionIdOrderByTimestampAsc(999L);
        long count = transcriptRepository.countByDialogueSessionId(999L);

        // Then
        assertTrue(results.isEmpty());
        assertEquals(0, count);
    }
}