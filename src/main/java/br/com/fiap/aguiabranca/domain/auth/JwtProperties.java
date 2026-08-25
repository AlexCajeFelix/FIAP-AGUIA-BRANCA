package br.com.fiap.aguiabranca.domain.auth;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * TODO(#6): secret ainda aceita o default de dev vindo do application.yml.
 * TODO(#8): expiration de 8h sem refresh — o app expulsa o usuario no meio do uso.
 */
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(String secret, Duration expiration) {
}
