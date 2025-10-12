package com.aid.train.backend.domain.scenario.repository;

import com.aid.train.backend.domain.scenario.entity.Scenario;

import java.util.List;
import java.util.Optional;

public interface ScenarioCustom {

    public List<Scenario> findAll();
    public List<Scenario> findAllByUserId(Long id);
    public List<Scenario> findDefaultAll();
    public long deleteByAndUserId(Long userId, Long scenarioId);
    public Optional<Scenario> findByAndUserId(Long userId, Long scenarioId);
}
