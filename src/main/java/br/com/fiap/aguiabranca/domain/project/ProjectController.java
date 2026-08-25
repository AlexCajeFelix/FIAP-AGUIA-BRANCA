package br.com.fiap.aguiabranca.domain.project;

import br.com.fiap.aguiabranca.domain.auth.AuthenticatedUser;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public List<ProjectResponse> list() {
        return projectService.list().stream().map(ProjectResponse::from).toList();
    }

    /** Literal antes de "/{id}": o Spring casa o caminho mais especifico primeiro. */
    @GetMapping("/summary")
    public DashboardResponse summary() {
        return projectService.dashboard();
    }

    @GetMapping("/{id}")
    public ProjectResponse findById(@PathVariable Long id) {
        return ProjectResponse.from(projectService.findById(id));
    }

    @GetMapping("/{id}/metrics-history")
    public List<MetricsHistoryResponse> history(@PathVariable Long id) {
        return projectService.historyOf(id).stream().map(MetricsHistoryResponse::from).toList();
    }

    @PostMapping("/from-idea/{ideaId}")
    @PreAuthorize("hasAnyRole('GESTOR', 'LIDERANCA')")
    public ResponseEntity<ProjectResponse> promote(@PathVariable Long ideaId,
            @Valid @RequestBody PromoteIdeaRequest request) {

        Project project = projectService.promote(ideaId, request);
        return ResponseEntity
                .created(URI.create("/projects/" + project.getId()))
                .body(ProjectResponse.from(project));
    }

    @PatchMapping("/{id}/metrics")
    @PreAuthorize("hasAnyRole('GESTOR', 'LIDERANCA')")
    public ProjectResponse updateMetrics(@PathVariable Long id, @Valid @RequestBody MetricsPatchRequest request,
            @AuthenticationPrincipal AuthenticatedUser actor) {

        return ProjectResponse.from(projectService.updateMetrics(id, request, actor));
    }
}
