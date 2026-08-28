package br.com.fiap.aguiabranca.domain.strategy;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.Objects;
import org.hibernate.annotations.SQLRestriction;

/**
 * Estrategia corporativa, com soft delete.
 *
 * O @SQLRestriction aplica "deleted_at is null" em toda query gerada pelo Hibernate — a linha
 * excluida some das leituras mas continua na tabela. Para provar que ela continua la, o teste
 * precisa consultar por SQL nativo: pelo repositorio ela e invisivel por construcao.
 */
@Entity
@Table(name = "strategies")
@SQLRestriction("deleted_at is null")
public class Strategy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Título é obrigatório")
    private String title;

    @NotBlank(message = "Descrição é obrigatória")
    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Horizon horizon;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "deleted_at")
    private Instant deletedAt;

    protected Strategy() {
    }

    public Strategy(String title, String description, Horizon horizon) {
        this.title = title;
        this.description = description;
        this.horizon = horizon;
        this.createdAt = Instant.now();
    }

    public void update(String title, String description, Horizon horizon) {
        this.title = title;
        this.description = description;
        this.horizon = horizon;
    }

    public void softDelete() {
        this.deletedAt = Instant.now();
    }

    public boolean isDeleted() {
        return deletedAt != null;
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

    public Horizon getHorizon() {
        return horizon;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Strategy strategy = (Strategy) o;
        return Objects.equals(id, strategy.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
