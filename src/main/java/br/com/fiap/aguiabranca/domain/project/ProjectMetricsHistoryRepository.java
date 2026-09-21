package br.com.fiap.aguiabranca.domain.project;

import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectMetricsHistoryRepository extends MongoRepository<ProjectMetricsHistory, Long> {

    List<ProjectMetricsHistory> findAllByProjectIdOrderByChangedAtAscIdAsc(Long projectId);

    long countByProjectId(Long projectId);
}
