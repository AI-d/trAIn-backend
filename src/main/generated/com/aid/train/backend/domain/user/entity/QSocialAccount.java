package com.aid.train.backend.domain.user.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QSocialAccount is a Querydsl query type for SocialAccount
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSocialAccount extends EntityPathBase<SocialAccount> {

    private static final long serialVersionUID = -304546195L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QSocialAccount socialAccount = new QSocialAccount("socialAccount");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isConnected = createBoolean("isConnected");

    public final DateTimePath<java.time.LocalDateTime> lastLoginAt = createDateTime("lastLoginAt", java.time.LocalDateTime.class);

    public final EnumPath<com.aid.train.backend.domain.user.enums.Provider> provider = createEnum("provider", com.aid.train.backend.domain.user.enums.Provider.class);

    public final StringPath providerId = createString("providerId");

    public final StringPath socialEmail = createString("socialEmail");

    public final StringPath socialName = createString("socialName");

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public final QUser user;

    public QSocialAccount(String variable) {
        this(SocialAccount.class, forVariable(variable), INITS);
    }

    public QSocialAccount(Path<? extends SocialAccount> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QSocialAccount(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QSocialAccount(PathMetadata metadata, PathInits inits) {
        this(SocialAccount.class, metadata, inits);
    }

    public QSocialAccount(Class<? extends SocialAccount> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new QUser(forProperty("user")) : null;
    }

}

