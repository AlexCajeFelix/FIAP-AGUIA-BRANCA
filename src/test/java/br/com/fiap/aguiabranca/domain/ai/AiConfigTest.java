package br.com.fiap.aguiabranca.domain.ai;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

class AiConfigTest {

    @Test
    void shouldStopWaitingWhenProviderDoesNotRespond() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        CountDownLatch release = new CountDownLatch(1);
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            server.setExecutor(executor);
            server.createContext("/", exchange -> {
                try {
                    release.await(5, TimeUnit.SECONDS);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } finally {
                    exchange.close();
                }
            });
            server.start();
            try {
                GeminiProperties properties = new GeminiProperties("test-key", null, Duration.ofMillis(150));
                RestClient client = new AiConfig().geminiRestClient(RestClient.builder(), properties)
                        .mutate().baseUrl("http://127.0.0.1:" + server.getAddress().getPort()).build();
                GeminiClient gemini = new GeminiClient(client, properties);
                assertTimeoutPreemptively(Duration.ofSeconds(3), () ->
                        assertThatThrownBy(() -> gemini.improve("Titulo", "Rascunho"))
                                .isInstanceOf(SuggestionUnavailableException.class));
            } finally {
                release.countDown();
                server.stop(0);
            }
        }
    }

    @Test
    void shouldDisableAssistantWithoutKeyAndEnableItWithConfiguredKey() {
        var context = new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(RestClientAutoConfiguration.class))
                .withUserConfiguration(AiConfig.class, GeminiClient.class,
                        IdeaSuggestionController.class, SuggestionLimiter.class);
        context.withPropertyValues("app.gemini.api-key=").run(application -> {
            assertThat(application).doesNotHaveBean(GeminiClient.class);
            assertThat(application).doesNotHaveBean(IdeaSuggestionController.class);
        });
        context.withPropertyValues("app.gemini.api-key=test-key").run(application -> {
            assertThat(application).hasSingleBean(GeminiClient.class);
            assertThat(application).hasSingleBean(IdeaSuggestionController.class);
        });
    }
}
