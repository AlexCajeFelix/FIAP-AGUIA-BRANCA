package br.com.fiap.aguiabranca.domain.auth;

import br.com.fiap.aguiabranca.domain.user.Role;
import br.com.fiap.aguiabranca.support.IntegrationTestSupport;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthorizationMatrixIntegrationTest extends IntegrationTestSupport {

    @Autowired
    @org.springframework.beans.factory.annotation.Qualifier("requestMappingHandlerMapping")
    private RequestMappingHandlerMapping handlerMapping;

    @Autowired
    private JwtProperties jwtProperties;

    public enum Access {
        ALLOWED,
        FORBIDDEN
    }

    public record RouteMatrixEntry(
            HttpMethod method,
            String path,
            boolean isPublic,
            Map<Role, Access> roleAccess) {
        public String key() {
            return method.name() + " " + path;
        }
    }

    // Tabela única e legível da matriz de autorização
    private static final List<RouteMatrixEntry> MATRIX = List.of(
            // Rotas Públicas
            new RouteMatrixEntry(HttpMethod.POST, "/auth/login", true, Map.of(
                    Role.OPERADOR, Access.ALLOWED,
                    Role.GESTOR, Access.ALLOWED,
                    Role.LIDERANCA, Access.ALLOWED)),
            new RouteMatrixEntry(HttpMethod.GET, "/actuator/health", true, Map.of(
                    Role.OPERADOR, Access.ALLOWED,
                    Role.GESTOR, Access.ALLOWED,
                    Role.LIDERANCA, Access.ALLOWED)),

            // Domínio Ideas
            new RouteMatrixEntry(HttpMethod.POST, "/ideas", false, Map.of(
                    Role.OPERADOR, Access.ALLOWED,
                    Role.GESTOR, Access.ALLOWED,
                    Role.LIDERANCA, Access.ALLOWED)),
            new RouteMatrixEntry(HttpMethod.GET, "/ideas", false, Map.of(
                    Role.OPERADOR, Access.ALLOWED,
                    Role.GESTOR, Access.ALLOWED,
                    Role.LIDERANCA, Access.ALLOWED)),
            new RouteMatrixEntry(HttpMethod.GET, "/ideas/{id}", false, Map.of(
                    Role.OPERADOR, Access.ALLOWED,
                    Role.GESTOR, Access.ALLOWED,
                    Role.LIDERANCA, Access.ALLOWED)),
            new RouteMatrixEntry(HttpMethod.POST, "/ideas/{id}/approval", false, Map.of(
                    Role.OPERADOR, Access.FORBIDDEN,
                    Role.GESTOR, Access.ALLOWED,
                    Role.LIDERANCA, Access.ALLOWED)),

            // Domínio Projects
            new RouteMatrixEntry(HttpMethod.GET, "/projects", false, Map.of(
                    Role.OPERADOR, Access.ALLOWED,
                    Role.GESTOR, Access.ALLOWED,
                    Role.LIDERANCA, Access.ALLOWED)),
            new RouteMatrixEntry(HttpMethod.GET, "/projects/summary", false, Map.of(
                    Role.OPERADOR, Access.ALLOWED,
                    Role.GESTOR, Access.ALLOWED,
                    Role.LIDERANCA, Access.ALLOWED)),
            new RouteMatrixEntry(HttpMethod.GET, "/projects/{id}", false, Map.of(
                    Role.OPERADOR, Access.ALLOWED,
                    Role.GESTOR, Access.ALLOWED,
                    Role.LIDERANCA, Access.ALLOWED)),
            new RouteMatrixEntry(HttpMethod.GET, "/projects/{id}/metrics-history", false, Map.of(
                    Role.OPERADOR, Access.ALLOWED,
                    Role.GESTOR, Access.ALLOWED,
                    Role.LIDERANCA, Access.ALLOWED)),
            new RouteMatrixEntry(HttpMethod.POST, "/projects/from-idea/{ideaId}", false, Map.of(
                    Role.OPERADOR, Access.FORBIDDEN,
                    Role.GESTOR, Access.ALLOWED,
                    Role.LIDERANCA, Access.ALLOWED)),
            new RouteMatrixEntry(HttpMethod.PATCH, "/projects/{id}/metrics", false, Map.of(
                    Role.OPERADOR, Access.FORBIDDEN,
                    Role.GESTOR, Access.ALLOWED,
                    Role.LIDERANCA, Access.ALLOWED)),

            // Domínio Strategies
            new RouteMatrixEntry(HttpMethod.GET, "/strategies", false, Map.of(
                    Role.OPERADOR, Access.ALLOWED,
                    Role.GESTOR, Access.ALLOWED,
                    Role.LIDERANCA, Access.ALLOWED)),
            new RouteMatrixEntry(HttpMethod.GET, "/strategies/{id}", false, Map.of(
                    Role.OPERADOR, Access.ALLOWED,
                    Role.GESTOR, Access.ALLOWED,
                    Role.LIDERANCA, Access.ALLOWED)),
            new RouteMatrixEntry(HttpMethod.POST, "/strategies", false, Map.of(
                    Role.OPERADOR, Access.FORBIDDEN,
                    Role.GESTOR, Access.ALLOWED,
                    Role.LIDERANCA, Access.ALLOWED)),
            new RouteMatrixEntry(HttpMethod.PUT, "/strategies/{id}", false, Map.of(
                    Role.OPERADOR, Access.FORBIDDEN,
                    Role.GESTOR, Access.ALLOWED,
                    Role.LIDERANCA, Access.ALLOWED)),
            new RouteMatrixEntry(HttpMethod.DELETE, "/strategies/{id}", false, Map.of(
                    Role.OPERADOR, Access.FORBIDDEN,
                    Role.GESTOR, Access.ALLOWED,
                    Role.LIDERANCA, Access.ALLOWED)));

    @Test
    @DisplayName("Garante que toda rota declarada no Spring MVC está registrada na matriz")
    void shouldFailIfAnySpringRouteIsMissingFromMatrix() {
        Set<String> matrixKeys = new TreeSet<>();
        for (RouteMatrixEntry entry : MATRIX) {
            matrixKeys.add(entry.key());
        }

        Set<String> springKeys = new TreeSet<>();
        for (RequestMappingInfo info : handlerMapping.getHandlerMethods().keySet()) {
            Set<String> patterns = info.getPatternValues();
            Set<RequestMethod> methods = info.getMethodsCondition().getMethods();

            for (String pattern : patterns) {
                if (pattern.startsWith("/error")) {
                    continue;
                }
                for (RequestMethod method : methods) {
                    springKeys.add(method.name() + " " + pattern);
                }
            }
        }

        assertThat(springKeys)
                .withFailMessage("Existem rotas no Spring MVC que NÃO foram declaradas na matriz de autorização: %s",
                        springKeys.stream().filter(k -> !matrixKeys.contains(k)).toList())
                .isSubsetOf(matrixKeys);
    }

    public record RoleTestCase(RouteMatrixEntry entry, Role role) {
        @Override
        public String toString() {
            return entry.method() + " " + entry.path() + " -> " + role + " (" + entry.roleAccess().get(role) + ")";
        }
    }

    static Stream<RoleTestCase> generateRoleTestCases() {
        return MATRIX.stream().flatMap(entry -> Stream.of(Role.OPERADOR, Role.GESTOR, Role.LIDERANCA)
                .map(role -> new RoleTestCase(entry, role)));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("generateRoleTestCases")
    @DisplayName("Valida acesso por perfil (20x/40x vs 403) para cada rota x perfil")
    void shouldValidateRoleAccessForEveryRoute(RoleTestCase testCase) throws Exception {
        RouteMatrixEntry entry = testCase.entry();
        Role role = testCase.role();
        Access access = entry.roleAccess().get(role);

        String token = tokenFor(role.name().toLowerCase() + "@teste.dev", role);
        String testPath = entry.path()
                .replace("{id}", "1")
                .replace("{ideaId}", "1");

        MockHttpServletRequestBuilder requestBuilder = request(entry.method(), testPath)
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .contentType(MediaType.APPLICATION_JSON);

        if (entry.method() == HttpMethod.POST || entry.method() == HttpMethod.PUT
                || entry.method() == HttpMethod.PATCH) {
            requestBuilder.content("{}");
        }

        if (access == Access.FORBIDDEN) {
            mockMvc.perform(requestBuilder)
                    .andExpect(status().isForbidden());
        } else {
            mockMvc.perform(requestBuilder)
                    .andExpect(result -> assertThat(result.getResponse().getStatus())
                            .isNotIn(HttpStatus.FORBIDDEN.value(), HttpStatus.UNAUTHORIZED.value()));
        }
    }

    @Test
    @DisplayName("Requisição sem token em rota protegida responde 401 (não 403)")
    void shouldReturn401WhenRequestingProtectedRouteWithoutToken() throws Exception {
        mockMvc.perform(request(HttpMethod.GET, "/ideas"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Requisição com token expirado responde 401")
    void shouldReturn401WhenTokenIsExpired() throws Exception {
        SecretKey key = Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
        Instant past = Instant.now().minusSeconds(3600);

        String expiredToken = Jwts.builder()
                .subject("1")
                .claim("email", "operador@teste.dev")
                .claim("role", Role.OPERADOR.name())
                .issuedAt(Date.from(past.minusSeconds(3600)))
                .expiration(Date.from(past))
                .signWith(key)
                .compact();

        mockMvc.perform(request(HttpMethod.GET, "/ideas")
                .header(HttpHeaders.AUTHORIZATION, bearer(expiredToken)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Requisição com token de assinatura inválida responde 401")
    void shouldReturn401WhenTokenHasInvalidSignature() throws Exception {
        SecretKey invalidKey = Keys.hmacShaKeyFor(
                "chave-falsa-com-tamanho-suficiente-para-hmac-sha-256-signature!".getBytes(StandardCharsets.UTF_8));
        Instant now = Instant.now();

        String invalidToken = Jwts.builder()
                .subject("1")
                .claim("email", "operador@teste.dev")
                .claim("role", Role.OPERADOR.name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(3600)))
                .signWith(invalidKey)
                .compact();

        mockMvc.perform(request(HttpMethod.GET, "/ideas")
                .header(HttpHeaders.AUTHORIZATION, bearer(invalidToken)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Rotas públicas respondem sem token e não retornam 401")
    void shouldAllowPublicRoutesWithoutToken() throws Exception {
        // GET /actuator/health
        mockMvc.perform(request(HttpMethod.GET, "/actuator/health"))
                .andExpect(result -> assertThat(result.getResponse().getStatus())
                        .isNotEqualTo(HttpStatus.UNAUTHORIZED.value()));

        // POST /auth/login
        mockMvc.perform(request(HttpMethod.POST, "/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(result -> assertThat(result.getResponse().getStatus())
                        .isNotEqualTo(HttpStatus.UNAUTHORIZED.value()));
    }
}
