package br.com.fiap.aguiabranca.domain.ai;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuracao do assistente de redacao.
 *
 * A chave vem do ambiente (.env, que esta no .gitignore) e nunca do codigo. Ela fica no
 * servidor de proposito: embutida no APK, qualquer pessoa que baixe o app extrai a chave com
 * apktool e gasta a cota — por isso o app chama esta API, e nao o Google direto.
 */
@ConfigurationProperties(prefix = "app.gemini")
public record GeminiProperties(
        String apiKey,
        String model,
        Duration timeout) {

    public GeminiProperties {
        model = (model == null || model.isBlank()) ? "gemini-2.5-flash" : model;
        timeout = timeout == null ? Duration.ofSeconds(20) : timeout;
        if (timeout.isZero() || timeout.isNegative() || timeout.compareTo(Duration.ofMinutes(1)) > 0) {
            throw new IllegalArgumentException("Timeout do Gemini deve ser maior que zero e no maximo 1 minuto.");
        }
    }

    @Override
    public String toString() {
        return "GeminiProperties[apiKey=***, model=" + model + ", timeout=" + timeout + "]";
    }
}
