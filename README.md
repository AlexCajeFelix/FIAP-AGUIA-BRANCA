# FIAP-AGUIA-BRANCA — Hub de Inovação

Backend do **Hub de Inovação e Gestão de Projetos Corporativos**: API que substitui os mocks
hoje consumidos pelo app Android via `ApiRepository` (npoint.io + fallback hardcoded) por
persistência real.

> **Status:** repositório em fundação. O código da aplicação ainda não foi commitado —
> o backlog de implementação está nas [issues](../../issues) e no
> [board](../../projects).

## Stack alvo

| Camada | Escolha |
|---|---|
| Runtime | Java 21 |
| Framework | Spring Boot 3.3.5 |
| Banco | PostgreSQL + Flyway |
| Auth | JWT HS256 (jjwt 0.12) |
| Erros | RFC 7807 (`ProblemDetail`) |
| Testes | JUnit 5 + Testcontainers |
| App cliente | Android (Kotlin) |

## Arquitetura pretendida

- **Fatias verticais** por domínio: `auth`, `idea`, `project`, `strategy`.
  Cada fatia carrega controller, service, repository, entidades e DTOs próprios.
- **Regras de negócio nas entidades**, não nos services (`Idea.review()`,
  `Project.updateProgress()`).
- **RBAC via `@PreAuthorize`** com três perfis: `OPERADOR`, `GESTOR`, `LIDERANCA`.
- **`AuthenticatedUser` como principal**, com a role carregada como claim do JWT.
- **Auditoria financeira** em `project_metrics_history` — todo `PATCH` de métrica grava snapshot.
- **Erros padronizados** em RFC 7807 por um `GlobalExceptionHandler` central.

## Convenções

### Branches

- `main` — protegida. Sem push direto, PR obrigatório, CI verde obrigatório.
- `feat/<slug>`, `fix/<slug>`, `chore/<slug>`, `docs/<slug>`, `test/<slug>`

### Commits

[Conventional Commits](https://www.conventionalcommits.org/):

```
feat(idea): adiciona endpoint de aprovacao
fix(project): corrige tipo do COUNT na constructor expression
chore(ci): adiciona cache de dependencias maven
```

### Labels

| Família | Valores |
|---|---|
| Tipo | `tipo:feat` `tipo:fix` `tipo:chore` `tipo:docs` `tipo:test` `tipo:seguranca` |
| Área | `area:backend` `area:android` `area:infra` `area:banco` |
| Prioridade | `p0` (bloqueia) `p1` (importante) `p2` (pode esperar) |
| Tamanho | `size:s` `size:m` `size:l` |

## Milestones

| Milestone | Objetivo |
|---|---|
| M1 — Repo e CI | Build reproduzível e pipeline verde |
| M2 — Endurecimento | Segurança e config prontas para ambiente real |
| M3 — Operação | Observabilidade, empacotamento, performance |
| M4 — Testes | Cobertura por fatia e matriz de RBAC |
| M5 — Integração Android | App sai do mock e passa a consumir a API |

## Ordem de ataque

As issues **#1 (Maven Wrapper)** e **#2 (build verde)** destravam todo o resto.
Nada de M2 em diante deve começar antes delas.
