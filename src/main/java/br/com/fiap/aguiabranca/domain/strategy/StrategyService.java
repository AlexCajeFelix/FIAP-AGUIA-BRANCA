package br.com.fiap.aguiabranca.domain.strategy;

import br.com.fiap.aguiabranca.shared.ErrorTypes;
import br.com.fiap.aguiabranca.shared.ResourceNotFoundException;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class StrategyService {

    private final StrategyRepository strategies;

    public StrategyService(StrategyRepository strategies) {
        this.strategies = strategies;
    }

    public List<Strategy> list() {
        return strategies.findAllByDeletedAtIsNullOrderByIdDesc();
    }

    public Strategy findById(Long id) {
        return strategies.findByIdAndDeletedAtIsNull(id).orElseThrow(() -> notFound(id));
    }

    public Strategy create(StrategyRequest request) {
        return strategies.save(new Strategy(request.title(), request.description(), request.horizon()));
    }

    public Strategy update(Long id, StrategyRequest request) {
        Strategy strategy = findById(id);
        strategy.update(request.title(), request.description(), request.horizon());
        return strategies.save(strategy);
    }

    /** Soft delete: marca deletedAt e regrava. O documento continua na colecao, fora das leituras. */
    public void delete(Long id) {
        Strategy strategy = findById(id);
        strategy.softDelete();
        strategies.save(strategy);
    }

    private ResourceNotFoundException notFound(Long id) {
        return new ResourceNotFoundException(ErrorTypes.STRATEGY_NOT_FOUND,
                "Estratégia " + id + " não encontrada.");
    }
}
