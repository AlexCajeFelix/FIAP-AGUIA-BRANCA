package br.com.fiap.aguiabranca.shared;

import br.com.fiap.aguiabranca.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * A pagina do Swagger UI, sem token.
 *
 * O OpenApiIntegrationTest cobre /v3/api-docs, que e o contrato em JSON — e por isso o 401 da
 * interface passou despercebido: liberar so a URL de entrada nao basta. O /swagger-ui.html
 * responde 302 e joga o navegador em /swagger-ui/index.html, que por sua vez pede os estaticos
 * sob /swagger-ui/**. Se qualquer um desses degraus exigir autenticacao, a pagina nao abre.
 *
 * Este teste percorre os tres degraus, e nao so o primeiro.
 */
class SwaggerUiAccessIntegrationTest extends IntegrationTestSupport {

    @Test
    @DisplayName("/swagger-ui.html redireciona para a pagina da interface")
    void shouldRedirectEntryPointToUi() throws Exception {
        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", containsString("/swagger-ui/index.html")));
    }

    @Test
    @DisplayName("O destino do redirect abre sem token")
    void shouldServeUiPageAnonymously() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Os estaticos que a pagina carrega abrem sem token")
    void shouldServeUiAssetsAnonymously() throws Exception {
        // O initializer e o script que aponta a interface para /v3/api-docs: sem ele a pagina
        // carrega vazia, que e uma falha mais silenciosa que o 401.
        mockMvc.perform(get("/swagger-ui/swagger-initializer.js"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("A configuracao que a interface consome continua publica")
    void shouldServeSwaggerConfigAnonymously() throws Exception {
        mockMvc.perform(get("/v3/api-docs/swagger-config"))
                .andExpect(status().isOk());
    }
}
