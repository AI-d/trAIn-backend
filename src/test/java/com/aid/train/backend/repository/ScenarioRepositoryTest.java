package com.aid.train.backend.repository;

import com.aid.train.backend.domain.scenario.dto.request.ScenarioRequestDto;
import com.aid.train.backend.domain.scenario.entity.Scenario;
import com.aid.train.backend.domain.user.entity.User;
import com.aid.train.backend.domain.user.enums.Provider;
import com.aid.train.backend.domain.user.enums.UserStatus;
import com.aid.train.backend.repository.scenario.ScenarioRepository;
import com.aid.train.backend.repository.user.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;

import java.time.LocalDateTime;
import java.util.List;

import static com.aid.train.backend.domain.scenario.entity.Scenario.Category.*;
import static com.aid.train.backend.domain.scenario.entity.Scenario.Difficulty.*;
import static com.aid.train.backend.domain.scenario.entity.Scenario.Status.PUBLISHED;
import static com.aid.train.backend.domain.scenario.entity.Scenario.Voice.*;

@SpringBootTest
@Transactional
@Rollback(value = false)
class ScenarioRepositoryTest {

    @Autowired
    private ScenarioRepository scenarioRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    EntityManager em;

    // 기본 시나리오 db 저장
    @BeforeEach
    void insert() {
        User testUser = User.builder()
                .email("test@example.com")
                .password("testPassword123") // LOCAL 계정에 필수 비밀번호 추가
                .name("테스트 사용자")
                .primaryProvider(Provider.LOCAL)
                .emailVerified(true)
                .status(UserStatus.ACTIVE)
                .build();

        userRepository.save(testUser);

        Scenario s1 = Scenario.builder()
                .title("상사에게 일정 지연 보고")
                .description("프로젝트 일정이 3일 지연된 상황에서 상사에게 간결하고 명확하게 보고하고 대안을 제시하는 연습")
                .prompt("보고는 \\\"원인→영향→대안\\\" 순서. 1~2문장 캐치볼, 장황 금지, 변명 대신 대안 2개 제시.")
                .voice(NOVA)
                .difficulty(MEDIUM)
                .category(WORK)
                .locale("ko-KR")
                .status(PUBLISHED)
                .isDefault(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Scenario s2 = Scenario.builder()
                .title("연인과 갈등 시 감정 정리 대화")
                .description("연인과 사소한 오해로 감정이 상했을 때, 비난 없이 감정/요구를 전달하는 연습")
                .prompt("말투는 부드럽고 구체적으로. \\\"사실-감정-요구\\\" 구조, 너/당신 지양, 나는 화법 사용.")
                .voice(ONYX)
                .difficulty(MEDIUM)
                .category(RELATIONSHIP)
                .locale("ko-KR")
                .status(PUBLISHED)
                .isDefault(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Scenario s3 = Scenario.builder()
                .title("친구에게 약속 시간 변경 요청")
                .description("개인 사정으로 약속 시간을 변경해야 할 때, 예의 있게 설득하는 연습")
                .prompt("짧게 사과 → 사유 간결 → 대안 2안 제시(시간/장소) → 이해 요청. 1~2문장 유지.")
                .voice(ECHO)
                .difficulty(EASY)
                .category(FRIEND)
                .locale("ko-KR")
                .status(PUBLISHED)
                .isDefault(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Scenario s4 = Scenario.builder()
                .title("부모님과 하루 나눔 대화")
                .description("저녁 식탁에서 오늘 하루 있었던 일을 따뜻하게 공유하고 공감하는 연습")
                .prompt("닫힌 질문보다 열린 질문. 판단/충고 자제, 공감 먼저. 1~2문장 캐치볼.")
                .voice(NOVA)
                .difficulty(EASY)
                .category(FAMILY)
                .locale("ko-KR")
                .status(PUBLISHED)
                .isDefault(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        scenarioRepository.saveAll(List.of(s1, s2, s3, s4));
        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("id로 시나리오를 조회합니다.")
    void findOneScenario() {
        // given
        Long id = 1L;
        // when
        Scenario scenario = scenarioRepository.findScenarioByScenarioId(id);
        // then
        System.out.println("scenario = " + scenario);
    }

    @Test
    @DisplayName("db에 저장된 모든 시나리오를 조회합니다.")
    void findAllScenarios() {
        // given
        // when
        List<Scenario> scenarioList = scenarioRepository.findAllScenario();
        // then
        System.out.println("scenario = " + scenarioList);
    }

    @Test
    @DisplayName("사용자가 생성한 시나리오를 db에 저장합니다.")
    void createScenario() {
        // given
        ScenarioRequestDto dto = ScenarioRequestDto.builder()
                .title("사용자 생성 시나리오 테스트 제목")
                .description("사용자 생성 시나리오 저장을 위한 테스트입니다.")
                .prompt("사용자 생성 시나리오 저장을 위한 테스트로 질문은 하지 말아주세요.")
                .voice(NOVA)
                .difficulty(EASY)
                .category(WORK)
                .locale("ko-KR")
                .build();

        User user = userRepository.findById(1L).orElseThrow();

        Scenario entity = Scenario.toEntity(dto, user);
        Scenario.Status status = entity.getStatus();
        // when
        Scenario saved = scenarioRepository.save(entity);
        // then
        System.out.println("saved = " + saved + "저장되었습니다.");
    }
}