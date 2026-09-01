package br.com.fiap.aguiabranca.domain.auth;

/**
 * Refresh inexistente, expirado, ja usado ou de familia revogada.
 *
 * Tipo proprio no ProblemDetail: o app diferencia "sessao expirada, peca login"
 * de "credenciais de login erradas" sem olhar o title.
 */
public class InvalidRefreshTokenException extends RuntimeException {

    public InvalidRefreshTokenException() {
        super("Refresh token invalido ou expirado.");
    }
}
