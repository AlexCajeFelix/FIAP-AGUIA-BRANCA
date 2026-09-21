package br.com.fiap.aguiabranca.domain.idea;

import java.util.List;
import java.util.Optional;

public interface IdeaRepository {

    Idea save(Idea idea);

    Optional<Idea> findById(Long id);

    List<Idea> findAllByStatusOrderByIdDesc(Idea.Status status);

    List<Idea> findAllByOwnerIdOrderByIdDesc(Long ownerId);

    List<Idea> findAllByOwnerIdAndStatusOrderByIdDesc(Long ownerId, Idea.Status status);

    List<Idea> findAllByOrderByIdDesc();

    Optional<Idea> findByIdAndOwnerId(Long id, Long ownerId);

    void deleteAll();
}
