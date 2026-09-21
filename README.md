# FIAP-AGUIA-BRANCA - Hub de Inovacao

Backend do Hub de Inovacao e Gestao de Projetos Corporativos. A API substitui os mocks hoje
consumidos pelo app Android por persistencia real em MongoDB.

Status atual: as fatias `auth`, `idea`, `project` e `strategy` tem controller, service, repository
e DTOs. O backlog fica nas [issues](../../issues) e no
[board](https://github.com/users/AlexCajeFelix/projects/4).

## Rodando Local

Suba o MongoDB:

```bash
docker compose up -d db
```

Rode a aplicacao em modo dev:

```bash
SPRING_PROFILES_ACTIVE=dev ./mvnw spring-boot:run
```

No Windows PowerShell:

```powershell
$env:SPRING_PROFILES_ACTIVE = "dev"
.\mvnw.cmd spring-boot:run
```

O profile `dev` cria dados de exemplo automaticamente se ainda nao existirem.

| E-mail | Senha | Perfil |
|---|---|---|
| `operador@aguiabranca.dev` | `operador123` | `OPERADOR` |
| `gestor@aguiabranca.dev` | `gestor123` | `GESTOR` |
| `lideranca@aguiabranca.dev` | `lideranca123` | `LIDERANCA` |

```bash
curl -s localhost:8081/auth/login -H 'Content-Type: application/json' \
  -d '{"email":"gestor@aguiabranca.dev","password":"gestor123"}'
```

## Configuracao

| Variavel | Exemplo |
|---|---|
| `MONGODB_URI` | `mongodb://localhost:27017/aguiabranca` |
| `JWT_SECRET` | segredo com pelo menos 32 bytes |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:3000` |

A aplicacao nao sobe fora de `dev` se `JWT_SECRET` estiver vazio, curto demais ou igual ao segredo
publico de desenvolvimento.

## Validacao

```bash
./mvnw -q verify
```

No Windows:

```powershell
.\mvnw.cmd -q verify
```

Os testes de integracao usam repositorios em memoria no profile `integration`, entao o build local
nao depende de Docker. A aplicacao real usa MongoDB.

## Rotas

| Metodo | Rota | Quem pode |
|---|---|---|
| `GET` | `/v3/api-docs` | publico |
| `GET` | `/swagger-ui.html`, `/swagger-ui/**` | publico |
| `POST` | `/auth/login` | publico |
| `POST` | `/auth/refresh` | publico |
| `POST` | `/auth/logout` | publico |
| `POST` | `/ideas` | qualquer autenticado |
| `GET` | `/ideas?status=` | autenticado; `OPERADOR` so ve as proprias |
| `GET` | `/ideas/{id}` | autenticado; ideia alheia responde 404 para `OPERADOR` |
| `POST` | `/ideas/{id}/approval` | `GESTOR`, `LIDERANCA` |
| `GET` | `/projects` | qualquer autenticado |
| `GET` | `/projects/summary` | qualquer autenticado |
| `GET` | `/projects/{id}` | qualquer autenticado |
| `GET` | `/projects/{id}/metrics-history` | qualquer autenticado |
| `POST` | `/projects/from-idea/{ideaId}` | `GESTOR`, `LIDERANCA` |
| `PATCH` | `/projects/{id}/metrics` | `GESTOR`, `LIDERANCA` |
| `GET` | `/strategies`, `/strategies/{id}` | qualquer autenticado |
| `POST` `PUT` `DELETE` | `/strategies` | `GESTOR`, `LIDERANCA` |

Erros seguem RFC 7807 (`ProblemDetail`). O campo estavel para o cliente decidir comportamento e o
`type`, nao o `title`.

## Arquitetura

- Fatias verticais por dominio: `auth`, `idea`, `project`, `strategy`.
- Regras de negocio nas entidades, como `Idea.review()` e `Project.updateProgress()`.
- RBAC via `@PreAuthorize` com `OPERADOR`, `GESTOR` e `LIDERANCA`.
- `AuthenticatedUser` como principal, com role carregada no JWT.
- Refresh token opaco, rotacionado e revogavel.
- Auditoria financeira em `project_metrics_history`.
- Repositorios de dominio desacoplados da tecnologia de persistencia.
- MongoDB em execucao normal; repositorios em memoria para `integration` e `openapi`.

## Board E Fluxo

A `main` e protegida: todo codigo entra por PR, com historico linear e check `build` verde.

```bash
git checkout main
git pull
git checkout -b feat/minha-task
./mvnw -q verify
git push -u origin feat/minha-task
gh pr create --fill
```

Use Conventional Commits:

```text
feat(auth): adiciona refresh token rotacionado
fix(project): corrige agregacao do dashboard
chore(ci): ajusta workflow de build
test(strategy): cobre soft delete
docs(readme): atualiza setup local
```

Board:

```text
https://github.com/users/AlexCajeFelix/projects/4
```
