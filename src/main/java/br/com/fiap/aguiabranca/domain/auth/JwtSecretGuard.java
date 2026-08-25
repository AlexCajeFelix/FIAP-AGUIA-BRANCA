package br.com.fiap.aguiabranca.domain.auth;

import java.util.Arrays;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Impede que o segredo de desenvolvimento suba em ambiente que nao seja dev.
 *
 * O valor de dev esta versionado em application-dev.yml — qualquer um que leia o repositorio
 * consegue forjar um token de LIDERANCA. O tamanho dele passa na validacao de bytes do
 * JwtProperties, entao so essa checagem por valor o pega.
 */
@Component
class JwtSecretGuard {

    static final String DEV_SECRET = "dev-secret-nao-use-em-producao-troque-me-agora";

    JwtSecretGuard(JwtProperties properties, Environment environment) {
        boolean devProfile = Arrays.asList(environment.getActiveProfiles()).contains("dev");

        if (!devProfile && DEV_SECRET.equals(properties.secret())) {
            throw new IllegalStateException(
                    "JWT_SECRET esta com o valor de desenvolvimento, que e publico no repositorio, "
                            + "e o profile ativo nao e dev. Defina JWT_SECRET no ambiente. "
                            + "Gere uma com: openssl rand -base64 48");
        }
    }
}
