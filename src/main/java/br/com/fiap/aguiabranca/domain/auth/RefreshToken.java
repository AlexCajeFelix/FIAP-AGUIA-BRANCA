package br.com.fiap.aguiabranca.domain.auth;

import br.com.fiap.aguiabranca.shared.persistence.SequentialDocument;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "refresh_tokens")
public class RefreshToken implements SequentialDocument {

    @Id
    private Long id;

    @NotBlank
    private String tokenHash;

    @NotNull
    private Long userId;

    @NotNull
    private UUID familyId;

    @NotNull
    private Instant expiresAt;

    private Instant revokedAt;

    @NotNull
    private Instant createdAt = Instant.now();

    protected RefreshToken() {
    }

    public RefreshToken(String tokenHash, Long userId, UUID familyId, Instant expiresAt) {
        this.tokenHash = tokenHash;
        this.userId = userId;
        this.familyId = familyId;
        this.expiresAt = expiresAt;
        this.createdAt = Instant.now();
    }

    public boolean isRevoked() {
        return revokedAt != null;
    }

    public boolean isExpired(Instant now) {
        return !expiresAt.isAfter(now);
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void assignId(Long id) {
        if (this.id != null) {
            throw new IllegalStateException("Refresh token já tem id " + this.id);
        }
        this.id = id;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public Long getUserId() {
        return userId;
    }

    public UUID getFamilyId() {
        return familyId;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getRevokedAt() {
        return revokedAt;
    }
}
