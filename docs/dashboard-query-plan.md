# Plano das queries do dashboard (#14)

Massa: `src/test/resources/db/dashboard-load-100k.sql` (100_000 projetos, status
uniforme, sem `idea_id`). Rodado em PostgreSQL 16 via Testcontainers depois de
`ANALYZE projects`.

## Decisão

**Os índices da V1 bastam.** Não há migration nova.

As duas queries agregam a tabela inteira, sem `WHERE`. Seq Scan nesse caso é o
plano correto — índice não evita ler `progress`/`budget` de todas as linhas.
`idx_projects_status` só ajudaria se houvesse filtro por status. Tempo medido
fica bem abaixo dos 200 ms (≈18 ms e ≈21 ms). Tabela de resumo ou cache só
fariam sentido com volume ordens de grandeza maior.

O teste `DashboardQueryPlanTest` reexecuta a medição no CI.

## `summarize()` — `COUNT` / `AVG(progress)` / `SUM(budget)`

```
Aggregate  (cost=2886.01..2886.02 rows=1 width=72) (actual time=17.640..17.641 rows=1 loops=1)
  Buffers: shared hit=1136
  ->  Seq Scan on projects  (cost=0.00..2136.00 rows=100000 width=10) (actual time=0.005..6.026 rows=100000 loops=1)
        Buffers: shared hit=1136
Planning Time: 0.445 ms
Execution Time: 17.660 ms
```

## `countByStatusGrouped()` — `GROUP BY status`

```
Sort  (cost=2636.08..2636.09 rows=4 width=18) (actual time=21.252..21.253 rows=4 loops=1)
  Sort Key: status
  Sort Method: quicksort  Memory: 25kB
  Buffers: shared hit=1136
  ->  HashAggregate  (cost=2636.00..2636.04 rows=4 width=18) (actual time=21.232..21.234 rows=4 loops=1)
        Group Key: status
        Batches: 1  Memory Usage: 24kB
        Buffers: shared hit=1136
        ->  Seq Scan on projects  (cost=0.00..2136.00 rows=100000 width=10) (actual time=0.008..6.052 rows=100000 loops=1)
              Buffers: shared hit=1136
Planning Time: 0.115 ms
Execution Time: 21.314 ms
```
