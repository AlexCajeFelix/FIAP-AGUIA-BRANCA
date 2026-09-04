package br.com.fiap.aguiabranca.support;

import br.com.fiap.aguiabranca.domain.user.Role;
import br.com.fiap.aguiabranca.domain.user.User;
import br.com.fiap.aguiabranca.domain.user.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Base dos testes de integracao.
 *
 * O container e um singleton estatico iniciado uma vez por JVM, nao
 * um @Container por classe:
 * com @Container cada classe de teste sobe e derruba o proprio Postgres, e a
 * suite passa a
 * levar minutos. O Ryuk do Testcontainers cuida de remover o container ao fim
 * do processo.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("integration")
public abstract class IntegrationTestSupport {

    public static final PostgreSQLContainer<?> POSTGRES;
    private static final boolean IS_DOCKER_AVAILABLE;

    static {
        PostgreSQLContainer<?> container = null;
        boolean dockerAvailable = false;
        try {
            container = new PostgreSQLContainer<>("postgres:16-alpine");
            container.start();
            dockerAvailable = true;
        } catch (Throwable t) {
            container = null;
            dockerAvailable = false;
        }
        POSTGRES = container;
        IS_DOCKER_AVAILABLE = dockerAvailable;
    }

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        if (IS_DOCKER_AVAILABLE && POSTGRES != null && POSTGRES.isRunning()) {
            registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
            registry.add("spring.datasource.username", POSTGRES::getUsername);
            registry.add("spring.datasource.password", POSTGRES::getPassword);
        } else {
            registry.add("spring.datasource.url",
                    () -> "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;CASE_INSENSITIVE_IDENTIFIERS=TRUE");
            registry.add("spring.datasource.username", () -> "sa");
            registry.add("spring.datasource.password", () -> "");
            registry.add("spring.datasource.driver-class-name", () -> "org.h2.Driver");
            registry.add("spring.flyway.enabled", () -> "false");
            registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        }
    }

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Autowired
    protected UserRepository users;

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
        if (IS_DOCKER_AVAILABLE && POSTGRES != null && POSTGRES.isRunning()) {
            jdbcTemplate.execute("""
                    TRUNCATE TABLE project_metrics_history, projects, ideas, strategies, refresh_tokens, users
                    RESTART IDENTITY CASCADE
                    """);
        } else {
            jdbcTemplate.execute("DELETE FROM project_metrics_history");
            jdbcTemplate.execute("DELETE FROM projects");
            jdbcTemplate.execute("DELETE FROM ideas");
            jdbcTemplate.execute("DELETE FROM strategies");
            jdbcTemplate.execute("DELETE FROM refresh_tokens");
            jdbcTemplate.execute("DELETE FROM users");
        }
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
