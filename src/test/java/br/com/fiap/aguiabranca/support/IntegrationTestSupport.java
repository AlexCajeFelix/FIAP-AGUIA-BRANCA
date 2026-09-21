package br.com.fiap.aguiabranca.support;

import br.com.fiap.aguiabranca.domain.user.Role;
import br.com.fiap.aguiabranca.domain.user.User;
import br.com.fiap.aguiabranca.domain.user.UserRepository;
import br.com.fiap.aguiabranca.domain.auth.RefreshTokenRepository;
import br.com.fiap.aguiabranca.domain.idea.IdeaRepository;
import br.com.fiap.aguiabranca.domain.project.ProjectMetricsHistoryRepository;
import br.com.fiap.aguiabranca.domain.project.ProjectRepository;
import br.com.fiap.aguiabranca.domain.strategy.StrategyRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Base dos testes de integracao. No profile integration os repositorios usam
 * armazenamento em memoria, entao a suite fica deterministica sem Docker.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("integration")
public abstract class IntegrationTestSupport {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected UserRepository users;

    @Autowired
    protected IdeaRepository ideas;

    @Autowired
    protected StrategyRepository strategies;

    @Autowired
    protected ProjectRepository projects;

    @Autowired
    protected ProjectMetricsHistoryRepository history;

    @Autowired
    protected RefreshTokenRepository refreshTokens;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @Autowired(required = false)
    protected br.com.fiap.aguiabranca.domain.auth.LoginRateLimiter loginRateLimiter;

    /**
     * Cada teste comeca do zero, inclusive sem o seed da V2.
     *
     * Depender do seed acopla o teste a uma migration que a #7 vai justamente tirar
     * do
     * caminho de producao — e ai a suite quebraria por um motivo que nada tem a ver
     * com
     * o que ela testa.
     */
    @BeforeEach
    void resetDatabase() {
        if (loginRateLimiter != null) {
            loginRateLimiter.clearAllLimits();
        }
        history.deleteAll();
        projects.deleteAll();
        ideas.deleteAll();
        strategies.deleteAll();
        refreshTokens.deleteAll();
        users.deleteAll();
    }

    protected User givenUser(String email, String rawPassword, Role role) {
        return users.save(new User(email, email, passwordEncoder.encode(rawPassword), role));
    }

    /** Devolve o access token de um usuario recem-criado com o perfil pedido. */
    protected String tokenFor(String email, Role role) throws Exception {
        String password = "senha-de-teste-123";
        givenUser(email, password, role);
        return login(email, password);
    }

    protected String login(String email, String password) throws Exception {
        String body = objectMapper.writeValueAsString(new LoginPayload(email, password));
        String response = mockMvc.perform(post("/auth/login")
                .contentType("application/json")
                .content(body))
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).path("accessToken").asText();
    }

    protected String bearer(String token) {
        return "Bearer " + token;
    }

    protected record LoginPayload(String email, String password) {
    }
}
