-- Refresh token revogavel. O valor em claro nunca entra no banco: so o SHA-256.
-- family_id amarra a cadeia de rotacao de um login; reuso de um token ja
-- rotacionado revoga a familia inteira (sessao comprometida).

CREATE TABLE refresh_tokens (
    id         BIGSERIAL PRIMARY KEY,
    token_hash VARCHAR(64)  NOT NULL UNIQUE,
    user_id    BIGINT       NOT NULL REFERENCES users (id),
    family_id  UUID         NOT NULL,
    expires_at TIMESTAMPTZ  NOT NULL,
    revoked_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_refresh_tokens_family ON refresh_tokens (family_id);
CREATE INDEX idx_refresh_tokens_user ON refresh_tokens (user_id);
