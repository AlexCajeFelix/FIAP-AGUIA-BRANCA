package br.com.fiap.aguiabranca.shared;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("sequences")
public class MongoSequence {

    @Id
    private String id;
    private long value;

    protected MongoSequence() {
    }

    public MongoSequence(String id, long value) {
        this.id = id;
        this.value = value;
    }

    public String getId() {
        return id;
    }

    public long getValue() {
        return value;
    }
}
