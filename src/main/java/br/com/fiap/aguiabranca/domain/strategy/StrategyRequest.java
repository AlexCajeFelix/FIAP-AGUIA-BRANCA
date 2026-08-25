package br.com.fiap.aguiabranca.domain.strategy;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record StrategyRequest(
        @NotBlank(message = "Título é obrigatório") @Size(max = 150, message = "Título tem no máximo 150 caracteres") String title,
        @NotBlank(message = "Descrição é obrigatória") String description,
        @NotNull(message = "Horizonte é obrigatório") Horizon horizon) {
}
