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

O `verify` tambem roda o gate de cobertura. Ele reprova o build abaixo do minimo acordado:

| Metrica | Minimo | Onde muda |
|---|---:|---|
| Linha | 85% | `jacoco.min.line` no `pom.xml` |
| Ramo | 55% | `jacoco.min.branch` no `pom.xml` |

Se o build morrer assim, e o gate falando, nao e flake:

```text
[WARNING] Rule violated for bundle fiap-aguia-branca:
          lines covered ratio is 0.79, but expected minimum is 0.85
```

O relatorio fica em `target/site/jacoco/index.html`; abra e procure as linhas vermelhas do que voce
mexeu. Na CI ele sai como artefato `cobertura-jacoco`, e o percentual aparece no resumo do run sem
precisar baixar nada.

Os dois numeros foram fixados um pouco abaixo do medido, para pegar regressao sem obrigar cada PR a
subir a barra. Subiu a cobertura de verdade? Suba o minimo junto, no mesmo PR: e uma linha.

Fora da contagem ficam a classe `main`, `*Config`, `*Properties` e os DTOs: nenhum tem ramo de
decisao, e incluir so infla o percentual sem dizer nada sobre risco.

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

A `main` e protegida: todo codigo entra por PR, com historico linear e check `build` verde. As
regras completas estao em [Regras da main](#regras-da-main).

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

## Regras da main

A `main` e protegida, e a regra vale para admin tambem, entao nao adianta ser o dono do repo.

| Regra | Efeito pratico |
|---|---|
| Push direto bloqueado | `git push origin main` e rejeitado com `GH006` |
| PR obrigatorio | Todo codigo entra por PR |
| CI verde obrigatoria | O check `build` precisa passar; o merge trava enquanto estiver vermelho |
| Branch atualizada | PR atras da `main` precisa de rebase/update antes do merge |
| 1 aprovacao | Ninguem merga o proprio PR sozinho, nem o dono do repo |
| Historico linear | Merge commit rejeitado; use `--squash` |
| Conversas resolvidas | Comentario pendente trava o merge |
| Force push bloqueado | Nao da para reescrever o historico |
| Delecao bloqueada | A `main` nao some por acidente |

Se voce tentar `git push origin main` e vir isso, esta tudo certo:

```text
remote: error: GH006: Protected branch update failed for refs/heads/main.
remote: - Changes must be made through a pull request.
```

Commitou na `main` local por engano? Leve o trabalho para uma branch:

```bash
git branch minha-branch      # salva o ponto atual
git reset --hard origin/main # limpa a main local
git checkout minha-branch
```

### Precisa de alguem para revisar

Com `enforce_admins` ligado e 1 aprovacao exigida, ninguem merga sozinho, inclusive quem e admin
do repositorio. Na pratica, todo PR precisa de outra pessoa do grupo clicando em Approve.

Isso e deliberado, nao um efeito colateral. As duas regras que faltavam foram ligadas junto porque
so fazem sentido em par: CI verde sem revisao aprova codigo que compila e nao presta; revisao sem
CI aprova codigo que nem builda.

`dismiss_stale_reviews` tambem esta ligado: se voce empurrar um commit novo depois de aprovado, a
aprovacao cai e precisa ser refeita. Evita que uma mudanca de ultima hora entre sem ninguem ver.

Travou porque esta sozinho e precisa mergear? A saida nao e desligar a protecao, e pedir o review
no grupo. Se for emergencia real, um admin desliga `enforce_admins` pelas configuracoes, merga e
religa na mesma sessao.

### Como saber o nome do check exigido

O check obrigatorio e o job `build` do workflow de CI. Renomear esse job quebra a protecao em
silencio: o GitHub passa a esperar um check que nunca mais chega, e todo PR fica travado. Se
precisar renomear, atualize a protecao junto:

```bash
gh api repos/{owner}/{repo}/branches/main/protection/required_status_checks \
  -X PATCH -f 'contexts[]=novo-nome'
```
