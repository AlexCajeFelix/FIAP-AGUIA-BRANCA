package br.com.fiap.aguiabranca.domain.project;

import java.util.List;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends MongoRepository<Project, Long> {

    /**
     * O que o JPQL fazia com COUNT/AVG/SUM agora e um $group so.
     *
     * Colecao vazia nao produz documento nenhum — diferente do Postgres, que devolvia uma
     * linha com count 0. Por isso o retorno e nulo nesse caso e quem chama normaliza.
     */
    @Aggregation(pipeline = {
            "{ $group: { _id: null, totalProjects: { $sum: 1 }, avgProgress: { $avg: '$progress' }, totalBudget: { $sum: '$budget' } } }",
            "{ $project: { _id: 0, totalProjects: 1, avgProgress: 1, totalBudget: 1 } }"
    })
    ProjectSummaryDto summarize();

    // O $sort vem depois do $project para ordenar pelo campo ja renomeado. Status e string no
    // documento, entao a ordem e alfabetica — a mesma que o Postgres dava na coluna VARCHAR.
    @Aggregation(pipeline = {
            "{ $group: { _id: '$status', total: { $sum: 1 } } }",
            "{ $project: { _id: 0, status: '$_id', total: 1 } }",
            "{ $sort: { status: 1 } }"
    })
    List<ProjectStatusCount> countByStatusGrouped();

    boolean existsByIdeaId(Long ideaId);

    List<Project> findAllByOrderByIdDesc();
}
