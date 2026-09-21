package br.com.fiap.aguiabranca.domain.ai;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.gemini.limits")
public record SuggestionLimitProperties(Integer requestsPerMinute, Integer maxConcurrent) {

    public SuggestionLimitProperties {
        requestsPerMinute = requestsPerMinute == null ? 6 : requestsPerMinute;
        maxConcurrent = maxConcurrent == null ? 3 : maxConcurrent;
        if (requestsPerMinute < 1 || maxConcurrent < 1) {
            throw new IllegalArgumentException("Limites do assistente devem ser positivos.");
        }
    }
}
