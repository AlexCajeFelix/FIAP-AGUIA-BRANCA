package br.com.fiap.aguiabranca.shared.persistence;

/**
 * Documento cujo id e um Long sequencial, e nao o ObjectId nativo do Mongo.
 *
 * O contrato da API expoe id numerico (/projects/12) e o app Android ja consome assim. Trocar
 * por ObjectId quebraria cliente e OpenAPI de uma vez — por isso a sequencia e explicita.
 */
public interface SequentialDocument {

    Long getId();

    /**
     * Atribui o id gerado. Chamado uma unica vez, pelo {@link MongoIdAssigner}, antes do
     * primeiro insert: sobrescrever id de documento ja gravado criaria uma copia orfa em vez
     * de atualizar a original.
     */
    void assignId(Long id);
}
