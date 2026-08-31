-- Massa realista para medir summarize() e countByStatusGrouped() (#14).
-- idea_id fica NULL de proposito: o dashboard agrega a tabela inteira, nao o join com ideas.

INSERT INTO projects (name, status, progress, budget, spent, created_at)
SELECT
    'Projeto carga ' || g,
    (ARRAY['PLANNING', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'])[1 + (g % 4)],
    g % 101,
    ((g % 500) * 1000)::numeric(15, 2),
    0,
    now()
FROM generate_series(1, 100000) AS g;
