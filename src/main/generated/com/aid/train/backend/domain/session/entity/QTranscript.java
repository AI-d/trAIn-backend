package com.aid.train.backend.domain.session.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QTranscript is a Querydsl query type for Transcript
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QTranscript extends EntityPathBase<Transcript> {

    private static final long serialVersionUID = -1624628610L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QTranscript transcript = new QTranscript("transcript");

    public final NumberPath<Float> confidenceScore = createNumber("confidenceScore", Float.class);

    public final StringPath content = createString("content");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final QDialogueSession dialogueSession;

    public final NumberPath<Long> endTimeMs = createNumber("endTimeMs", Long.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final EnumPath<com.aid.train.backend.domain.session.enums.Speaker> speaker = createEnum("speaker", com.aid.train.backend.domain.session.enums.Speaker.class);

    public final NumberPath<Long> startTimeMs = createNumber("startTimeMs", Long.class);

    public final DateTimePath<java.time.LocalDateTime> timestamp = createDateTime("timestamp", java.time.LocalDateTime.class);

    public QTranscript(String variable) {
        this(Transcript.class, forVariable(variable), INITS);
    }

    public QTranscript(Path<? extends Transcript> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QTranscript(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QTranscript(PathMetadata metadata, PathInits inits) {
        this(Transcript.class, metadata, inits);
    }

    public QTranscript(Class<? extends Transcript> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.dialogueSession = inits.isInitialized("dialogueSession") ? new QDialogueSession(forProperty("dialogueSession"), inits.get("dialogueSession")) : null;
    }

}

