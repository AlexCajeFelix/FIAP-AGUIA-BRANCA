package br.com.fiap.aguiabranca.shared;

import br.com.fiap.aguiabranca.domain.idea.Idea;
import br.com.fiap.aguiabranca.domain.idea.IdeaRepository;
import br.com.fiap.aguiabranca.domain.project.Project;
import br.com.fiap.aguiabranca.domain.project.ProjectRepository;
import br.com.fiap.aguiabranca.domain.strategy.Horizon;
import br.com.fiap.aguiabranca.domain.strategy.Strategy;
import br.com.fiap.aguiabranca.domain.strategy.StrategyRepository;
import br.com.fiap.aguiabranca.domain.user.Role;
import br.com.fiap.aguiabranca.domain.user.User;
import br.com.fiap.aguiabranca.domain.user.UserRepository;
import java.math.BigDecimal;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
public class DevDataInitializer implements CommandLineRunner {

    private final UserRepository users;
    private final IdeaRepository ideas;
    private final ProjectRepository projects;
    private final StrategyRepository strategies;

    public DevDataInitializer(UserRepository users, IdeaRepository ideas, ProjectRepository projects,
            StrategyRepository strategies) {
        this.users = users;
        this.ideas = ideas;
        this.projects = projects;
        this.strategies = strategies;
    }

    @Override
    public void run(String... args) {
        if (users.findByEmail("operador@aguiabranca.dev").isPresent()) {
            return;
        }

        User operador = users.save(new User("Operador Dev", "operador@aguiabranca.dev",
                "$2a$10$pkEmGv/Da/3o9moj4JeQ5OZtm3Ctks2wCLsdcncBFZVHNMo5E9AOq", Role.OPERADOR));
        User operadora = users.save(new User("Operadora Dev", "operadora@aguiabranca.dev",
                "$2a$10$pkEmGv/Da/3o9moj4JeQ5OZtm3Ctks2wCLsdcncBFZVHNMo5E9AOq", Role.OPERADOR));
        users.save(new User("Gestor Dev", "gestor@aguiabranca.dev",
                "$2a$10$6heaHXMXLtD8PxBrF4sNeeOgvYKOn2gSUh.DfDoadMLOmXeFIz1Du", Role.GESTOR));
        users.save(new User("Lideranca Dev", "lideranca@aguiabranca.dev",
                "$2a$10$M.ZV/ZbIR8qecgGk8nNYhOBgX0qArczlOfjaN3GTRh4qOY02neiHW", Role.LIDERANCA));

        Idea rastreamento = ideas.save(new Idea("Rastreamento de frota em tempo real",
                "Telemetria embarcada para acompanhar posicao e consumo da frota durante a rota.", operador));
        rastreamento.review(Idea.Status.APPROVED);
        ideas.save(rastreamento);
        ideas.save(new Idea("Portal de autoatendimento do cliente",
                "Consulta de coletas, segunda via de nota e abertura de ocorrencia sem passar pelo SAC.", operador));
        ideas.save(new Idea("Roteirizacao dinamica de entregas",
                "Recalcular rota conforme transito e janela de entrega, em vez de rota fixa do dia anterior.",
                operadora));

        Project project = Project.fromIdea(rastreamento, new BigDecimal("850000.00"));
        project.updateProgress(35);
        project.updateSpent(new BigDecimal("297500.00"));
        projects.save(project);
        projects.save(new Project("Modernizacao do centro de distribuicao", 0, new BigDecimal("1200000.00")));
        projects.save(new Project("Digitalizacao de canhotos", 100, new BigDecimal("180000.00")));

        strategies.save(new Strategy("Reduzir custo por quilometro rodado",
                "Meta de queda de 8% no custo por km ate o fim do ciclo, via telemetria e roteirizacao.",
                Horizon.MEDIUM));
        strategies.save(new Strategy("Elevar indice de entrega no prazo",
                "Chegar a 97% de entregas dentro da janela combinada com o cliente.", Horizon.SHORT));
        strategies.save(new Strategy("Eletrificar a frota urbana",
                "Substituir gradualmente a frota leve urbana por veiculos eletricos.", Horizon.LONG));
    }
}
