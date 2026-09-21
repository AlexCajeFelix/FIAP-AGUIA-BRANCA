package br.com.fiap.aguiabranca.domain.ai;

import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.hamcrest.Matchers.containsString;

/**
 * O cliente do Gemini sem chamar o Gemini.
 *
 * Teste que bate na API de verdade depende de chave, de cota e de rede — tres motivos para o
 * CI ficar vermelho sem nada ter quebrado no codigo.
 */
class GeminiClientTest {

    private static final GeminiProperties PROPERTIES =
            new GeminiProperties("chave-de-teste", "gemini-2.5-flash", Duration.ofSeconds(5));

    @Test
    @DisplayName("Devolve o texto do primeiro candidato")
    void shouldReturnGeneratedText() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        GeminiClient client = new GeminiClient(builder.baseUrl("https://generativelanguage.googleapis.com/v1beta").build(), PROPERTIES);

        server.expect(requestTo("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent"))
                .andExpect(header("x-goog-api-key", "chave-de-teste"))
                .andExpect(jsonPath("$.systemInstruction.parts[0].text").isNotEmpty())
                .andExpect(jsonPath("$.contents[0].role").value("user"))
                .andExpect(jsonPath("$.contents[0].parts[0].text").value("Titulo: Titulo\nRascunho: rascunho"))
                .andRespond(withSuccess("""
                        {"candidates":[{"finishReason":"STOP","content":{"parts":[{"text":"  Texto reescrito.  "}]}}]}
                        """, MediaType.APPLICATION_JSON));

        assertThat(client.improve("Titulo", "rascunho")).isEqualTo("Texto reescrito.");
        server.verify();
    }

    @Test
    @DisplayName("Resposta sem candidato vira SuggestionUnavailable, nao NullPointerException")
    void shouldFailWhenThereIsNoCandidate() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        GeminiClient client = new GeminiClient(builder.baseUrl("https://generativelanguage.googleapis.com/v1beta").build(), PROPERTIES);

        // Acontece de verdade quando o modelo bloqueia a resposta por politica de conteudo.
        server.expect(requestTo(containsString("generateContent")))
                .andRespond(withSuccess("{\"candidates\":[]}", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> client.improve("Titulo", "rascunho"))
                .isInstanceOf(SuggestionUnavailableException.class);
    }

    @Test
    @DisplayName("Falha do Google vira erro de dominio com mensagem para a tela")
    void shouldTranslateUpstreamFailure() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        GeminiClient client = new GeminiClient(builder.baseUrl("https://generativelanguage.googleapis.com/v1beta").build(), PROPERTIES);

        server.expect(requestTo(containsString("generateContent"))).andRespond(withServerError());

        assertThatThrownBy(() -> client.improve("Titulo", "rascunho"))
                .isInstanceOf(SuggestionUnavailableException.class)
                .hasMessageContaining("assistente");
    }

    @Test
    @DisplayName("Modelo e timeout tem default: configuracao pela metade nao derruba o boot")
    void shouldApplyDefaults() {
        GeminiProperties defaults = new GeminiProperties("chave", null, null);

        assertThat(defaults.model()).isEqualTo("gemini-2.5-flash");
        assertThat(defaults.timeout()).isEqualTo(Duration.ofSeconds(20));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{}", "{\"candidates\":[null]}",
            "{\"candidates\":[{\"finishReason\":\"STOP\"}]}",
            "{\"candidates\":[{\"finishReason\":\"STOP\",\"content\":{\"parts\":[null,{}]}}]}",
            "{\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"Sem finalizacao\"}]}}]}"
    })
    void shouldRejectMalformedOrIncompleteResponses(String response) {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        GeminiClient client = new GeminiClient(builder.baseUrl("https://example.test").build(), PROPERTIES);
        server.expect(requestTo(containsString("generateContent")))
                .andRespond(withSuccess(response, MediaType.APPLICATION_JSON));
        assertThatThrownBy(() -> client.improve("Titulo", "rascunho"))
                .isInstanceOf(SuggestionUnavailableException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"MAX_TOKENS", "SAFETY", "RECITATION", "OTHER"})
    void shouldNotReturnPartialOrBlockedText(String finishReason) {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        GeminiClient client = new GeminiClient(builder.baseUrl("https://example.test").build(), PROPERTIES);
        server.expect(requestTo(containsString("generateContent")))
                .andRespond(withSuccess("""
                        {"candidates":[{"finishReason":"%s","content":{"parts":[{"text":"Texto parcial"}]}}]}
                        """.formatted(finishReason), MediaType.APPLICATION_JSON));
        assertThatThrownBy(() -> client.improve("Titulo", "rascunho"))
                .isInstanceOf(SuggestionUnavailableException.class);
    }

    @Test
    void shouldJoinAllTextPartsWithoutExposingThoughts() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        GeminiClient client = new GeminiClient(builder.baseUrl("https://example.test").build(), PROPERTIES);
        server.expect(requestTo(containsString("generateContent")))
                .andRespond(withSuccess("""
                        {"candidates":[{"finishReason":"STOP","content":{"parts":[
                            {"text":"Raciocinio privado","thought":true}, null, {},
                            {"text":"Problema. "},{"text":"Proposta e beneficio."}
                        ]}}]}
                        """, MediaType.APPLICATION_JSON));
        assertThat(client.improve("Titulo", "rascunho")).isEqualTo("Problema. Proposta e beneficio.");
    }

    @Test
    void shouldRejectTextThatCannotBeUsedAsAnotherDraft() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        GeminiClient client = new GeminiClient(builder.baseUrl("https://example.test").build(), PROPERTIES);
        server.expect(requestTo(containsString("generateContent")))
                .andRespond(withSuccess("""
                        {"candidates":[{"finishReason":"STOP","content":{"parts":[{"text":"%s"}]}}]}
                        """.formatted("a".repeat(2001)), MediaType.APPLICATION_JSON));
        assertThatThrownBy(() -> client.improve("Titulo", "rascunho"))
                .isInstanceOf(SuggestionUnavailableException.class);
    }

    @Test
    void shouldHideKeyFromConfigurationDiagnosticsAndRejectInfiniteTimeout() {
        assertThat(PROPERTIES.toString()).doesNotContain(PROPERTIES.apiKey());
        assertThatThrownBy(() -> new GeminiProperties("key", null, Duration.ZERO))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new GeminiProperties("key", null, Duration.ofSeconds(-1)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
