package br.com.fiap.aguiabranca.domain.strategy;

import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StrategyRepository extends MongoRepository<Strategy, Long> {

    /**
     * O filtro de soft delete vive aqui porque o Mongo nao tem o @SQLRestriction do Hibernate.
     * Toda leitura do dominio passa por um destes dois metodos — findById cru enxergaria o
     * documento excluido e o traria de volta para a API.
     */
    List<Strategy> findAllByDeletedAtIsNullOrderByIdDesc();

    Optional<Strategy> findByIdAndDeletedAtIsNull(Long id);
}
