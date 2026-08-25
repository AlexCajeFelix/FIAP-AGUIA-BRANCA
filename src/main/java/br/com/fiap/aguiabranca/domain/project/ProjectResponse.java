package br.com.fiap.aguiabranca.domain.project;

import java.math.BigDecimal;
import java.time.Instant;

public record ProjectResponse(
        Long id,
        String name,
        ProjectStatus status,
        int progress,
        BigDecimal budget,
        BigDecimal spent,
        Long ideaId,
        Instant createdAt) {

    public static ProjectResponse from(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getStatus(),
                project.getProgress(),
                project.getBudget(),
                project.getSpent(),
                project.getIdea() == null ? null : project.getIdea().getId(),
                project.getCreatedAt());
    }
}
