package br.com.fiap.aguiabranca.shared.persistence;

import com.mongodb.client.model.IndexOptions;
import java.util.List;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

/**
 * O que sobrou do Flyway.
 *
 * O Mongo nao tem DDL, entao nao ha migration para versionar: o que precisa existir sao os
 * indices e os validadores. Este runner cria os dois de forma idempotente a cada boot.
 *
 * Os validadores $jsonSchema sao a traducao dos CHECK da V1 (role, status, horizon, progresso
 * entre 0 e 100). Sem eles o banco aceitaria status "APROVADO" escrito errado e o erro so
 * apareceria na tela, dias depois. Os campos de dinheiro exigem bsonType decimal de proposito:
 * e o alarme se alguem quebrar a conversao e o valor voltar a ser gravado como texto.
 */
@Component
// Sem banco nenhum o contrato OpenAPI ainda e gerado (profile openapi); criar indice ali
// so faria o boot travar tentando conectar.
@Profile("!openapi")
// Antes de qualquer runner que escreva: o seed de dev depende do unico de email existir.
@Order(0)
public class MongoSchemaInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(MongoSchemaInitializer.class);

    private final MongoTemplate mongo;

    public MongoSchemaInitializer(MongoTemplate mongo) {
        this.mongo = mongo;
    }

    @Override
    public void run(ApplicationArguments args) {
        users();
        ideas();
        projects();
        metricsHistory();
        strategies();
        refreshTokens();
        log.info("Indices e validadores do MongoDB aplicados");
    }

    private void users() {
        validate("users", schema(
                List.of("name", "email", "passwordHash", "role", "createdAt"),
                new Document("role", enumOf("OPERADOR", "GESTOR", "LIDERANCA"))));
        index("users", new Document("email", 1), unique("idx_users_email"));
    }

    private void ideas() {
        validate("ideas", schema(
                List.of("title", "description", "status", "ownerId", "createdAt"),
                new Document("status", enumOf("DRAFT", "IN_REVIEW", "APPROVED", "REJECTED"))));
        // A listagem filtra por status e o OPERADOR so enxerga as proprias ideias: os dois
        // caminhos de leitura da tela inicial, como na V1.
        index("ideas", new Document("status", 1), named("idx_ideas_status"));
        index("ideas", new Document("ownerId", 1), named("idx_ideas_owner"));
    }

    private void projects() {
        validate("projects", schema(
                List.of("name", "status", "progress", "budget", "spent", "createdAt"),
                new Document("status", enumOf("PLANNING", "IN_PROGRESS", "COMPLETED", "CANCELLED"))
                        .append("progress", new Document("bsonType", "int").append("minimum", 0).append("maximum", 100))
                        .append("budget", new Document("bsonType", "decimal"))
                        .append("spent", new Document("bsonType", "decimal"))));
        index("projects", new Document("status", 1), named("idx_projects_status"));
        // O UNIQUE que impede promover a mesma ideia duas vezes sob corrida. O filtro parcial
        // por $type e o que substitui o "UNIQUE aceita varios NULL" do Postgres: projeto sem
        // ideia tem ideaId ausente ou nulo e simplesmente nao entra no indice.
        index("projects", new Document("ideaId", 1),
                unique("idx_projects_idea").partialFilterExpression(
                        new Document("ideaId", new Document("$type", "long"))));
    }

    private void metricsHistory() {
        validate("project_metrics_history", schema(
                List.of("projectId", "metric", "newValue", "changedById", "changedAt"),
                new Document("metric", enumOf("PROGRESS", "SPENT"))
                        .append("newValue", new Document("bsonType", "decimal"))
                        .append("oldValue", new Document("bsonType", List.of("decimal", "null")))));
        // A auditoria e sempre lida como "historico deste projeto em ordem de tempo".
        index("project_metrics_history", new Document("projectId", 1).append("changedAt", 1),
                named("idx_metrics_history_project"));
    }

    private void strategies() {
        validate("strategies", schema(
                List.of("title", "description", "horizon", "createdAt"),
                new Document("horizon", enumOf("SHORT", "MEDIUM", "LONG"))));
        // Soft delete: toda leitura filtra deletedAt nulo.
        index("strategies", new Document("deletedAt", 1), named("idx_strategies_deleted"));
    }

    private void refreshTokens() {
        validate("refresh_tokens", schema(
                List.of("tokenHash", "userId", "familyId", "expiresAt", "createdAt"),
                new Document()));
        index("refresh_tokens", new Document("tokenHash", 1), unique("idx_refresh_token_hash"));
        index("refresh_tokens", new Document("familyId", 1), named("idx_refresh_family"));
    }

    private Document schema(List<String> required, Document properties) {
        Document jsonSchema = new Document("bsonType", "object").append("required", required);
        if (!properties.isEmpty()) {
            jsonSchema.append("properties", properties);
        }
        return new Document("$jsonSchema", jsonSchema);
    }

    private Document enumOf(String... values) {
        return new Document("enum", List.of(values));
    }

    private void validate(String collection, Document validator) {
        if (!mongo.collectionExists(collection)) {
            mongo.createCollection(collection);
        }
        mongo.getDb().runCommand(new Document("collMod", collection)
                .append("validator", validator)
                .append("validationLevel", "strict")
                .append("validationAction", "error"));
    }

    private void index(String collection, Document keys, IndexOptions options) {
        mongo.getDb().getCollection(collection).createIndex(keys, options);
    }

    private IndexOptions named(String name) {
        return new IndexOptions().name(name);
    }

    private IndexOptions unique(String name) {
        return named(name).unique(true);
    }
}
