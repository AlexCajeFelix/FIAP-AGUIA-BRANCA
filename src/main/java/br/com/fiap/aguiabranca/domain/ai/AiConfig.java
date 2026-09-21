package br.com.fiap.aguiabranca.domain.ai;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import java.net.http.HttpClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/** Registra a configuracao do assistente. Os beans ficam condicionados a existir chave. */
@Configuration
@EnableConfigurationProperties({GeminiProperties.class, SuggestionLimitProperties.class})
public class AiConfig {

    @Bean("geminiRestClient")
    @ConditionalOnExpression("!'${app.gemini.api-key:}'.isBlank()")
    RestClient geminiRestClient(RestClient.Builder builder, GeminiProperties properties) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(properties.timeout())
                .build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(properties.timeout());
        return builder.clone()
                .baseUrl("https://generativelanguage.googleapis.com/v1beta")
                .requestFactory(factory)
                .build();
    }
}
