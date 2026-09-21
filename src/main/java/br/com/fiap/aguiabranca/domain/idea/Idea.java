package br.com.fiap.aguiabranca.domain.idea;

import br.com.fiap.aguiabranca.shared.DomainRuleException;
import br.com.fiap.aguiabranca.shared.ErrorTypes;
import br.com.fiap.aguiabranca.shared.persistence.SequentialDocument;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.Objects;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "ideas")
public class Idea implements SequentialDocument {

    public enum Status {
        DRAFT,
        IN_REVIEW,
        APPROVED,
        REJECTED
    }

    @Id
    private Long id;

    @NotBlank(message = "Título é obrigatório")
    private String title;

    @NotBlank(message = "Descrição é obrigatória")
    private String description;

    @NotNull
    private Status status = Status.DRAFT;

    // Sem join no Mongo: guarda-se a referencia por id e quem precisa do usuario inteiro
    // carrega pelo UserRepository. A API ja expunha apenas o id do dono.
    private Long ownerId;

    @NotNull
    private Instant createdAt = Instant.now();

    private Long reviewedById;

    private Instant reviewedAt;

    public Idea() {
    }

    public Idea(String title, String description) {
        this.title = title;
        this.description = description;
        this.status = Status.DRAFT;
        this.createdAt = Instant.now();
    }

    public Idea(String title, String description, Long ownerId) {
        this(title, description);
        this.ownerId = ownerId;
    }

    public void review(Status newStatus) {
        review(newStatus, null);
    }

    public void review(Status newStatus, Long reviewerId) {
        if (newStatus == null || newStatus == Status.DRAFT) {
            throw new DomainRuleException(ErrorTypes.VALIDATION, "Status de revisão inválido.");
        }
        // Revisar de novo o que ja foi decidido apagaria a decisao anterior sem deixar rastro.
        if (isReviewed()) {
            throw new DomainRuleException(ErrorTypes.IDEA_ALREADY_REVIEWED,
                    "Ideia já revisada com status " + this.status + ".");
        }
        this.status = newStatus;
        this.reviewedById = reviewerId;
        this.reviewedAt = Instant.now();
    }

    public boolean isReviewed() {
        return status == Status.APPROVED || status == Status.REJECTED;
    }

    public boolean isApproved() {
        return status == Status.APPROVED;
    }

    /** O OPERADOR so enxerga o que e dele; os demais perfis enxergam tudo. */
    public boolean isOwnedBy(Long userId) {
        return ownerId != null && Objects.equals(ownerId, userId);
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void assignId(Long id) {
        if (this.id != null) {
            throw new IllegalStateException("Ideia já tem id " + this.id);
        }
        this.id = id;
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

    public Long getOwnerId() {
        return ownerId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Long getReviewedById() {
        return reviewedById;
    }

    public Instant getReviewedAt() {
        return reviewedAt;
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
