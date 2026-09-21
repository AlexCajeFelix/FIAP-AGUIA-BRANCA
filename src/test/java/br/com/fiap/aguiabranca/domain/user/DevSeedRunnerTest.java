package br.com.fiap.aguiabranca.domain.user;

import br.com.fiap.aguiabranca.support.MongoContainerSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * O outro lado da moeda: em dev o seed continua criando as contas documentadas no README.
 *
 * Banco proprio porque o runner so age com a colecao de usuarios vazia — dividir o banco da
 * suite faria este teste depender da ordem de execucao das outras classes.
 */
@SpringBootTest
@ActiveProfiles("dev")
class DevSeedRunnerTest {

    @DynamicPropertySource
    static void mongoProperties(DynamicPropertyRegistry registry) {
        MongoContainerSupport.registerProperties(registry, "aguiabranca_seed_test");
    }

    @Autowired
    private UserRepository users;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("O profile dev cria um usuario por perfil, com as senhas do README")
    void devProfileShouldSeedOneUserPerRole() {
        assertThat(users.count()).isEqualTo(4);
        assertThat(users.findByEmail("operador@aguiabranca.dev")).get()
                .extracting(User::getRole).isEqualTo(Role.OPERADOR);
        assertThat(users.findByEmail("gestor@aguiabranca.dev")).get()
                .extracting(User::getRole).isEqualTo(Role.GESTOR);

        User lideranca = users.findByEmail("lideranca@aguiabranca.dev").orElseThrow();
        assertThat(lideranca.getRole()).isEqualTo(Role.LIDERANCA);
        // A senha do README precisa continuar valendo: hash trocado sem atualizar a doc
        // manda todo mundo para o "credenciais invalidas" sem pista nenhuma.
        assertThat(passwordEncoder.matches("lideranca123", lideranca.getPasswordHash())).isTrue();
    }
}
