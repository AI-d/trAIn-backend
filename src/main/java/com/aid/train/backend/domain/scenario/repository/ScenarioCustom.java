package com.aid.train.backend.domain.scenario.repository;

import com.aid.train.backend.domain.scenario.entity.Scenario;

import java.util.List;

public interface ScenarioCustom {

    public Scenario findScenarioByScenarioId(Long id);
    public List<Scenario> findAllScenario();
}
