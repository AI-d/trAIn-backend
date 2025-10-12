package com.aid.train.backend.domain.user.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUser is a Querydsl query type for User
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUser extends EntityPathBase<User> {

    private static final long serialVersionUID = -193096514L;

    public static final QUser user = new QUser("user");

    public final ListPath<com.aid.train.backend.domain.terms.entity.UserConsent, com.aid.train.backend.domain.terms.entity.QUserConsent> consents = this.<com.aid.train.backend.domain.terms.entity.UserConsent, com.aid.train.backend.domain.terms.entity.QUserConsent>createList("consents", com.aid.train.backend.domain.terms.entity.UserConsent.class, com.aid.train.backend.domain.terms.entity.QUserConsent.class, PathInits.DIRECT2);

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> deletedAt = createDateTime("deletedAt", java.time.LocalDateTime.class);

    public final StringPath email = createString("email");

    public final BooleanPath emailVerified = createBoolean("emailVerified");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath name = createString("name");

    public final StringPath password = createString("password");

    public final EnumPath<com.aid.train.backend.domain.user.enums.Provider> primaryProvider = createEnum("primaryProvider", com.aid.train.backend.domain.user.enums.Provider.class);

    public final ListPath<SocialAccount, QSocialAccount> socialAccounts = this.<SocialAccount, QSocialAccount>createList("socialAccounts", SocialAccount.class, QSocialAccount.class, PathInits.DIRECT2);

    public final EnumPath<com.aid.train.backend.domain.user.enums.UserStatus> status = createEnum("status", com.aid.train.backend.domain.user.enums.UserStatus.class);

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public QUser(String variable) {
        super(User.class, forVariable(variable));
    }

    public QUser(Path<? extends User> path) {
        super(path.getType(), path.getMetadata());
    }

    public QUser(PathMetadata metadata) {
        super(User.class, metadata);
    }

}

