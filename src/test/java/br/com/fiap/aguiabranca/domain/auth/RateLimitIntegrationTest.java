package br.com.fiap.aguiabranca.domain.auth;

import br.com.fiap.aguiabranca.domain.user.Role;
import br.com.fiap.aguiabranca.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RateLimitIntegrationTest extends IntegrationTestSupport {

    @Test
    @DisplayName("N+1 tentativas do mesmo IP excedem o limite por IP e retornam 429 com Retry-After")
    void shouldBlockAfterExceedingIpRateLimit() throws Exception {
        String clientIp = "192.168.1.50";

        // Capacity de IP configurada como 5 em application-integration.properties
        for (int i = 1; i <= 5; i++) {
            mockMvc.perform(post("/auth/login")
                    .header("X-Forwarded-For", clientIp)
                    .contentType("application/json")
                    .content("""
                            {"email": "user%d@teste.dev", "password": "senha-errada"}
                            """.formatted(i)))
                    .andExpect(status().isUnauthorized());
        }

        // A 6a tentativa (N+1) a partir do mesmo IP deve ser bloqueada com 429
        mockMvc.perform(post("/auth/login")
                .header("X-Forwarded-For", clientIp)
                .contentType("application/json")
                .content("""
                        {"email": "outro@teste.dev", "password": "senha-errada"}
                        """))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists(HttpHeaders.RETRY_AFTER))
                .andExpect(jsonPath("$.type").value("https://aguiabranca.fiap.br/errors/rate-limit-excedido"))
                .andExpect(jsonPath("$.status").value(429))
                .andExpect(jsonPath("$.title").value("Limite de tentativas excedido"));
    }

    @Test
    @DisplayName("N+1 tentativas para o mesmo e-mail a partir de IPs diferentes excedem o limite por e-mail")
    void shouldBlockAfterExceedingEmailRateLimit() throws Exception {
        String targetEmail = "alvo@teste.dev";

        // Capacity de E-mail configurada como 3 em application-integration.properties
        for (int i = 1; i <= 3; i++) {
            mockMvc.perform(post("/auth/login")
                    .header("X-Forwarded-For", "10.0.0." + i)
                    .contentType("application/json")
                    .content("""
                            {"email": "%s", "password": "senha-errada"}
                            """.formatted(targetEmail)))
                    .andExpect(status().isUnauthorized());
        }

        // A 4a tentativa para o mesmo e-mail (vinda de um novo IP) deve ser bloqueada
        // com 429
        mockMvc.perform(post("/auth/login")
                .header("X-Forwarded-For", "10.0.0.99")
                .contentType("application/json")
                .content("""
                        {"email": "%s", "password": "senha-errada"}
                        """.formatted(targetEmail)))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists(HttpHeaders.RETRY_AFTER))
                .andExpect(jsonPath("$.type").value("https://aguiabranca.fiap.br/errors/rate-limit-excedido"))
                .andExpect(jsonPath("$.status").value(429));
    }

    @Test
    @DisplayName("O limite por e-mail bloqueia e-mails inexistentes sem revelar existencia da conta")
    void shouldNotLeakAccountExistenceWhenRateLimited() throws Exception {
        String nonExistentEmail = "inexistente-limit@teste.dev";

        for (int i = 1; i <= 3; i++) {
            mockMvc.perform(post("/auth/login")
                    .header("X-Forwarded-For", "10.1.0." + i)
                    .contentType("application/json")
                    .content("""
                            {"email": "%s", "password": "senha-errada"}
                            """.formatted(nonExistentEmail)))
                    .andExpect(status().isUnauthorized());
        }

        // A 4a tentativa para e-mail inexistente deve responder 429 exatamente igual
        mockMvc.perform(post("/auth/login")
                .header("X-Forwarded-For", "10.1.0.99")
                .contentType("application/json")
                .content("""
                        {"email": "%s", "password": "senha-errada"}
                        """.formatted(nonExistentEmail)))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.type").value("https://aguiabranca.fiap.br/errors/rate-limit-excedido"))
                .andExpect(jsonPath("$.status").value(429));
    }

    @Test
    @DisplayName("Login bem-sucedido zera o contador de tentativas por e-mail")
    void shouldResetEmailCounterOnSuccessfulLogin() throws Exception {
        String userEmail = "sucesso-reset@teste.dev";
        String userPassword = "senha-correta-123";
        givenUser(userEmail, userPassword, Role.OPERADOR);

        // Realiza 2 tentativas com senha errada de IPs isolados
        for (int i = 1; i <= 2; i++) {
            mockMvc.perform(post("/auth/login")
                    .header("X-Forwarded-For", "172.16.0." + i)
                    .contentType("application/json")
                    .content("""
                            {"email": "%s", "password": "senha-errada"}
                            """.formatted(userEmail)))
                    .andExpect(status().isUnauthorized());
        }

        // Login com sucesso da 3a tentativa
        mockMvc.perform(post("/auth/login")
                .header("X-Forwarded-For", "172.16.0.3")
                .contentType("application/json")
                .content("""
                        {"email": "%s", "password": "%s"}
                        """.formatted(userEmail, userPassword)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());

        // Apos o login bem-sucedido, o contador de e-mail foi resetado.
        // É possível fazer mais 3 tentativas de e-mail sem tomar 429 no e-mail (desde
        // que usando IPs diferentes).
        for (int i = 4; i <= 6; i++) {
            mockMvc.perform(post("/auth/login")
                    .header("X-Forwarded-For", "172.16.0." + i)
                    .contentType("application/json")
                    .content("""
                            {"email": "%s", "password": "senha-errada"}
                            """.formatted(userEmail)))
                    .andExpect(status().isUnauthorized());
        }
    }
}
