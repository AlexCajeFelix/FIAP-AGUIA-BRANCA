package br.com.fiap.aguiabranca.domain.idea;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IdeaRepository extends JpaRepository<Idea, Long> {

    List<Idea> findAllByStatusOrderByIdDesc(Idea.Status status);

    List<Idea> findAllByOwnerIdOrderByIdDesc(Long ownerId);

    List<Idea> findAllByOwnerIdAndStatusOrderByIdDesc(Long ownerId, Idea.Status status);

    List<Idea> findAllByOrderByIdDesc();

    Optional<Idea> findByIdAndOwnerId(Long id, Long ownerId);
}
