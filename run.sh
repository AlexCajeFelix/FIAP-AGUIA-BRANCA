#!/usr/bin/env bash
#
# Sobe o Hub inteiro. Uso:
#
#   ./run.sh              # banco + API no Docker
#   ./run.sh --app        # o mesmo, e ainda instala o app no emulador/aparelho conectado
#   ./run.sh --down       # derruba tudo
#
# Nao precisa de JDK nem de Maven: o build acontece dentro do Docker.

set -euo pipefail

RAIZ="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
APP_ANDROID="${APP_ANDROID:-$RAIZ/app-android}"

azul() { printf '\033[1;34m%s\033[0m\n' "$1"; }
erro() { printf '\033[1;31m%s\033[0m\n' "$1" >&2; }

if ! command -v docker > /dev/null; then
    erro "Docker nao encontrado. Instale o Docker e rode de novo."
    exit 1
fi

if [ "${1:-}" = "--down" ]; then
    azul "Derrubando a stack..."
    docker compose -f "$RAIZ/compose.yaml" down
    exit 0
fi

# O compose le o .env. Sem ele, os defaults do proprio compose valem, mas o arquivo deixa as
# portas e o segredo visiveis para quem for mexer.
if [ ! -f "$RAIZ/.env" ]; then
    cp "$RAIZ/.env.example" "$RAIZ/.env"
    azul "Criado .env a partir de .env.example"
fi

PORTA="$(grep -E '^APP_PORT=' "$RAIZ/.env" | cut -d= -f2 | tr -d '[:space:]')"
PORTA="${PORTA:-8080}"
API="http://localhost:$PORTA"

azul "Subindo MongoDB e API (o primeiro build baixa as dependencias e demora alguns minutos)..."
docker compose -f "$RAIZ/compose.yaml" up -d --build

azul "Esperando a API responder em $API ..."
for _ in $(seq 1 60); do
    if curl -sf -m 2 "$API/actuator/health" > /dev/null 2>&1; then
        break
    fi
    sleep 3
done

if ! curl -sf -m 2 "$API/actuator/health" > /dev/null 2>&1; then
    erro "A API nao respondeu. Veja o log com: docker compose logs -f app"
    exit 1
fi

# Instala o app no aparelho conectado, se pedirem e se houver SDK e device.
if [ "${1:-}" = "--app" ]; then
    ADB="${ANDROID_HOME:-$HOME/Android/Sdk}/platform-tools/adb"
    if [ ! -x "$ADB" ]; then
        erro "SDK do Android nao encontrado em ${ANDROID_HOME:-$HOME/Android/Sdk}. Pulei a instalacao do app."
    elif [ -z "$("$ADB" devices | sed -n '2p')" ]; then
        erro "Nenhum emulador ou aparelho conectado (adb devices vazio). Pulei a instalacao do app."
    elif [ ! -d "$APP_ANDROID" ]; then
        erro "Projeto Android nao encontrado em $APP_ANDROID. Use APP_ANDROID=/caminho ./run.sh --app"
    else
        azul "Instalando o app Android..."
        (cd "$APP_ANDROID" && ./gradlew :app:installDebug -q)
        "$ADB" shell am start -n com.example.aguia_azul/.MainActivity > /dev/null
        azul "App aberto no emulador."
    fi
fi

cat <<RESUMO

$(azul "Tudo no ar.")

  API .............. $API
  Swagger .......... $API/swagger-ui.html
  Contrato OpenAPI . $API/v3/api-docs
  MongoDB .......... mongodb://localhost:27017/aguiabranca

  Usuarios de desenvolvimento (profile dev):

    operador@aguiabranca.dev    operador123    OPERADOR
    operadora@aguiabranca.dev   operador123    OPERADOR
    gestor@aguiabranca.dev      gestor123      GESTOR
    lideranca@aguiabranca.dev   lideranca123   LIDERANCA

  No emulador, o app aponta para http://10.0.2.2:$PORTA (o host visto de dentro).

  Logs:    docker compose logs -f app
  Derruba: ./run.sh --down

RESUMO
