package com.aid.train.backend.controller.scenario;

import com.aid.train.backend.domain.scenario.dto.request.ScenarioRequestDto;
import com.aid.train.backend.domain.scenario.entity.Scenario;
import com.aid.train.backend.domain.user.entity.User;
import com.aid.train.backend.repository.scenario.ScenarioRepository;
import com.aid.train.backend.repository.user.UserRepository;
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

    private final ScenarioRepository scenarioRepository;
    private final UserRepository userRepository;

    @GetMapping("/{id}")
    public ResponseEntity<?> findScenario(@PathVariable Long id) {
        Scenario scenario = scenarioRepository.findScenarioByScenarioId(id);
        return ResponseEntity.ok().body(scenario);
    }

    @GetMapping
    public ResponseEntity<?> findAllScenarios() {
        List<Scenario> allScenario = scenarioRepository.findAllScenario();
        return ResponseEntity.ok().body(allScenario);
    }

    @PutMapping
    public ResponseEntity<?> createNewScenario(@RequestBody ScenarioRequestDto dto) {
        User user = userRepository.findById(dto.ownerId()).orElseThrow();
        Scenario entity = Scenario.toEntity(dto,user);
        Scenario saved = scenarioRepository.save(entity);
        return ResponseEntity.ok().body(saved);
    }

}
