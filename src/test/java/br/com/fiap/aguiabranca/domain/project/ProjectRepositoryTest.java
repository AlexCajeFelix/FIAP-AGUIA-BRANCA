package br.com.fiap.aguiabranca.domain.project;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
class ProjectRepositoryTest {

    @Autowired
    private ProjectRepository projectRepository;

    @Test
    @DisplayName("Deve calcular corretamente a sumarizacao JPQL com tipos Long, Double e BigDecimal")
    void shouldSummarizeProjectsWithCorrectTypes() {
        projectRepository.save(new Project("Projeto A", 20, new BigDecimal("100000.00")));
        projectRepository.save(new Project("Projeto B", 80, new BigDecimal("200000.00")));

        ProjectSummaryDto summary = projectRepository.summarize();

        assertNotNull(summary);
        assertEquals(2L, summary.totalProjects());
        assertEquals(50.0, summary.avgProgress());
        assertEquals(new BigDecimal("300000.00"), summary.totalBudget());
    }
}
