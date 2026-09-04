package br.com.fiap.aguiabranca.domain.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.rate-limit")
public record RateLimitProperties(
        boolean enabled,
        LimitRule ip,
        LimitRule email
) {
    public RateLimitProperties {
        if (ip == null) {
            ip = new LimitRule(10, 1);
        }
        if (email == null) {
            email = new LimitRule(5, 15);
        }
    }

    public record LimitRule(
            int capacity,
            int durationMinutes
    ) {
    }
}
