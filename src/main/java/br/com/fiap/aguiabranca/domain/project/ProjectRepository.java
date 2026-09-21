package br.com.fiap.aguiabranca.domain.project;

import java.util.List;

public interface ProjectRepository {

    Project save(Project project);

    List<Project> saveAll(List<Project> projects);

    java.util.Optional<Project> findById(Long id);

    boolean existsById(Long id);

    ProjectSummaryDto summarize();

    List<ProjectStatusCount> countByStatusGrouped();

    boolean existsByIdeaId(Long ideaId);

    List<Project> findAllByOrderByIdDesc();

    void deleteAll();
}
