package com.aid.train.backend.repository.scenario;

import com.aid.train.backend.entity.scenario.QScenario;
import com.aid.train.backend.entity.scenario.Scenario;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.aid.train.backend.entity.scenario.QScenario.scenario;
import static com.aid.train.backend.entity.scenario.Scenario.Status.PUBLISHED;

@RequiredArgsConstructor
public class ScenarioRepositoryImpl implements ScenarioCustom{


    // queryDsl 을 사용하기 위한 의존객체
    private final JPAQueryFactory factory;

    // 1. 기본 시나리오
    // 1-1. id로 시나리오 조회
    @Override
    public Scenario findScenarioByScenarioId(Long id) {
        return factory
                .selectFrom(scenario)
                .where(scenario.id.eq(id))
                .fetchOne();
    }
    // 1-2. 전체 시나리오 조회
    @Override
    public List<Scenario> findAllScenario() {
        return factory
                .select(scenario)
                .from(scenario)
                .where(scenario.status.eq(PUBLISHED))
                .fetch();
    }

    // 2. 사용자 시나리오
    // 2-1. 사용자 시나리오 저장
}
