package br.com.fiap.aguiabranca.domain.project;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("integration")
class ProjectRepositoryTest {

    @Autowired
    private ProjectRepository projectRepository;

    @BeforeEach
    void resetRepository() {
        projectRepository.deleteAll();
    }

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

    @Test
    @DisplayName("Deve agrupar projetos por status")
    void shouldGroupProjectsByStatus() {
        Project first = new Project("Projeto A", 0, new BigDecimal("100000.00"));
        Project second = new Project("Projeto B", 0, new BigDecimal("200000.00"));
        second.updateProgress(20);
        projectRepository.saveAll(List.of(first, second));

        List<ProjectStatusCount> grouped = projectRepository.countByStatusGrouped();

        assertEquals(2, grouped.size());
        assertEquals(ProjectStatus.PLANNING, grouped.get(0).status());
        assertEquals(1L, grouped.get(0).total());
        assertEquals(ProjectStatus.IN_PROGRESS, grouped.get(1).status());
        assertEquals(1L, grouped.get(1).total());
    }
}
