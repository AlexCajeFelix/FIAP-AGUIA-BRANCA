package br.com.fiap.aguiabranca.shared;

import br.com.fiap.aguiabranca.domain.auth.RateLimitExceededException;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Traduz excecao em RFC 7807.
 *
 * AccessDeniedException NAO e tratada aqui de proposito: ela precisa subir ate
 * o
 * ExceptionTranslationFilter do Spring Security, que e quem sabe diferenciar
 * "nao sei quem
 * voce e" (401) de "sei e voce nao pode" (403). Capturar aqui devolveria 403
 * tambem para
 * requisicao anonima, e o app trataria sessao valida como expirada.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        log.debug("404 em {}: {}", request.getRequestURI(), ex.getMessage());
        return problem(HttpStatus.NOT_FOUND, "Recurso nao encontrado", ex.getType(), ex.getMessage(), request);
    }

    @ExceptionHandler(DomainRuleException.class)
    public ProblemDetail handleDomainRule(DomainRuleException ex, HttpServletRequest request) {
        // WARN e nao ERROR: regra de negocio recusada e funcionamento esperado, nao
        // falha da app.
        // Sai correlacionado pelo MDC, entao da para reconstruir a requisicao inteira
        // pelo ID.
        log.warn("422 em {} ({}): {}", request.getRequestURI(), ex.getType(), ex.getMessage());
        return problem(HttpStatus.UNPROCESSABLE_ENTITY, "Regra de negocio violada", ex.getType(),
                ex.getMessage(), request);
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ProblemDetail> handleRateLimitExceeded(RateLimitExceededException ex,
            HttpServletRequest request) {
        log.warn("429 em {}: {}", request.getRequestURI(), ex.getMessage());
        ProblemDetail detail = problem(HttpStatus.TOO_MANY_REQUESTS, "Limite de tentativas excedido",
                ErrorTypes.RATE_LIMIT_EXCEEDED, ex.getMessage(), request);
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header(HttpHeaders.RETRY_AFTER, String.valueOf(ex.getRetryAfterSeconds()))
                .body(detail);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ProblemDetail handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        // Mensagem deliberadamente igual para e-mail inexistente e senha errada:
        // distinguir
        // as duas entrega ao atacante uma lista de contas validas.
        return problem(HttpStatus.UNAUTHORIZED, "Credenciais invalidas", ErrorTypes.INVALID_CREDENTIALS,
                "E-mail ou senha incorretos.", request);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        // 422 e nao 400: o corpo chegou bem-formado, o que falhou foi a regra do campo.
        // A #25 conta com essa distincao para escolher a mensagem no app.
        ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_ENTITY);
        detail.setType(ErrorTypes.of(ErrorTypes.VALIDATION));
        detail.setTitle("Payload invalido");
        detail.setDetail("Um ou mais campos nao passaram na validacao.");

        Map<String, String> fields = new TreeMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fields.put(error.getField(), error.getDefaultMessage());
        }
        detail.setProperty("errors", fields);
        if (request instanceof org.springframework.web.context.request.ServletWebRequest servletRequest) {
            detail.setInstance(instanceOf(servletRequest.getRequest()));
        }

        return ResponseEntity.unprocessableEntity().body(detail);
    }

    private ProblemDetail problem(HttpStatus status, String title, String type, String message,
            HttpServletRequest request) {
        ProblemDetail detail = ProblemDetail.forStatus(status);
        detail.setType(ErrorTypes.of(type));
        detail.setTitle(title);
        detail.setDetail(message);
        detail.setInstance(instanceOf(request));
        return detail;
    }

    /**
     * O instance aponta para a ocorrencia, nao para o recurso: com o correlation ID
     * ali, um
     * print de tela do erro basta para achar as linhas de log daquela requisicao
     * exata.
     */
    public static URI instanceOf(HttpServletRequest request) {
        String requestId = RequestId.current();
        return requestId == null
                ? URI.create(request.getRequestURI())
                : URI.create("urn:request-id:" + requestId);
    }

    /**
     * Usado tambem pelos handlers da cadeia de filtros, que respondem fora do MVC.
     */
    public static Map<String, Object> asMap(HttpStatus status, String type, String title, String detail,
            String instance) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("type", type);
        body.put("title", title);
        body.put("status", status.value());
        body.put("detail", detail);
        body.put("instance", instance);
        return body;
    }
}
