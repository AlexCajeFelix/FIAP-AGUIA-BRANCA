package br.com.fiap.aguiabranca.domain.auth;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(String secret, Duration expiration, Duration refreshExpiration) {

    /** HS256 assina com bloco de 256 bits: chave menor enfraquece a assinatura. */
    static final int MIN_SECRET_BYTES = 32;

    /**
     * Valida na propria vinculacao da propriedade, nao num listener depois do boot.
     *
     * A jjwt 0.12 tambem recusa chave curta, mas so na primeira assinatura — ou seja, no
     * primeiro login de um usuario real, em runtime. Aqui a aplicacao simplesmente nao sobe.
     */
    public JwtProperties {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                    "JWT_SECRET nao definida. Gere uma com: openssl rand -base64 48");
        }

        int bytes = secret.getBytes(StandardCharsets.UTF_8).length;
        if (bytes < MIN_SECRET_BYTES) {
            // Informa o tamanho, nunca o valor: mensagem de erro vai para log, e log vaza.
            throw new IllegalStateException(
                    "JWT_SECRET tem " + bytes + " bytes; o minimo para HS256 e " + MIN_SECRET_BYTES
                            + ". Gere uma com: openssl rand -base64 48");
        }
    }
}
