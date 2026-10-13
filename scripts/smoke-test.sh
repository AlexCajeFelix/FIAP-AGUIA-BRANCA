#!/usr/bin/env bash

set -uo pipefail

API="${1:?uso: $0 <url-base> <staging|production> [versao-esperada]}"
AMBIENTE="${2:?informe o ambiente: staging ou production}"
VERSAO_ESPERADA="${3:-}"

PASSOU=0
FALHOU=0
LINHAS=()

registra() {
    if [ "$1" = ok ]; then
        PASSOU=$((PASSOU + 1)); printf '  \033[32mOK\033[0m    %s — %s\n' "$2" "$3"
        LINHAS+=("| ✅ | $2 | $3 |")
    else
        FALHOU=$((FALHOU + 1)); printf '  \033[31mFALHA\033[0m %s — %s\n' "$2" "$3"
        LINHAS+=("| ❌ | $2 | $3 |")
    fi
}

req() {
    local args=(-s -o /tmp/smoke-body -w '%{http_code}' -X "$1" "$API$2" -H 'X-Request-Id: smoke-test')
    [ -n "${3:-}" ] && args+=(-H "Authorization: Bearer $3")
    [ -n "${4:-}" ] && args+=(-H 'Content-Type: application/json' -d "$4")
    local status
    status="$(curl "${args[@]}" || echo 000)"
    printf '%s %s' "$status" "$(cat /tmp/smoke-body 2>/dev/null)"
}

confere() {
    if [ "$1" = "$2" ]; then registra ok "$3" "$4"; else registra falha "$3" "$5"; fi
}

espera() {
    local nome="$1" esperado="$2"; shift 2
    local resp status
    resp="$(req "$@")"; status="${resp%% *}"
    if [ "$status" = "$esperado" ]; then
        registra ok "$nome" "HTTP $status"
    else
        registra falha "$nome" "esperado $esperado, veio $status: ${resp#* }"
    fi
    CORPO="${resp#* }"
}

echo "Smoke test em $API ($AMBIENTE)"

espera "Health check" 200 GET /actuator/health
confere "$(jq -r .status <<<"$CORPO" 2>/dev/null)" UP "MongoDB conectado" "status UP" "$CORPO"

espera "Contrato OpenAPI" 200 GET /v3/api-docs
AMB_API="$(jq -r '."x-environment"' <<<"$CORPO" 2>/dev/null)"
VER_API="$(jq -r .info.version <<<"$CORPO" 2>/dev/null)"
confere "$AMB_API" "$AMBIENTE" "Ambiente correto" "API diz: $AMB_API" "API diz: $AMB_API"
if [ -n "$VERSAO_ESPERADA" ]; then
    confere "$VER_API" "$VERSAO_ESPERADA" "Versao implantada" "$VER_API" \
        "esperada $VERSAO_ESPERADA, veio $VER_API"
fi

espera "Swagger UI" 200 GET /swagger-ui/index.html

espera "Rota protegida sem token" 401 GET /ideas
espera "Login com senha errada" 401 POST /auth/login "" \
    '{"email":"ninguem@aguiabranca.dev","password":"senha-errada"}'

CABECALHO="$(curl -s -D - -o /dev/null "$API/actuator/health" -H 'X-Request-Id: smoke-corr-id' \
    | tr -d '\r' | awk -F': ' 'tolower($1)=="x-request-id"{print $2}')"
confere "$CABECALHO" smoke-corr-id "Correlation ID" "X-Request-Id ecoado" "veio '$CABECALHO'"

if [ "$AMBIENTE" = staging ]; then
    espera "Login do gestor (seed)" 200 POST /auth/login "" \
        '{"email":"gestor@aguiabranca.dev","password":"gestor123"}'
    TOKEN="$(jq -r .accessToken <<<"$CORPO" 2>/dev/null)"

    espera "Cria ideia" 201 POST /ideas "$TOKEN" \
        "{\"title\":\"Smoke test $(date +%s)\",\"description\":\"Ideia criada pelo pipeline para validar o deploy.\"}"
    IDEIA="$(jq -r .id <<<"$CORPO" 2>/dev/null)"

    espera "Aprova ideia" 200 POST "/ideas/$IDEIA/approval" "$TOKEN" '{"status":"APPROVED"}'
    espera "Promove a projeto" 201 POST "/projects/from-idea/$IDEIA" "$TOKEN" '{"budget":150000.00}'
    PROJETO="$(jq -r .id <<<"$CORPO" 2>/dev/null)"

    espera "Atualiza metricas (transacao)" 200 PATCH "/projects/$PROJETO/metrics" "$TOKEN" \
        '{"progress":40,"spent":52000.00}'
    espera "Painel da lideranca" 200 GET /projects/summary "$TOKEN"
else
    espera "Conta de dev nao existe em producao" 401 POST /auth/login "" \
        '{"email":"gestor@aguiabranca.dev","password":"gestor123"}'
fi

echo
echo "Resultado: $PASSOU ok, $FALHOU falha(s)"

if [ -n "${GITHUB_STEP_SUMMARY:-}" ]; then
    {
        echo "### Smoke test — $AMBIENTE"
        echo
        echo "Versao implantada: \`${VER_API:-?}\` · $PASSOU ok, $FALHOU falha(s)"
        echo
        echo "| | Verificacao | Resultado |"
        echo "|---|---|---|"
        printf '%s\n' "${LINHAS[@]}"
        echo
    } >> "$GITHUB_STEP_SUMMARY"
fi

[ "$FALHOU" -eq 0 ]
