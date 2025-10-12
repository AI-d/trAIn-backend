package com.aid.train.backend.domain.scenario.controller;

import com.aid.train.backend.domain.scenario.dto.request.ScenarioRequestDto;
import com.aid.train.backend.domain.scenario.entity.Scenario;
import com.aid.train.backend.domain.scenario.service.ScenarioService;
import com.aid.train.backend.domain.user.entity.User;
import com.aid.train.backend.global.response.ApiResponse;
import com.aid.train.backend.domain.scenario.repository.ScenarioRepository;
import com.aid.train.backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/scenario")
@RequiredArgsConstructor
@Slf4j
public class ScenarioController {

    private final ScenarioService scenarioService;

    /**
     * 개별 시나리오 조회
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> findScenario(@PathVariable Long id) {
        Scenario scenario = scenarioService.findOneScenario(id);
        return ResponseEntity.ok().body(ApiResponse.success(id + "번 시나리오 조회에 성공했습니다.", scenario));
    }

    /**
     * 전체 시나리오 조회
     * @return
     */
    @GetMapping
    public ResponseEntity<?> findAllScenarios() {
        List<Scenario> allScenario = scenarioService.findAllScenarios();
        return ResponseEntity.ok().body(ApiResponse.success("전체 시나리오 조회를 성공했습니다.", allScenario));
    }

    /**
     * 사용자 시나리오 생성
     * @param dto
     * @return
     */
    @PutMapping
    public ResponseEntity<?> createNewScenario(@RequestBody ScenarioRequestDto dto) {
        Scenario saved = scenarioService.createScenario(dto.ownerId(), dto);
        return ResponseEntity.ok().body(ApiResponse.success("시나리오 생성을 성공했습니다.", saved));
    }

    /**
     * 사용자가 생성한 모든 시나리오 조회
     */
    @GetMapping("/{userId}")
    public ResponseEntity<?> findAllScenariosByUser(@PathVariable Long id) {
        List<Scenario> scenarios = scenarioService.findAllScenarioMadeUser(id);
        return ResponseEntity.ok().body(ApiResponse.success("사용자가 생성한 모든 시나리오 조회를 성공했습니다.", scenarios));
    }

    /**
     * 사용자가 생성한 단일 시나리오 조회
     */
    @GetMapping("/{userId}/{id}")
    public ResponseEntity<?> findScenarioByUser(@PathVariable Long userId, @PathVariable Long id) {
        Scenario scenario = scenarioService.findOneScenarioByUser(userId, id);
        return ResponseEntity.ok().body(ApiResponse.success(id + "번 시나리오 조회에 성공했습니다.", scenario));
    }

    /**
     * 기본 시나리오 조회
     */
    @GetMapping("/default")
    public ResponseEntity<?> findDefaultScenario() {
        List<Scenario> defaultScenarios = scenarioService.findDefaultScenarios();
        return ResponseEntity.ok().body(ApiResponse.success("기본 시나리오 조회에 성공했습니다.", defaultScenarios));
    }

    /**
     * 사용자가 생성한 시나리오 삭제
     */
    @DeleteMapping("/{userId}/{id}")
    public ResponseEntity<?> deleteScenario(@PathVariable Long userId, @PathVariable Long id) {
        scenarioService.deleteScenario(userId, id);
        return ResponseEntity.ok().body(ApiResponse.success("시나리오 삭제에 성공했습니다", ""));
    }
}
