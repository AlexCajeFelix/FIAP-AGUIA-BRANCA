package br.com.fiap.aguiabranca.domain.project;

/**
 * Ciclo de vida do projeto.
 *
 * Os nomes sao contrato com o app (#24): o cliente desserializa direto neste enum, entao
 * renomear um valor quebra a tela de filtro sem aviso.
 */
public enum ProjectStatus {
    PLANNING,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
}
