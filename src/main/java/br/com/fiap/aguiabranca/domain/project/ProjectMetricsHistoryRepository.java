package br.com.fiap.aguiabranca.domain.project;

import java.util.List;

public interface ProjectMetricsHistoryRepository {

    ProjectMetricsHistory save(ProjectMetricsHistory entry);

    List<ProjectMetricsHistory> saveAll(List<ProjectMetricsHistory> entries);

    List<ProjectMetricsHistory> findAllByProjectIdOrderByChangedAtAscIdAsc(Long projectId);

    long countByProjectId(Long projectId);

    void deleteAll();
}
