package br.com.fiap.aguiabranca.domain.strategy;

import br.com.fiap.aguiabranca.domain.user.Role;
import br.com.fiap.aguiabranca.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class StrategyIntegrationTest extends IntegrationTestSupport {

    private static final String PAYLOAD = """
            {"title": "Eletrificar frota", "description": "Trocar onibus diesel por eletrico.", "horizon": "MEDIUM"}
            """;

    @Test
    @DisplayName("POST GET PUT DELETE cobrem o caminho feliz de Gestor")
    void shouldSupportFullCrudForGestor() throws Exception {
        String token = tokenFor("gestor@teste.dev", Role.GESTOR);

        String created = mockMvc.perform(post("/strategies")
                .header("Authorization", bearer(token))
                .contentType("application/json")
                .content(PAYLOAD))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/strategies/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Eletrificar frota"))
                .andExpect(jsonPath("$.horizon").value("MEDIUM"))
                .andReturn().getResponse().getContentAsString();

        mockMvc.perform(get("/strategies/1").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Eletrificar frota"));

        mockMvc.perform(get("/strategies").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));

        mockMvc.perform(put("/strategies/1")
                .header("Authorization", bearer(token))
                .contentType("application/json")
                .content("""
                        {"title": "Eletrificar frota urbana", "description": "Priorizar linhas metropolitanas.", "horizon": "SHORT"}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Eletrificar frota urbana"))
                .andExpect(jsonPath("$.horizon").value("SHORT"));

        mockMvc.perform(delete("/strategies/1").header("Authorization", bearer(token)))
                .andExpect(status().isNoContent());

        assertThat(created).contains("Eletrificar frota");
    }

    @Test
    @DisplayName("DELETE faz soft delete: a linha continua na tabela e some das leituras")
    void shouldSoftDeleteAndHideFromReads() throws Exception {
        String token = tokenFor("gestor-delete@teste.dev", Role.GESTOR);

        mockMvc.perform(post("/strategies")
                .header("Authorization", bearer(token))
                .contentType("application/json")
                .content(PAYLOAD))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/strategies/1").header("Authorization", bearer(token)))
                .andExpect(status().isNoContent());

        // Pelo Hibernate a linha some; o SQL nativo prova que ela continua la.
        var deletedAt = jdbcTemplate.queryForObject(
                "SELECT deleted_at FROM strategies WHERE id = 1",
                java.sql.Timestamp.class);
        assertThat(deletedAt).isNotNull();

        mockMvc.perform(get("/strategies").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        mockMvc.perform(get("/strategies/1").header("Authorization", bearer(token)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value("https://aguiabranca.fiap.br/errors/estrategia-nao-encontrada"))
                .andExpect(jsonPath("$.status").value(404));
    }

    @ParameterizedTest
    @EnumSource(Role.class)
    @DisplayName("Leitura de estrategias e 200 para os tres perfis")
    void shouldAllowReadForEveryRole(Role role) throws Exception {
        String writer = tokenFor("escritor-" + role.name() + "@teste.dev", Role.GESTOR);
        mockMvc.perform(post("/strategies")
                .header("Authorization", bearer(writer))
                .contentType("application/json")
                .content(PAYLOAD))
                .andExpect(status().isCreated());

        String reader = tokenFor(role.name().toLowerCase() + "@teste.dev", role);
        mockMvc.perform(get("/strategies").header("Authorization", bearer(reader)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Eletrificar frota"));
        mockMvc.perform(get("/strategies/1").header("Authorization", bearer(reader)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Escrita por OPERADOR responde 403")
    void shouldForbidWriteForOperador() throws Exception {
        String token = tokenFor("operador@teste.dev", Role.OPERADOR);

        mockMvc.perform(post("/strategies")
                .header("Authorization", bearer(token))
                .contentType("application/json")
                .content(PAYLOAD))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.type").value("https://aguiabranca.fiap.br/errors/sem-permissao"));

        mockMvc.perform(put("/strategies/1")
                .header("Authorization", bearer(token))
                .contentType("application/json")
                .content(PAYLOAD))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/strategies/1").header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE de id inexistente responde 404 em ProblemDetail")
    void shouldReturnNotFoundForUnknownDelete() throws Exception {
        String token = tokenFor("gestor-404@teste.dev", Role.LIDERANCA);

        mockMvc.perform(delete("/strategies/999").header("Authorization", bearer(token)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value("https://aguiabranca.fiap.br/errors/estrategia-nao-encontrada"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.instance").isNotEmpty());
    }
}
