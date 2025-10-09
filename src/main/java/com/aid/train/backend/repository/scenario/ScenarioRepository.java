package com.aid.train.backend.repository.scenario;

import com.aid.train.backend.entity.scenario.Scenario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ScenarioRepository extends JpaRepository<Scenario, Long>, ScenarioCustom  {
}
