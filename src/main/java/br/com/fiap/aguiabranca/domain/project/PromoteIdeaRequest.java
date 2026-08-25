package br.com.fiap.aguiabranca.domain.project;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record PromoteIdeaRequest(
        @NotNull(message = "Orçamento é obrigatório") @DecimalMin(value = "0.0", inclusive = false, message = "Orçamento deve ser maior que zero") BigDecimal budget) {
}
