package br.com.fiap.aguiabranca.support;

import br.com.fiap.aguiabranca.domain.user.Role;
import br.com.fiap.aguiabranca.domain.user.User;
import br.com.fiap.aguiabranca.domain.user.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/** Base dos testes de integracao: contexto completo, MockMvc e um MongoDB de verdade. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("integration")
public abstract class IntegrationTestSupport {

    @DynamicPropertySource
    static void mongoProperties(DynamicPropertyRegistry registry) {
        MongoContainerSupport.registerProperties(registry);
    }

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected MongoTemplate mongoTemplate;

    @Autowired
    protected UserRepository users;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @Autowired(required = false)
    protected br.com.fiap.aguiabranca.domain.auth.LoginRateLimiter loginRateLimiter;

    /**
     * Cada teste comeca do zero, inclusive com os ids recomecando do 1 — varios casos apontam
     * para /strategies/1 logo depois de criar o primeiro documento.
     *
     * Apaga documentos em vez de dropar colecoes: o drop levaria junto os indices unicos e os
     * validadores $jsonSchema, que o MongoSchemaInitializer so cria uma vez, na subida do
     * contexto. A suite passaria a rodar sem as garantias que ela deveria estar provando.
     */
    @BeforeEach
    void resetDatabase() {
        if (loginRateLimiter != null) {
            loginRateLimiter.clearAllLimits();
        }
        for (String collection : mongoTemplate.getCollectionNames()) {
            if (!collection.startsWith("system.")) {
                mongoTemplate.remove(new Query(), collection);
            }
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
