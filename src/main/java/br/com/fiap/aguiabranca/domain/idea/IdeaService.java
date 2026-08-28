package br.com.fiap.aguiabranca.domain.idea;

import br.com.fiap.aguiabranca.domain.auth.AuthenticatedUser;
import br.com.fiap.aguiabranca.domain.user.User;
import br.com.fiap.aguiabranca.domain.user.UserRepository;
import br.com.fiap.aguiabranca.shared.ErrorTypes;
import br.com.fiap.aguiabranca.shared.ResourceNotFoundException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IdeaService {

    private final IdeaRepository ideas;
    private final UserRepository users;

    public IdeaService(IdeaRepository ideas, UserRepository users) {
        this.ideas = ideas;
        this.users = users;
    }

    @Transactional
    public Idea submit(IdeaRequest request, AuthenticatedUser actor) {
        User owner = users.getReferenceById(actor.id());
        return ideas.save(new Idea(request.title(), request.description(), owner));
    }

    /**
     * O recorte por dono acontece na consulta, nao filtrando a lista depois: filtrar em
     * memoria ainda traz a ideia alheia do banco e vaza no dia em que alguem esquecer o filtro.
     */
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
    public Idea findVisible(Long id, AuthenticatedUser actor) {
        if (actor.isOperador()) {
            return ideas.findByIdAndOwnerId(id, actor.id()).orElseThrow(() -> notFound(id));
        }
        return ideas.findById(id).orElseThrow(() -> notFound(id));
    }

    @Transactional
    public Idea review(Long id, IdeaReviewRequest request, AuthenticatedUser actor) {
        Idea idea = ideas.findById(id).orElseThrow(() -> notFound(id));
        idea.review(request.status(), users.getReferenceById(actor.id()));
        return idea;
    }

    private ResourceNotFoundException notFound(Long id) {
        return new ResourceNotFoundException(ErrorTypes.IDEA_NOT_FOUND, "Ideia " + id + " não encontrada.");
    }
}
