# crypto-scout-client

Production-ready Java microservice that collects crypto market data from Bybit and metrics from CoinMarketCap, then
publishes structured events to RabbitMQ Streams. Built on ActiveJ for fully async I/O.

## Features

- **Bybit streams (public):** Subscribes to linear and spot channels for `BTCUSDT` and `ETHUSDT` (tickers, 1m klines)
  and liquidation events, via `BybitStream`, and publishes to a crypto stream.
- **Bybit metrics (HTTP):** Periodically parses Bybit programs (Mega Drop, Launch Pool/Pad, ByVotes, ByStarter, Airdrop
  Hunt) via `BybitParser` and publishes to a metrics stream.
- **CoinMarketCap metrics:** Retrieves Fear & Greed Index via `CmcParser` and publishes to a metrics stream.
- **AMQP (RabbitMQ Streams):** Publishes messages to three streams configured in `application.properties`.
- **Health endpoint:** `GET /health` returns `ok`.
- **Async runtime:** Single-threaded reactor (ActiveJ `NioReactor`) with virtual threads for blocking tasks.

## Architecture

- **Launcher:** `com.github.akarazhev.cryptoscout.Client` wires modules and awaits shutdown.
- **Modules:**
    - `CoreModule` – reactor and executor (virtual threads).
    - `WebModule` – HTTP server, HTTP/WebSocket clients, health route, DNS.
    - `ClientModule` – AMQP publisher lifecycle.
    - `BybitModule` – Bybit streams and parser + consumers.
    - `CmcModule` – CMC parser + consumer.
- **Publishing:** `AmqpPublisher` routes payloads to configured streams based on provider/source.

## Requirements

- Java 25 JDK (Temurin recommended) to build
- Maven
- RabbitMQ with Streams enabled and reachable at the configured host/port

## Configuration

Defaults are loaded from `src/main/resources/application.properties` via `AppConfig`.

Runtime overrides:

- Environment variables and JVM system properties override the bundled defaults at startup.
- Podman Compose loads environment variables from `secret/client.env` (see the "Podman Compose (with secrets)" section).
- No rebuild is required when changing configuration via env vars or `-D` system properties; a restart is sufficient.

Property-to-env mapping (dot to underscore, uppercased) examples:

- `server.port` → `SERVER_PORT`
- `amqp.rabbitmq.host` → `AMQP_RABBITMQ_HOST`
- `amqp.rabbitmq.username` → `AMQP_RABBITMQ_USERNAME`
- `amqp.rabbitmq.password` → `AMQP_RABBITMQ_PASSWORD`
- `amqp.stream.port` → `AMQP_STREAM_PORT`

- **Modules**
    - `crypto.bybit.module.enabled=true` – Enable Bybit public streams publisher (`CryptoBybitModule`).
    - `metrics.bybit.module.enabled=true` – Enable Bybit programs metrics parser (`MetricsBybitModule`).
    - `metrics.cmc.module.enabled=true` – Enable CoinMarketCap metrics parser (`MetricsCmcModule`).
    - Set any of these to `false` to disable the corresponding module. Flags are evaluated in
      `src/main/java/com/github/akarazhev/cryptoscout/Client.java` via `AppConfig.getAsBoolean(...)`.
- **Server**
    - `server.port=8081`
- **RabbitMQ (Streams)**
    - `amqp.rabbitmq.host=localhost`
    - `amqp.rabbitmq.username=crypto_scout_mq`
    - `amqp.rabbitmq.password=`
    - `amqp.stream.port=5552`
    - `amqp.crypto.bybit.stream=crypto-bybit-stream`
    - `amqp.metrics.bybit.stream=metrics-bybit-stream`
    - `amqp.metrics.cmc.stream=metrics-cmc-stream`
- **Bybit connection**
    - `bybit.connect.timeout.ms=10000`
    - `bybit.initial.reconnect.interval.ms=100`
    - `bybit.max.reconnect.interval.ms=30000`
    - `bybit.max.reconnect.attempts=15`
    - `bybit.backoff.multiplier=1.5`
    - `bybit.ping.interval.ms=20000`
    - `bybit.pong.timeout.ms=15000`
    - `bybit.fast.reconnect.attempts=3`
    - `bybit.fetch.interval.ms=600000`
    - `bybit.circuit.breaker.threshold=5`
    - `bybit.circuit.breaker.timeout.ms=30000`
    - `bybit.reconnect.rate.limit.ms=1000`
    - `bybit.rest.rate.limit.ms=100`
    - `bybit.auth.expires.ms=10000`
    - `bybit.api.key=`
    - `bybit.api.secret=`
- **CoinMarketCap**
    - `cmc.connect.timeout.ms=10000`
    - `cmc.fetch.interval.ms=600000`
    - `cmc.circuit.breaker.threshold=5`
    - `cmc.circuit.breaker.timeout.ms=30000`
    - `cmc.rate.limit.ms=2100`
    - `cmc.api.key=`

