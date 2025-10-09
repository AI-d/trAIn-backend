package com.aid.train.backend.repository.scenario;

import com.aid.train.backend.entity.scenario.Scenario;

import java.util.List;

public interface ScenarioCustom {

    public Scenario findScenarioByScenarioId(Long id);
    public List<Scenario> findAllScenario();
}
