package br.com.fiap.aguiabranca.domain.project;

import br.com.fiap.aguiabranca.support.IntegrationTestSupport;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Mede o plano das agregacoes do dashboard com 100k projetos (#14).
 *
 * As duas queries varrem a tabela inteira — nao ha filtro aplicavel. Seq Scan
 * nesse cenario e o plano honesto; o indice idx_projects_status da V1 cobre o
 * GROUP BY de status, mas nao evita a leitura das colunas de budget/progress.
 */
class DashboardQueryPlanTest extends IntegrationTestSupport {

    private static final double MAX_MS = 200.0;

    @Autowired
    private ProjectRepository projectRepository;

    @Test
    @DisplayName("summarize e countByStatusGrouped rodam abaixo de 200ms com 100k projetos")
    void dashboardQueriesShouldStayUnderBudgetWithRealisticVolume() {
        new ResourceDatabasePopulator(new ClassPathResource("db/dashboard-load-100k.sql"))
                .execute(jdbcTemplate.getDataSource());

        Long loaded = jdbcTemplate.queryForObject("SELECT count(*) FROM projects", Long.class);
        assertThat(loaded).isEqualTo(100_000L);

        jdbcTemplate.execute("ANALYZE projects");

        String summarizePlan = explain("""
                SELECT count(*), avg(progress), sum(budget) FROM projects
                """);
        String groupedPlan = explain("""
                SELECT status, count(*) FROM projects GROUP BY status ORDER BY status
                """);

        System.out.println("=== EXPLAIN summarize() ===");
        System.out.println(summarizePlan);
        System.out.println("=== EXPLAIN countByStatusGrouped() ===");
        System.out.println(groupedPlan);

        assertThat(executionMs(summarizePlan)).isLessThan(MAX_MS);
        assertThat(executionMs(groupedPlan)).isLessThan(MAX_MS);

        long summarizeStart = System.nanoTime();
        ProjectSummaryDto summary = projectRepository.summarize();
        double summarizeJavaMs = (System.nanoTime() - summarizeStart) / 1_000_000.0;

        long groupedStart = System.nanoTime();
        List<ProjectStatusCount> grouped = projectRepository.countByStatusGrouped();
        double groupedJavaMs = (System.nanoTime() - groupedStart) / 1_000_000.0;

        assertThat(summary.totalProjects()).isEqualTo(100_000L);
        assertThat(grouped).hasSize(ProjectStatus.values().length);
        assertThat(summarizeJavaMs).isLessThan(MAX_MS);
        assertThat(groupedJavaMs).isLessThan(MAX_MS);
    }

    private String explain(String sql) {
        List<String> lines = jdbcTemplate.query(
                "EXPLAIN (ANALYZE, BUFFERS, FORMAT TEXT) " + sql,
                (rs, rowNum) -> rs.getString(1));
        return String.join(System.lineSeparator(), lines);
    }

    private static double executionMs(String plan) {
        return plan.lines()
                .filter(line -> line.contains("Execution Time:"))
                .map(line -> line.replace("Execution Time:", "").replace("ms", "").trim())
                .mapToDouble(value -> Double.parseDouble(value.replace(',', '.')))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("EXPLAIN sem Execution Time:\n" + plan));
    }
}
