#!/usr/bin/env bash

set -euo pipefail

RAIZ="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$RAIZ"

azul() { printf '\033[1;34m==> %s\033[0m\n' "$1"; }
erro() { printf '\033[1;31m%s\033[0m\n' "$1" >&2; }

AMBIENTE="${1:-}"
case "$AMBIENTE" in
    staging)    OVERRIDE=docker-compose.staging.yml; PORTA_PADRAO=8082 ;;
    production) OVERRIDE=docker-compose.prod.yml;    PORTA_PADRAO=8083 ;;
    *)
        erro "uso: $0 <staging|production> <imagem> | --down"
        exit 2
        ;;
esac

COMPOSE=(docker compose -p "aguiabranca-$AMBIENTE" -f docker-compose.yml -f "$OVERRIDE")
if [ -f "deploy/$AMBIENTE.env" ]; then
    COMPOSE+=(--env-file "deploy/$AMBIENTE.env")
fi

if [ "${2:-}" = "--down" ]; then
    azul "Derrubando $AMBIENTE"
    APP_IMAGE="${APP_IMAGE:-nenhuma}" JWT_SECRET="${JWT_SECRET:-nenhum}" "${COMPOSE[@]}" down
    exit 0
fi

export APP_IMAGE="${2:-${APP_IMAGE:-}}"
if [ -z "$APP_IMAGE" ]; then
    erro "Informe a imagem: $0 $AMBIENTE <registry/imagem:tag>"
    exit 2
fi
export APP_PORT="${APP_PORT:-$PORTA_PADRAO}"
export APP_VERSION="${APP_VERSION:-${APP_IMAGE##*:}}"

azul "Deploy de $APP_IMAGE em $AMBIENTE (porta $APP_PORT)"
"${COMPOSE[@]}" config --quiet

"${COMPOSE[@]}" up -d --no-build --pull missing --remove-orphans --wait --wait-timeout 240

"${COMPOSE[@]}" ps
azul "$AMBIENTE no ar em http://localhost:$APP_PORT"
