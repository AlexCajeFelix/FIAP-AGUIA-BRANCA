package br.com.fiap.aguiabranca.shared;

import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import br.com.fiap.aguiabranca.domain.auth.RefreshToken;
import br.com.fiap.aguiabranca.domain.auth.RefreshTokenRepository;
import br.com.fiap.aguiabranca.domain.idea.Idea;
import br.com.fiap.aguiabranca.domain.idea.IdeaRepository;
import br.com.fiap.aguiabranca.domain.project.Project;
import br.com.fiap.aguiabranca.domain.project.ProjectMetricsHistory;
import br.com.fiap.aguiabranca.domain.project.ProjectMetricsHistoryRepository;
import br.com.fiap.aguiabranca.domain.project.ProjectRepository;
import br.com.fiap.aguiabranca.domain.project.ProjectStatus;
import br.com.fiap.aguiabranca.domain.project.ProjectStatusCount;
import br.com.fiap.aguiabranca.domain.project.ProjectSummaryDto;
import br.com.fiap.aguiabranca.domain.strategy.Strategy;
import br.com.fiap.aguiabranca.domain.strategy.StrategyRepository;
import br.com.fiap.aguiabranca.domain.user.User;
import br.com.fiap.aguiabranca.domain.user.UserRepository;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

final class MongoRepositories {

    private MongoRepositories() {
    }

    @Repository
    @Profile("!integration & !openapi")
    static class Users implements UserRepository {
        private final MongoOperations mongo;
        private final MongoSequenceGenerator sequences;

        Users(MongoOperations mongo, MongoSequenceGenerator sequences) {
            this.mongo = mongo;
            this.sequences = sequences;
        }

        public User save(User user) {
            user.assignId(sequences.next("users"));
            return mongo.save(user);
        }

        public Optional<User> findById(Long id) {
            return Optional.ofNullable(mongo.findById(id, User.class));
        }

        public Optional<User> findByEmail(String email) {
            return Optional.ofNullable(mongo.findOne(query(where("email").is(email)), User.class));
        }

        public void deleteAll() {
            mongo.remove(new Query(), User.class);
        }
    }

    @Repository
    @Profile("!integration & !openapi")
    static class Ideas implements IdeaRepository {
        private final MongoOperations mongo;
        private final MongoSequenceGenerator sequences;

        Ideas(MongoOperations mongo, MongoSequenceGenerator sequences) {
            this.mongo = mongo;
            this.sequences = sequences;
        }

        public Idea save(Idea idea) {
            idea.assignId(sequences.next("ideas"));
            return mongo.save(idea);
        }

        public Optional<Idea> findById(Long id) {
            return Optional.ofNullable(mongo.findById(id, Idea.class));
        }

        public List<Idea> findAllByStatusOrderByIdDesc(Idea.Status status) {
            return mongo.find(query(where("status").is(status)).with(Sort.by(DESC, "_id")), Idea.class);
        }

        public List<Idea> findAllByOwnerIdOrderByIdDesc(Long ownerId) {
            return mongo.find(query(where("ownerId").is(ownerId)).with(Sort.by(DESC, "_id")), Idea.class);
        }

        public List<Idea> findAllByOwnerIdAndStatusOrderByIdDesc(Long ownerId, Idea.Status status) {
            return mongo.find(query(where("ownerId").is(ownerId).and("status").is(status)).with(Sort.by(DESC, "_id")),
                    Idea.class);
        }

        public List<Idea> findAllByOrderByIdDesc() {
            return mongo.find(new Query().with(Sort.by(DESC, "_id")), Idea.class);
        }

        public Optional<Idea> findByIdAndOwnerId(Long id, Long ownerId) {
            return Optional.ofNullable(mongo.findOne(query(where("_id").is(id).and("ownerId").is(ownerId)),
                    Idea.class));
        }

        public void deleteAll() {
            mongo.remove(new Query(), Idea.class);
        }
    }

    @Repository
    @Profile("!integration & !openapi")
    static class Projects implements ProjectRepository {
        private final MongoOperations mongo;
        private final MongoSequenceGenerator sequences;

        Projects(MongoOperations mongo, MongoSequenceGenerator sequences) {
            this.mongo = mongo;
            this.sequences = sequences;
        }

        public Project save(Project project) {
            project.assignId(sequences.next("projects"));
            return mongo.save(project);
        }

        public List<Project> saveAll(List<Project> projects) {
            return projects.stream().map(this::save).toList();
        }

        public Optional<Project> findById(Long id) {
            return Optional.ofNullable(mongo.findById(id, Project.class));
        }

