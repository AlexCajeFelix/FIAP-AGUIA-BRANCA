package br.com.fiap.aguiabranca.domain.project;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
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

    public Project() {
    }

    public Project(String name, int progress, BigDecimal budget) {
        this.name = name;
        updateProgress(progress);
        this.budget = budget;
    }

    public void updateProgress(int newProgress) {
        if (newProgress < 0 || newProgress > 100) {
            throw new IllegalArgumentException("Progresso deve ser entre 0 e 100");
        }
        this.progress = newProgress;
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
