package br.com.fiap.aguiabranca.domain.ai;

import br.com.fiap.aguiabranca.domain.auth.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Assistente de redacao de ideias.
 *
 * O endpoint so existe quando ha GEMINI_API_KEY no ambiente (ver AiConfig): sem a chave a
 * rota responde 404 e o resto da API segue igual — nenhum teste e nenhum build dependem de
 * segredo para passar.
 *
 * Nada e gravado aqui: a sugestao volta para a tela e a pessoa decide se usa.
 */
@RestController
@ConditionalOnExpression("!'${app.gemini.api-key:}'.isBlank()")
@RequestMapping("/ideas")
@Tag(name = "Ideias")
public class IdeaSuggestionController {

    private final GeminiClient gemini;
    private final SuggestionLimiter limiter;

    public IdeaSuggestionController(GeminiClient gemini, SuggestionLimiter limiter) {
        this.gemini = gemini;
        this.limiter = limiter;
    }

    @PostMapping("/suggest")
    @Operation(summary = "Reescreve o rascunho de uma ideia com apoio de IA")
    public SuggestionResponse suggest(@Valid @RequestBody SuggestionRequest request,
            @AuthenticationPrincipal AuthenticatedUser actor) {
        return limiter.execute(actor.id(),
                () -> new SuggestionResponse(gemini.improve(request.title(), request.draft())));
    }
}
