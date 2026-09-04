package br.com.fiap.aguiabranca.shared;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI hubOpenApi() {
        OpenAPI api = new OpenAPI()
                .info(new Info()
                        .title("Hub de Inovação — Águia Branca")
                        .version("0.0.1")
                        .description("""
                                API do hub. Autenticacao Bearer JWT (claim `role`).
                                Tres perfis: OPERADOR (submete e acompanha o que e seu), \
                                GESTOR (revisa e toca projeto) e LIDERANCA (enxerga tudo e define estrategia).
                                """))
                .components(new Components()
                        .addSecuritySchemes("bearer-jwt", bearer("Access token JWT HS256."))
                        .addSecuritySchemes("operador", bearer("Perfil OPERADOR."))
                        .addSecuritySchemes("gestor", bearer("Perfil GESTOR."))
                        .addSecuritySchemes("lideranca", bearer("Perfil LIDERANCA.")))
                .addSecurityItem(new SecurityRequirement().addList("bearer-jwt"));
        api.setExtensions(new LinkedHashMap<>(Map.of("x-roles", List.of("OPERADOR", "GESTOR", "LIDERANCA"))));
        return api;
    }

    private static SecurityScheme bearer(String description) {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description(description);
    }
}
