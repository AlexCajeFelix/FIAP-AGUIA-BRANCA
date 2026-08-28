-- Schema inicial do Hub de Inovacao.
-- Todo dinheiro e NUMERIC(15,2): ponto flutuante em valor financeiro acumula erro de
-- arredondamento que aparece como centavo perdido no fechamento.

CREATE TABLE users (
    id            BIGSERIAL PRIMARY KEY,
    name          VARCHAR(120) NOT NULL,
    email         VARCHAR(180) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    role          VARCHAR(20)  NOT NULL,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT ck_users_role CHECK (role IN ('OPERADOR', 'GESTOR', 'LIDERANCA'))
);

CREATE TABLE ideas (
    id             BIGSERIAL PRIMARY KEY,
    title          VARCHAR(150) NOT NULL,
    description    TEXT         NOT NULL,
    status         VARCHAR(20)  NOT NULL,
    owner_id       BIGINT       NOT NULL REFERENCES users (id),
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    reviewed_by_id BIGINT       REFERENCES users (id),
    reviewed_at    TIMESTAMPTZ,
    CONSTRAINT ck_ideas_status CHECK (status IN ('DRAFT', 'IN_REVIEW', 'APPROVED', 'REJECTED'))
);

-- A listagem do app filtra por status e o OPERADOR so enxerga as proprias ideias:
-- os dois indices cobrem os dois caminhos de leitura da tela inicial.
CREATE INDEX idx_ideas_status ON ideas (status);
CREATE INDEX idx_ideas_owner ON ideas (owner_id);

CREATE TABLE projects (
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(150)   NOT NULL,
    status     VARCHAR(20)    NOT NULL,
    progress   INTEGER        NOT NULL DEFAULT 0,
    budget     NUMERIC(15, 2) NOT NULL,
    spent      NUMERIC(15, 2) NOT NULL DEFAULT 0,
    -- UNIQUE e o que impede promover a mesma ideia duas vezes mesmo sob corrida:
    -- a checagem no service perde para duas requisicoes simultaneas, a constraint nao.
    idea_id    BIGINT         UNIQUE REFERENCES ideas (id),
    created_at TIMESTAMPTZ    NOT NULL DEFAULT now(),
    CONSTRAINT ck_projects_status CHECK (status IN ('PLANNING', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED')),
    CONSTRAINT ck_projects_progress CHECK (progress BETWEEN 0 AND 100)
);

CREATE INDEX idx_projects_status ON projects (status);

CREATE TABLE project_metrics_history (
    id            BIGSERIAL PRIMARY KEY,
    project_id    BIGINT         NOT NULL REFERENCES projects (id),
    metric        VARCHAR(30)    NOT NULL,
    old_value     NUMERIC(15, 2),
    new_value     NUMERIC(15, 2) NOT NULL,
    changed_by_id BIGINT         NOT NULL REFERENCES users (id),
    changed_at    TIMESTAMPTZ    NOT NULL DEFAULT now(),
    CONSTRAINT ck_metrics_metric CHECK (metric IN ('PROGRESS', 'SPENT'))
);

-- A auditoria e sempre lida como "historico deste projeto em ordem de tempo".
CREATE INDEX idx_metrics_history_project ON project_metrics_history (project_id, changed_at);

CREATE TABLE strategies (
    id          BIGSERIAL PRIMARY KEY,
    title       VARCHAR(150) NOT NULL,
    description TEXT         NOT NULL,
    horizon     VARCHAR(20)  NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    -- Soft delete: deleted_at preenchido marca excluido, a linha continua na tabela.
    deleted_at  TIMESTAMPTZ,
    CONSTRAINT ck_strategies_horizon CHECK (horizon IN ('SHORT', 'MEDIUM', 'LONG'))
);

CREATE INDEX idx_strategies_active ON strategies (deleted_at) WHERE deleted_at IS NULL;
