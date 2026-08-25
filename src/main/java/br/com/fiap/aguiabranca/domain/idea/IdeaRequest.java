package br.com.fiap.aguiabranca.domain.idea;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record IdeaRequest(
        @NotBlank(message = "Título é obrigatório") @Size(max = 150, message = "Título tem no máximo 150 caracteres") String title,
        @NotBlank(message = "Descrição é obrigatória") String description) {
}
