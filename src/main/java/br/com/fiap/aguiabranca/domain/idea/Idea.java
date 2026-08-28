package br.com.fiap.aguiabranca.domain.idea;

import br.com.fiap.aguiabranca.domain.user.User;
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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by_id")
    private User reviewedBy;

    @Column(name = "reviewed_at")
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
        this.owner = owner;
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
        this.reviewedBy = reviewer;
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
        return owner != null && Objects.equals(owner.getId(), userId);
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

    public User getOwner() {
        return owner;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public User getReviewedBy() {
        return reviewedBy;
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
