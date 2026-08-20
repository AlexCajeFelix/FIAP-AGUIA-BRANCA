package br.com.fiap.aguiabranca.domain.project;

import java.math.BigDecimal;

public record ProjectSummaryDto(
        Long totalProjects,
        Double avgProgress,
        BigDecimal totalBudget) {
}
