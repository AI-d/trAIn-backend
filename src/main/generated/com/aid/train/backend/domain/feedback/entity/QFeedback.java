package com.aid.train.backend.domain.feedback.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QFeedback is a Querydsl query type for Feedback
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QFeedback extends EntityPathBase<Feedback> {

    private static final long serialVersionUID = -354856654L;

    public static final QFeedback feedback = new QFeedback("feedback");

    public final StringPath audioUrl = createString("audioUrl");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> dialogueSessionId = createNumber("dialogueSessionId", Long.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath improvements = createString("improvements");

    public final StringPath scores = createString("scores");

    public final StringPath transcript = createString("transcript");

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public QFeedback(String variable) {
        super(Feedback.class, forVariable(variable));
    }

    public QFeedback(Path<? extends Feedback> path) {
        super(path.getType(), path.getMetadata());
    }

    public QFeedback(PathMetadata metadata) {
        super(Feedback.class, metadata);
    }

}

