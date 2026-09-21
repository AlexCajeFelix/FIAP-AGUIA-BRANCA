package br.com.fiap.aguiabranca.shared.persistence;

import java.math.BigDecimal;
import java.util.List;
import org.bson.types.Decimal128;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

/**
 * O minimo para um documento deste projeto ir e voltar do Mongo inteiro.
 *
 * Fica separado do {@link MongoConfig} porque teste de slice (@DataMongoTest) nao carrega
 * @Component nenhum: sem poder importar so isto, o id sequencial nao seria atribuido e o
 * Mongo geraria um ObjectId onde o dominio espera Long.
 */
@Configuration
public class MongoPersistenceConfig {

    @Bean
    public SequenceGenerator sequenceGenerator(MongoOperations mongo) {
        return new SequenceGenerator(mongo);
    }

    @Bean
    public MongoIdAssigner mongoIdAssigner(SequenceGenerator sequences) {
        return new MongoIdAssigner(sequences);
    }

    /**
     * Dinheiro como Decimal128, nunca String.
     *
     * A representacao default de BigDecimal varia entre versoes do Spring Data, e guardar
     * valor financeiro como texto seria silencioso e fatal: o $sum do dashboard devolve null
     * somando string, e o total viraria zero na tela sem erro nenhum. Decimal128 e o
     * equivalente do NUMERIC(15,2) que a V1 usava, com a escala preservada.
     */
    @Bean
    public MongoCustomConversions mongoCustomConversions() {
        return new MongoCustomConversions(List.of(
                BigDecimalToDecimal128Converter.INSTANCE,
                Decimal128ToBigDecimalConverter.INSTANCE));
    }

    @WritingConverter
    enum BigDecimalToDecimal128Converter implements Converter<BigDecimal, Decimal128> {
        INSTANCE;

        @Override
        public Decimal128 convert(BigDecimal source) {
            return new Decimal128(source);
        }
    }

    @ReadingConverter
    enum Decimal128ToBigDecimalConverter implements Converter<Decimal128, BigDecimal> {
        INSTANCE;

        @Override
        public BigDecimal convert(Decimal128 source) {
            return source.bigDecimalValue();
        }
    }
}
