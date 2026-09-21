package br.com.fiap.aguiabranca.domain.auth;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

/** Fragmento customizado de {@link RefreshTokenRepository}; o sufixo Impl e o que o Spring procura. */
public class RefreshTokenRepositoryImpl implements RefreshTokenRevocation {

    private final MongoTemplate mongo;

    public RefreshTokenRepositoryImpl(MongoTemplate mongo) {
        this.mongo = mongo;
    }

    @Override
    public Optional<RefreshToken> claimIfActive(String tokenHash, Instant now) {
        return Optional.ofNullable(mongo.findAndModify(
                Query.query(Criteria.where("tokenHash").is(tokenHash).and("revokedAt").is(null)),
                new Update().set("revokedAt", now),
                FindAndModifyOptions.options().returnNew(true),
                RefreshToken.class));
    }

    @Override
    public void revokeFamily(UUID familyId, Instant now) {
        mongo.updateMulti(
                Query.query(Criteria.where("familyId").is(familyId).and("revokedAt").is(null)),
                new Update().set("revokedAt", now),
                RefreshToken.class);
    }
}
