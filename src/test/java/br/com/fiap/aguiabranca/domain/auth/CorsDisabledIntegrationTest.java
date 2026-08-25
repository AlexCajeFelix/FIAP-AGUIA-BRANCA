package br.com.fiap.aguiabranca.domain.auth;

import br.com.fiap.aguiabranca.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

/**
 * Configuracao default — nenhuma origem listada. Nada de cross-origin pode passar.
 */
class CorsDisabledIntegrationTest extends IntegrationTestSupport {

    @Test
    @DisplayName("Sem origem configurada, o preflight nao recebe Allow-Origin")
    void shouldNotAllowAnyOriginByDefault() throws Exception {
        mockMvc.perform(options("/ideas")
                .header("Origin", "https://qualquer-um.example")
                .header("Access-Control-Request-Method", "GET"))
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }

    @Test
    @DisplayName("Sem origem configurada, resposta normal tambem nao ganha Allow-Origin")
    void shouldNotDecorateSimpleRequests() throws Exception {
        mockMvc.perform(get("/ideas").header("Origin", "https://qualquer-um.example"))
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }
}
