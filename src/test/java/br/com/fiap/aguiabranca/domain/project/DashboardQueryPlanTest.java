package br.com.fiap.aguiabranca.domain.project;

import br.com.fiap.aguiabranca.support.IntegrationTestSupport;
import java.util.ArrayList;
import java.util.List;
import org.bson.Document;
import org.bson.types.Decimal128;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.BulkOperations;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Mede o plano das agregacoes do dashboard com 100k projetos (#14).
 *
 * As duas agregacoes varrem a colecao inteira — nao ha filtro aplicavel, entao COLLSCAN e o
 * plano honesto. O indice idx_projects_status cobre consultas por status, mas nao evita a
 * leitura de budget e progress que o $group precisa.
 */
class DashboardQueryPlanTest extends IntegrationTestSupport {

    private static final double MAX_MS = 200.0;
    private static final int PROJECTS = 100_000;

    @Autowired
    private ProjectRepository projectRepository;

    @Test
    @DisplayName("summarize e countByStatusGrouped rodam abaixo de 200ms com 100k projetos")
    void dashboardQueriesShouldStayUnderBudgetWithRealisticVolume() {
        loadProjects();

        assertThat(projectRepository.count()).isEqualTo(PROJECTS);

        String summarizePlan = explain(summarizePipeline());
        String groupedPlan = explain(groupedPipeline());

        System.out.println("=== explain summarize() ===");
        System.out.println(summarizePlan);
        System.out.println("=== explain countByStatusGrouped() ===");
        System.out.println(groupedPlan);

        // Varredura completa e o esperado aqui. A assercao existe para o dia em que alguem
        // trocar a agregacao por algo que finja usar indice.
        assertThat(summarizePlan).contains("COLLSCAN");
        assertThat(groupedPlan).contains("COLLSCAN");

        long summarizeStart = System.nanoTime();
        ProjectSummaryDto summary = projectRepository.summarize();
        double summarizeMs = (System.nanoTime() - summarizeStart) / 1_000_000.0;

        long groupedStart = System.nanoTime();
        List<ProjectStatusCount> grouped = projectRepository.countByStatusGrouped();
        double groupedMs = (System.nanoTime() - groupedStart) / 1_000_000.0;

        System.out.printf("summarize: %.1fms | countByStatusGrouped: %.1fms%n", summarizeMs, groupedMs);

        assertThat(summary.totalProjects()).isEqualTo((long) PROJECTS);
        assertThat(grouped).hasSize(ProjectStatus.values().length);
        assertThat(summarizeMs).isLessThan(MAX_MS);
        assertThat(groupedMs).isLessThan(MAX_MS);
    }

    /**
     * Carga por bulk sem ordem, com _id explicito.
     *
     * Gravar pelo repositorio faria 100k idas ao contador de sequencia antes de 100k inserts —
     * a carga sozinha levaria minutos e o teste mediria o Testcontainers, nao o dashboard.
     * ideaId fica de fora de proposito: o dashboard agrega a colecao inteira.
     */
    private void loadProjects() {
        String[] status = { "PLANNING", "IN_PROGRESS", "COMPLETED", "CANCELLED" };
        List<Document> documents = new ArrayList<>(PROJECTS);
        for (int i = 1; i <= PROJECTS; i++) {
            documents.add(new Document("_id", (long) i)
                    .append("name", "Projeto carga " + i)
                    .append("status", status[i % status.length])
                    .append("progress", i % 101)
                    .append("budget", new Decimal128(new java.math.BigDecimal((i % 500) * 1000).setScale(2)))
                    .append("spent", new Decimal128(new java.math.BigDecimal("0.00")))
                    .append("createdAt", java.util.Date.from(java.time.Instant.now()))
                    .append("_class", Project.class.getName()));
        }
        mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, "projects")
                .insert(documents)
                .execute();
    }

    private List<Document> summarizePipeline() {
        return List.of(new Document("$group", new Document("_id", null)
                .append("totalProjects", new Document("$sum", 1))
                .append("avgProgress", new Document("$avg", "$progress"))
                .append("totalBudget", new Document("$sum", "$budget"))));
    }

    private List<Document> groupedPipeline() {
        return List.of(
                new Document("$group", new Document("_id", "$status").append("total", new Document("$sum", 1))),
                new Document("$sort", new Document("_id", 1)));
    }

    private String explain(List<Document> pipeline) {
        Document command = new Document("explain",
                new Document("aggregate", "projects")
                        .append("pipeline", pipeline)
                        .append("cursor", new Document()))
                .append("verbosity", "executionStats");
        return mongoTemplate.getDb().runCommand(command).toJson();
    }
}
