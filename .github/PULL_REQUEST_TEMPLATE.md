## O que muda

<!-- 2-3 linhas. O "porquê" fica na issue; aqui é o "o quê". -->

Closes #

## Como validar

<!-- Comandos que o revisor roda para confirmar. Ex.: ./mvnw -q verify -->

```
```

## Checklist

- [ ] `./mvnw -q verify` passa localmente
- [ ] Commits no padrão Conventional Commits
- [ ] Sem segredo, token ou credencial no diff
- [ ] Migration nova é idempotente e não roda em produção sem querer
- [ ] Mudança de contrato de API está refletida no `README.md` / `api.http`
- [ ] Se quebra o app Android, a issue correspondente em `area:android` foi aberta

## Risco

<!-- O que pode quebrar em produção e qual o plano de rollback. -->
