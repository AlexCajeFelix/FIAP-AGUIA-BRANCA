package br.com.fiap.aguiabranca.domain.idea;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;

@Entity
@Table(name = "ideas")
public class Idea {

    public enum Status {
        DRAFT,
        IN_REVIEW,
        APPROVED,
        REJECTED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Título é obrigatório")
    private String title;

    @NotBlank(message = "Descrição é obrigatória")
    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Status status = Status.DRAFT;

    public Idea() {
    }

    public Idea(String title, String description) {
        this.title = title;
        this.description = description;
        this.status = Status.DRAFT;
    }

    public void review(Status newStatus) {
        if (newStatus == null || newStatus == Status.DRAFT) {
            throw new IllegalArgumentException("Status de revisão inválido");
        }
        this.status = newStatus;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Status getStatus() {
        return status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Idea idea = (Idea) o;
        return Objects.equals(id, idea.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
