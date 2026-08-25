package br.com.fiap.aguiabranca.domain.auth;

import br.com.fiap.aguiabranca.domain.user.Role;
import br.com.fiap.aguiabranca.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ActuatorIntegrationTest extends IntegrationTestSupport {

    @Test
    @DisplayName("Health responde 200 sem autenticacao")
    void shouldExposeHealthWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    @DisplayName("Health anonimo nao vaza detalhe de infraestrutura")
    void shouldHideDetailsFromAnonymous() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.components").doesNotExist());
    }

    @Test
    @DisplayName("Health autenticado mostra o status do Postgres")
    void shouldShowDatabaseStatusWhenAuthorized() throws Exception {
        String token = tokenFor("gestor-actuator@teste.dev", Role.GESTOR);

        mockMvc.perform(get("/actuator/health").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.components.db.status").value("UP"));
    }

    @Test
    @DisplayName("Endpoints fora de health e info nao existem, nem com token")
    void shouldNotExposeOtherEndpoints() throws Exception {
        String token = tokenFor("lideranca-actuator@teste.dev", Role.LIDERANCA);

        // /actuator/env entregaria as variaveis de ambiente, JWT_SECRET incluso.
        mockMvc.perform(get("/actuator/env").header("Authorization", bearer(token)))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/actuator/beans").header("Authorization", bearer(token)))
                .andExpect(status().isNotFound());
    }
}