Ensure the three RabbitMQ Streams listed above exist and the configured user has permission to publish.

## Build

```bash
mvn clean package -DskipTests
```

This produces a shaded JAR at `target/crypto-scout-client-0.0.1.jar`.

## Run (local)

```bash
java -jar target/crypto-scout-client-0.0.1.jar
```

Health check:

```bash
curl -fsS http://localhost:8081/health
# ok
```

## Container image (Podman or Docker)

The provided `Dockerfile` uses Temurin JRE 25 and runs the shaded JAR.

- Non-root user: UID/GID `10001` (user `app`).
- STOPSIGNAL: `SIGTERM` for graceful shutdown.
- Java OOM fast-exit: `ENV JAVA_TOOL_OPTIONS="-XX:+ExitOnOutOfMemoryError"`.
- OCI labels: `org.opencontainers.image.*` (title, description, version, license, vendor, source).
- Pinned base image:
  `eclipse-temurin:25-jre-alpine@sha256:bf9c91071c4f90afebb31d735f111735975d6fe2b668a82339f8204202203621`.

- Build (Podman):

```bash
podman build -t crypto-scout-client:0.0.1 .
```

- Build (Docker):

```bash
docker build -t crypto-scout-client:0.0.1 .
```

- Run (Podman, map HTTP port):

```bash
podman run --rm -p 8081:8081 --name crypto-scout-client crypto-scout-client:0.0.1
```

- Run (Docker, map HTTP port):

```bash
docker run --rm -p 8081:8081 --name crypto-scout-client crypto-scout-client:0.0.1
```

Defaults from `src/main/resources/application.properties` are bundled in the image, but you can override any value at
runtime using environment variables or JVM system properties (e.g., `-Dserver.port=9090`, `-Damqp.rabbitmq.host=rmq`).
No image rebuild is required—update your env file or container environment and restart the container.

## Podman Compose (with secrets)

Use the provided `podman-compose.yml` to run the service with a secrets file.

0) Build the shaded JAR (required for the Dockerfile copy step):

```bash
mvn clean package -DskipTests
```

1) Build the image:

```bash
podman build -t crypto-scout-client:0.0.1 .
```

2) Create external network (once):

```bash
podman network create crypto-scout-bridge
```

3) Create and populate secrets:

```bash
cp secret/client.env.example secret/client.env
$EDITOR secret/client.env
```

4) Start with compose:

```bash
podman-compose -f podman-compose.yml up -d
# or
podman compose -f podman-compose.yml up -d
```

5) Check health and logs:

```bash
curl -fsS http://localhost:8081/health
podman logs -f crypto-scout-client
```
Notes:

- The app reads defaults from `src/main/resources/application.properties`, then applies runtime overrides from
  environment variables and JVM system properties. With Podman Compose, `secret/client.env` is injected as env vars.
- Real secrets should be placed in `secret/client.env` (ignored by Git). Never commit real credentials.
- To apply config changes, edit `secret/client.env` and restart: `podman compose -f podman-compose.yml up -d`.
- If you change the external HTTP port (`SERVER_PORT`), update the `ports` mapping in `podman-compose.yml` accordingly.
- If RabbitMQ runs on your host machine, set `AMQP_RABBITMQ_HOST=host.containers.internal` in `secret/client.env` so the
  container can reach the host.

- Compose hardening in `podman-compose.yml`:
  - `init: true`
  - `pids_limit: 256`
  - `ulimits.nofile: 4096`
  - `stop_signal: SIGTERM`
  - `stop_grace_period: 30s`
  - healthcheck `start_period: 30s`
  - `read_only` rootfs with `tmpfs: /tmp (nodev,nosuid)`
  - `cap_drop: ALL`
  - `security_opt: no-new-privileges=true`

## Production notes

- **Java version alignment:** Build targets Java 25 and Docker image uses JRE 25 — aligned.
- **RabbitMQ prerequisites:** Ensure Streams exist and the configured user can publish to:
  - `amqp.crypto.bybit.stream`
  - `amqp.metrics.bybit.stream`
  - `amqp.metrics.cmc.stream`
- **Module toggles:** Control active modules with `metrics.cmc.module.enabled`, `metrics.bybit.module.enabled`,
  and `crypto.bybit.module.enabled` in `application.properties` (defaults `true`; set to `false` to disable). Evaluated
  in `Client.getModule()` at startup.
- **Secrets:** Do not commit secrets. Keep API keys/passwords empty in the repository and inject values securely at
  runtime via environment variables (e.g., `secret/client.env` with Podman Compose or your orchestrator’s secret store).
  Rebuilds are not required for config/secrets changes—restart with updated env.
- **Health endpoint:** `GET /health` returns `ok` for liveness checks.
- **Observability:** Console logging via `src/main/resources/logback.xml` (INFO). JMX is enabled via ActiveJ
  `JmxModule`.
## Logging

Configured via `src/main/resources/logback.xml` (console, INFO level by default).

## License

MIT License. See `LICENSE`.