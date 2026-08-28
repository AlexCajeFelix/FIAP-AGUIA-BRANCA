package br.com.fiap.aguiabranca.domain.idea;

import jakarta.validation.constraints.NotNull;

/** DRAFT aqui e recusado pela entidade: revisar e sair do rascunho, nao voltar para ele. */
public record IdeaReviewRequest(
        @NotNull(message = "Status é obrigatório") Idea.Status status) {
}
