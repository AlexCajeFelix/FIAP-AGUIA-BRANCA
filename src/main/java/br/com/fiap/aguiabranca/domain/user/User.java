package br.com.fiap.aguiabranca.domain.user;

import br.com.fiap.aguiabranca.shared.persistence.SequentialDocument;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.Objects;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "users")
public class User implements SequentialDocument {

    @Id
    private Long id;

    @NotBlank
    private String name;

    @NotBlank
    private String email;

    @NotBlank
    private String passwordHash;

    @NotNull
    private Role role;

    @NotNull
    private Instant createdAt = Instant.now();

    protected User() {
    }

    public User(String name, String email, String passwordHash, Role role) {
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.createdAt = Instant.now();
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void assignId(Long id) {
        if (this.id != null) {
            throw new IllegalStateException("Usuário já tem id " + this.id);
        }
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Role getRole() {
        return role;
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
        User user = (User) o;
        return id != null && Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
