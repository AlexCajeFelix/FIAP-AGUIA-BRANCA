package br.com.fiap.aguiabranca.domain.project;

import br.com.fiap.aguiabranca.domain.auth.AuthenticatedUser;
import br.com.fiap.aguiabranca.domain.idea.Idea;
import br.com.fiap.aguiabranca.domain.idea.IdeaRepository;
import br.com.fiap.aguiabranca.domain.user.User;
import br.com.fiap.aguiabranca.domain.user.UserRepository;
import br.com.fiap.aguiabranca.shared.DomainRuleException;
import br.com.fiap.aguiabranca.shared.ErrorTypes;
import br.com.fiap.aguiabranca.shared.ResourceNotFoundException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectService {

    private final ProjectRepository projects;
    private final ProjectMetricsHistoryRepository history;
    private final IdeaRepository ideas;
    private final UserRepository users;

    public ProjectService(ProjectRepository projects, ProjectMetricsHistoryRepository history,
            IdeaRepository ideas, UserRepository users) {
        this.projects = projects;
        this.history = history;
        this.ideas = ideas;
        this.users = users;
    }

    @Transactional(readOnly = true)
    public List<Project> list() {
        return projects.findAllByOrderByIdDesc();
    }

    @Transactional(readOnly = true)
    public Project findById(Long id) {
        return projects.findById(id).orElseThrow(() -> notFound(id));
    }

    @Transactional(readOnly = true)
    public DashboardResponse dashboard() {
        return new DashboardResponse(projects.summarize(), projects.countByStatusGrouped());
    }

    @Transactional
    public Project promote(Long ideaId, PromoteIdeaRequest request) {
        Idea idea = ideas.findById(ideaId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorTypes.IDEA_NOT_FOUND,
                        "Ideia " + ideaId + " não encontrada."));

        if (!idea.isApproved()) {
            throw new DomainRuleException(ErrorTypes.IDEA_NOT_APPROVED,
                    "Só ideia aprovada vira projeto. Status atual: " + idea.getStatus() + ".");
        }
        if (projects.existsByIdeaId(ideaId)) {
            throw new DomainRuleException(ErrorTypes.IDEA_ALREADY_PROMOTED,
                    "Ideia " + ideaId + " já foi promovida a projeto.");
        }

        return projects.save(Project.fromIdea(idea, request.budget()));
    }

    /**
     * Atualiza metrica e grava o snapshot na MESMA transacao.
     *
     * A ordem importa: a entidade valida antes de qualquer snapshot ir para o repositorio, e
     * como tudo esta numa transacao so, uma validacao que estoure no meio desfaz o que veio
     * antes. E isso que impede historico orfao de uma mudanca que nao aconteceu.
     */
    @Transactional
    public Project updateMetrics(Long id, MetricsPatchRequest request, AuthenticatedUser actor) {
        if (request.isEmpty()) {
            throw new DomainRuleException(ErrorTypes.PROJECT_NO_METRIC,
                    "Informe ao menos uma métrica: progress ou spent.");
        }

        Project project = projects.findById(id).orElseThrow(() -> notFound(id));
        User changedBy = users.getReferenceById(actor.id());
        List<ProjectMetricsHistory> snapshots = new ArrayList<>();

        if (request.progress() != null) {
            BigDecimal previous = BigDecimal.valueOf(project.getProgress());
            project.updateProgress(request.progress());
            snapshots.add(new ProjectMetricsHistory(project, ProjectMetricsHistory.Metric.PROGRESS,
                    previous, BigDecimal.valueOf(project.getProgress()), changedBy));
        }

        if (request.spent() != null) {
            BigDecimal previous = project.getSpent();
            project.updateSpent(request.spent());
            snapshots.add(new ProjectMetricsHistory(project, ProjectMetricsHistory.Metric.SPENT,
                    previous, project.getSpent(), changedBy));
        }

        history.saveAll(snapshots);
        return project;
    }

    @Transactional(readOnly = true)
    public List<ProjectMetricsHistory> historyOf(Long id) {
        if (!projects.existsById(id)) {
            throw notFound(id);
        }
        return history.findAllByProjectIdOrderByChangedAtAscIdAsc(id);
    }

    private ResourceNotFoundException notFound(Long id) {
        return new ResourceNotFoundException(ErrorTypes.PROJECT_NOT_FOUND, "Projeto " + id + " não encontrado.");
    }
}
