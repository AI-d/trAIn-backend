package com.aid.train.backend.domain.scenario.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QScenario is a Querydsl query type for Scenario
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QScenario extends EntityPathBase<Scenario> {

    private static final long serialVersionUID = 477424456L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QScenario scenario = new QScenario("scenario");

    public final EnumPath<Scenario.Category> category = createEnum("category", Scenario.Category.class);

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final StringPath description = createString("description");

    public final EnumPath<Scenario.Difficulty> difficulty = createEnum("difficulty", Scenario.Difficulty.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isDefault = createBoolean("isDefault");

    public final StringPath locale = createString("locale");

    public final com.aid.train.backend.domain.user.entity.QUser owner;

    public final StringPath prompt = createString("prompt");

    public final ListPath<com.aid.train.backend.domain.session.entity.DialogueSession, com.aid.train.backend.domain.session.entity.QDialogueSession> sessions = this.<com.aid.train.backend.domain.session.entity.DialogueSession, com.aid.train.backend.domain.session.entity.QDialogueSession>createList("sessions", com.aid.train.backend.domain.session.entity.DialogueSession.class, com.aid.train.backend.domain.session.entity.QDialogueSession.class, PathInits.DIRECT2);

    public final EnumPath<Scenario.Status> status = createEnum("status", Scenario.Status.class);

    public final StringPath title = createString("title");

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public final EnumPath<Scenario.Voice> voice = createEnum("voice", Scenario.Voice.class);

    public QScenario(String variable) {
        this(Scenario.class, forVariable(variable), INITS);
    }

    public QScenario(Path<? extends Scenario> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QScenario(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QScenario(PathMetadata metadata, PathInits inits) {
        this(Scenario.class, metadata, inits);
    }

    public QScenario(Class<? extends Scenario> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.owner = inits.isInitialized("owner") ? new com.aid.train.backend.domain.user.entity.QUser(forProperty("owner")) : null;
    }

}

