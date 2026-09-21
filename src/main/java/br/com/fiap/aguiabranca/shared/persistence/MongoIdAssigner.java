package br.com.fiap.aguiabranca.shared.persistence;

import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;

/**
 * Preenche o id sequencial antes do documento virar BSON.
 *
 * Roda em onBeforeConvert porque depois disso o id ja foi lido: atribuir mais tarde gravaria
 * null no _id e o Mongo geraria um ObjectId no lugar, quebrando o contrato numerico.
 */
public class MongoIdAssigner extends AbstractMongoEventListener<SequentialDocument> {

    private final SequenceGenerator sequences;

    public MongoIdAssigner(SequenceGenerator sequences) {
        this.sequences = sequences;
    }

    @Override
    public void onBeforeConvert(BeforeConvertEvent<SequentialDocument> event) {
        SequentialDocument document = event.getSource();
        if (document.getId() == null) {
            // O nome da colecao e o nome da sequencia: cada colecao tem a propria contagem,
            // como cada tabela tinha a propria BIGSERIAL.
            document.assignId(sequences.next(event.getCollectionName()));
        }
    }
}
