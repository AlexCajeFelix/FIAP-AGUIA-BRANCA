package br.com.fiap.aguiabranca.domain.idea;

import br.com.fiap.aguiabranca.domain.user.Role;
import br.com.fiap.aguiabranca.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class IdeaIntegrationTest extends IntegrationTestSupport {

    @Test
    @DisplayName("POST /ideas cria ideia e responde 201 com Location")
    void shouldCreateIdeaAndReturn201WithLocation() throws Exception {
        String token = tokenFor("operador@teste.dev", Role.OPERADOR);

        mockMvc.perform(post("/ideas")
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "title": "Redução de emissão de CO2",
                            "description": "Implementar frota elétrica para trajetos curtos."
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, endsWith("/ideas/1")))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Redução de emissão de CO2"))
                .andExpect(jsonPath("$.description").value("Implementar frota elétrica para trajetos curtos."))
                .andExpect(jsonPath("$.status").value("DRAFT"));
    }

    @Test
    @DisplayName("GET /ideas filtrado por status devolve so o subconjunto pedido")
    void shouldFilterIdeasByStatus() throws Exception {
        String gestorToken = tokenFor("gestor@teste.dev", Role.GESTOR);
        String operadorToken = tokenFor("operador@teste.dev", Role.OPERADOR);

        // Criar primeira ideia (DRAFT)
        String res1 = mockMvc.perform(post("/ideas")
                .header(HttpHeaders.AUTHORIZATION, bearer(operadorToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"title": "Ideia 1", "description": "Desc 1"}
                        """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long idea1Id = objectMapper.readTree(res1).path("id").asLong();

        // Criar segunda ideia e aprovar (APPROVED)
        String res2 = mockMvc.perform(post("/ideas")
                .header(HttpHeaders.AUTHORIZATION, bearer(operadorToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"title": "Ideia 2", "description": "Desc 2"}
                        """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long idea2Id = objectMapper.readTree(res2).path("id").asLong();

        mockMvc.perform(post("/ideas/" + idea2Id + "/approval")
                .header(HttpHeaders.AUTHORIZATION, bearer(gestorToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"status": "APPROVED"}
                        """))
                .andExpect(status().isOk());

        // Filtrar apenas APPROVED
        mockMvc.perform(get("/ideas")
                .param("status", "APPROVED")
                .header(HttpHeaders.AUTHORIZATION, bearer(gestorToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(idea2Id))
                .andExpect(jsonPath("$[0].status").value("APPROVED"));

        // Filtrar apenas DRAFT
        mockMvc.perform(get("/ideas")
                .param("status", "DRAFT")
                .header(HttpHeaders.AUTHORIZATION, bearer(gestorToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(idea1Id))
                .andExpect(jsonPath("$[0].status").value("DRAFT"));
    }

    @Test
    @DisplayName("Aprovacao por GESTOR muda o status e responde 200")
    void shouldAllowGestorToApproveIdea() throws Exception {
        String gestorToken = tokenFor("gestor@teste.dev", Role.GESTOR);
        String operadorToken = tokenFor("operador@teste.dev", Role.OPERADOR);

        String response = mockMvc.perform(post("/ideas")
                .header(HttpHeaders.AUTHORIZATION, bearer(operadorToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"title": "Otimizar rotas", "description": "Uso de IA para rotas mais eficientes."}
                        """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long ideaId = objectMapper.readTree(response).path("id").asLong();

        mockMvc.perform(post("/ideas/" + ideaId + "/approval")
                .header(HttpHeaders.AUTHORIZATION, bearer(gestorToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"status": "APPROVED"}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ideaId))
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    @DisplayName("Aprovacao por OPERADOR responde 403")
    void shouldForbiddenOperadorFromApprovingIdea() throws Exception {
        String gestorToken = tokenFor("gestor@teste.dev", Role.GESTOR);
        String operadorToken = tokenFor("operador@teste.dev", Role.OPERADOR);

        String response = mockMvc.perform(post("/ideas")
                .header(HttpHeaders.AUTHORIZATION, bearer(gestorToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"title": "Ideia para teste RBAC", "description": "Descricao de teste."}
                        """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long ideaId = objectMapper.readTree(response).path("id").asLong();

        mockMvc.perform(post("/ideas/" + ideaId + "/approval")
                .header(HttpHeaders.AUTHORIZATION, bearer(operadorToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"status": "APPROVED"}
                        """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.type").value("https://aguiabranca.fiap.br/errors/sem-permissao"))
                .andExpect(jsonPath("$.title").isNotEmpty())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("OPERADOR nao enxerga ideia de outro usuario - nem na listagem, nem em GET /ideas/{id} (404)")
    void shouldIsolateIdeasBetweenDifferentOperadors() throws Exception {
        String tokenOperador1 = tokenFor("operador1@teste.dev", Role.OPERADOR);
        String tokenOperador2 = tokenFor("operador2@teste.dev", Role.OPERADOR);

        // Operador 1 cria uma ideia
        String response = mockMvc.perform(post("/ideas")
                .header(HttpHeaders.AUTHORIZATION, bearer(tokenOperador1))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"title": "Ideia Confidencial Operador 1", "description": "Detalhes privados."}
                        """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long ideaIdOp1 = objectMapper.readTree(response).path("id").asLong();

        // Operador 2 lista ideias -> Nao deve enxergar a ideia do Operador 1
        mockMvc.perform(get("/ideas")
                .header(HttpHeaders.AUTHORIZATION, bearer(tokenOperador2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        // Operador 2 tenta buscar diretamente por ID -> deve responder 404 (sem vazar
        // existencia com 403 ou 200)
        mockMvc.perform(get("/ideas/" + ideaIdOp1)
                .header(HttpHeaders.AUTHORIZATION, bearer(tokenOperador2)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value("https://aguiabranca.fiap.br/errors/ideia-nao-encontrada"))
                .andExpect(jsonPath("$.title").isNotEmpty())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("Aprovar ideia ja aprovada responde 422")
    void shouldReturn422WhenApprovingAlreadyReviewedIdea() throws Exception {
        String gestorToken = tokenFor("gestor@teste.dev", Role.GESTOR);

        String response = mockMvc.perform(post("/ideas")
                .header(HttpHeaders.AUTHORIZATION, bearer(gestorToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"title": "Ideia Dupla Revisao", "description": "Teste de regra de negocio."}
                        """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long ideaId = objectMapper.readTree(response).path("id").asLong();

        // Primeira aprovacao -> 200 OK
        mockMvc.perform(post("/ideas/" + ideaId + "/approval")
                .header(HttpHeaders.AUTHORIZATION, bearer(gestorToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"status": "APPROVED"}
                        """))
                .andExpect(status().isOk());

        // Segunda tentativa de aprovacao -> 422 Unprocessable Entity
        mockMvc.perform(post("/ideas/" + ideaId + "/approval")
                .header(HttpHeaders.AUTHORIZATION, bearer(gestorToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"status": "APPROVED"}
                        """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.type").value("https://aguiabranca.fiap.br/errors/ideia-ja-revisada"))
                .andExpect(jsonPath("$.title").isNotEmpty())
                .andExpect(jsonPath("$.status").value(422));
    }

    @Test
    @DisplayName("Todos os erros retornados validados como ProblemDetail (type, title, status)")
    void shouldValidateProblemDetailStructureOnErrors() throws Exception {
        String gestorToken = tokenFor("gestor@teste.dev", Role.GESTOR);

        // Teste de 404
        mockMvc.perform(get("/ideas/999999")
                .header(HttpHeaders.AUTHORIZATION, bearer(gestorToken)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value("https://aguiabranca.fiap.br/errors/ideia-nao-encontrada"))
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.status").value(404));

        // Teste de 422 Unprocessable Entity (Validacao de payload invalido ao criar
        // ideia)
        mockMvc.perform(post("/ideas")
                .header(HttpHeaders.AUTHORIZATION, bearer(gestorToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"title": "", "description": ""}
                        """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.type").value("https://aguiabranca.fiap.br/errors/validacao"))
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.status").value(422));
    }
}
