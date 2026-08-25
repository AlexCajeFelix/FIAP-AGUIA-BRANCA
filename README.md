# FIAP-AGUIA-BRANCA — Hub de Inovação

Backend do **Hub de Inovação e Gestão de Projetos Corporativos**: API que substitui os mocks
hoje consumidos pelo app Android via `ApiRepository` (npoint.io + fallback hardcoded) por
persistência real.

> **Status:** aplicação de pé. As quatro fatias (`auth`, `idea`, `project`, `strategy`) têm
> controller, service, repository e DTOs; o schema é versionado por Flyway. O backlog de
> endurecimento, operação e testes está nas [issues](../../issues) e no
> [board](https://github.com/users/AlexCajeFelix/projects/4).

## Rodando local

Precisa de **Docker** (o Postgres dos testes sobe por Testcontainers) e de um Postgres para a app:

```bash
docker run -d --name aguiabranca-db -p 5432:5432 \
  -e POSTGRES_DB=aguiabranca -e POSTGRES_USER=aguiabranca -e POSTGRES_PASSWORD=aguiabranca \
  postgres:16-alpine

SPRING_PROFILES_ACTIVE=dev ./mvnw spring-boot:run
```

### O segredo do JWT

A aplicação **não sobe** sem `JWT_SECRET`, e recusa segredo com menos de 32 bytes — HS256 assina
com bloco de 256 bits, e chave menor enfraquece a assinatura. Gere o seu:

```bash
openssl rand -base64 48
```

O profile `dev` traz um segredo pronto para não travar quem está começando. Ele é **público**:
está versionado em `application-dev.yml`, então qualquer pessoa que leia o repositório consegue
forjar um token de `LIDERANCA`. Por isso a aplicação recusa o boot se esse valor aparecer com
qualquer profile diferente de `dev`.

Fora de dev, a variável vem do ambiente — `.env.example` lista o que preencher.

O Flyway aplica `V1__initial_schema.sql` em qualquer ambiente. O seed de desenvolvimento vive em
`db/seed/` e **só entra com o profile `dev`** — em produção essas linhas não existem. Usuários
criados pelo seed, um por perfil:

| E-mail | Senha | Perfil |
|---|---|---|
| `operador@aguiabranca.dev` | `operador123` | `OPERADOR` |
| `gestor@aguiabranca.dev` | `gestor123` | `GESTOR` |
| `lideranca@aguiabranca.dev` | `lideranca123` | `LIDERANCA` |

```bash
curl -s localhost:8080/auth/login -H 'Content-Type: application/json' \
  -d '{"email":"gestor@aguiabranca.dev","password":"gestor123"}'
```

## Rotas

| Método | Rota | Quem pode |
|---|---|---|
| `POST` | `/auth/login` | público |
| `POST` | `/ideas` | qualquer autenticado |
| `GET` | `/ideas?status=` | autenticado — `OPERADOR` só vê as próprias |
| `GET` | `/ideas/{id}` | autenticado — ideia alheia responde 404 para `OPERADOR` |
| `POST` | `/ideas/{id}/approval` | `GESTOR`, `LIDERANCA` |
| `GET` | `/projects` | qualquer autenticado |
| `GET` | `/projects/summary` | qualquer autenticado |
| `GET` | `/projects/{id}` | qualquer autenticado |
| `GET` | `/projects/{id}/metrics-history` | qualquer autenticado |
| `POST` | `/projects/from-idea/{ideaId}` | `GESTOR`, `LIDERANCA` |
| `PATCH` | `/projects/{id}/metrics` | `GESTOR`, `LIDERANCA` |
| `GET` | `/strategies`, `/strategies/{id}` | qualquer autenticado |
| `POST` `PUT` `DELETE` | `/strategies` | `GESTOR`, `LIDERANCA` |

Erro sai em RFC 7807. O campo estável para o cliente decidir comportamento é o **`type`**, nunca
o `title` — que é texto livre e muda.

---

## Índice

- [Stack alvo](#stack-alvo)
- [Arquitetura pretendida](#arquitetura-pretendida)
- [Como trabalhar aqui](#como-trabalhar-aqui) ← **comece por aqui**
  - [O board](#o-board)
  - [Fluxo completo de uma task](#fluxo-completo-de-uma-task)
  - [Abrindo uma issue nova](#abrindo-uma-issue-nova)
  - [Labels](#labels)
  - [Campos do board](#campos-do-board)
  - [Branches e commits](#branches-e-commits)
  - [Regras da main](#regras-da-main)
- [Milestones](#milestones)
- [Colinha de comandos](#colinha-de-comandos)

---

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

---

# Como trabalhar aqui

## O board

**→ https://github.com/users/AlexCajeFelix/projects/4**

Cinco colunas. A regra que mantém o board honesto: **o card mora numa coluna só, e quem move é
quem está com ele na mão.**

| Coluna | O que significa | Quando mover para cá |
|---|---|---|
| **Backlog** | Existe, mas ainda não pode ou não deve começar | Nasce aqui |
| **Pronto** | Destravado, com critério de aceite claro, pode pegar | Quando as dependências fecharam |
| **Em andamento** | Alguém está tocando **agora** | Ao criar a branch |
| **Em revisão** | PR aberto esperando review | Ao abrir o PR |
| **Concluído** | Mergeado e validado | Merge do PR (automático, ver abaixo) |

**Limite de trabalho em paralelo:** no máximo **2 cards** em `Em andamento` por pessoa. Mais que
isso e nada termina — só existe coisa pela metade.

Se um card está em `Em andamento` e você parou de mexer nele, mova de volta para `Pronto`. Card
parado em andamento é a mentira mais comum de board de Kanban.

### Mover um card

**Pelo navegador (o jeito normal):** arraste o card entre as colunas.

**Pelo terminal**, se você já está no fluxo do `gh`:

```bash
# 1) achar o ID do item (o card) a partir do numero da issue
gh project item-list 4 --owner @me --format json \
  | jq -r '.items[] | select(.content.number == 15) | .id'

# 2) mover para "Em andamento"
gh project item-edit \
  --id <ITEM_ID_DO_PASSO_1> \
  --project-id PVT_kwHOC3YJlc4BgXof \
  --field-id PVTSSF_lAHOC3YJlc4BgXofzhakCuM \
  --single-select-option-id f5cb7ae8
```

Os IDs das colunas (nada disso é adivinhável, por isso está anotado aqui):

| Coluna | `--single-select-option-id` |
|---|---|
| Backlog | `d2215c36` |
| Pronto | `ee5638cc` |
| Em andamento | `f5cb7ae8` |
| Em revisão | `5183676e` |
| Concluído | `f0a034ae` |

> Se algum dia esses IDs não baterem, **não invente** — releia com
> `gh project field-list 4 --owner @me --format json`.

### Fechamento automático

O card vai sozinho para `Concluído` quando o PR que diz `Closes #N` é mergeado. É por isso que
o `Closes #` no template de PR não é enfeite: sem ele, você fecha a issue na mão e o board
desatualiza.

---

## Fluxo completo de uma task

Do "peguei" ao "acabou". Exemplo com a issue **#15**.

### 1. Pegue o card

Escolha algo em **`Pronto`** — não em `Backlog`. Se está no Backlog, ou tem dependência aberta ou
ninguém ainda decidiu que vale a pena.

```bash
gh issue view 15                      # leia o critério de aceite ANTES de codar
gh issue edit 15 --add-assignee @me
```

Mova o card para **`Em andamento`**.

### 2. Crie a branch

Sempre a partir da `main` atualizada:

```bash
git checkout main
git pull
git checkout -b test/integracao-ideias
```

### 3. Trabalhe, commitando pequeno

```bash
git add -A
git commit -m "test(idea): cobre submissao e listagem filtrada"
```

O critério de aceite da issue é a sua checklist. Vá marcando os checkboxes na própria issue
conforme fecha cada item — dá visibilidade sem ninguém precisar perguntar "como tá?".

### 4. Rode o build antes de abrir o PR

```bash
./mvnw -q verify
```

Abrir PR vermelho gasta o tempo de quem revisa. (Enquanto a issue #1 não fechar, esse comando
ainda não existe.)

### 5. Abra o PR

```bash
git push -u origin test/integracao-ideias
gh pr create --fill
```

Preencha o template — principalmente o **`Closes #15`** e o **como validar**. Mova o card para
**`Em revisão`**.

### 6. Review

Comentário pendente **bloqueia o merge** (a `main` exige conversas resolvidas). Resolva ou
responda cada um.

### 7. Merge

```bash
gh pr merge --squash --delete-branch
```

Use **squash**: a `main` exige histórico linear, então merge commit é rejeitado.

O card vai para `Concluído` sozinho por causa do `Closes #15`. Fim.

---

## Abrindo uma issue nova

**Pelo navegador:** *Issues → New issue* → escolha **Bug** ou **Feature**. Os templates já pedem
área, contexto e critério de aceite nos campos certos.

**Pelo terminal:**

```bash
gh issue create \
  --title "Cache de resposta do dashboard" \
  --label tipo:feat --label area:backend --label p2 --label size:m \
  --milestone "M3 — Operação"
```

Depois adicione ao board:

```bash
gh project item-add 4 --owner @me \
  --url https://github.com/AlexCajeFelix/FIAP-AGUIA-BRANCA/issues/28
```

### O que faz uma issue boa aqui

Toda issue segue três seções:

```markdown
## Contexto
Por que isso existe — 2-3 linhas, citando arquivos reais.

## Critério de aceite
- [ ] item verificável
- [ ] item verificável

## Notas técnicas
Armadilhas, arquivos a tocar, comandos.
```

**A regra do critério de aceite:** cada checkbox tem que ser verificável por alguém que não
escreveu o código.

| ❌ Não serve | ✅ Serve |
|---|---|
| "funcionar corretamente" | "`GET /ideas/{id}` de ideia alheia responde 404 para `OPERADOR`" |
| "estar seguro" | "app não sobe se `JWT_SECRET` tiver menos de 32 bytes" |
| "ter boa performance" | "query executa em menos de 200 ms com 100k projetos" |

Se você não consegue escrever como verificar, a issue ainda não está pronta para sair do Backlog.

---

## Labels

Quatro famílias. **Toda issue leva uma de cada** — é o que faz os filtros funcionarem.

| Família | Valores | Para quê |
|---|---|---|
| **Tipo** | `tipo:feat` `tipo:fix` `tipo:chore` `tipo:docs` `tipo:test` `tipo:seguranca` | Natureza do trabalho |
| **Área** | `area:backend` `area:android` `area:infra` `area:banco` | Onde encosta |
| **Prioridade** | `p0` `p1` `p2` | Ordem de ataque |
| **Tamanho** | `size:s` `size:m` `size:l` | Esforço estimado |

**Prioridade:**

- `p0` — **bloqueia outras frentes.** Alguém está parado por causa disso.
- `p1` — importante, entra no ciclo atual.
- `p2` — pode esperar sem prejuízo.

**Tamanho:**

- `size:s` — até meio dia
- `size:m` — 1 a 2 dias
- `size:l` — 3 dias ou mais → **considere quebrar em issues menores**

Filtrando:

```bash
gh issue list --label p0 --state open              # o que trava o time
gh issue list --label area:android                 # backlog do app
gh issue list --milestone "M1 — Repo e CI"
gh issue list --search "no:assignee label:p0"      # p0 sem dono
```

---

## Campos do board

Além do Status, cada card tem dois campos próprios:

| Campo | Tipo | Valores |
|---|---|---|
| **Área** | seleção | Backend, Android, Infra, Banco |
| **Estimativa** | número | pontos de esforço |

A **Estimativa** segue o tamanho, para o board somar por coluna:

| Label | Estimativa |
|---|---|
| `size:s` | 1 |
| `size:m` | 3 |
| `size:l` | 5 |

No board dá para agrupar por `Área` em vez de Status (**⋯ → Group by**) quando você quer olhar
só a frente Android, por exemplo.

---

## Branches e commits

### Branches

Sempre a partir da `main`, no formato `<tipo>/<slug-curto>`:

```
feat/refresh-token
fix/tipo-count-jpql
chore/maven-wrapper
docs/guia-contribuicao
test/matriz-rbac
```

Uma branch por issue. Branch de vida longa vira conflito de merge.

### Commits

[Conventional Commits](https://www.conventionalcommits.org/) — `<tipo>(<escopo>): <o que muda>`:

```
feat(idea): adiciona endpoint de aprovacao
fix(project): corrige tipo do COUNT na constructor expression
chore(ci): adiciona cache de dependencias maven
test(auth): cobre token expirado e assinatura invalida
docs(readme): documenta fluxo do board
```

Escopo é a fatia ou o módulo (`idea`, `project`, `auth`, `strategy`, `ci`, `android`).
Escreva no **imperativo** e descreva o efeito, não o arquivo: `corrige tipo do COUNT`, não
`altera ProjectRepository`.

---

## Regras da main

A `main` é protegida — **e a regra vale para admin também**, então não adianta ser o dono do repo.

| Regra | Efeito prático |
|---|---|
| Push direto bloqueado | `git push origin main` é rejeitado com `GH006` |
| PR obrigatório | Todo código entra por PR |
| Histórico linear | Merge commit rejeitado — **use `--squash`** |
| Conversas resolvidas | Comentário pendente trava o merge |
| Force push bloqueado | Não dá para reescrever o histórico |
| Deleção bloqueada | A `main` não some por acidente |

Se você tentar `git push origin main` e vir isso, **está tudo certo**:

```
remote: error: GH006: Protected branch update failed for refs/heads/main.
remote: - Changes must be made through a pull request.
```

Commitou na `main` local por engano? Leve o trabalho para uma branch:

```bash
git branch minha-branch      # salva o ponto atual
git reset --hard origin/main # limpa a main local
git checkout minha-branch
```

### Ainda não está ligado

Duas regras ficaram de fora de propósito, e a issue [#5](../../issues/5) existe para fechá-las:

- **CI verde obrigatório** — depende do workflow da [#3](../../issues/3) existir. Não dá para
  exigir um check que nunca rodou: isso travaria todo PR permanentemente.
- **Aprovação obrigatória** — hoje há um único colaborador com write, e o GitHub não deixa
  ninguém aprovar o próprio PR. Sobe para 1 assim que entrar a segunda pessoa.

---

## Milestones

| Milestone | Objetivo | Issues |
|---|---|---|
| **M1** — Repo e CI | Build reproduzível e pipeline verde | [#1–#5](../../milestone/1) |
| **M2** — Endurecimento | Segurança e config prontas para ambiente real | [#6–#10](../../milestone/2) |
| **M3** — Operação | Observabilidade, empacotamento, performance | [#11–#14](../../milestone/3) |
| **M4** — Testes | Cobertura por fatia e matriz de RBAC | [#15–#19](../../milestone/4) |
| **M5** — Integração Android | App sai do mock e passa a consumir a API | [#20–#26](../../milestone/5) |

A ordem não é rígida entre M2, M3 e M4 — mas **M1 vem antes de tudo**, e o M5 depende do
contrato OpenAPI da [#20](../../issues/20) estar publicado.

---

## Colinha de comandos

```bash
# --- ver trabalho ---
gh issue list --state open                    # tudo que está aberto
gh issue list --label p0                      # o que bloqueia
gh issue list --assignee @me                  # o que é meu
gh issue view 15                              # ler uma issue
gh project item-list 4 --owner @me            # o board no terminal

# --- pegar uma task ---
gh issue edit 15 --add-assignee @me
git checkout main && git pull
git checkout -b test/integracao-ideias

# --- entregar ---
./mvnw -q verify
git push -u origin test/integracao-ideias
gh pr create --fill
gh pr checks                                  # o CI passou?
gh pr merge --squash --delete-branch

# --- inspecionar o board (quando os IDs mudarem) ---
gh project field-list 4 --owner @me --format json
gh project item-list 4 --owner @me --format json
```

> `gh project` exige os scopes `project` e `read:project` no token. Se der erro de scope:
> `gh auth refresh -s project,read:project,workflow,repo`
