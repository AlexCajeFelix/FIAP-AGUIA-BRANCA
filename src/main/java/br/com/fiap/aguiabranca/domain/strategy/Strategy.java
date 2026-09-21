package br.com.fiap.aguiabranca.domain.strategy;

import br.com.fiap.aguiabranca.shared.persistence.SequentialDocument;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.Objects;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Estrategia corporativa, com soft delete.
 *
 * O @SQLRestriction do Hibernate aplicava "deleted_at is null" em toda query gerada; no Mongo
 * nao ha equivalente declarativo, entao o filtro e explicito no repositorio
 * (findAllByDeletedAtIsNull...). O documento excluido continua na colecao, invisivel para as
 * leituras do dominio e visivel para quem consultar pelo MongoTemplate — que e como o teste
 * prova que a linha nao sumiu.
 */
@Document(collection = "strategies")
public class Strategy implements SequentialDocument {

    @Id
    private Long id;

    @NotBlank(message = "Título é obrigatório")
    private String title;

    @NotBlank(message = "Descrição é obrigatória")
    private String description;

    @NotNull
    private Horizon horizon;

    @NotNull
    private Instant createdAt = Instant.now();

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

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void assignId(Long id) {
        if (this.id != null) {
            throw new IllegalStateException("Estratégia já tem id " + this.id);
        }
        this.id = id;
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
