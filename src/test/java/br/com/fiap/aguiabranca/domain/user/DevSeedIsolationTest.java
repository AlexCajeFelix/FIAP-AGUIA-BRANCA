package br.com.fiap.aguiabranca.domain.user;

import br.com.fiap.aguiabranca.shared.persistence.DevSeedRunner;
import br.com.fiap.aguiabranca.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Prova que o seed nao alcanca producao.
 *
 * O que garantia isso no Flyway era o location separado (db/seed, incluido so pelo profile
 * dev). Agora quem garante e o @Profile("dev") do DevSeedRunner — entao o que se afirma aqui e
 * que, fora de dev, o componente nem existe no contexto. Nenhuma conta com senha conhecida
 * pode nascer por descuido de configuracao.
 */
class DevSeedIsolationTest extends IntegrationTestSupport {

    @Autowired
    private ApplicationContext context;

    @Test
    @DisplayName("Fora do profile dev o seed nem entra no contexto")
    void seedRunnerShouldNotExistOutsideDev() {
        assertThat(context.getBeanNamesForType(DevSeedRunner.class)).isEmpty();
    }

    @Test
    @DisplayName("Sem o seed, nenhuma conta de desenvolvimento existe")
    void developmentAccountsShouldNotExist() {
        assertThat(users.findByEmail("lideranca@aguiabranca.dev")).isEmpty();
        assertThat(users.findByEmail("gestor@aguiabranca.dev")).isEmpty();
    }
}
