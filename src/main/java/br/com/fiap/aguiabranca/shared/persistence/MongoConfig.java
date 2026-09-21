package br.com.fiap.aguiabranca.shared.persistence;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.core.mapping.event.ValidatingMongoEventListener;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

/** O que a aplicacao inteira precisa do Mongo, alem do minimo de {@link MongoPersistenceConfig}. */
@Configuration
@Import(MongoPersistenceConfig.class)
public class MongoConfig {

    /**
     * Sem isto as anotacoes de validacao das entidades viram decoracao: o Hibernate as
     * aplicava antes de cada insert, o driver do Mongo nao aplica nada.
     */
    @Bean
    public ValidatingMongoEventListener validatingMongoEventListener(LocalValidatorFactoryBean factory) {
        return new ValidatingMongoEventListener(factory);
    }

    /**
     * Transacao no Mongo exige replica set — o compose sobe um de um no so, e o
     * MongoDBContainer dos testes ja vem em replica set. Quem apontar para um mongod avulso
     * ve falhar o unico metodo que depende disso: ProjectService.updateMetrics.
     */
    @Bean
    public MongoTransactionManager mongoTransactionManager(MongoDatabaseFactory databaseFactory) {
        return new MongoTransactionManager(databaseFactory);
    }
}
