package br.com.fiap.aguiabranca.shared;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Amarra todas as linhas de log de uma requisicao a um mesmo identificador.
 *
 * Ordem mais alta possivel de proposito: precisa envolver tambem a cadeia do Spring Security,
 * senao falha de autenticacao — justamente o que mais se investiga — sai sem correlacao.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestIdFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {

        String requestId = resolve(request.getHeader(RequestId.HEADER));
        MDC.put(RequestId.MDC_KEY, requestId);
        // Escrito antes de seguir a cadeia: depois que a resposta e comitada, header nao entra mais.
        response.setHeader(RequestId.HEADER, requestId);

        try {
            chain.doFilter(request, response);
        } finally {
            // Nao e opcional: o Tomcat reusa threads do pool e, sem a limpeza, a proxima
            // requisicao que cair nesta thread herda o ID da anterior. E um bug que so
            // aparece sob carga e faz a investigacao apontar para a requisicao errada.
            MDC.remove(RequestId.MDC_KEY);
        }
    }

    /**
     * Valor recebido do cliente e aceito, mas nao em qualquer formato: ele entra no log, e
     * cabecalho e entrada do usuario. Um valor arbitrario de 8 KB polui todas as linhas.
     */
    private String resolve(String received) {
        if (received == null || received.length() > 64) {
            return UUID.randomUUID().toString();
        }
        String sanitized = received.replaceAll("[^A-Za-z0-9_.:-]", "");
        // Sobrou vazio: o header veio so com caracteres que a limpeza descarta. Deixar assim
        // poria um ID em branco no MDC, e a linha de log ficaria igual a de quem nao tem
        // correlacao nenhuma — pior do que gerar um valor novo.
        return sanitized.isBlank() ? UUID.randomUUID().toString() : sanitized;
    }
}
