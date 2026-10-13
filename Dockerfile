# syntax=docker/dockerfile:1.7
#
# Imagem da API em dois estagios: o JDK + Maven so existem no build; a imagem final leva
# apenas o JRE e o jar em camadas. Mesma imagem vai para staging e producao — o que muda
# entre os ambientes e so variavel de ambiente, nunca o binario.

# ---------- Estagio 1: build ----------
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Wrapper e pom primeiro: enquanto eles nao mudam, a camada de dependencias fica em cache e o
# rebuild depois de mexer so em src/ leva segundos.
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw
RUN --mount=type=cache,target=/root/.m2 ./mvnw -B -q dependency:go-offline

COPY src ./src
# Os testes rodam no pipeline (job build), com Testcontainers; aqui so empacota.
RUN --mount=type=cache,target=/root/.m2 ./mvnw -B -q clean package -DskipTests \
    && cp target/*.jar app.jar \
    && java -Djarmode=layertools -jar app.jar extract --destination extracted

# ---------- Estagio 2: runtime ----------
FROM eclipse-temurin:21-jre-alpine AS runner

ARG APP_VERSION=local
ARG VCS_REF=unknown
LABEL org.opencontainers.image.title="fiap-aguia-branca" \
      org.opencontainers.image.description="Hub de Inovacao Aguia Branca - API Spring Boot" \
      org.opencontainers.image.source="https://github.com/AlexCajeFelix/FIAP-AGUIA-BRANCA" \
      org.opencontainers.image.version="${APP_VERSION}" \
      org.opencontainers.image.revision="${VCS_REF}"

WORKDIR /app

# Usuario sem privilegio: um RCE na aplicacao nao vira root no container. O diretorio de log
# nasce com o dono certo para o volume nomeado herdar a permissao na primeira montagem.
RUN addgroup -S appgroup && adduser -S appuser -G appgroup \
    && mkdir -p /app/logs && chown appuser:appgroup /app/logs

# Camadas do Spring Boot da menos para a mais volatil: dependencias quase nunca mudam, o
# codigo da aplicacao muda a cada commit. O push para o registry so reenvia a ultima.
COPY --from=builder --chown=appuser:appgroup /app/extracted/dependencies/ ./
COPY --from=builder --chown=appuser:appgroup /app/extracted/spring-boot-loader/ ./
COPY --from=builder --chown=appuser:appgroup /app/extracted/snapshot-dependencies/ ./
COPY --from=builder --chown=appuser:appgroup /app/extracted/application/ ./

USER appuser

ENV APP_VERSION=${APP_VERSION} \
    SERVER_PORT=8080 \
    JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError"

EXPOSE 8080

# wget vem no busybox do alpine; o JRE nao traz curl. O start-period cobre o boot do Spring.
HEALTHCHECK --interval=10s --timeout=3s --start-period=60s --retries=5 \
    CMD wget -qO- "http://127.0.0.1:${SERVER_PORT}/actuator/health" | grep -q '"UP"' || exit 1

# exec: a JVM vira o PID 1 e recebe o SIGTERM do `docker stop` (shutdown gracioso).
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]
