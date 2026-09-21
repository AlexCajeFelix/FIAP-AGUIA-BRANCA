package br.com.fiap.aguiabranca.domain.project;

import br.com.fiap.aguiabranca.domain.user.User;
import java.math.BigDecimal;
import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Snapshot de auditoria financeira: quem mudou qual metrica, de quanto para quanto e quando.
 *
 * Gravado na mesma transacao da alteracao. Fora dela, um erro posterior deixaria historico
 * de uma mudanca que nunca aconteceu.
 */
@Document("project_metrics_history")
public class ProjectMetricsHistory {

    public enum Metric {
        PROGRESS,
        SPENT
    }

    @Id
    private Long id;

    private Long projectId;

    private Metric metric;

    private BigDecimal oldValue;

    private BigDecimal newValue;

    private Long changedById;

    private Instant changedAt = Instant.now();

    protected ProjectMetricsHistory() {
    }

    public ProjectMetricsHistory(Project project, Metric metric, BigDecimal oldValue, BigDecimal newValue,
            User changedBy) {
        this.projectId = project.getId();
        this.metric = metric;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.changedById = changedBy.getId();
        this.changedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public void assignId(Long id) {
        if (this.id == null) {
            this.id = id;
        }
    }

    public Project getProject() {
        Project project = new Project();
        project.assignId(projectId);
        return project;
    }

    public Long getProjectId() {
        return projectId;
    }

    public Metric getMetric() {
        return metric;
    }

    public BigDecimal getOldValue() {
        return oldValue;
    }

    public BigDecimal getNewValue() {
        return newValue;
    }

    public User getChangedBy() {
        User user = new User();
        user.assignId(changedById);
        return user;
    }

    public Long getChangedById() {
        return changedById;
    }

    public Instant getChangedAt() {
        return changedAt;
    }
}
