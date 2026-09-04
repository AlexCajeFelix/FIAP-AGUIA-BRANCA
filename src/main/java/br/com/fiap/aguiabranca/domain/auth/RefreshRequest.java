package br.com.fiap.aguiabranca.domain.auth;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequest(
        @NotBlank(message = "Refresh token e obrigatorio") String refreshToken) {
}
