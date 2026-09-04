package br.com.fiap.aguiabranca.domain.auth;

import br.com.fiap.aguiabranca.shared.ErrorTypes;
import br.com.fiap.aguiabranca.shared.GlobalExceptionHandler;
import br.com.fiap.aguiabranca.shared.RequestId;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.util.List;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import org.springframework.web.filter.ForwardedHeaderFilter;

@Configuration
@EnableMethodSecurity
@EnableConfigurationProperties({ JwtProperties.class, CorsProperties.class, RateLimitProperties.class })
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;
    private final ObjectMapper objectMapper;
    private final CorsProperties corsProperties;

    public SecurityConfig(JwtAuthenticationFilter jwtFilter, ObjectMapper objectMapper,
            CorsProperties corsProperties) {
        this.jwtFilter = jwtFilter;
        this.objectMapper = objectMapper;
        this.corsProperties = corsProperties;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                // API stateless com token: nao ha sessao nem formulario para o CSRF proteger.
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/login").permitAll()
                        .requestMatchers("/v3/api-docs", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/auth/login", "/auth/refresh", "/auth/logout").permitAll()
                        // Publica para o healthcheck do compose (#12) conseguir bater sem token.
                        .requestMatchers("/actuator/health").permitAll()
                        // Restrições RBAC definidas aqui (nível de URL) em vez de apenas @PreAuthorize,
                        // para que o Spring Security retorne 403 ANTES que @Valid dispare 422.
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/ideas/*/approval")
                        .hasAnyRole("GESTOR", "LIDERANCA")
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/projects/from-idea/*")
                        .hasAnyRole("GESTOR", "LIDERANCA")
                        .requestMatchers(org.springframework.http.HttpMethod.PATCH, "/projects/*/metrics")
                        .hasAnyRole("GESTOR", "LIDERANCA")
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/strategies")
                        .hasAnyRole("GESTOR", "LIDERANCA")
                        .requestMatchers(org.springframework.http.HttpMethod.PUT, "/strategies/*")
                        .hasAnyRole("GESTOR", "LIDERANCA")
                        .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/strategies/*")
                        .hasAnyRole("GESTOR", "LIDERANCA")
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(this::unauthenticated)
                        .accessDeniedHandler(this::forbidden))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    /**
     * Sem token, ou token invalido: 401. O app da #22 trata isso como sessao
     * expirada.
     */
    private void unauthenticated(HttpServletRequest request, HttpServletResponse response,
            org.springframework.security.core.AuthenticationException ex) throws java.io.IOException {
        write(response, HttpStatus.UNAUTHORIZED, ErrorTypes.UNAUTHENTICATED, "Nao autenticado",
                "Envie um token valido no header Authorization.",
                GlobalExceptionHandler.instanceOf(request).toString());
    }

    /**
     * Autenticado, mas sem o perfil exigido: 403. O app apenas informa, nao
     * desloga.
     */
    private void forbidden(HttpServletRequest request, HttpServletResponse response,
            org.springframework.security.access.AccessDeniedException ex) throws java.io.IOException {
        write(response, HttpStatus.FORBIDDEN, ErrorTypes.FORBIDDEN, "Sem permissao",
                "Seu perfil nao tem permissao para esta operacao.",
                GlobalExceptionHandler.instanceOf(request).toString());
    }

    private void write(HttpServletResponse response, HttpStatus status, String type, String title,
            String detail, String instance) throws java.io.IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(),
                GlobalExceptionHandler.asMap(status, type, title, detail, instance));
    }

    /**
     * Um unico ponto de configuracao, no lugar de @CrossOrigin espalhado pelos
     * controllers:
     * anotacao por controller e o que faz uma rota nova nascer com regra diferente
     * das outras.
     *
     * Sem origem configurada devolve uma fonte vazia — nenhuma requisicao
     * cross-origin recebe
     * Access-Control-Allow-Origin, entao o navegador barra.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        if (corsProperties.isEnabled()) {
            CorsConfiguration configuration = new CorsConfiguration();
            // Lista explicita, nunca "*": combinado com allowCredentials(true) o proprio
            // Spring
            // recusa o curinga em runtime, e o header sai ecoando a origem que pediu.
            configuration.setAllowedOrigins(corsProperties.allowedOrigins());
            configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
            configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", RequestId.HEADER));
            // Exposto, e nao so permitido: header de resposta fora da lista branca do CORS
            // o
            // navegador esconde do JavaScript. O ID chegaria e o front nao conseguiria
            // le-lo.
            configuration.setExposedHeaders(List.of(RequestId.HEADER));
            configuration.setAllowCredentials(true);
            configuration.setMaxAge(Duration.ofHours(1));
            source.registerCorsConfiguration("/**", configuration);
        }

        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    /**
     * Trata headers padrao de proxy/load-balancer (X-Forwarded-For) para resolver
     * o IP real do cliente (request.getRemoteAddr()) com seguranca.
     */
    @Bean
    public ForwardedHeaderFilter forwardedHeaderFilter() {
        return new ForwardedHeaderFilter();
    }
}
