package br.com.fiap.aguiabranca.shared;

/**
 * Regra de negocio violada — vira 422.
 *
 * Estende IllegalArgumentException de proposito: as entidades ja lancavam
 * IllegalArgumentException e os testes de dominio afirmam sobre esse tipo. Herdar mantem
 * esses testes validos e ainda permite ao handler distinguir "regra do dominio" de
 * "argumento invalido qualquer".
 */
public class DomainRuleException extends IllegalArgumentException {

    private final String type;

    public DomainRuleException(String type, String message) {
        super(message);
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
