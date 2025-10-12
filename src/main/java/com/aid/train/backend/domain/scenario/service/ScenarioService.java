package com.aid.train.backend.domain.scenario.service;

import com.aid.train.backend.domain.scenario.dto.request.ScenarioRequestDto;
import com.aid.train.backend.domain.scenario.entity.Scenario;
import com.aid.train.backend.domain.scenario.repository.ScenarioRepository;
import com.aid.train.backend.domain.user.entity.User;
import com.aid.train.backend.domain.user.repository.UserRepository;
import com.aid.train.backend.global.exception.TrainException;
import com.aid.train.backend.global.exception.enums.ErrorCode;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

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
    public List<Scenario> findAllScenarios() {
        List<Scenario> scenarios = scenarioRepository.findAll();
        if(scenarios.isEmpty()) {
            throw new TrainException(SCENARIO_NOT_FOUND);
        }
        return scenarios;
    }
    /**
     * 애플리케이션에서 제공하는 기본 시나리오를 조회합니다.
     */
    @Transactional(readOnly = true)
    public List<Scenario> findDefaultScenarios() {
        List<Scenario> defaultScenarios = scenarioRepository.findDefaultAll();
        if(defaultScenarios.isEmpty()) {
            throw new TrainException(SCENARIO_NOT_FOUND);
        }
        return defaultScenarios;
    }

    /**
     * id 별 단일 시나리오를 조회합니다.
     */
    @Transactional(readOnly = true)
    public Scenario findOneScenario(Long id) {
        Scenario scenario = scenarioRepository.findById(id).orElseThrow(
                () -> new TrainException(SCENARIO_NOT_FOUND)
        );

        return scenario;
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
    public List<Scenario> findAllScenarioMadeUser(Long id) {
        List<Scenario> userScenarios = scenarioRepository.findAllByUserId(id);
        if(userScenarios.isEmpty()) {
            throw new TrainException(SCENARIO_NOT_FOUND);
        }
        return userScenarios;
    }

    /**
     * 사용자가 생성한 단일 시나리오를 조회합니다.
     */
    @Transactional(readOnly = true)
    public Scenario findOneScenarioByUser(Long userId, Long scenarioId) {
        Scenario scenario = scenarioRepository.findByAndUserId(userId, scenarioId).orElseThrow(
                () -> new TrainException(SCENARIO_NOT_FOUND)
        );
        return scenario;
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

}
