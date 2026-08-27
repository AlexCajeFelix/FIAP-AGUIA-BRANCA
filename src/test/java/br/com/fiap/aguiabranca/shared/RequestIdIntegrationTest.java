package br.com.fiap.aguiabranca.shared;

import br.com.fiap.aguiabranca.domain.user.Role;
import br.com.fiap.aguiabranca.support.IntegrationTestSupport;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RequestIdIntegrationTest extends IntegrationTestSupport {

    private ListAppender<ILoggingEvent> logs;
    private ch.qos.logback.classic.Logger handlerLogger;

    @BeforeEach
    void captureLogs() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        handlerLogger = context.getLogger(GlobalExceptionHandler.class);
        logs = new ListAppender<>();
        logs.setContext(context);
        logs.start();
        handlerLogger.addAppender(logs);
        handlerLogger.setLevel(Level.DEBUG);
    }

    @AfterEach
    void releaseLogs() {
        handlerLogger.detachAppender(logs);
    }

    @Test
    @DisplayName("Usa o X-Request-Id recebido quando o cliente manda um")
    void shouldReuseIncomingRequestId() throws Exception {
        String incoming = "app-android-abc-123";

        mockMvc.perform(get("/actuator/health").header(RequestId.HEADER, incoming))
                .andExpect(status().isOk())
                .andExpect(header().string(RequestId.HEADER, incoming));
    }

    @Test
    @DisplayName("Gera um UUID quando o header nao vem")
    void shouldGenerateRequestIdWhenAbsent() throws Exception {
        String generated = mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(header().exists(RequestId.HEADER))
                .andReturn().getResponse().getHeader(RequestId.HEADER);

        assertThat(generated).isNotNull().matches("[0-9a-f-]{36}");
    }

    @Test
    @DisplayName("Header absurdamente longo e descartado em favor de um UUID")
    void shouldRejectOversizedIncomingId() throws Exception {
        // Cabecalho e entrada do usuario, e este valor entra em toda linha de log.
        String abusive = "x".repeat(5000);

        String applied = mockMvc.perform(get("/actuator/health").header(RequestId.HEADER, abusive))
                .andReturn().getResponse().getHeader(RequestId.HEADER);

        assertThat(applied).isNotEqualTo(abusive).matches("[0-9a-f-]{36}");
    }

    @Test
    @DisplayName("Caracteres de controle sao removidos do ID recebido")
    void shouldStripControlCharactersFromIncomingId() throws Exception {
        // O ID vai para dentro da linha de log: um \n vindo do cliente forjaria uma linha
        // inteira de log falsa, e o valor ainda voltaria no header da resposta.
        String applied = mockMvc.perform(get("/actuator/health")
                .header(RequestId.HEADER, "app-android\r\nFATAL linha-forjada"))
                .andReturn().getResponse().getHeader(RequestId.HEADER);

        assertThat(applied).isEqualTo("app-androidFATALlinha-forjada");
    }

    @Test
    @DisplayName("ID que some inteiro na limpeza vira um UUID, nunca vazio")
    void shouldFallBackWhenSanitizationEmptiesTheId() throws Exception {
        String applied = mockMvc.perform(get("/actuator/health")
                .header(RequestId.HEADER, "@@@ ### $$$"))
                .andReturn().getResponse().getHeader(RequestId.HEADER);

        // ID em branco no MDC sai no log identico a uma requisicao sem correlacao alguma.
        assertThat(applied).matches("[0-9a-f-]{36}");
    }

    @Test
    @DisplayName("O MDC fica limpo depois da requisicao")
    void shouldClearMdcAfterRequest() throws Exception {
        mockMvc.perform(get("/actuator/health")).andExpect(status().isOk());

        // O Tomcat reusa threads: sem a limpeza, a proxima requisicao nesta thread herdaria o ID.
        assertThat(MDC.get(RequestId.MDC_KEY)).isNull();
    }

    @Test
    @DisplayName("O instance do ProblemDetail carrega o mesmo ID da resposta")
    void shouldCarryRequestIdInProblemDetailInstance() throws Exception {
        String token = tokenFor("gestor-reqid@teste.dev", Role.GESTOR);
        String incoming = "correlacao-de-teste-1";

        mockMvc.perform(patch("/projects/999/metrics")
                .header("Authorization", bearer(token))
                .header(RequestId.HEADER, incoming)
                .contentType("application/json")
                .content("{}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(header().string(RequestId.HEADER, incoming))
                .andExpect(jsonPath("$.instance").value("urn:request-id:" + incoming));
    }

    @Test
    @DisplayName("Resposta 401 tambem sai correlacionada, mesmo vindo de fora do MVC")
    void shouldCorrelateUnauthenticatedResponses() throws Exception {
        String incoming = "correlacao-sem-token";

        // O 401 e escrito pelo entry point do Spring Security, fora do @RestControllerAdvice:
        // se o filtro de ID nao envolvesse a cadeia de seguranca, esta resposta sairia sem ID.
        mockMvc.perform(get("/ideas").header(RequestId.HEADER, incoming))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.instance").value("urn:request-id:" + incoming));
    }

    @Test
    @DisplayName("As linhas de log do GlobalExceptionHandler saem com o ID no MDC")
    void shouldCorrelateHandlerLogLines() throws Exception {
        String token = tokenFor("gestor-log@teste.dev", Role.GESTOR);
        String incoming = "correlacao-de-log-9";

        mockMvc.perform(patch("/projects/999/metrics")
                .header("Authorization", bearer(token))
                .header(RequestId.HEADER, incoming)
                .contentType("application/json")
                .content("{}"))
                .andExpect(status().isUnprocessableEntity());

        assertThat(logs.list)
                .isNotEmpty()
                .allSatisfy(event -> assertThat(event.getMDCPropertyMap())
                        .containsEntry(RequestId.MDC_KEY, incoming));
    }
}
