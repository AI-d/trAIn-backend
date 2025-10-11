package com.aid.train.backend.domain.scenario.repository;

import com.aid.train.backend.domain.scenario.entity.Scenario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScenarioRepository extends JpaRepository<Scenario, Long>, ScenarioCustom  {
}
