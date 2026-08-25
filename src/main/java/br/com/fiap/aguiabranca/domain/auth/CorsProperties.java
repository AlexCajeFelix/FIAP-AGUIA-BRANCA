package br.com.fiap.aguiabranca.domain.auth;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Origens autorizadas a chamar a API de um navegador.
 *
 * Lista vazia por default e a decisao central: entregar pronto e desligado. Enquanto o unico
 * cliente for o app Android — que nao passa por CORS —, nenhuma origem cross-origin funciona
 * sem alguem escrever explicitamente qual.
 */
@ConfigurationProperties(prefix = "app.cors")
public record CorsProperties(List<String> allowedOrigins) {

    public CorsProperties {
        allowedOrigins = allowedOrigins == null ? List.of() : List.copyOf(allowedOrigins);
    }

    public boolean isEnabled() {
        return !allowedOrigins.isEmpty();
    }
}
