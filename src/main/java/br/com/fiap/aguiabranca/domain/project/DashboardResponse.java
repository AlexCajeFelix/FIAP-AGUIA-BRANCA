package br.com.fiap.aguiabranca.domain.project;

import java.util.List;

/** O que a tela inicial do app pede numa chamada so. */
public record DashboardResponse(ProjectSummaryDto summary, List<ProjectStatusCount> byStatus) {
}
