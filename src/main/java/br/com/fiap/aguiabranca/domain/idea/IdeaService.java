package br.com.fiap.aguiabranca.domain.idea;

import br.com.fiap.aguiabranca.domain.auth.AuthenticatedUser;
import br.com.fiap.aguiabranca.shared.DomainRuleException;
import br.com.fiap.aguiabranca.shared.ErrorTypes;
import br.com.fiap.aguiabranca.shared.ResourceNotFoundException;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class IdeaService {

    private final IdeaRepository ideas;

    public IdeaService(IdeaRepository ideas) {
        this.ideas = ideas;
    }

    public Idea submit(IdeaRequest request, AuthenticatedUser actor) {
        return ideas.save(new Idea(request.title(), request.description(), actor.id()));
    }

    /**
     * O recorte por dono acontece na consulta, nao filtrando a lista depois: filtrar em
     * memoria ainda traz a ideia alheia do banco e vaza no dia em que alguem esquecer o filtro.
     */
    public List<Idea> list(Idea.Status status, AuthenticatedUser actor) {
        if (actor.isOperador()) {
            return status == null
                    ? ideas.findAllByOwnerIdOrderByIdDesc(actor.id())
                    : ideas.findAllByOwnerIdAndStatusOrderByIdDesc(actor.id(), status);
        }
        return status == null ? ideas.findAllByOrderByIdDesc() : ideas.findAllByStatusOrderByIdDesc(status);
    }

    /**
     * Para o OPERADOR, ideia de outro responde 404 e nao 403: 403 confirmaria que o id existe,
     * o que ja e vazamento — da para mapear o backlog alheio so pela diferenca de status.
     */
    public Idea findVisible(Long id, AuthenticatedUser actor) {
        if (actor.isOperador()) {
            return ideas.findByIdAndOwnerId(id, actor.id()).orElseThrow(() -> notFound(id));
        }
        return ideas.findById(id).orElseThrow(() -> notFound(id));
    }

    /** A escrita condicional impede duas revisoes simultaneas de apagarem a decisao uma da outra. */
    public Idea review(Long id, IdeaReviewRequest request, AuthenticatedUser actor) {
        Idea idea = ideas.findById(id).orElseThrow(() -> notFound(id));
        idea.review(request.status(), actor.id());
        if (!ideas.saveReviewIfPending(idea)) {
            throw new DomainRuleException(ErrorTypes.IDEA_ALREADY_REVIEWED,
                    "Ideia ja revisada por outra pessoa. Atualize a lista para ver a decisao.");
        }
        return idea;
    }

    private ResourceNotFoundException notFound(Long id) {
        return new ResourceNotFoundException(ErrorTypes.IDEA_NOT_FOUND, "Ideia " + id + " não encontrada.");
    }
}
