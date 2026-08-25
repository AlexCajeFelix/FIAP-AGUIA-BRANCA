-- Massa de desenvolvimento: um usuario por perfil e dados de exemplo para as telas.
--
-- Vive em db/seed, fora do location de producao. So o profile dev inclui esta pasta, entao
-- em prod estas linhas nao existem e a versao nao aparece no flyway_schema_history.
--
-- A versao e 9000, nao 2, de proposito: o Flyway funde os dois locations numa sequencia unica
-- de versoes. Com o seed em V2, a proxima migration real de producao tambem seria V2 e os dois
-- ambientes divergiriam no mesmo numero. Numero alto mantem as duas faixas separadas.
--
-- Senhas: operador123 / gestor123 / lideranca123 (BCrypt custo 10).

INSERT INTO users (name, email, password_hash, role) VALUES
    ('Operador Dev',  'operador@aguiabranca.dev',  '$2a$10$pkEmGv/Da/3o9moj4JeQ5OZtm3Ctks2wCLsdcncBFZVHNMo5E9AOq', 'OPERADOR'),
    ('Operadora Dev', 'operadora@aguiabranca.dev', '$2a$10$pkEmGv/Da/3o9moj4JeQ5OZtm3Ctks2wCLsdcncBFZVHNMo5E9AOq', 'OPERADOR'),
    ('Gestor Dev',    'gestor@aguiabranca.dev',    '$2a$10$6heaHXMXLtD8PxBrF4sNeeOgvYKOn2gSUh.DfDoadMLOmXeFIz1Du', 'GESTOR'),
    ('Lideranca Dev', 'lideranca@aguiabranca.dev', '$2a$10$M.ZV/ZbIR8qecgGk8nNYhOBgX0qArczlOfjaN3GTRh4qOY02neiHW', 'LIDERANCA');

INSERT INTO ideas (title, description, status, owner_id) VALUES
    ('Rastreamento de frota em tempo real',
     'Telemetria embarcada para acompanhar posicao e consumo da frota durante a rota.',
     'APPROVED',
     (SELECT id FROM users WHERE email = 'operador@aguiabranca.dev')),
    ('Portal de autoatendimento do cliente',
     'Consulta de coletas, segunda via de nota e abertura de ocorrencia sem passar pelo SAC.',
     'IN_REVIEW',
     (SELECT id FROM users WHERE email = 'operador@aguiabranca.dev')),
    ('Roteirizacao dinamica de entregas',
     'Recalcular rota conforme transito e janela de entrega, em vez de rota fixa do dia anterior.',
     'DRAFT',
     (SELECT id FROM users WHERE email = 'operadora@aguiabranca.dev'));

INSERT INTO projects (name, status, progress, budget, spent, idea_id) VALUES
    ('Rastreamento de frota em tempo real', 'IN_PROGRESS', 35, 850000.00, 297500.00,
     (SELECT id FROM ideas WHERE title = 'Rastreamento de frota em tempo real')),
    ('Modernizacao do centro de distribuicao', 'PLANNING', 0, 1200000.00, 0.00, NULL),
    ('Digitalizacao de canhotos', 'COMPLETED', 100, 180000.00, 172400.00, NULL);

INSERT INTO strategies (title, description, horizon) VALUES
    ('Reduzir custo por quilometro rodado',
     'Meta de queda de 8% no custo por km ate o fim do ciclo, via telemetria e roteirizacao.',
     'MEDIUM'),
    ('Elevar indice de entrega no prazo',
     'Chegar a 97% de entregas dentro da janela combinada com o cliente.',
     'SHORT'),
    ('Eletrificar a frota urbana',
     'Substituir gradualmente a frota leve urbana por veiculos eletricos.',
     'LONG');
