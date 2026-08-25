package br.com.fiap.aguiabranca.domain.strategy;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StrategyRepository extends JpaRepository<Strategy, Long> {

    List<Strategy> findAllByOrderByIdDesc();
}
