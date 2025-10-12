package com.aid.train.backend.domain.session.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QDialogueSession is a Querydsl query type for DialogueSession
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QDialogueSession extends EntityPathBase<DialogueSession> {

    private static final long serialVersionUID = 313544662L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QDialogueSession dialogueSession = new QDialogueSession("dialogueSession");

    public final StringPath aiRealtimeSessionId = createString("aiRealtimeSessionId");

    public final NumberPath<Integer> audioDurationSeconds = createNumber("audioDurationSeconds", Integer.class);

    public final StringPath audioUrl = createString("audioUrl");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> endedAt = createDateTime("endedAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> janusBotFeedId = createNumber("janusBotFeedId", Long.class);

    public final NumberPath<Long> janusRoomId = createNumber("janusRoomId", Long.class);

    public final NumberPath<Long> janusUserFeedId = createNumber("janusUserFeedId", Long.class);

    public final StringPath realtimeMetrics = createString("realtimeMetrics");

    public final com.aid.train.backend.domain.scenario.entity.QScenario scenario;

    public final StringPath sessionId = createString("sessionId");

    public final DateTimePath<java.time.LocalDateTime> startedAt = createDateTime("startedAt", java.time.LocalDateTime.class);

    public final EnumPath<com.aid.train.backend.domain.session.enums.SessionStatus> status = createEnum("status", com.aid.train.backend.domain.session.enums.SessionStatus.class);

    public final ListPath<Transcript, QTranscript> transcripts = this.<Transcript, QTranscript>createList("transcripts", Transcript.class, QTranscript.class, PathInits.DIRECT2);

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public final com.aid.train.backend.domain.user.entity.QUser user;

    public QDialogueSession(String variable) {
        this(DialogueSession.class, forVariable(variable), INITS);
    }

    public QDialogueSession(Path<? extends DialogueSession> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QDialogueSession(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QDialogueSession(PathMetadata metadata, PathInits inits) {
        this(DialogueSession.class, metadata, inits);
    }

    public QDialogueSession(Class<? extends DialogueSession> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.scenario = inits.isInitialized("scenario") ? new com.aid.train.backend.domain.scenario.entity.QScenario(forProperty("scenario"), inits.get("scenario")) : null;
        this.user = inits.isInitialized("user") ? new com.aid.train.backend.domain.user.entity.QUser(forProperty("user")) : null;
    }

}

