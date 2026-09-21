package br.com.fiap.aguiabranca.shared.persistence;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

/**
 * Substituto do BIGSERIAL: devolve o proximo id de uma colecao.
 *
 * O $inc dentro de findAndModify e atomico no servidor — duas requisicoes simultaneas recebem
 * numeros diferentes sem lock na aplicacao. Contador por colecao, guardado em "counters".
 */
public class SequenceGenerator {

    static final String COLLECTION = "counters";

    private final MongoOperations mongo;

    public SequenceGenerator(MongoOperations mongo) {
        this.mongo = mongo;
    }

    public long next(String sequenceName) {
        Counter counter = mongo.findAndModify(
                Query.query(Criteria.where("_id").is(sequenceName)),
                new Update().inc("seq", 1L),
                // upsert cria o contador na primeira chamada; returnNew devolve o valor ja
                // incrementado, que e justamente o id que ninguem mais vai receber.
                FindAndModifyOptions.options().returnNew(true).upsert(true),
                Counter.class);
        return counter == null ? 1L : counter.seq();
    }

    @Document(collection = COLLECTION)
    record Counter(@Id String id, long seq) {
    }
}
