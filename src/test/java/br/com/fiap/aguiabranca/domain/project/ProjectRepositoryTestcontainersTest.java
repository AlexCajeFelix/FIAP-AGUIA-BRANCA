package br.com.fiap.aguiabranca.domain.project;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
// Em banco nao-embarcado o ddl-auto default e "none": sem isto o Hibernate nao cria nada e o
// teste morre com relation "projects" does not exist. O slice valida a expressao JPQL no
// dialeto do Postgres, nao o schema — por isso gera o schema pelas entidades, sem Flyway.
@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@EnabledIf("isDockerAvailable")
class ProjectRepositoryTestcontainersTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private ProjectRepository projectRepository;

    static boolean isDockerAvailable() {
        try {
            return DockerClientFactory.instance().isDockerAvailable();
        } catch (Throwable t) {
            return false;
        }
    }

    @Test
    @DisplayName("Deve calcular corretamente a sumarizacao JPQL no PostgreSQL via Testcontainers")
    void shouldSummarizeProjectsOnPostgres() {
        projectRepository.save(new Project("Projeto A", 20, new BigDecimal("100000.00")));
        projectRepository.save(new Project("Projeto B", 80, new BigDecimal("200000.00")));

        ProjectSummaryDto summary = projectRepository.summarize();

        assertNotNull(summary);
        assertEquals(2L, summary.totalProjects());
        assertEquals(50.0, summary.avgProgress());
        assertEquals(new BigDecimal("300000.00"), summary.totalBudget());
    }
}
