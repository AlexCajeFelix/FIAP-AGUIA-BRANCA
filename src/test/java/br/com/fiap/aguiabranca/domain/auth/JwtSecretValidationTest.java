package br.com.fiap.aguiabranca.domain.auth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Valida o boot, nao o runtime: por isso ApplicationContextRunner em vez de @SpringBootTest.
 * O que se afirma aqui e "o contexto NAO sobe", e subir a app inteira so para vê-la falhar
 * custaria um Postgres por caso de teste.
 */
class JwtSecretValidationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of())
            .withUserConfiguration(JwtTestConfiguration.class)
            .withPropertyValues("app.jwt.expiration=PT8H");

    @Test
    @DisplayName("Sobe com segredo proprio de 32 bytes ou mais")
    void shouldStartWithStrongSecret() {
        runner.withPropertyValues("app.jwt.secret=um-segredo-bem-comprido-com-mais-de-32-bytes")
                .run(context -> assertThat(context).hasNotFailed());
    }

    @Test
    @DisplayName("Nao sobe com segredo menor que 32 bytes")
    void shouldFailWhenSecretIsTooShort() {
        runner.withPropertyValues("app.jwt.secret=curto-demais")
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(rootMessage(context.getStartupFailure())).contains("12 bytes", "minimo para HS256");
                });
    }

    @Test
    @DisplayName("Nao sobe sem JWT_SECRET definida")
    void shouldFailWhenSecretIsMissing() {
        runner.withPropertyValues("app.jwt.secret=")
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(rootMessage(context.getStartupFailure())).contains("JWT_SECRET nao definida");
                });
    }

    @Test
    @DisplayName("Nao sobe com o segredo de dev fora do profile dev")
    void shouldRejectDevSecretOutsideDevProfile() {
        runner.withPropertyValues("app.jwt.secret=" + JwtSecretGuard.DEV_SECRET)
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(rootMessage(context.getStartupFailure()))
                            .contains("valor de desenvolvimento");
                });
    }

    @Test
    @DisplayName("Aceita o segredo de dev quando o profile dev esta ativo")
    void shouldAcceptDevSecretUnderDevProfile() {
        runner.withPropertyValues("app.jwt.secret=" + JwtSecretGuard.DEV_SECRET)
                .withSystemProperties("spring.profiles.active=dev")
                .run(context -> assertThat(context).hasNotFailed());
    }

    @Test
    @DisplayName("A mensagem de erro nunca imprime o valor do segredo")
    void shouldNeverPrintTheSecretValue() {
        String secret = "curto";

        runner.withPropertyValues("app.jwt.secret=" + secret)
                .run(context -> {
                    assertThat(context).hasFailed();
                    // Mensagem de boot vai para log, e log vaza: ela informa o tamanho, nunca o valor.
                    assertThat(fullTrace(context.getStartupFailure())).doesNotContain(secret);
                });
    }

    private static String rootMessage(Throwable failure) {
        Throwable root = failure;
        while (root.getCause() != null) {
            root = root.getCause();
        }
        return String.valueOf(root.getMessage());
    }

    private static String fullTrace(Throwable failure) {
        StringBuilder text = new StringBuilder();
        for (Throwable current = failure; current != null; current = current.getCause()) {
            text.append(current.getMessage()).append('\n');
        }
        return text.toString();
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(JwtProperties.class)
    @Import(JwtSecretGuard.class)
    static class JwtTestConfiguration {
    }
}
