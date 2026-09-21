package br.com.fiap.aguiabranca.support;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.MongoDBContainer;

/**
 * O MongoDB dos testes: um container por JVM, nao um por classe.
 *
 * Com @Container em cada classe a suite sobe e derruba um Mongo por vez e passa a levar
 * minutos; o Ryuk do Testcontainers remove este ao fim do processo.
 *
 * Nao ha fallback embarcado. O antecessor deste arquivo caia para H2 quando o Docker faltava,
 * e o efeito pratico era build verde sem banco nenhum — o mesmo problema que o @EnabledIf
 * causava no teste de Testcontainers. Sem Docker, aqui, a suite falha dizendo o porque.
 */
public final class MongoContainerSupport {

    /** O MongoDBContainer ja sobe em replica set de um no, entao transacao funciona no teste. */
    public static final MongoDBContainer MONGO = new MongoDBContainer("mongo:7");

    private static final String DATABASE = "aguiabranca_test";

    static {
        MONGO.start();
    }

    private MongoContainerSupport() {
    }

    public static void registerProperties(DynamicPropertyRegistry registry) {
        registerProperties(registry, DATABASE);
    }

    /**
     * Banco proprio para quem nao pode dividir dados com o resto da suite — o teste do seed
     * de desenvolvimento, por exemplo, so roda se a colecao de usuarios estiver vazia.
     */
    public static void registerProperties(DynamicPropertyRegistry registry, String database) {
        registry.add("spring.data.mongodb.uri", () -> MONGO.getConnectionString() + "/" + database);
    }
}
