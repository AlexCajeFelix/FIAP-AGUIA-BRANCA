package br.com.fiap.aguiabranca.domain.idea;

import br.com.fiap.aguiabranca.domain.user.User;
import br.com.fiap.aguiabranca.shared.DomainRuleException;
import br.com.fiap.aguiabranca.shared.ErrorTypes;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.Objects;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("ideas")
public class Idea {

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

    private Long ownerId;

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

    public Idea(String title, String description, User owner) {
        this(title, description);
        this.ownerId = owner.getId();
    }

    public void review(Status newStatus) {
        review(newStatus, null);
    }

    public void review(Status newStatus, User reviewer) {
        if (newStatus == null || newStatus == Status.DRAFT) {
            throw new IllegalArgumentException("Status de revisão inválido");
        }
        // Revisar de novo o que ja foi decidido apagaria a decisao anterior sem deixar rastro.
        if (isReviewed()) {
            throw new DomainRuleException(ErrorTypes.IDEA_ALREADY_REVIEWED,
                    "Ideia já revisada com status " + this.status + ".");
        }
        this.status = newStatus;
        this.reviewedById = reviewer == null ? null : reviewer.getId();
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
        return Objects.equals(ownerId, userId);
    }

    public Long getId() {
        return id;
    }

    public void assignId(Long id) {
        if (this.id == null) {
            this.id = id;
        }
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

    public User getOwner() {
        return ownerId == null ? null : new UserRef(ownerId);
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public User getReviewedBy() {
        return reviewedById == null ? null : new UserRef(reviewedById);
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

    private static final class UserRef extends User {
        private UserRef(Long id) {
            assignId(id);
        }
    }
}
