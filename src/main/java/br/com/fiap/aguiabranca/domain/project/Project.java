package br.com.fiap.aguiabranca.domain.project;

import br.com.fiap.aguiabranca.domain.idea.Idea;
import br.com.fiap.aguiabranca.shared.DomainRuleException;
import br.com.fiap.aguiabranca.shared.ErrorTypes;
import br.com.fiap.aguiabranca.shared.persistence.SequentialDocument;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "projects")
public class Project implements SequentialDocument {

    @Id
    private Long id;

    @NotBlank(message = "Nome é obrigatório")
    private String name;

    @Min(0)
    @Max(100)
    private int progress;

    @NotNull
    private BigDecimal budget;

    @NotNull
    private ProjectStatus status = ProjectStatus.PLANNING;

    @NotNull
    private BigDecimal spent = BigDecimal.ZERO;

    // O indice unico parcial sobre ideaId e o que impede a mesma ideia virar dois projetos:
    // a checagem no service perde para duas requisicoes simultaneas, o indice nao.
    private Long ideaId;

    @NotNull
    private Instant createdAt = Instant.now();

    public Project() {
    }

    public Project(String name, int progress, BigDecimal budget) {
        this.name = name;
        updateProgress(progress);
        this.budget = budget;
        this.spent = BigDecimal.ZERO;
        this.createdAt = Instant.now();
    }

    /** Promocao de ideia aprovada. A checagem de "aprovada" e do service, que ve o repositorio. */
    public static Project fromIdea(Idea idea, BigDecimal budget) {
        Project project = new Project(idea.getTitle(), 0, budget);
        project.ideaId = idea.getId();
        return project;
    }

    public void updateProgress(int newProgress) {
        if (newProgress < 0 || newProgress > 100) {
            throw new DomainRuleException(ErrorTypes.PROJECT_INVALID_PROGRESS,
                    "Progresso deve ser entre 0 e 100.");
        }
        this.progress = newProgress;
        if (this.status == ProjectStatus.CANCELLED) {
            return;
        }
        if (newProgress == 100) {
            this.status = ProjectStatus.COMPLETED;
        } else if (newProgress > 0) {
            this.status = ProjectStatus.IN_PROGRESS;
        } else {
            this.status = ProjectStatus.PLANNING;
        }
    }

    public void updateSpent(BigDecimal newSpent) {
        if (newSpent == null || newSpent.signum() < 0) {
            throw new DomainRuleException(ErrorTypes.PROJECT_INVALID_PROGRESS,
                    "Valor gasto não pode ser negativo.");
        }
        this.spent = newSpent;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void assignId(Long id) {
        if (this.id != null) {
            throw new IllegalStateException("Projeto já tem id " + this.id);
        }
        this.id = id;
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

    public Long getIdeaId() {
        return ideaId;
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
