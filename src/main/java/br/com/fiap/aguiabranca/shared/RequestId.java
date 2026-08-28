package br.com.fiap.aguiabranca.shared;

import org.slf4j.MDC;

/** Acesso ao correlation ID da requisicao corrente. */
public final class RequestId {

    public static final String HEADER = "X-Request-Id";
    public static final String MDC_KEY = "requestId";

    public static String current() {
        return MDC.get(MDC_KEY);
    }

    private RequestId() {
    }
}
