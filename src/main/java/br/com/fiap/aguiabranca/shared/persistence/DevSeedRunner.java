package br.com.fiap.aguiabranca.shared.persistence;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Massa de desenvolvimento: um usuario por perfil e dados de exemplo para as telas.
 *
 * Substitui o V9000__seed_dev_data.sql. O @Profile("dev") faz o mesmo papel que o location
 * separado fazia no Flyway: fora de dev estas contas nao existem por construcao, e nao porque
 * alguem lembrou de apagar a linha depois.
 *
 * Senhas: operador123 / gestor123 / lideranca123 (BCrypt custo 10, os mesmos hashes da V9000 —
 * gerar de novo a cada boot mudaria o hash e nao adianta nada aqui).
 */
@Component
@Profile("dev")
// Depois do MongoSchemaInitializer: inserir antes dos indices existirem deixaria o unico de
// email para tras e aceitaria duplicata na primeira subida.
@Order(100)
public class DevSeedRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DevSeedRunner.class);

    private static final String OPERADOR_HASH = "$2a$10$pkEmGv/Da/3o9moj4JeQ5OZtm3Ctks2wCLsdcncBFZVHNMo5E9AOq";
    private static final String GESTOR_HASH = "$2a$10$6heaHXMXLtD8PxBrF4sNeeOgvYKOn2gSUh.DfDoadMLOmXeFIz1Du";
    private static final String LIDERANCA_HASH = "$2a$10$M.ZV/ZbIR8qecgGk8nNYhOBgX0qArczlOfjaN3GTRh4qOY02neiHW";

    private final UserRepository users;
    private final IdeaRepository ideas;
    private final ProjectRepository projects;
    private final StrategyRepository strategies;

    public DevSeedRunner(UserRepository users, IdeaRepository ideas, ProjectRepository projects,
            StrategyRepository strategies) {
        this.users = users;
        this.ideas = ideas;
        this.projects = projects;
        this.strategies = strategies;
    }

    @Override
    public void run(ApplicationArguments args) {
        // Idempotente: o banco de dev sobrevive a varios boots, e duplicar o seed encheria as
        // telas de copias.
        if (users.count() > 0) {
            return;
        }

        User operador = users.save(new User("Operador Dev", "operador@aguiabranca.dev", OPERADOR_HASH, Role.OPERADOR));
        User operadora = users.save(new User("Operadora Dev", "operadora@aguiabranca.dev", OPERADOR_HASH, Role.OPERADOR));
        users.save(new User("Gestor Dev", "gestor@aguiabranca.dev", GESTOR_HASH, Role.GESTOR));
        users.save(new User("Lideranca Dev", "lideranca@aguiabranca.dev", LIDERANCA_HASH, Role.LIDERANCA));

        Idea rastreamento = new Idea("Rastreamento de frota em tempo real",
                "Telemetria embarcada para acompanhar posicao e consumo da frota durante a rota.",
                operador.getId());
        rastreamento.review(Idea.Status.APPROVED, operador.getId());
        rastreamento = ideas.save(rastreamento);

        Idea portal = new Idea("Portal de autoatendimento do cliente",
                "Consulta de coletas, segunda via de nota e abertura de ocorrencia sem passar pelo SAC.",
                operador.getId());
        portal.review(Idea.Status.IN_REVIEW, operador.getId());
        ideas.save(portal);

        ideas.save(new Idea("Roteirizacao dinamica de entregas",
                "Recalcular rota conforme transito e janela de entrega, em vez de rota fixa do dia anterior.",
                operadora.getId()));

        Project rastreio = Project.fromIdea(rastreamento, new BigDecimal("850000.00"));
        rastreio.updateProgress(35);
        rastreio.updateSpent(new BigDecimal("297500.00"));
        projects.save(rastreio);

        projects.save(new Project("Modernizacao do centro de distribuicao", 0, new BigDecimal("1200000.00")));

        // O construtor termina em PLANNING mesmo recebendo progresso: quem move o status e o
        // updateProgress. A V9000 gravava COMPLETED direto no INSERT.
        Project canhotos = new Project("Digitalizacao de canhotos", 0, new BigDecimal("180000.00"));
        canhotos.updateProgress(100);
        canhotos.updateSpent(new BigDecimal("172400.00"));
        projects.save(canhotos);

        strategies.save(new Strategy("Reduzir custo por quilometro rodado",
                "Meta de queda de 8% no custo por km ate o fim do ciclo, via telemetria e roteirizacao.",
                Horizon.MEDIUM));
        strategies.save(new Strategy("Elevar indice de entrega no prazo",
                "Chegar a 97% de entregas dentro da janela combinada com o cliente.", Horizon.SHORT));
        strategies.save(new Strategy("Eletrificar a frota urbana",
                "Substituir gradualmente a frota leve urbana por veiculos eletricos.", Horizon.LONG));

        log.info("Seed de desenvolvimento aplicado: {} usuarios", users.count());
    }
}
