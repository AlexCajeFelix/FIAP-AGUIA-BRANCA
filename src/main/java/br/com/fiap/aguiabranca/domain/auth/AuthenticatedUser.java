package br.com.fiap.aguiabranca.domain.auth;

import br.com.fiap.aguiabranca.domain.user.Role;
import br.com.fiap.aguiabranca.domain.user.User;

/**
 * Principal da requisicao autenticada.
 *
 * Carrega id e role vindos do token, sem tocar o banco a cada request. O id e o que permite
 * ao service decidir "esta ideia e sua?" sem recarregar o usuario.
 */
public record AuthenticatedUser(Long id, String email, Role role) {

    public static AuthenticatedUser from(User user) {
        return new AuthenticatedUser(user.getId(), user.getEmail(), user.getRole());
    }

    public boolean isOperador() {
        return role == Role.OPERADOR;
    }
}
