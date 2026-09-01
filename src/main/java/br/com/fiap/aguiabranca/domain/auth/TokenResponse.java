package br.com.fiap.aguiabranca.domain.auth;

import br.com.fiap.aguiabranca.domain.user.Role;

public record TokenResponse(String accessToken, String refreshToken, long expiresIn, Role role) {
}
