package br.com.fiap.aguiabranca.domain.project;

import br.com.fiap.aguiabranca.domain.auth.AuthenticatedUser;
import br.com.fiap.aguiabranca.domain.idea.Idea;
import br.com.fiap.aguiabranca.domain.idea.IdeaRepository;
import br.com.fiap.aguiabranca.shared.DomainRuleException;
import br.com.fiap.aguiabranca.shared.ErrorTypes;
import br.com.fiap.aguiabranca.shared.ResourceNotFoundException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectService {

    /** Colecao vazia nao gera documento no $group; o dashboard continua respondendo zero. */
    private static final ProjectSummaryDto EMPTY_SUMMARY = new ProjectSummaryDto(0L, null, null);

    private final ProjectRepository projects;
    private final ProjectMetricsHistoryRepository history;
    private final IdeaRepository ideas;

    public ProjectService(ProjectRepository projects, ProjectMetricsHistoryRepository history,
            IdeaRepository ideas) {
        this.projects = projects;
        this.history = history;
        this.ideas = ideas;
    }

    public List<Project> list() {
        return projects.findAllByOrderByIdDesc();
    }

    public Project findById(Long id) {
        return projects.findById(id).orElseThrow(() -> notFound(id));
    }

    public DashboardResponse dashboard() {
        ProjectSummaryDto summary = projects.summarize();
        return new DashboardResponse(summary == null ? EMPTY_SUMMARY : summary,
                projects.countByStatusGrouped());
    }

    /**
     * A checagem de ideia ja promovida existe para dar erro de dominio legivel, mas quem
     * garante a regra sob corrida e o indice unico parcial sobre ideaId: duas requisicoes
     * simultaneas passam as duas pelo existsByIdeaId, e o banco recusa a segunda.
     */
    public Project promote(Long ideaId, PromoteIdeaRequest request) {
        Idea idea = ideas.findById(ideaId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorTypes.IDEA_NOT_FOUND,
                        "Ideia " + ideaId + " não encontrada."));

        if (!idea.isApproved()) {
            throw new DomainRuleException(ErrorTypes.IDEA_NOT_APPROVED,
                    "Só ideia aprovada vira projeto. Status atual: " + idea.getStatus() + ".");
        }
        if (projects.existsByIdeaId(ideaId)) {
            throw alreadyPromoted(ideaId);
        }

        try {
            return projects.save(Project.fromIdea(idea, request.budget()));
        } catch (DuplicateKeyException ex) {
            throw alreadyPromoted(ideaId);
        }
    }

    /**
     * Atualiza metrica e grava o snapshot na MESMA transacao.
     *
     * A ordem importa: a entidade valida antes de qualquer snapshot ir para o repositorio, e
     * como tudo esta numa transacao so, uma validacao que estoure no meio desfaz o que veio
     * antes. E isso que impede historico orfao de uma mudanca que nao aconteceu. No Mongo a
     * transacao exige replica set — este e o unico metodo do sistema que depende disso.
     */
    @Transactional
    public Project updateMetrics(Long id, MetricsPatchRequest request, AuthenticatedUser actor) {
        if (request.isEmpty()) {
            throw new DomainRuleException(ErrorTypes.PROJECT_NO_METRIC,
                    "Informe ao menos uma métrica: progress ou spent.");
        }

        Project project = projects.findById(id).orElseThrow(() -> notFound(id));
        List<ProjectMetricsHistory> snapshots = new ArrayList<>();

        if (request.progress() != null) {
            BigDecimal previous = BigDecimal.valueOf(project.getProgress());
            project.updateProgress(request.progress());
            snapshots.add(new ProjectMetricsHistory(project.getId(), ProjectMetricsHistory.Metric.PROGRESS,
                    previous, BigDecimal.valueOf(project.getProgress()), actor.id()));
        }

        if (request.spent() != null) {
            BigDecimal previous = project.getSpent();
            project.updateSpent(request.spent());
            snapshots.add(new ProjectMetricsHistory(project.getId(), ProjectMetricsHistory.Metric.SPENT,
                    previous, project.getSpent(), actor.id()));
        }

        history.saveAll(snapshots);
        // Sem dirty checking o save e o que persiste a alteracao da entidade.
        return projects.save(project);
    }

    public List<ProjectMetricsHistory> historyOf(Long id) {
        if (!projects.existsById(id)) {
            throw notFound(id);
        }
        return history.findAllByProjectIdOrderByChangedAtAscIdAsc(id);
    }

    private DomainRuleException alreadyPromoted(Long ideaId) {
        return new DomainRuleException(ErrorTypes.IDEA_ALREADY_PROMOTED,
                "Ideia " + ideaId + " já foi promovida a projeto.");
    }

    private ResourceNotFoundException notFound(Long id) {
        return new ResourceNotFoundException(ErrorTypes.PROJECT_NOT_FOUND, "Projeto " + id + " não encontrado.");
    }
}
