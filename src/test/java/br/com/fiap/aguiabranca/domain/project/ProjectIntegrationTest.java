package br.com.fiap.aguiabranca.domain.project;

import br.com.fiap.aguiabranca.domain.user.Role;
import br.com.fiap.aguiabranca.support.IntegrationTestSupport;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.hamcrest.Matchers;

/**
 * Integracao da fatia de Projetos: rota, transacao e snapshot de auditoria.
 *
 * As afirmacoes sobre o que ficou gravado passam pelos repositorios de dominio
 * (`projects` e `history`), nao pela resposta da rota. Conferir o snapshot pelo corpo do PATCH
 * nao provaria nada: o JSON e montado da entidade em memoria e sairia igual mesmo que a
 * gravacao do historico nunca tivesse acontecido. Ler do repositorio olha o que sobrou no
 * armazenamento depois que a requisicao terminou.
 */
class ProjectIntegrationTest extends IntegrationTestSupport {

    private static final String BUDGET = "150000.00";

    // ───────────────────────── promocao de ideia ─────────────────────────

    @Test
    @DisplayName("Promover ideia aprovada cria projeto e responde 201 com Location")
    void shouldPromoteApprovedIdeaAndReturn201() throws Exception {
        String gestor = tokenFor("gestor@teste.dev", Role.GESTOR);
        Long ideaId = givenApprovedIdea(gestor, "Frota eletrica para trajetos curtos");

        mockMvc.perform(post("/projects/from-idea/" + ideaId)
                .header(HttpHeaders.AUTHORIZATION, bearer(gestor))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"budget\": " + BUDGET + "}"))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, Matchers.endsWith("/projects/1")))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Frota eletrica para trajetos curtos"))
                .andExpect(jsonPath("$.status").value("PLANNING"))
                .andExpect(jsonPath("$.progress").value(0))
                .andExpect(jsonPath("$.ideaId").value(ideaId))
                // Valor financeiro e numero no contrato, nunca string (#23).
                .andExpect(jsonPath("$.budget").isNumber())
                .andExpect(jsonPath("$.spent").isNumber());

        assertThat(countProjects()).isEqualTo(1);
    }

    @Test
    @DisplayName("Promover ideia nao aprovada responde 422 e nao cria projeto")
    void shouldReject422WhenIdeaIsNotApproved() throws Exception {
        String gestor = tokenFor("gestor@teste.dev", Role.GESTOR);
        Long ideaId = givenIdea(gestor, "Ideia ainda em rascunho"); // fica DRAFT

        mockMvc.perform(post("/projects/from-idea/" + ideaId)
                .header(HttpHeaders.AUTHORIZATION, bearer(gestor))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"budget\": " + BUDGET + "}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.type").value("https://aguiabranca.fiap.br/errors/ideia-nao-aprovada"))
                .andExpect(jsonPath("$.status").value(422));

        assertThat(countProjects()).isZero();
    }

    @Test
    @DisplayName("Promover a mesma ideia duas vezes responde 422 na segunda e nao duplica projeto")
    void shouldReject422OnSecondPromotionOfSameIdea() throws Exception {
        String gestor = tokenFor("gestor@teste.dev", Role.GESTOR);
        Long ideaId = givenApprovedIdea(gestor, "Ideia promovida uma vez so");

        mockMvc.perform(post("/projects/from-idea/" + ideaId)
                .header(HttpHeaders.AUTHORIZATION, bearer(gestor))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"budget\": " + BUDGET + "}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/projects/from-idea/" + ideaId)
                .header(HttpHeaders.AUTHORIZATION, bearer(gestor))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"budget\": " + BUDGET + "}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.type").value("https://aguiabranca.fiap.br/errors/ideia-ja-promovida"))
                .andExpect(jsonPath("$.status").value(422));

        assertThat(countProjects()).isEqualTo(1);
    }

    // ───────────────────────── snapshot de auditoria ─────────────────────────

    @Test
    @DisplayName("PATCH de metrica grava UMA linha de historico com o valor anterior")
    void shouldWriteOneHistoryRowCarryingPreviousValue() throws Exception {
        String gestor = tokenFor("gestor@teste.dev", Role.GESTOR);
        Long projectId = givenProject(gestor, "Projeto com metrica");

        mockMvc.perform(patch("/projects/" + projectId + "/metrics")
                .header(HttpHeaders.AUTHORIZATION, bearer(gestor))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"progress\": 40}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.progress").value(40))
                // 0 -> 40 sai de PLANNING e entra em IN_PROGRESS pela regra da entidade.
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        List<ProjectMetricsHistory> entries = historyOf(projectId);
        assertThat(entries).hasSize(1);
        assertThat(entries.get(0).getMetric()).isEqualTo(ProjectMetricsHistory.Metric.PROGRESS);
        assertThat(entries.get(0).getOldValue()).isEqualByComparingTo("0");
        assertThat(entries.get(0).getNewValue()).isEqualByComparingTo("40");
    }

    @Test
    @DisplayName("Duas atualizacoes seguidas geram exatamente duas linhas, na ordem correta")
    void shouldWriteTwoHistoryRowsInChronologicalOrder() throws Exception {
        String gestor = tokenFor("gestor@teste.dev", Role.GESTOR);
        Long projectId = givenProject(gestor, "Projeto com duas medicoes");

        patchMetrics(gestor, projectId, "{\"progress\": 30}").andExpect(status().isOk());
        patchMetrics(gestor, projectId, "{\"progress\": 65}").andExpect(status().isOk());

        List<ProjectMetricsHistory> entries = historyOf(projectId);
        assertThat(entries).hasSize(2);

        assertThat(entries.get(0).getOldValue()).isEqualByComparingTo("0");
        assertThat(entries.get(0).getNewValue()).isEqualByComparingTo("30");
        // O oldValue da segunda tem de ser o newValue da primeira: e isso que torna a
        // trilha reconstruivel. Se vier 0 de novo, o snapshot leu a entidade antes da escrita.
        assertThat(entries.get(1).getOldValue()).isEqualByComparingTo("30");
        assertThat(entries.get(1).getNewValue()).isEqualByComparingTo("65");
    }

    @Test
    @DisplayName("PATCH com progresso fora de 0-100 responde 422 pela rota")
    void shouldReject422WhenProgressIsOutOfRange() throws Exception {
        String gestor = tokenFor("gestor@teste.dev", Role.GESTOR);
        Long projectId = givenProject(gestor, "Projeto com progresso invalido");

        patchMetrics(gestor, projectId, "{\"progress\": 150}")
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.type").value("https://aguiabranca.fiap.br/errors/progresso-invalido"))
                .andExpect(jsonPath("$.status").value(422));

        patchMetrics(gestor, projectId, "{\"progress\": -1}")
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.type").value("https://aguiabranca.fiap.br/errors/progresso-invalido"));

        assertThat(historyOf(projectId)).isEmpty();
    }

    @Test
    @DisplayName("PATCH que falha validacao no meio nao deixa historico orfao nem altera o projeto")
    void shouldRollbackHistoryWhenPatchFailsMidway() throws Exception {
        String gestor = tokenFor("gestor@teste.dev", Role.GESTOR);
        Long projectId = givenProject(gestor, "Projeto para teste de rollback");

        // progress e valido e gera snapshot; spent negativo estoura depois. Se o snapshot
        // escapasse da transacao da atualizacao, sobraria uma linha de uma mudanca que nao houve.
        patchMetrics(gestor, projectId, "{\"progress\": 50, \"spent\": -1}")
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422));

        assertThat(historyOf(projectId)).isEmpty();

        Project project = projects.findById(projectId).orElseThrow();
        assertThat(project.getProgress()).isZero();
        assertThat(project.getStatus()).isEqualTo(ProjectStatus.PLANNING);
        assertThat(project.getSpent()).isEqualByComparingTo("0");
    }

    // ───────────────────────── autorizacao ─────────────────────────

    @Test
    @DisplayName("OPERADOR recebe 403 ao promover ideia e ao alterar metrica")
    void shouldForbidOperadorFromPromotingAndPatchingMetrics() throws Exception {
        String gestor = tokenFor("gestor@teste.dev", Role.GESTOR);
        String operador = tokenFor("operador@teste.dev", Role.OPERADOR);

        Long ideaId = givenApprovedIdea(gestor, "Ideia que o operador nao promove");
        Long projectId = givenProject(gestor, "Projeto que o operador nao altera");

        mockMvc.perform(post("/projects/from-idea/" + ideaId)
                .header(HttpHeaders.AUTHORIZATION, bearer(operador))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"budget\": " + BUDGET + "}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.type").value("https://aguiabranca.fiap.br/errors/sem-permissao"))
                .andExpect(jsonPath("$.status").value(403));

        patchMetrics(operador, projectId, "{\"progress\": 10}")
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.type").value("https://aguiabranca.fiap.br/errors/sem-permissao"));

        // 403 tem de barrar antes do efeito colateral, nao depois.
        assertThat(countProjects()).isEqualTo(1);
        assertThat(historyOf(projectId)).isEmpty();
    }

    // ───────────────────────── apoio ─────────────────────────

    private org.springframework.test.web.servlet.ResultActions patchMetrics(String token, Long projectId, String body)
            throws Exception {
        return mockMvc.perform(patch("/projects/" + projectId + "/metrics")
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    private Long givenIdea(String token, String title) throws Exception {
        String response = mockMvc.perform(post("/ideas")
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\": \"" + title + "\", \"description\": \"Descricao de apoio do teste.\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(response).path("id").asLong();
    }

    private Long givenApprovedIdea(String gestorToken, String title) throws Exception {
        Long ideaId = givenIdea(gestorToken, title);
        mockMvc.perform(post("/ideas/" + ideaId + "/approval")
                .header(HttpHeaders.AUTHORIZATION, bearer(gestorToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\": \"APPROVED\"}"))
                .andExpect(status().isOk());

        return ideaId;
    }

    private Long givenProject(String gestorToken, String title) throws Exception {
        Long ideaId = givenApprovedIdea(gestorToken, title);
        String response = mockMvc.perform(post("/projects/from-idea/" + ideaId)
                .header(HttpHeaders.AUTHORIZATION, bearer(gestorToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"budget\": " + BUDGET + "}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(response).path("id").asLong();
    }

    private int countProjects() {
        return projects.findAllByOrderByIdDesc().size();
    }

    private List<ProjectMetricsHistory> historyOf(Long projectId) {
        return history.findAllByProjectIdOrderByChangedAtAscIdAsc(projectId);
    }
}
