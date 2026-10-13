package br.com.fiap.aguiabranca.domain.user;

import br.com.fiap.aguiabranca.shared.persistence.DevSeedRunner;
import br.com.fiap.aguiabranca.support.MongoContainerSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Os profiles que o pipeline de deploy liga: staging (com massa de demonstracao, para o smoke
 * test logar) e prod (sem conta nenhuma conhecida). Banco proprio por classe, pelo mesmo motivo
 * do DevSeedRunnerTest: o seed so age com a colecao de usuarios vazia.
 */
class EnvironmentProfilesTest {

    @Nested
    @SpringBootTest
    @AutoConfigureMockMvc
    @ActiveProfiles("staging")
    class Staging {

        @DynamicPropertySource
        static void mongoProperties(DynamicPropertyRegistry registry) {
            MongoContainerSupport.registerProperties(registry, "aguiabranca_staging_test");
        }

        @Autowired
        private UserRepository users;

        @Autowired
        private MockMvc mockMvc;

        @Test
        @DisplayName("Staging carrega as contas de demonstracao usadas pelo smoke test")
        void stagingShouldSeedDemoAccounts() {
            assertThat(users.findByEmail("gestor@aguiabranca.dev")).get()
                    .extracting(User::getRole).isEqualTo(Role.GESTOR);
        }

        @Test
        @DisplayName("O contrato OpenAPI diz em qual ambiente a API esta rodando")
        void openApiShouldExposeEnvironment() throws Exception {
            mockMvc.perform(get("/v3/api-docs"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.x-environment").value("staging"))
                    .andExpect(jsonPath("$.info.description").value(containsString("staging")));
        }
    }

    @Nested
    @SpringBootTest
    @AutoConfigureMockMvc
    @ActiveProfiles("prod")
    class Production {

        @DynamicPropertySource
        static void mongoProperties(DynamicPropertyRegistry registry) {
            MongoContainerSupport.registerProperties(registry, "aguiabranca_prod_test");
        }

        @Autowired
        private ApplicationContext context;

        @Autowired
        private UserRepository users;

        @Autowired
        private MockMvc mockMvc;

        @Test
        @DisplayName("Producao nao tem seed nem conta de desenvolvimento")
        void productionShouldNotSeed() {
            assertThat(context.getBeanNamesForType(DevSeedRunner.class)).isEmpty();
            assertThat(users.findByEmail("gestor@aguiabranca.dev")).isEmpty();
        }

        @Test
        @DisplayName("O contrato OpenAPI identifica o ambiente de producao")
        void openApiShouldExposeEnvironment() throws Exception {
            mockMvc.perform(get("/v3/api-docs"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.x-environment").value("production"));
        }
    }
}
