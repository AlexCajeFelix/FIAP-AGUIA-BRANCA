package br.com.fiap.aguiabranca.domain.strategy;

import br.com.fiap.aguiabranca.shared.ErrorTypes;
import br.com.fiap.aguiabranca.shared.ResourceNotFoundException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StrategyService {

    private final StrategyRepository strategies;

    public StrategyService(StrategyRepository strategies) {
        this.strategies = strategies;
    }

    @Transactional(readOnly = true)
    public List<Strategy> list() {
        return strategies.findAllByOrderByIdDesc();
    }

    @Transactional(readOnly = true)
    public Strategy findById(Long id) {
        return strategies.findById(id).orElseThrow(() -> notFound(id));
    }

    @Transactional
    public Strategy create(StrategyRequest request) {
        return strategies.save(new Strategy(request.title(), request.description(), request.horizon()));
    }

    @Transactional
    public Strategy update(Long id, StrategyRequest request) {
        Strategy strategy = strategies.findById(id).orElseThrow(() -> notFound(id));
        strategy.update(request.title(), request.description(), request.horizon());
        return strategy;
    }

    /** Soft delete: marca deleted_at. O @SQLRestriction tira a linha das leituras seguintes. */
    @Transactional
    public void delete(Long id) {
        strategies.findById(id).orElseThrow(() -> notFound(id)).softDelete();
    }

    private ResourceNotFoundException notFound(Long id) {
        return new ResourceNotFoundException(ErrorTypes.STRATEGY_NOT_FOUND,
                "Estratégia " + id + " não encontrada.");
    }
}
