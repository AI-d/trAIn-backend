package com.aid.train.backend.domain.scenario.service;

import com.aid.train.backend.domain.scenario.dto.request.ScenarioRequestDto;
import com.aid.train.backend.domain.scenario.dto.response.ScenarioResponseDto;
import com.aid.train.backend.domain.scenario.entity.Scenario;
import com.aid.train.backend.domain.scenario.repository.ScenarioRepository;
import com.aid.train.backend.domain.user.entity.User;
import com.aid.train.backend.domain.user.repository.UserRepository;
import com.aid.train.backend.global.exception.TrainException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.aid.train.backend.global.exception.enums.ErrorCode.SCENARIO_NOT_FOUND;
import static com.aid.train.backend.global.exception.enums.ErrorCode.USER_NOT_FOUND;

@Transactional
@Service
@RequiredArgsConstructor
public class ScenarioService {

    private final ScenarioRepository scenarioRepository;
    private final UserRepository userRepository;

    /**
     * DB에 저장된 모든 시나리오를 조회합니다.
     */
    @Transactional(readOnly = true)
    public List<ScenarioResponseDto> findAllScenarios() {
        List<Scenario> scenarios = scenarioRepository.findAll();
        if(scenarios.isEmpty()) {
            throw new TrainException(SCENARIO_NOT_FOUND);
        }
        List<ScenarioResponseDto> scenarioList = scenarios.stream()
                .map(scenario -> ScenarioResponseDto.fromEntity(scenario))
                .collect(Collectors.toList());
        return scenarioList;
    }
    /**
     * 애플리케이션에서 제공하는 기본 시나리오를 조회합니다.
     */
    @Transactional(readOnly = true)
    public List<ScenarioResponseDto> findDefaultScenarios() {
        List<Scenario> defaultScenarios = scenarioRepository.findDefaultAll();
        if(defaultScenarios.isEmpty()) {
            throw new TrainException(SCENARIO_NOT_FOUND);
        }
        List<ScenarioResponseDto> defaultList = defaultScenarios.stream()
                .map(scenario -> ScenarioResponseDto.fromEntity(scenario))
                .collect(Collectors.toList());
        return defaultList;
    }

    /**
     * id 별 단일 시나리오를 조회합니다.
     */
    @Transactional(readOnly = true)
    public ScenarioResponseDto findOneScenario(Long id) {
        Scenario scenario = scenarioRepository.findById(id).orElseThrow(
                () -> new TrainException(SCENARIO_NOT_FOUND)
        );

        return ScenarioResponseDto.fromEntity(scenario);
    }

    /**
     * 사용자 시나리오를 생성합니다.
     */
    @Transactional(readOnly = true)
    public Scenario createScenario(Long id, ScenarioRequestDto dto) {
        User user = getUser(id);
        Scenario newScenario = Scenario.toEntity(dto, user);
        return scenarioRepository.save(newScenario);
    }

    /**
     * 사용자가 생성한 모든 시나리오를 조회합니다.
     */
    @Transactional(readOnly = true)
    public List<ScenarioResponseDto> findAllScenarioMadeUser(Long id) {
        List<Scenario> userScenarios = scenarioRepository.findAllByUserId(id);
       /* if(userScenarios.isEmpty()) {
            throw new TrainException(SCENARIO_NOT_FOUND);
        }*/
        List<ScenarioResponseDto> scenarioList = userScenarios.stream()
                .map(scenario -> ScenarioResponseDto.fromEntity(scenario))
                .collect(Collectors.toList());
        return scenarioList;
    }

    /**
     * 사용자가 생성한 단일 시나리오를 조회합니다.
     */
    @Transactional(readOnly = true)
    public ScenarioResponseDto findOneScenarioByUser(Long userId, Long scenarioId) {
        Scenario scenario = scenarioRepository.findByAndUserId(userId, scenarioId).orElseThrow(
                () -> new TrainException(SCENARIO_NOT_FOUND)
        );
        return ScenarioResponseDto.fromEntity(scenario);
    }

    /**
     * 사용자가 생성한 시나리오를 삭제합니다.
     */
    public String deleteScenario(Long userId, Long scenarioId) {
        long flag = scenarioRepository.deleteByAndUserId(userId, scenarioId);
        if(flag == 0) {
            throw new TrainException(SCENARIO_NOT_FOUND);
        }
        return scenarioId + "번 시나리오가 삭제되었습니다.";
    }

    /**
     *
     * 사용자 id로 사용자를 조회합니다.
     * @param id - 사용자 id
     * @return user - 데이터베이스에서 id로 조회한 사용자
     */
    private User getUser(Long id) {
        return userRepository.findById(id).orElseThrow(
                () -> new TrainException(USER_NOT_FOUND)
        );
    }

