package br.com.fiap.aguiabranca.domain.project;

import br.com.fiap.aguiabranca.shared.persistence.SequentialDocument;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Snapshot de auditoria financeira: quem mudou qual metrica, de quanto para quanto e quando.
 *
 * Gravado na mesma transacao da alteracao. Fora dela, um erro posterior deixaria historico
 * de uma mudanca que nunca aconteceu — no Mongo isso exige replica set, e e o unico ponto do
 * sistema que depende disso.
 */
@Document(collection = "project_metrics_history")
public class ProjectMetricsHistory implements SequentialDocument {

    public enum Metric {
        PROGRESS,
        SPENT
    }

    @Id
    private Long id;

    @NotNull
    private Long projectId;

    @NotNull
    private Metric metric;

    private BigDecimal oldValue;

    @NotNull
    private BigDecimal newValue;

    @NotNull
    private Long changedById;

    @NotNull
    private Instant changedAt = Instant.now();

    protected ProjectMetricsHistory() {
    }

    public ProjectMetricsHistory(Long projectId, Metric metric, BigDecimal oldValue, BigDecimal newValue,
            Long changedById) {
        this.projectId = projectId;
        this.metric = metric;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.changedById = changedById;
        this.changedAt = Instant.now();
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void assignId(Long id) {
        if (this.id != null) {
            throw new IllegalStateException("Snapshot já tem id " + this.id);
        }
        this.id = id;
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

    public Long getChangedById() {
        return changedById;
    }

    public Instant getChangedAt() {
        return changedAt;
    }
}
