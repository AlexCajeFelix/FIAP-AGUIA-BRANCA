package br.com.fiap.aguiabranca.shared;

import br.com.fiap.aguiabranca.domain.auth.RefreshToken;
import br.com.fiap.aguiabranca.domain.auth.RefreshTokenRepository;
import br.com.fiap.aguiabranca.domain.idea.Idea;
import br.com.fiap.aguiabranca.domain.idea.IdeaRepository;
import br.com.fiap.aguiabranca.domain.project.Project;
import br.com.fiap.aguiabranca.domain.project.ProjectMetricsHistory;
import br.com.fiap.aguiabranca.domain.project.ProjectMetricsHistoryRepository;
import br.com.fiap.aguiabranca.domain.project.ProjectRepository;
import br.com.fiap.aguiabranca.domain.project.ProjectStatusCount;
import br.com.fiap.aguiabranca.domain.project.ProjectSummaryDto;
import br.com.fiap.aguiabranca.domain.strategy.Strategy;
import br.com.fiap.aguiabranca.domain.strategy.StrategyRepository;
import br.com.fiap.aguiabranca.domain.user.User;
import br.com.fiap.aguiabranca.domain.user.UserRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

final class InMemoryRepositories {

    private InMemoryRepositories() {
    }

    private static final class Store<T> {
        private final AtomicLong nextId = new AtomicLong(1);
        private final Map<Long, T> rows = new LinkedHashMap<>();

        synchronized long nextId() {
            return nextId.getAndIncrement();
        }

        synchronized T save(Long id, T value) {
            rows.put(id, value);
            return value;
        }

        synchronized Optional<T> get(Long id) {
            return Optional.ofNullable(rows.get(id));
        }

        synchronized List<T> all() {
            return new ArrayList<>(rows.values());
        }

        synchronized void clear() {
            rows.clear();
            nextId.set(1);
        }
    }

    @Repository
    @Profile({ "integration", "openapi" })
    static class Users implements UserRepository {
        private final Store<User> store = new Store<>();

        public User save(User user) {
            user.assignId(store.nextId());
            return store.save(user.getId(), user);
        }

        public Optional<User> findById(Long id) {
            return store.get(id);
        }

        public Optional<User> findByEmail(String email) {
            return store.all().stream().filter(user -> user.getEmail().equals(email)).findFirst();
        }

        public void deleteAll() {
            store.clear();
        }
    }

    @Repository
    @Profile({ "integration", "openapi" })
    static class Ideas implements IdeaRepository {
        private final Store<Idea> store = new Store<>();

        public Idea save(Idea idea) {
            idea.assignId(store.nextId());
            return store.save(idea.getId(), idea);
        }

        public Optional<Idea> findById(Long id) {
            return store.get(id);
        }

        public List<Idea> findAllByStatusOrderByIdDesc(Idea.Status status) {
            return ordered().stream().filter(idea -> idea.getStatus() == status).toList();
        }

        public List<Idea> findAllByOwnerIdOrderByIdDesc(Long ownerId) {
            return ordered().stream().filter(idea -> idea.isOwnedBy(ownerId)).toList();
        }

        public List<Idea> findAllByOwnerIdAndStatusOrderByIdDesc(Long ownerId, Idea.Status status) {
            return ordered().stream()
                    .filter(idea -> idea.isOwnedBy(ownerId) && idea.getStatus() == status)
                    .toList();
        }

        public List<Idea> findAllByOrderByIdDesc() {
            return ordered();
        }

        public Optional<Idea> findByIdAndOwnerId(Long id, Long ownerId) {
            return findById(id).filter(idea -> idea.isOwnedBy(ownerId));
        }

        public void deleteAll() {
            store.clear();
        }

        private List<Idea> ordered() {
            return store.all().stream().sorted(Comparator.comparing(Idea::getId).reversed()).toList();
        }
    }

    @Repository
    @Profile({ "integration", "openapi" })
    static class Projects implements ProjectRepository {
        private final Store<Project> store = new Store<>();

        public Project save(Project project) {
            project.assignId(store.nextId());
            return store.save(project.getId(), project);
        }