    /**
     * 시나리오 정보를 가지고 GPT에게 요청하는 프롬프트를 생성합니다.
     *
     */
    public String createPrompt(Scenario scenario, User user) {
        return String.format(
                """
                ## [최중요 지시] 역할극 시뮬레이션 모드 (STRICT ROLE-PLAY MODE)
                
                **당신은 지금부터 Dialogym 플랫폼의 대화 연습 시뮬레이터입니다. 당신의 목표는 오직 사용자에게 대화 연습 환경을 제공하는 것입니다. 이 모드에서는 당신의 역할 외에 다른 모든 대화 내용은 엄격히 금지됩니다.**
                
                ## USER INFO
                ** 개인화된 대화를 위해 사용자의 직업, 생년월일을 제공합니다.**
                - 직업: %s
                - 직업 상세 정보: %s
                - 생년월일: %s
          
                ## SCENARIO INFO
                - 난이도: %s
                - 카테고리: %s
                - 주제: %s
                - 상황 설명: %s
                
                ## YOUR ROLE (당신의 역할: %s)
                - 구체적인 역할 지시 사항:
                %s
                
                ## USER'S ROLE (사용자의 역할)
                사용자는 당신의 대화 상대 역할을 맡고 있으며, 이 시나리오에서 **자신이 해야 할 말**을 합니다.
                
                ## CORE RULES (핵심 규칙) - 위반 시 기능 오류로 간주됨
                1.  **ONLY YOUR ROLE (가장 중요)**: 당신은 오직 **[YOUR ROLE]에 명시된 역할**의 발화만 생성해야 합니다. 사용자의 역할에 해당하는 대화 내용이나 진행 상황을 설명하는 발언은 **어떤 경우에도 절대 금지**됩니다.
                2.  **NEVER SPEAK FOR THE USER (3중 금지)**:
                    a.  **사용자가 해야 할 말(예: 휴가 요청, 문제 상황 설명)**을 예측하거나 대신 말하지 마세요.
                    b.  **사용자가 다음 대화에서 무엇을 해야 한다고 지시**하거나 대화 스킬을 평가하지 마세요.
                    c.  **사용자가 시나리오를 이탈했더라도, 당신의 역할(고객, 팀장 등)을 유지하며 반응해야 합니다.**
                3.  **START CONVERSATION FIRST**: 당신은 **[YOUR ROLE]에 충실하게** 상황에 맞는 첫 발화 문장을 **스스로 생성하여** 대화를 즉시 시작해야 합니다.
                4.  **SHORT RESPONSE HANDLING**: 사용자가 첫 응답이나 짧은 응답(예: "네", "잠시만요")을 했을 때, 당신은 **절대 사용자 역할의 다음 발화(예: "휴가 가려고 합니다")를 대신 말하지 말고**, 당신의 역할에 해당하는 **확인 질문(예: "어떤 일로 오셨나요?", "말씀하세요")**을 다시 던져서 사용자가 스스로 대화를 시작하도록 유도해야 합니다.
                5.  **STOP & WAIT**: 당신의 발화 직후에는 **무조건 멈추고 사용자의 응답만을 기다려야 합니다.**
                6.  **Reaction Only**: 당신의 모든 발화는 **직전 사용자의 발화에 대한 '반응'**이어야 합니다. 미리 스크립트를 짜듯 대화를 진행하지 마세요.
                7.  **Language & Length**: 반드시 **한국어**로 대화하며, 짧고 자연스러운 문장(대부분 1-2문장)으로 응답하세요.
                8.  **No Consecutive Questions**: 답변 후 추가적인 질문을 연달아 하지 마세요.
                
                ## 대화 복구 우선 규칙 (CONVERSATION RECOVERY PRIORITY)
                **이전 대화 기록이 시스템 메시지로 제공된 경우:**
                - 위의 모든 규칙보다 우선하여 대화를 이어가세요
                - 절대 새로운 인사("안녕하세요")를 하지 마세요
                - 마지막 대화 상황에서 자연스럽게 반응하세요
                - START CONVERSATION FIRST 규칙은 일시 중단됩니다
                
                3. **START CONVERSATION FIRST**:
                   **새로운 대화 시작 시에만** 당신은 **[YOUR ROLE]에 충실하게** 상황에 맞는 첫 발화 문장을 **스스로 생성하여** 대화를 즉시 시작해야 합니다.
                
                ## 비정상 상황 대응 지침 (Crucial Instruction for Abnormal Response)
                **만약 사용자의 응답이 이상하거나, 무의미하거나, 시나리오의 흐름을 심각하게 방해하더라도,** 당신은 다음 중 하나를 수행해야 합니다.
                
                -   **옵션 A (권장):** 당신의 **[YOUR ROLE]**을 유지하면서, 사용자의 이상한 발언에 대해 **'상황을 다시 확인하는'** 반응을 보입니다. (예: (고객 역할일 때) "죄송합니다만, 방금 뭐라고 말씀하셨나요?", (팀장 역할일 때) "혹시 제가 이해를 잘 못했는데, 지금 휴가 요청을 하시려는 건가요?")
                -   **옵션 B:** 시나리오를 **계속 진행할 수 없다고 판단되면**, 당신의 역할(예: 고객)을 유지하며 "잠시 후 다시 통화하겠습니다." 또는 "제가 지금 대화를 이어가기 어렵습니다." 등의 간단한 종결 발언 후 **대화를 중단**합니다. **절대 사용자를 대신하거나 교육하려고 하지 마세요.**
                
                ## 지시:
                **위의 모든 지시와 [YOUR ROLE]에 따라, 역할에 맞는 첫 발화 문장을 즉시 생성하여 대화를 시작하세요. 당신의 첫 발화 외에는 다른 내용을 추가하지 마세요.**
                
                ## 시나리오 이탈 시 대응
                사용자가 시나리오와 관련 없는 말을 하거나 화를 낼 때:
                - 당신의 역할(고객/팀장 등)을 절대 바꾸지 마세요
                - 자연스럽게 "죄송합니다만, 지금 [상황]에 대해 이야기하고 있는데요" 같은 반응
                - 또는 "혹시 다른 문제가 있으신가요?" 같은 확인 질문
                """,
                user.getJobType(),
                user.getJobDetail(),
                user.getBirthDate(),
                scenario.getDifficulty(),
                scenario.getCategory(),
                scenario.getTitle(),
                scenario.getDescription(),
                scenario.getRole(),
                scenario.getPrompt()

            );

    }

}
