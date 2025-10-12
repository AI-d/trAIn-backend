package com.aid.train.backend.domain.verification.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QPendingSocialUser is a Querydsl query type for PendingSocialUser
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPendingSocialUser extends EntityPathBase<PendingSocialUser> {

    private static final long serialVersionUID = 2064069228L;

    public static final QPendingSocialUser pendingSocialUser = new QPendingSocialUser("pendingSocialUser");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final StringPath email = createString("email");

    public final DateTimePath<java.time.LocalDateTime> expiryDate = createDateTime("expiryDate", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isCompleted = createBoolean("isCompleted");

    public final StringPath name = createString("name");

    public final EnumPath<com.aid.train.backend.domain.user.enums.Provider> provider = createEnum("provider", com.aid.train.backend.domain.user.enums.Provider.class);

    public final StringPath providerId = createString("providerId");

    public final StringPath tempToken = createString("tempToken");

    public QPendingSocialUser(String variable) {
        super(PendingSocialUser.class, forVariable(variable));
    }

    public QPendingSocialUser(Path<? extends PendingSocialUser> path) {
        super(path.getType(), path.getMetadata());
    }

    public QPendingSocialUser(PathMetadata metadata) {
        super(PendingSocialUser.class, metadata);
    }

}

