package br.com.fiap.aguiabranca.domain.project;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectMetricsHistoryRepository extends JpaRepository<ProjectMetricsHistory, Long> {

    List<ProjectMetricsHistory> findAllByProjectIdOrderByChangedAtAscIdAsc(Long projectId);

    long countByProjectId(Long projectId);
}
