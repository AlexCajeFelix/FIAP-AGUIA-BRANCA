package br.com.fiap.aguiabranca.domain.auth;

import br.com.fiap.aguiabranca.domain.user.Role;

public record TokenResponse(String accessToken, long expiresIn, Role role) {
}
