package br.com.fiap.aguiabranca.domain.strategy;

import java.time.Instant;

public record StrategyResponse(
        Long id,
        String title,
        String description,
        Horizon horizon,
        Instant createdAt) {

    public static StrategyResponse from(Strategy strategy) {
        return new StrategyResponse(
                strategy.getId(),
                strategy.getTitle(),
                strategy.getDescription(),
                strategy.getHorizon(),
                strategy.getCreatedAt());
    }
}
