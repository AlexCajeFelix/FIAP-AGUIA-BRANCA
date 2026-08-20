package br.com.fiap.aguiabranca.domain.project;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    @Query("SELECT new br.com.fiap.aguiabranca.domain.project.ProjectSummaryDto(" +
            "COUNT(p), AVG(p.progress), SUM(p.budget)) FROM Project p")
    ProjectSummaryDto summarize();

}
