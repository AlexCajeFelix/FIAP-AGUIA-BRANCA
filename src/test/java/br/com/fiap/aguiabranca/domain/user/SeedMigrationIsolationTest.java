package br.com.fiap.aguiabranca.domain.user;

import br.com.fiap.aguiabranca.support.IntegrationTestSupport;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Prova que o seed nao alcanca producao.
 *
 * Roda o Flyway pela API Java em um banco novo por caso, em vez de subir o contexto do Spring:
 * o que se afirma aqui e sobre a configuracao do Flyway, e dois contextos com locations
 * diferentes brigando pelo mesmo banco dariam um teste que depende da ordem de execucao.
 */
class SeedMigrationIsolationTest {

    private static final String PRODUCTION_LOCATIONS = "classpath:db/migration";
    private static final String DEVELOPMENT_LOCATIONS = "classpath:db/migration,classpath:db/seed";

    @Test
    @DisplayName("Locations de producao nao criam nenhuma linha do seed")
    void productionLocationsShouldNotApplySeed() throws SQLException {
        String url = freshDatabase("prod_sem_seed");
        migrate(url, PRODUCTION_LOCATIONS);

        assertThat(count(url, "SELECT count(*) FROM users")).isZero();
        assertThat(count(url, "SELECT count(*) FROM ideas")).isZero();
        assertThat(count(url, "SELECT count(*) FROM projects")).isZero();
        assertThat(count(url, "SELECT count(*) FROM strategies")).isZero();
    }

    @Test
    @DisplayName("flyway_schema_history de producao nao contem a versao do seed")
    void productionHistoryShouldNotContainSeedVersion() throws SQLException {
        String url = freshDatabase("prod_historico");
        migrate(url, PRODUCTION_LOCATIONS);

        assertThat(count(url, "SELECT count(*) FROM flyway_schema_history WHERE version = '9000'"))
                .isZero();
        // A V1 continua registrada: o schema real e o mesmo dos dois lados.
        assertThat(count(url, "SELECT count(*) FROM flyway_schema_history WHERE version = '1'"))
                .isOne();
    }

    @Test
    @DisplayName("Locations de desenvolvimento continuam criando o seed")
    void developmentLocationsShouldApplySeed() throws SQLException {
        String url = freshDatabase("dev_com_seed");
        migrate(url, DEVELOPMENT_LOCATIONS);

        assertThat(count(url, "SELECT count(*) FROM users")).isEqualTo(4);
        assertThat(count(url, "SELECT count(*) FROM users WHERE role = 'LIDERANCA'")).isOne();
        assertThat(count(url, "SELECT count(*) FROM flyway_schema_history WHERE version = '9000'"))
                .isOne();
    }

    @Test
    @DisplayName("O schema aplicado e o mesmo nos dois ambientes — so o conteudo difere")
    void schemaShouldBeIdenticalAcrossEnvironments() throws SQLException {
        String production = freshDatabase("comparacao_prod");
        String development = freshDatabase("comparacao_dev");
        migrate(production, PRODUCTION_LOCATIONS);
        migrate(development, DEVELOPMENT_LOCATIONS);

        // Divergencia de schema entre ambientes e o que faz "funciona em dev" virar erro no deploy.
        assertThat(tableNames(development)).isEqualTo(tableNames(production));
    }

    private static void migrate(String url, String locations) {
        Flyway.configure()
                .dataSource(url, IntegrationTestSupport.POSTGRES.getUsername(),
                        IntegrationTestSupport.POSTGRES.getPassword())
                .locations(locations.split(","))
                .load()
                .migrate();
    }

    /** CREATE DATABASE nao roda dentro de transacao — dai o Statement direto no autocommit. */
    private static String freshDatabase(String name) throws SQLException {
        var container = IntegrationTestSupport.POSTGRES;
        try (Connection connection = DriverManager.getConnection(container.getJdbcUrl(),
                container.getUsername(), container.getPassword());
                Statement statement = connection.createStatement()) {
            statement.execute("DROP DATABASE IF EXISTS " + name);
            statement.execute("CREATE DATABASE " + name);
        }
        return "jdbc:postgresql://" + container.getHost() + ":" + container.getFirstMappedPort() + "/" + name;
    }

    private static long count(String url, String sql) throws SQLException {
        var container = IntegrationTestSupport.POSTGRES;
        try (Connection connection = DriverManager.getConnection(url, container.getUsername(),
                container.getPassword());
                Statement statement = connection.createStatement();
                ResultSet rs = statement.executeQuery(sql)) {
            rs.next();
            return rs.getLong(1);
        }
    }

    private static String tableNames(String url) throws SQLException {
        var container = IntegrationTestSupport.POSTGRES;
        StringBuilder names = new StringBuilder();
        try (Connection connection = DriverManager.getConnection(url, container.getUsername(),
                container.getPassword());
                Statement statement = connection.createStatement();
                ResultSet rs = statement.executeQuery("""
                        SELECT table_name FROM information_schema.tables
                        WHERE table_schema = 'public' AND table_name <> 'flyway_schema_history'
                        ORDER BY table_name
                        """)) {
            while (rs.next()) {
                names.append(rs.getString(1)).append(',');
            }
        }
        return names.toString();
    }
}
