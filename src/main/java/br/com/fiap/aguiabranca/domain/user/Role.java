package br.com.fiap.aguiabranca.domain.user;

/**
 * Os tres perfis do hub.
 *
 * OPERADOR submete ideia e acompanha o que e seu; GESTOR revisa e toca projeto;
 * LIDERANCA enxerga tudo e define estrategia.
 */
public enum Role {
    OPERADOR,
    GESTOR,
    LIDERANCA;

    /** O Spring Security espera a autoridade com prefixo ROLE_ para hasRole() funcionar. */
    public String authority() {
        return "ROLE_" + name();
    }
}
