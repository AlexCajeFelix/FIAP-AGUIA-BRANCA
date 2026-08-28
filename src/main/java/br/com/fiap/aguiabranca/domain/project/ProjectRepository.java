package br.com.fiap.aguiabranca.domain.project;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    @Query("SELECT new br.com.fiap.aguiabranca.domain.project.ProjectSummaryDto(" +
            "COUNT(p), AVG(p.progress), SUM(p.budget)) FROM Project p")
    ProjectSummaryDto summarize();

    // COUNT devolve Long e o construtor do record exige Long: deixar int aqui explode em
    // runtime com ConstructorResultMappingException, nao em compilacao.
    @Query("SELECT new br.com.fiap.aguiabranca.domain.project.ProjectStatusCount(" +
            "p.status, COUNT(p)) FROM Project p GROUP BY p.status ORDER BY p.status")
    List<ProjectStatusCount> countByStatusGrouped();

    boolean existsByIdeaId(Long ideaId);

    List<Project> findAllByOrderByIdDesc();
}
