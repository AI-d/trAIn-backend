package com.aid.train.backend.domain.scenario.controller;

import com.aid.train.backend.domain.scenario.dto.request.ScenarioRequestDto;
import com.aid.train.backend.domain.scenario.dto.response.ScenarioResponseDto;
import com.aid.train.backend.domain.scenario.entity.Scenario;
import com.aid.train.backend.domain.scenario.service.ScenarioService;
import com.aid.train.backend.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/scenarios")
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
        ScenarioResponseDto scenario = scenarioService.findOneScenario(id);
        return ResponseEntity.ok().body(ApiResponse.success(id + "번 시나리오 조회에 성공했습니다.", scenario));
    }

    /**
     * 전체 시나리오 조회
     * @return
     */
    @GetMapping
    public ResponseEntity<?> findAllScenarios() {
        List<ScenarioResponseDto> allScenario = scenarioService.findAllScenarios();
        return ResponseEntity.ok().body(ApiResponse.success("전체 시나리오 조회를 성공했습니다.", allScenario));
    }

    /**
     * 사용자 시나리오 생성
     * @param dto
     * @return
     */
    @PostMapping
    public ResponseEntity<?> createNewScenario(@RequestBody ScenarioRequestDto dto) {
        Scenario saved = scenarioService.createScenario(dto.ownerId(), dto);
        return ResponseEntity.ok().body(ApiResponse.success("시나리오 생성을 성공했습니다.", saved));
    }

    /**
     * 사용자가 생성한 모든 시나리오 조회
     */
    @GetMapping("/me/{userId}")
    public ResponseEntity<?> findAllScenariosByUser(@PathVariable Long userId) {
        List<ScenarioResponseDto> scenarios = scenarioService.findAllScenarioMadeUser(userId);
        return ResponseEntity.ok().body(ApiResponse.success("사용자가 생성한 모든 시나리오 조회를 성공했습니다.", scenarios));
    }

    /**
     * 사용자가 생성한 단일 시나리오 조회
     */
    @GetMapping("/me/{userId}/{id}")
    public ResponseEntity<?> findScenarioByUser(@PathVariable Long userId, @PathVariable Long id) {
        ScenarioResponseDto scenario = scenarioService.findOneScenarioByUser(userId, id);
        return ResponseEntity.ok().body(ApiResponse.success(id + "번 시나리오 조회에 성공했습니다.", scenario));
    }

    /**
     * 기본 시나리오 조회
     */
    @GetMapping("/default")
    public ResponseEntity<?> findDefaultScenario() {
        List<ScenarioResponseDto> defaultScenarios = scenarioService.findDefaultScenarios();
        return ResponseEntity.ok().body(ApiResponse.success("기본 시나리오 조회에 성공했습니다.", defaultScenarios));
    }

    /**
     * 사용자가 생성한 시나리오 삭제
     */
    @DeleteMapping("/me/{userId}/{id}")
    public ResponseEntity<?> deleteScenario(@PathVariable Long userId, @PathVariable Long id) {
        scenarioService.deleteScenario(userId, id);
        return ResponseEntity.ok().body(ApiResponse.success("시나리오 삭제에 성공했습니다", ""));
    }
}
