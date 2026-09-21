package br.com.fiap.aguiabranca.domain.idea;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

/** A decisao e gravada somente enquanto a ideia ainda esta pendente, em uma operacao atomica. */
public class IdeaRepositoryImpl implements IdeaReviewStore {

    private final MongoTemplate mongo;

    public IdeaRepositoryImpl(MongoTemplate mongo) {
        this.mongo = mongo;
    }

    @Override
    public boolean saveReviewIfPending(Idea reviewed) {
        return mongo.updateFirst(
                Query.query(Criteria.where("_id").is(reviewed.getId())
                        .and("status").in(Idea.Status.DRAFT, Idea.Status.IN_REVIEW)),
                new Update().set("status", reviewed.getStatus())
                        .set("reviewedById", reviewed.getReviewedById())
                        .set("reviewedAt", reviewed.getReviewedAt()),
                Idea.class).getMatchedCount() == 1;
    }
}
