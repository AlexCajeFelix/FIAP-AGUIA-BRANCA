package br.com.fiap.aguiabranca.domain.auth;

import br.com.fiap.aguiabranca.domain.user.Role;
import br.com.fiap.aguiabranca.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthFlowIntegrationTest extends IntegrationTestSupport {

    @Test
    @DisplayName("Login valido devolve access token com o perfil do usuario")
    void shouldReturnTokenOnValidLogin() throws Exception {
        givenUser("gestor@teste.dev", "senha-forte-123", Role.GESTOR);

        mockMvc.perform(post("/auth/login")
                .contentType("application/json")
                .content("""
                        {"email": "gestor@teste.dev", "password": "senha-forte-123"}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.role").value("GESTOR"))
                .andExpect(jsonPath("$.expiresIn").isNumber());
    }

    @Test
    @DisplayName("Senha errada e e-mail inexistente respondem 401 com a mesma mensagem")
    void shouldNotRevealWhetherAccountExists() throws Exception {
        givenUser("existe@teste.dev", "senha-forte-123", Role.OPERADOR);

        String wrongPassword = mockMvc.perform(post("/auth/login")
                .contentType("application/json")
                .content("""
                        {"email": "existe@teste.dev", "password": "senha-errada-123"}
                        """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.type").value("https://aguiabranca.fiap.br/errors/credenciais-invalidas"))
                .andReturn().getResponse().getContentAsString();

        String unknownAccount = mockMvc.perform(post("/auth/login")
                .contentType("application/json")
                .content("""
                        {"email": "naoexiste@teste.dev", "password": "senha-errada-123"}
                        """))
                .andExpect(status().isUnauthorized())
                .andReturn().getResponse().getContentAsString();

        // Se as duas respostas diferissem, daria para enumerar contas validas so pelo corpo.
        org.junit.jupiter.api.Assertions.assertEquals(wrongPassword, unknownAccount);
    }

    @Test
    @DisplayName("Rota protegida sem token responde 401 em ProblemDetail")
    void shouldRejectRequestWithoutToken() throws Exception {
        mockMvc.perform(get("/ideas"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.type").value("https://aguiabranca.fiap.br/errors/nao-autenticado"))
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("Token com assinatura invalida responde 401, nao 403")
    void shouldRejectTamperedToken() throws Exception {
        String token = tokenFor("operador@teste.dev", Role.OPERADOR);
        String tampered = token.substring(0, token.lastIndexOf('.') + 1) + "assinaturaForjada";

        mockMvc.perform(get("/ideas").header("Authorization", bearer(tampered)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Token valido acessa rota protegida")
    void shouldAcceptValidToken() throws Exception {
        String token = tokenFor("valido@teste.dev", Role.OPERADOR);

        mockMvc.perform(get("/ideas").header("Authorization", bearer(token)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Resposta de erro nao devolve o hash da senha nem o segredo do token")
    void shouldNotLeakSecrets() throws Exception {
        givenUser("segredo@teste.dev", "senha-forte-123", Role.OPERADOR);

        mockMvc.perform(post("/auth/login")
                .contentType("application/json")
                .content("""
                        {"email": "segredo@teste.dev", "password": "errada"}
                        """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value(not(containsString("$2a$"))));
    }
}
