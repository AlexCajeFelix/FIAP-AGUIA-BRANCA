package br.com.fiap.aguiabranca.domain.auth;

import br.com.fiap.aguiabranca.domain.user.Role;
import br.com.fiap.aguiabranca.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RefreshTokenIntegrationTest extends IntegrationTestSupport {

    private static final String REFRESH_TYPE = "https://aguiabranca.fiap.br/errors/refresh-invalido";

    @Test
    @DisplayName("Refresh feliz troca o par e o access novo acessa rota protegida")
    void shouldRotateRefreshAndIssueNewAccess() throws Exception {
        Tokens original = loginTokens("gestor@teste.dev", Role.GESTOR);

        String body = mockMvc.perform(post("/auth/refresh")
                .contentType("application/json")
                .content(refreshBody(original.refreshToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.role").value("GESTOR"))
                .andExpect(jsonPath("$.expiresIn").value(1800))
                .andReturn().getResponse().getContentAsString();

        Tokens rotated = tokensFrom(body);
        assertThat(rotated.refreshToken()).isNotEqualTo(original.refreshToken());

        mockMvc.perform(get("/ideas").header("Authorization", bearer(rotated.accessToken())))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Reuso de refresh ja rotacionado responde 401 e revoga a familia")
    void shouldRevokeFamilyOnRefreshReuse() throws Exception {
        Tokens original = loginTokens("reuso@teste.dev", Role.OPERADOR);

        String firstRefresh = mockMvc.perform(post("/auth/refresh")
                .contentType("application/json")
                .content(refreshBody(original.refreshToken())))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        Tokens rotated = tokensFrom(firstRefresh);

        mockMvc.perform(post("/auth/refresh")
                .contentType("application/json")
                .content(refreshBody(original.refreshToken())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.type").value(REFRESH_TYPE))
                .andExpect(jsonPath("$.status").value(401));

        mockMvc.perform(post("/auth/refresh")
                .contentType("application/json")
                .content(refreshBody(rotated.refreshToken())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.type").value(REFRESH_TYPE));
    }

    @Test
    @DisplayName("Logout invalida o refresh corrente")
    void shouldInvalidateRefreshOnLogout() throws Exception {
        Tokens tokens = loginTokens("sair@teste.dev", Role.GESTOR);

        mockMvc.perform(post("/auth/logout")
                .contentType("application/json")
                .content(refreshBody(tokens.refreshToken())))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/auth/refresh")
                .contentType("application/json")
                .content(refreshBody(tokens.refreshToken())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.type").value(REFRESH_TYPE));
    }

    @Test
    @DisplayName("Refresh expirado responde 401 com type proprio")
    void shouldRejectExpiredRefresh() throws Exception {
        Tokens tokens = loginTokens("expirado@teste.dev", Role.LIDERANCA);

        jdbcTemplate.update("UPDATE refresh_tokens SET expires_at = now() - interval '1 second'");

        mockMvc.perform(post("/auth/refresh")
                .contentType("application/json")
                .content(refreshBody(tokens.refreshToken())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.type").value(REFRESH_TYPE))
                .andExpect(jsonPath("$.title").value("Refresh token invalido"));
    }

    private Tokens loginTokens(String email, Role role) throws Exception {
        givenUser(email, "senha-forte-123", role);
        String response = mockMvc.perform(post("/auth/login")
                .contentType("application/json")
                .content("""
                        {"email": "%s", "password": "senha-forte-123"}
                        """.formatted(email)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return tokensFrom(response);
    }

    private Tokens tokensFrom(String json) throws Exception {
        var node = objectMapper.readTree(json);
        return new Tokens(node.path("accessToken").asText(), node.path("refreshToken").asText());
    }

    private static String refreshBody(String refreshToken) throws Exception {
        return """
                {"refreshToken": "%s"}
                """.formatted(refreshToken);
    }

    private record Tokens(String accessToken, String refreshToken) {
    }
}
