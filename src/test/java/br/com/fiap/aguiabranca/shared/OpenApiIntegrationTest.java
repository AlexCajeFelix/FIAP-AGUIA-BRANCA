package br.com.fiap.aguiabranca.shared;

import br.com.fiap.aguiabranca.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.hasItems;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OpenApiIntegrationTest extends IntegrationTestSupport {

    @Test
    @DisplayName("/v3/api-docs e publico e descreve JWT, os tres perfis e valores financeiros como number")
    void shouldExposeOpenApiContract() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.components.securitySchemes.bearer-jwt.type").value("http"))
                .andExpect(jsonPath("$.components.securitySchemes.bearer-jwt.scheme").value("bearer"))
                .andExpect(jsonPath("$.components.securitySchemes.operador.scheme").value("bearer"))
                .andExpect(jsonPath("$.components.securitySchemes.gestor.scheme").value("bearer"))
                .andExpect(jsonPath("$.components.securitySchemes.lideranca.scheme").value("bearer"))
                .andExpect(jsonPath("$.components.schemas.ProjectStatus.enum",
                        hasItems("PLANNING", "IN_PROGRESS", "COMPLETED", "CANCELLED")))
                .andExpect(jsonPath("$.components.schemas.ProjectResponse.properties.status['$ref']")
                        .value("#/components/schemas/ProjectStatus"))
                .andExpect(jsonPath("$.components.schemas.ProjectResponse.properties.budget.type")
                        .value("number"))
                .andExpect(jsonPath("$.components.schemas.ProjectResponse.properties.spent.type")
                        .value("number"))
                .andExpect(jsonPath("$.info.description").value(org.hamcrest.Matchers.containsString("OPERADOR")))
                .andExpect(jsonPath("$.info.description").value(org.hamcrest.Matchers.containsString("GESTOR")))
                .andExpect(jsonPath("$.info.description")
                        .value(org.hamcrest.Matchers.containsString("LIDERANCA")));
    }
}