        public List<Project> saveAll(List<Project> projects) {
            return projects.stream().map(this::save).toList();
        }

        public Optional<Project> findById(Long id) {
            return store.get(id);
        }

        public boolean existsById(Long id) {
            return store.get(id).isPresent();
        }

        public ProjectSummaryDto summarize() {
            List<Project> all = store.all();
            long count = all.size();
            double avg = all.stream().mapToInt(Project::getProgress).average().orElse(0);
            BigDecimal budget = all.stream().map(Project::getBudget).reduce(BigDecimal.ZERO, BigDecimal::add);
            return new ProjectSummaryDto(count, avg, budget);
        }

        public List<ProjectStatusCount> countByStatusGrouped() {
            return store.all().stream()
                    .collect(java.util.stream.Collectors.groupingBy(Project::getStatus, java.util.stream.Collectors.counting()))
                    .entrySet().stream()
                    .sorted(java.util.Map.Entry.comparingByKey())
                    .map(entry -> new ProjectStatusCount(entry.getKey(), entry.getValue()))
                    .toList();
        }

        public boolean existsByIdeaId(Long ideaId) {
            return store.all().stream().anyMatch(project -> ideaId.equals(project.getIdeaId()));
        }

        public List<Project> findAllByOrderByIdDesc() {
            return store.all().stream().sorted(Comparator.comparing(Project::getId).reversed()).toList();
        }

        public void deleteAll() {
            store.clear();
        }
    }

    @Repository
    @Profile({ "integration", "openapi" })
    static class ProjectHistory implements ProjectMetricsHistoryRepository {
        private final Store<ProjectMetricsHistory> store = new Store<>();

        public ProjectMetricsHistory save(ProjectMetricsHistory entry) {
            entry.assignId(store.nextId());
            return store.save(entry.getId(), entry);
        }

        public List<ProjectMetricsHistory> saveAll(List<ProjectMetricsHistory> entries) {
            return entries.stream().map(this::save).toList();
        }

        public List<ProjectMetricsHistory> findAllByProjectIdOrderByChangedAtAscIdAsc(Long projectId) {
            return store.all().stream()
                    .filter(entry -> projectId.equals(entry.getProjectId()))
                    .sorted(Comparator.comparing(ProjectMetricsHistory::getChangedAt)
                            .thenComparing(ProjectMetricsHistory::getId))
                    .toList();
        }

        public long countByProjectId(Long projectId) {
            return store.all().stream().filter(entry -> projectId.equals(entry.getProjectId())).count();
        }

        public void deleteAll() {
            store.clear();
        }
    }

    @Repository
    @Profile({ "integration", "openapi" })
    static class Strategies implements StrategyRepository {
        private final Store<Strategy> store = new Store<>();

        public Strategy save(Strategy strategy) {
            strategy.assignId(store.nextId());
            return store.save(strategy.getId(), strategy);
        }

        public Optional<Strategy> findById(Long id) {
            return store.get(id).filter(strategy -> !strategy.isDeleted());
        }

        public List<Strategy> findAllByOrderByIdDesc() {
            return store.all().stream()
                    .filter(strategy -> !strategy.isDeleted())
                    .sorted(Comparator.comparing(Strategy::getId).reversed())
                    .toList();
        }

        public void deleteAll() {
            store.clear();
        }
    }

    @Repository
    @Profile({ "integration", "openapi" })
    static class RefreshTokens implements RefreshTokenRepository {
        private final Store<RefreshToken> store = new Store<>();

        public RefreshToken save(RefreshToken refreshToken) {
            refreshToken.assignId(store.nextId());
            return store.save(refreshToken.getId(), refreshToken);
        }

        public Optional<RefreshToken> findByTokenHashForUpdate(String hash) {
            return store.all().stream().filter(token -> token.getTokenHash().equals(hash)).findFirst();
        }

        public List<RefreshToken> findByFamilyId(UUID familyId) {
            return store.all().stream().filter(token -> token.getFamilyId().equals(familyId)).toList();
        }

        public void deleteAll() {
            store.clear();
        }
    }
}
