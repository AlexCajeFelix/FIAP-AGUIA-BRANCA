package br.com.fiap.aguiabranca.domain.project;

import br.com.fiap.aguiabranca.shared.persistence.MongoPersistenceConfig;
import br.com.fiap.aguiabranca.support.MongoContainerSupport;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * As agregacoes do dashboard contra um MongoDB real.
 *
 * O MongoPersistenceConfig e importado a mao porque slice nao carrega @Component: sem ele o id
 * sequencial nao seria atribuido e o dinheiro nao seria gravado como Decimal128 — e $sum sobre
 * string devolve null em silencio, que e exatamente o erro que este teste existe para pegar.
 */
@DataMongoTest
@Import(MongoPersistenceConfig.class)
class ProjectRepositoryTest {

    @DynamicPropertySource
    static void mongoProperties(DynamicPropertyRegistry registry) {
        MongoContainerSupport.registerProperties(registry);
    }

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeEach
    void reset() {
        mongoTemplate.remove(new Query(), "projects");
        mongoTemplate.remove(new Query(), "counters");
    }

    @Test
    @DisplayName("summarize devolve os tipos Long, Double e BigDecimal com a escala preservada")
    void shouldSummarizeProjectsWithCorrectTypes() {
        projectRepository.save(new Project("Projeto A", 20, new BigDecimal("100000.00")));
        projectRepository.save(new Project("Projeto B", 80, new BigDecimal("200000.00")));

        ProjectSummaryDto summary = projectRepository.summarize();

        assertNotNull(summary);
        assertEquals(2L, summary.totalProjects());
        assertEquals(50.0, summary.avgProgress());
        assertEquals(new BigDecimal("300000.00"), summary.totalBudget());
    }

    @Test
    @DisplayName("Colecao vazia nao produz documento no $group — summarize devolve nulo")
    void shouldReturnNullWhenThereIsNoProject() {
        // O Postgres devolvia uma linha com count 0; o Mongo nao devolve nada. Quem normaliza
        // e o ProjectService, e este teste fixa o contrato entre os dois.
        assertThat(projectRepository.summarize()).isNull();
    }

    @Test
    @DisplayName("countByStatusGrouped agrupa por status em ordem alfabetica")
    void shouldGroupByStatus() {
        projectRepository.save(new Project("Planejado", 0, new BigDecimal("10.00")));
        projectRepository.save(new Project("Em andamento", 40, new BigDecimal("20.00")));
        projectRepository.save(new Project("Concluido", 100, new BigDecimal("30.00")));

        List<ProjectStatusCount> grouped = projectRepository.countByStatusGrouped();

        assertThat(grouped).containsExactly(
                new ProjectStatusCount(ProjectStatus.COMPLETED, 1L),
                new ProjectStatusCount(ProjectStatus.IN_PROGRESS, 1L),
                new ProjectStatusCount(ProjectStatus.PLANNING, 1L));
    }

    @Test
    @DisplayName("O id sequencial substitui o BIGSERIAL: 1, 2, 3 e nao ObjectId")
    void shouldAssignSequentialIds() {
        Project first = projectRepository.save(new Project("Primeiro", 0, new BigDecimal("1.00")));
        Project second = projectRepository.save(new Project("Segundo", 0, new BigDecimal("1.00")));

        assertThat(first.getId()).isEqualTo(1L);
        assertThat(second.getId()).isEqualTo(2L);
    }
}
