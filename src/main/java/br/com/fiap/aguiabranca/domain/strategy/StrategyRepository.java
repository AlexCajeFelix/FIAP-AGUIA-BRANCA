package br.com.fiap.aguiabranca.domain.strategy;

import java.util.List;

public interface StrategyRepository {

    Strategy save(Strategy strategy);

    java.util.Optional<Strategy> findById(Long id);

    List<Strategy> findAllByOrderByIdDesc();

    void deleteAll();
}