        public boolean existsById(Long id) {
            return findById(id).isPresent();
        }

        public ProjectSummaryDto summarize() {
            List<Project> all = mongo.findAll(Project.class);
            long count = all.size();
            double avg = all.stream().mapToInt(Project::getProgress).average().orElse(0);
            BigDecimal budget = all.stream().map(Project::getBudget).reduce(BigDecimal.ZERO, BigDecimal::add);
            return new ProjectSummaryDto(count, avg, budget);
        }

        public List<ProjectStatusCount> countByStatusGrouped() {
            return mongo.findAll(Project.class).stream()
                    .collect(java.util.stream.Collectors.groupingBy(Project::getStatus, java.util.stream.Collectors.counting()))
                    .entrySet().stream()
                    .sorted(java.util.Map.Entry.comparingByKey())
                    .map(entry -> new ProjectStatusCount(entry.getKey(), entry.getValue()))
                    .toList();
        }

        public boolean existsByIdeaId(Long ideaId) {
            return mongo.exists(query(where("ideaId").is(ideaId)), Project.class);
        }

        public List<Project> findAllByOrderByIdDesc() {
            return mongo.find(new Query().with(Sort.by(DESC, "_id")), Project.class);
        }

        public void deleteAll() {
            mongo.remove(new Query(), Project.class);
        }
    }

    @Repository
    @Profile("!integration & !openapi")
    static class ProjectHistory implements ProjectMetricsHistoryRepository {
        private final MongoOperations mongo;
        private final MongoSequenceGenerator sequences;

        ProjectHistory(MongoOperations mongo, MongoSequenceGenerator sequences) {
            this.mongo = mongo;
            this.sequences = sequences;
        }

        public ProjectMetricsHistory save(ProjectMetricsHistory entry) {
            entry.assignId(sequences.next("project_metrics_history"));
            return mongo.save(entry);
        }

        public List<ProjectMetricsHistory> saveAll(List<ProjectMetricsHistory> entries) {
            return entries.stream().map(this::save).toList();
        }

        public List<ProjectMetricsHistory> findAllByProjectIdOrderByChangedAtAscIdAsc(Long projectId) {
            return mongo.find(query(where("projectId").is(projectId)).with(Sort.by(ASC, "changedAt", "_id")),
                    ProjectMetricsHistory.class);
        }

        public long countByProjectId(Long projectId) {
            return mongo.count(query(where("projectId").is(projectId)), ProjectMetricsHistory.class);
        }

        public void deleteAll() {
            mongo.remove(new Query(), ProjectMetricsHistory.class);
        }
    }

    @Repository
    @Profile("!integration & !openapi")
    static class Strategies implements StrategyRepository {
        private final MongoOperations mongo;
        private final MongoSequenceGenerator sequences;

        Strategies(MongoOperations mongo, MongoSequenceGenerator sequences) {
            this.mongo = mongo;
            this.sequences = sequences;
        }

        public Strategy save(Strategy strategy) {
            strategy.assignId(sequences.next("strategies"));
            return mongo.save(strategy);
        }

        public Optional<Strategy> findById(Long id) {
            return Optional.ofNullable(mongo.findOne(query(where("_id").is(id).and("deletedAt").is(null)),
                    Strategy.class));
        }

        public List<Strategy> findAllByOrderByIdDesc() {
            return mongo.find(query(where("deletedAt").is(null)).with(Sort.by(DESC, "_id")), Strategy.class);
        }

        public void deleteAll() {
            mongo.remove(new Query(), Strategy.class);
        }
    }

    @Repository
    @Profile("!integration & !openapi")
    static class RefreshTokens implements RefreshTokenRepository {
        private final MongoOperations mongo;
        private final MongoSequenceGenerator sequences;

        RefreshTokens(MongoOperations mongo, MongoSequenceGenerator sequences) {
            this.mongo = mongo;
            this.sequences = sequences;
        }

        public RefreshToken save(RefreshToken refreshToken) {
            refreshToken.assignId(sequences.next("refresh_tokens"));
            return mongo.save(refreshToken);
        }

        public Optional<RefreshToken> findByTokenHashForUpdate(String hash) {
            return Optional.ofNullable(mongo.findOne(query(where("tokenHash").is(hash)), RefreshToken.class));
        }

        public List<RefreshToken> findByFamilyId(UUID familyId) {
            return mongo.find(query(where("familyId").is(familyId)), RefreshToken.class);
        }

        public void deleteAll() {
            mongo.remove(new Query(), RefreshToken.class);
        }
    }
}
