package br.com.fiap.aguiabranca.shared;

import static org.springframework.data.mongodb.core.FindAndModifyOptions.options;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

@Component
@Profile("!integration & !openapi")
public class MongoSequenceGenerator {

    private final MongoOperations mongo;

    public MongoSequenceGenerator(MongoOperations mongo) {
        this.mongo = mongo;
    }

    public long next(String name) {
        MongoSequence sequence = mongo.findAndModify(
                query(where("_id").is(name)),
                new Update().inc("value", 1),
                options().returnNew(true).upsert(true),
                MongoSequence.class);
        return sequence == null ? 1L : sequence.getValue();
    }
}
