package br.com.fiap.aguiabranca.domain.auth;

import br.com.fiap.aguiabranca.shared.ErrorTypes;
import br.com.fiap.aguiabranca.shared.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

@Configuration
@EnableMethodSecurity
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;
    private final ObjectMapper objectMapper;

    public SecurityConfig(JwtAuthenticationFilter jwtFilter, ObjectMapper objectMapper) {
        this.jwtFilter = jwtFilter;
        this.objectMapper = objectMapper;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                // API stateless com token: nao ha sessao nem formulario para o CSRF proteger.
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/login").permitAll()
                        // TODO(#11): a rota esta liberada mas o starter-actuator nao esta no
                        // pom — qualquer healthcheck bate em 404.
                        .requestMatchers("/actuator/health").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(this::unauthenticated)
                        .accessDeniedHandler(this::forbidden))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    /** Sem token, ou token invalido: 401. O app da #22 trata isso como sessao expirada. */
    private void unauthenticated(HttpServletRequest request, HttpServletResponse response,
            org.springframework.security.core.AuthenticationException ex) throws java.io.IOException {
        write(response, HttpStatus.UNAUTHORIZED, ErrorTypes.UNAUTHENTICATED, "Nao autenticado",
                "Envie um token valido no header Authorization.", request.getRequestURI());
    }

    /** Autenticado, mas sem o perfil exigido: 403. O app apenas informa, nao desloga. */
    private void forbidden(HttpServletRequest request, HttpServletResponse response,
            org.springframework.security.access.AccessDeniedException ex) throws java.io.IOException {
        write(response, HttpStatus.FORBIDDEN, ErrorTypes.FORBIDDEN, "Sem permissao",
                "Seu perfil nao tem permissao para esta operacao.", request.getRequestURI());
    }

    private void write(HttpServletResponse response, HttpStatus status, String type, String title,
            String detail, String instance) throws java.io.IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(),
                GlobalExceptionHandler.asMap(status, type, title, detail, instance));
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }
}
