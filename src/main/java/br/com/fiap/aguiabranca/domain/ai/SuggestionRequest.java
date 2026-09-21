package br.com.fiap.aguiabranca.domain.ai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SuggestionRequest(
        @NotBlank(message = "Título é obrigatório") @Size(max = 150) String title,
        @NotBlank(message = "Rascunho é obrigatório") @Size(max = 2000) String draft) {
}
