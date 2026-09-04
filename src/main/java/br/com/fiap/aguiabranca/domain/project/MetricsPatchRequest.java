package br.com.fiap.aguiabranca.domain.project;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;

/**
 * PATCH parcial: os dois campos sao opcionais, mas mandar os dois nulos e erro — sem isso,
 * um PATCH vazio responderia 200 sem ter feito nada e sem gravar historico.
 */
public record MetricsPatchRequest(Integer progress, BigDecimal spent) {

    @JsonIgnore
    public boolean isEmpty() {
        return progress == null && spent == null;
    }
}
