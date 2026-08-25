package br.com.fiap.aguiabranca.domain.project;

/** Uma fatia do grafico de status do dashboard. */
public record ProjectStatusCount(ProjectStatus status, Long total) {
}
