package br.com.fiap.aguiabranca.domain.project;

import br.com.fiap.aguiabranca.domain.idea.Idea;
import br.com.fiap.aguiabranca.shared.DomainRuleException;
import br.com.fiap.aguiabranca.shared.ErrorTypes;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "projects")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nome é obrigatório")
    private String name;

    @Min(0)
    @Max(100)
    private int progress;

    @NotNull
    private BigDecimal budget;

    @NotNull
    @Enumerated(EnumType.STRING)
    private ProjectStatus status = ProjectStatus.PLANNING;

    @NotNull
    private BigDecimal spent = BigDecimal.ZERO;

    // OneToOne com a coluna UNIQUE no banco: a mesma ideia nao vira dois projetos.
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idea_id", unique = true)
    private Idea idea;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public Project() {
    }

    public Project(String name, int progress, BigDecimal budget) {
        this.name = name;
        updateProgress(progress);
        this.budget = budget;
        this.status = ProjectStatus.PLANNING;
        this.spent = BigDecimal.ZERO;
        this.createdAt = Instant.now();
    }

    /** Promocao de ideia aprovada. A checagem de "aprovada" e do service, que ve o repositorio. */
    public static Project fromIdea(Idea idea, BigDecimal budget) {
        Project project = new Project(idea.getTitle(), 0, budget);
        project.idea = idea;
        return project;
    }

    public void updateProgress(int newProgress) {
        if (newProgress < 0 || newProgress > 100) {
            throw new DomainRuleException(ErrorTypes.PROJECT_INVALID_PROGRESS,
                    "Progresso deve ser entre 0 e 100.");
        }
        this.progress = newProgress;
        if (newProgress == 100) {
            this.status = ProjectStatus.COMPLETED;
        } else if (newProgress > 0 && this.status == ProjectStatus.PLANNING) {
            this.status = ProjectStatus.IN_PROGRESS;
        }
    }

    public void updateSpent(BigDecimal newSpent) {
        if (newSpent == null || newSpent.signum() < 0) {
            throw new DomainRuleException(ErrorTypes.PROJECT_INVALID_PROGRESS,
                    "Valor gasto não pode ser negativo.");
        }
        this.spent = newSpent;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getProgress() {
        return progress;
    }

    public BigDecimal getBudget() {
        return budget;
    }

    public ProjectStatus getStatus() {
        return status;
    }

    public BigDecimal getSpent() {
        return spent;
    }

    public Idea getIdea() {
        return idea;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Project project = (Project) o;
        return Objects.equals(id, project.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
