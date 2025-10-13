package com.aid.train.backend.domain.scenario.repository;

import com.aid.train.backend.domain.scenario.entity.Scenario;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

import static com.aid.train.backend.domain.scenario.entity.QScenario.scenario;
import static com.aid.train.backend.domain.scenario.entity.Scenario.Status.*;
import static com.aid.train.backend.domain.user.entity.QUser.user;

@RequiredArgsConstructor
public class ScenarioRepositoryImpl implements ScenarioCustom{


    // queryDsl 을 사용하기 위한 의존객체
    private final JPAQueryFactory factory;

    // 1-2. 전체 시나리오 조회
    @Override
    public List<Scenario> findAll() {
        return factory
                .select(scenario)
                .from(scenario)
                .where(scenario.status.ne(DELETED))
                .fetch();
    }

    @Override
    public List<Scenario> findAllByUserId(Long id) {
        return factory
                .select(scenario)
                .from(scenario)
                .leftJoin(scenario.owner, user).fetchJoin()
                .where(scenario.owner.id.eq(id),
                        scenario.isDefault.isFalse())
                .fetch();
    }

    @Override
    public List<Scenario> findDefaultAll() {
        return factory
                .selectFrom(scenario)
                .where(scenario.isDefault.isTrue())
                .fetch();
    }

    @Override
    public long deleteByAndUserId(Long userId, Long scenarioId) {
        return factory
                .update(scenario)
                .set(scenario.status, DELETED)
                .where(scenario.owner.id.eq(userId),
                        scenario.id.eq(scenarioId),
                        scenario.isDefault.isFalse()
                )
                .execute();
    }

    @Override
    public Optional<Scenario> findByAndUserId(Long userId, Long scenarioId) {
        Scenario s = factory
                .selectFrom(scenario)
                .leftJoin(scenario.owner, user).fetchJoin()
                .where(scenario.id.eq(scenarioId),
                        scenario.owner.id.eq(userId),
                        scenario.isDefault.isFalse())
                .fetchOne();

        return Optional.ofNullable(s);
    }
}
