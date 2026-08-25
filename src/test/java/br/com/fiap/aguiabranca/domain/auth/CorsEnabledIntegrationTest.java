package br.com.fiap.aguiabranca.domain.auth;

import br.com.fiap.aguiabranca.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestPropertySource(properties = "app.cors.allowed-origins=https://hub.aguiabranca.dev")
class CorsEnabledIntegrationTest extends IntegrationTestSupport {

    private static final String ALLOWED = "https://hub.aguiabranca.dev";
    private static final String DENIED = "https://site-qualquer.example";

    @Test
    @DisplayName("Preflight de origem permitida responde 200 com os headers corretos")
    void shouldAllowConfiguredOrigin() throws Exception {
        mockMvc.perform(options("/ideas")
                .header("Origin", ALLOWED)
                .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", ALLOWED))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }

    @Test
    @DisplayName("Preflight de origem nao listada e recusado")
    void shouldRejectUnknownOrigin() throws Exception {
        mockMvc.perform(options("/ideas")
                .header("Origin", DENIED)
                .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }

    @Test
    @DisplayName("Authorization esta entre os headers permitidos")
    void shouldAllowAuthorizationHeader() throws Exception {
        // Sem isto o navegador barra o preflight e nenhuma chamada autenticada sai do front.
        mockMvc.perform(options("/ideas")
                .header("Origin", ALLOWED)
                .header("Access-Control-Request-Method", "GET")
                .header("Access-Control-Request-Headers", "Authorization"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Headers", containsString("Authorization")));
    }

    @Test
    @DisplayName("Com credenciais habilitadas, Allow-Origin nunca sai como curinga")
    void shouldNeverEchoWildcardWithCredentials() throws Exception {
        // "*" com allowCredentials e recusado pelo proprio Spring em runtime; o header tem
        // que ecoar a origem que pediu.
        mockMvc.perform(options("/ideas")
                .header("Origin", ALLOWED)
                .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", ALLOWED));
    }
}
