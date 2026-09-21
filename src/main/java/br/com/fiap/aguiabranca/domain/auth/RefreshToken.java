package br.com.fiap.aguiabranca.domain.auth;

import br.com.fiap.aguiabranca.domain.user.User;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("refresh_tokens")
public class RefreshToken {

    @Id
    private Long id;

    @Indexed(unique = true)
    private String tokenHash;

    private Long userId;

    private UUID familyId;

    private Instant expiresAt;

    private Instant revokedAt;

    private Instant createdAt = Instant.now();

    protected RefreshToken() {
    }

    public RefreshToken(String tokenHash, User user, UUID familyId, Instant expiresAt) {
        this.tokenHash = tokenHash;
        this.userId = user.getId();
        this.familyId = familyId;
        this.expiresAt = expiresAt;
        this.createdAt = Instant.now();
    }

    public void revoke(Instant when) {
        if (this.revokedAt == null) {
            this.revokedAt = when;
        }
    }

    public void expireAt(Instant when) {
        this.expiresAt = when;
    }

    public boolean isRevoked() {
        return revokedAt != null;
    }

    public boolean isExpired(Instant now) {
        return !expiresAt.isAfter(now);
    }

    public Long getId() {
        return id;
    }

    public void assignId(Long id) {
        if (this.id == null) {
            this.id = id;
        }
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public User getUser() {
        User user = new User();
        user.assignId(userId);
        return user;
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
