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
        if(userScenarios.isEmpty()) {
            throw new TrainException(SCENARIO_NOT_FOUND);
        }
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
    public String createPrompt(Scenario scenario) {
        return String.format(
                """
                당신은 대화 연습을 위한 역할극 대화 파트너입니다.
                사용자의 대화 연습을 도와주는 것이 목적입니다.
                다음 시나리오 정보를 기반으로 대화를 진행하세요.
            
                - 난이도: %s
                - 카테고리: %s
                - 대화 주제: %s
                - 시나리오 설명: %s
            
                아래의 지침을 따르세요:
                1. 사용자의 실력을 고려해 난이도에 맞는 어휘와 문장을 사용하세요.
                2. 카테고리에 맞는 상황 설정과 맥락을 유지하세요.
                3. 대화가 자연스럽게 이어지도록 짧은 문장으로 응답하세요.
                4. 반드시 한국어로 대화하세요.
                """,
                        scenario.getDifficulty(),
                        scenario.getCategory(),
                        scenario.getTitle(),
                        scenario.getDescription(),
                        scenario.getLocale()
                );

    }

}
