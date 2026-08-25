package br.com.fiap.aguiabranca.domain.project;

import br.com.fiap.aguiabranca.domain.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Snapshot de auditoria financeira: quem mudou qual metrica, de quanto para quanto e quando.
 *
 * Gravado na mesma transacao da alteracao. Fora dela, um erro posterior deixaria historico
 * de uma mudanca que nunca aconteceu.
 */
@Entity
@Table(name = "project_metrics_history")
public class ProjectMetricsHistory {

    public enum Metric {
        PROGRESS,
        SPENT
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Metric metric;

    @Column(name = "old_value")
    private BigDecimal oldValue;

    @Column(name = "new_value", nullable = false)
    private BigDecimal newValue;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "changed_by_id", nullable = false)
    private User changedBy;

    @Column(name = "changed_at", nullable = false)
    private Instant changedAt = Instant.now();

    protected ProjectMetricsHistory() {
    }

    public ProjectMetricsHistory(Project project, Metric metric, BigDecimal oldValue, BigDecimal newValue,
            User changedBy) {
        this.project = project;
        this.metric = metric;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.changedBy = changedBy;
        this.changedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Project getProject() {
        return project;
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
        return changedBy;
    }

    public Instant getChangedAt() {
        return changedAt;
    }
}
