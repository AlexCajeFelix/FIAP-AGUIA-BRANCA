package br.com.fiap.aguiabranca.domain.ai;

import br.com.fiap.aguiabranca.domain.auth.JwtService;
import br.com.fiap.aguiabranca.domain.user.Role;
import br.com.fiap.aguiabranca.domain.user.User;
import br.com.fiap.aguiabranca.support.IntegrationTestSupport;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "app.gemini.api-key=chave-falsa-para-testes")
class IdeaSuggestionIntegrationTest extends IntegrationTestSupport {

    private static final String DRAFT = "{\"title\":\"Fila\",\"draft\":\"Organizar a inspecao\"}";
    private static final AtomicLong USER_IDS = new AtomicLong(1000);

    @MockBean
    private GeminiClient gemini;

    @Autowired
    private JwtService jwt;

    private String actor(Role role) {
        User user = new User("Pessoa", "pessoa@teste.dev", "hash-teste", role);
        user.assignId(USER_IDS.incrementAndGet());
        return bearer(jwt.generate(user));
    }

    @ParameterizedTest
    @EnumSource(Role.class)
    void shouldAllowEveryAuthenticatedRoleWithoutPersistingIdeas(Role role) throws Exception {
        when(gemini.improve("Fila", "Organizar a inspecao")).thenReturn("Sugestao para revisar.");
        mockMvc.perform(post("/ideas/suggest").header(HttpHeaders.AUTHORIZATION, actor(role))
                        .contentType(MediaType.APPLICATION_JSON).content(DRAFT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Sugestao para revisar."));
        assertThat(mongoTemplate.getCollection("ideas").countDocuments()).isZero();
    }

    @Test
    void shouldRejectAnonymousAndInvalidRequestsBeforeCallingProvider() throws Exception {
        mockMvc.perform(post("/ideas/suggest").contentType(MediaType.APPLICATION_JSON).content(DRAFT))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/ideas/suggest").header(HttpHeaders.AUTHORIZATION, actor(Role.OPERADOR))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"\",\"draft\":\"\"}"))
                .andExpect(status().isUnprocessableEntity());
        mockMvc.perform(post("/ideas/suggest").header(HttpHeaders.AUTHORIZATION, actor(Role.OPERADOR))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SuggestionRequest("Fila", "a".repeat(2001)))))
                .andExpect(status().isUnprocessableEntity());
        verifyNoInteractions(gemini);
    }

    @Test
    void shouldReturnRecoverableProviderFailureWithCorrelationId() throws Exception {
        when(gemini.improve(anyString(), anyString())).thenThrow(new SuggestionUnavailableException("Tente novamente."));
        mockMvc.perform(post("/ideas/suggest").header(HttpHeaders.AUTHORIZATION, actor(Role.OPERADOR))
                        .header("X-Request-Id", "teste-ia")
                        .contentType(MediaType.APPLICATION_JSON).content(DRAFT))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.type").value("https://aguiabranca.fiap.br/errors/sugestao-indisponivel"))
                .andExpect(jsonPath("$.instance").value("urn:request-id:teste-ia"));
    }

    @Test
    void shouldReturn429WithRetryAfterAndAvoidExtraProviderCall() throws Exception {
        when(gemini.improve(anyString(), anyString())).thenReturn("Sugestao.");
        String token = actor(Role.OPERADOR);
        for (int i = 0; i < 6; i++) {
            mockMvc.perform(post("/ideas/suggest").header(HttpHeaders.AUTHORIZATION, token)
                            .contentType(MediaType.APPLICATION_JSON).content(DRAFT))
                    .andExpect(status().isOk());
        }
        mockMvc.perform(post("/ideas/suggest").header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON).content(DRAFT))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists(HttpHeaders.RETRY_AFTER));
        verify(gemini, times(6)).improve("Fila", "Organizar a inspecao");
    }
}
