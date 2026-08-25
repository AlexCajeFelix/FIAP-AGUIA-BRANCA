package br.com.fiap.aguiabranca.domain.idea;

import br.com.fiap.aguiabranca.domain.auth.AuthenticatedUser;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ideas")
public class IdeaController {

    private final IdeaService ideaService;

    public IdeaController(IdeaService ideaService) {
        this.ideaService = ideaService;
    }

    /** Qualquer perfil autenticado submete ideia — inclusive o OPERADOR. */
    @PostMapping
    public ResponseEntity<IdeaResponse> submit(@Valid @RequestBody IdeaRequest request,
            @AuthenticationPrincipal AuthenticatedUser actor) {

        Idea idea = ideaService.submit(request, actor);
        return ResponseEntity
                .created(URI.create("/ideas/" + idea.getId()))
                .body(IdeaResponse.from(idea));
    }

    @GetMapping
    public List<IdeaResponse> list(@RequestParam(required = false) Idea.Status status,
            @AuthenticationPrincipal AuthenticatedUser actor) {

        return ideaService.list(status, actor).stream().map(IdeaResponse::from).toList();
    }

    @GetMapping("/{id}")
    public IdeaResponse findById(@PathVariable Long id, @AuthenticationPrincipal AuthenticatedUser actor) {
        return IdeaResponse.from(ideaService.findVisible(id, actor));
    }

    @PostMapping("/{id}/approval")
    @PreAuthorize("hasAnyRole('GESTOR', 'LIDERANCA')")
    public IdeaResponse review(@PathVariable Long id, @Valid @RequestBody IdeaReviewRequest request,
            @AuthenticationPrincipal AuthenticatedUser actor) {

        return IdeaResponse.from(ideaService.review(id, request, actor));
    }
}
