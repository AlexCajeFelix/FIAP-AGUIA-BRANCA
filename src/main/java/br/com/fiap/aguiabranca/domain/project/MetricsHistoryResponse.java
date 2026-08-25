package br.com.fiap.aguiabranca.domain.project;

import java.math.BigDecimal;
import java.time.Instant;

public record MetricsHistoryResponse(
        Long id,
        ProjectMetricsHistory.Metric metric,
        BigDecimal oldValue,
        BigDecimal newValue,
        Instant changedAt) {

    public static MetricsHistoryResponse from(ProjectMetricsHistory entry) {
        return new MetricsHistoryResponse(
                entry.getId(),
                entry.getMetric(),
                entry.getOldValue(),
                entry.getNewValue(),
                entry.getChangedAt());
    }
}
