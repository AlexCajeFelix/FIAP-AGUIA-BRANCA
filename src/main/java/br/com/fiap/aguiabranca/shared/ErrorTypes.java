package br.com.fiap.aguiabranca.shared;

import java.net.URI;

/**
 * URIs de "type" do RFC 7807.
 *
 * O cliente decide comportamento pelo type, nunca pelo title — title e texto livre e muda
 * sem aviso. Por isso estes valores sao contrato: mudar um quebra o app.
 */
public final class ErrorTypes {

    private static final String BASE = "https://aguiabranca.fiap.br/errors/";

    public static final String VALIDATION = BASE + "validacao";
    public static final String IDEA_NOT_FOUND = BASE + "ideia-nao-encontrada";
    public static final String IDEA_ALREADY_REVIEWED = BASE + "ideia-ja-revisada";
    public static final String IDEA_NOT_APPROVED = BASE + "ideia-nao-aprovada";
    public static final String IDEA_ALREADY_PROMOTED = BASE + "ideia-ja-promovida";
    public static final String PROJECT_NOT_FOUND = BASE + "projeto-nao-encontrado";
    public static final String PROJECT_INVALID_PROGRESS = BASE + "progresso-invalido";
    public static final String PROJECT_NO_METRIC = BASE + "nenhuma-metrica-informada";
    public static final String STRATEGY_NOT_FOUND = BASE + "estrategia-nao-encontrada";
    public static final String INVALID_CREDENTIALS = BASE + "credenciais-invalidas";
    public static final String INVALID_REFRESH = BASE + "refresh-invalido";
    public static final String UNAUTHENTICATED = BASE + "nao-autenticado";
    public static final String FORBIDDEN = BASE + "sem-permissao";

    public static URI of(String type) {
        return URI.create(type);
    }

    private ErrorTypes() {
    }
}
