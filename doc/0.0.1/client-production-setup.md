# crypto-scout-client – Production Setup Report

## GitHub Short Description (proposal)

Async Java 25 service that streams crypto market data (Bybit, CoinMarketCap) and publishes structured events to RabbitMQ
Streams. Built with ActiveJ.

## Overview

This document summarizes the documentation work completed for the `crypto-scout-client` service and provides a
production setup guide.

This project and its documentation were authored using AI-driven tools and curated by the maintainer.

- Updated `README.md` with a professional, production-ready overview, architecture, configuration (including module
  toggles), build/run instructions, Podman usage, health check, and logging details.
- Added this production setup report to guide deployment and verification, and documented how to enable/disable modules
  for production scenarios.
- Documented DNS resolver configuration keys (`dns.address`, `dns.timeout.ms`) and their environment variable mappings
  (`DNS_ADDRESS`, `DNS_TIMEOUT_MS`).

## Code review summary (0.0.1)

- **Readiness semantics (validated):** `/ready` depends on `AmqpPublisher.isReady()` (RabbitMQ Streams environment and
  producers initialized). Keep this for traffic gating in orchestrators.

## Solution review (0.0.1)

- **Ready for production:** Yes, under documented prerequisites (RabbitMQ Streams enabled and reachable; required
  streams and credentials configured; secrets via env/JVM; outbound network access to Bybit/CMC).
- **Validated implementation:**
    - `WebModule` exposes `GET /health` -> `ok`; `GET /ready` -> `ok` only when `AmqpPublisher.isReady()`; else HTTP 503
      `not-ready`.
    - Configuration precedence: defaults from `src/main/resources/application.properties` via `AppConfig`, overridden by
      environment variables and JVM system properties at startup. Podman Compose injects env file:
      `secret/parser-client.env` (parser).
    - Container/compose hardening: non-root user, read-only rootfs, `tmpfs` `/tmp` with `nodev,nosuid`, `cap_drop: ALL`,
      `security_opt: no-new-privileges=true`, resource limits, pinned base image, healthcheck hitting `/ready`.
    - DNS: `WebConfig` uses `dns.address` and `dns.timeout.ms` to configure ActiveJ `DnsClient`.

## Recommendations for 0.0.2

- **HTTP clients timeouts:** Consider adding read/write/request timeouts to the ActiveJ `HttpClient` builder in
  `WebModule`, in addition to connect timeout.
- **AMQP TLS:** Add optional TLS configuration for RabbitMQ Streams connections for environments requiring
  encryption-in-transit.
- **Logging (optional):** Logging is provided transitively by `jcryptolib`. If you need custom formatting/levels or a
  different backend, add an explicit SLF4J binding (e.g., Logback) and a sample `src/main/resources/logback.xml`.
- **CI:** Wire tests into CI (`mvn -B -ntp verify`) and optionally add image build.

## What the service does

- Consumes crypto data from Bybit public streams (Spot PMST and Linear PML) for BTCUSDT and ETHUSDT: tickers, public
  trades, and order books (depths 50, 200, 1000). Spot subscribes to 15m/60m/240m/D klines; Linear subscribes to
  15m/60m/240m/D klines and all-liquidations.
- Periodically parses Bybit program metrics (Mega Drop, Launch Pool, Launchpad, ByVotes, ByStarter, Airdrop Hunt).
- Retrieves CoinMarketCap Fear & Greed Index (API Pro Latest) and BTC/USD quotes (1D, 1W).
- Publishes all collected and parsed payloads to RabbitMQ Streams (two streams: `amqp.bybit.stream` for Bybit streams,
  `amqp.crypto.scout.stream` for parser data).
- Exposes a lightweight health endpoint: `GET /health` -> `ok`.

## Key components (by code)

- Entry point: `src/main/java/com/github/akarazhev/cryptoscout/Client.java`
    - Combines modules and runs until shutdown is requested.
- Modules (`src/main/java/com/github/akarazhev/cryptoscout/module/`):
    - `CoreModule` – Single-threaded `NioReactor` and virtual-thread `Executor`.
    - `WebModule` – HTTP server (port from `WebConfig`), ActiveJ HTTP/WebSocket clients, DNS, `GET /health` and
      `GET /ready` routes.
    - `ClientModule` – Lifecycle for `AmqpPublisher`.
    - `BybitSpotModule` – provides two Spot streams for BTCUSDT/ETHUSDT (PMST): klines 15m/60m/240m/D, tickers,
      public trades, order books 50/200/1000 + consumers.
    - `BybitLinearModule` – provides two Linear streams for BTCUSDT/ETHUSDT (PML): klines 15m/60m/240m/D, tickers,
      public trades, order books 50/200/1000, all-liquidations + consumers.
    - `BybitParserModule` – Bybit programs HTTP parser + consumer.
    - `CmcParserModule` – CMC HTTP parser + consumer.
- AMQP publisher: `src/main/java/com/github/akarazhev/cryptoscout/client/AmqpPublisher.java`
    - Routes messages to streams based on provider/source.
- Consumer base class: `AbstractBybitStreamConsumer` – provides common lifecycle logic (`start()`/`stop()`) for Bybit
  stream consumers.
- Consumers: `BybitSpotBtcUsdtConsumer`, `BybitSpotEthUsdtConsumer`, `BybitLinearBtcUsdtConsumer`,
  `BybitLinearEthUsdtConsumer` (extend `AbstractBybitStreamConsumer`), `BybitParserConsumer`, `CmcParserConsumer`.
- Configuration readers: `src/main/java/com/github/akarazhev/cryptoscout/config/*`
    - `WebConfig` (server port, DNS), `AmqpConfig` (RabbitMQ Streams parameters).

## Configuration

Default properties: `src/main/resources/application.properties`.

- Modules (enable/disable at startup via flags read by `AppConfig` in `Client.getModule()`):
    - `bybit.stream.module.enabled=false` – Enable Bybit public streams publishers (`BybitSpotModule` and
      `BybitLinearModule`). Set to `true` to enable.
    - `bybit.parser.module.enabled=false` – Enable Bybit programs metrics parser (`BybitParserModule`). Set to `true`
      to enable.
    - `cmc.parser.module.enabled=true` – Enable CoinMarketCap metrics parser (`CmcParserModule`). Set to `false`
      to disable.
- Server:
    - `server.port=8081`
- DNS:
    - `dns.address=8.8.8.8` (resolver address)
    - `dns.timeout.ms=10000` (milliseconds)
- RabbitMQ (Streams):
    - `amqp.rabbitmq.host=localhost`
    - `amqp.rabbitmq.username=crypto_scout_mq`
    - `amqp.rabbitmq.password=`
    - `amqp.stream.port=5552`
    - `amqp.bybit.stream=bybit-stream` (Bybit WebSocket stream data)
    - `amqp.crypto.scout.stream=crypto-scout-stream` (parser data: Bybit programs + CMC)
- Bybit connection and API (used by the Bybit client library):
    - `bybit.connect.timeout.ms`, `bybit.initial.reconnect.interval.ms`, `bybit.max.reconnect.interval.ms`,
      `bybit.max.reconnect.attempts`, `bybit.backoff.multiplier`, `bybit.ping.interval.ms`, `bybit.pong.timeout.ms`,
      `bybit.fast.reconnect.attempts`, `bybit.fetch.start.at`, `bybit.fetch.interval.min`, `bybit.fetch.attempts`,
      `bybit.fetch.attempts.delay`, `bybit.circuit.breaker.threshold`, `bybit.circuit.breaker.timeout.ms`,
      `bybit.reconnect.rate.limit.ms`, `bybit.rest.rate.limit.ms`, `bybit.auth.expires.ms`
    - `bybit.api.key`, `bybit.api.secret` (empty by default)
- CoinMarketCap:
    - `cmc.connect.timeout.ms`, `cmc.fetch.start.at`, `cmc.fetch.interval.min`, `cmc.fetch.attempts`,
      `cmc.fetch.attempts.delay`, `cmc.circuit.breaker.threshold`, `cmc.circuit.breaker.timeout.ms`, `cmc.rate.limit.ms`
    - `cmc.api.key` (empty by default)

Note: Defaults are read via `AppConfig` from `src/main/resources/application.properties`. Environment variables and JVM
system properties override these defaults at runtime. With Podman Compose, `secret/parser-client.env` is injected as
env vars. No rebuild is required for config changes applied via env or `-D` properties—restart the application to
apply updates.

## Build

- Prerequisites: Java 25 JDK, Maven, network access to Bybit/CMC endpoints if running with metrics enabled.
- Build the shaded JAR:
    - `mvn clean package -DskipTests`
    - Output: `target/crypto-scout-client-0.0.1.jar`

## Run locally

- Start the service:
    - `java -jar target/crypto-scout-client-0.0.1.jar`
- Health check:
    - `curl -fsS http://localhost:8081/health` -> `ok`

## RabbitMQ Streams prerequisites

- Ensure RabbitMQ Streams is enabled and reachable at `amqp.rabbitmq.host:amqp.stream.port`.
- Pre-create the streams and ensure the user has publish permission:
    - `amqp.bybit.stream` (Bybit WebSocket stream data)
    - `amqp.crypto.scout.stream` (parser data: Bybit programs + CMC)

## Container (Podman or Docker)

- The `Dockerfile` copies the shaded JAR and runs it on `eclipse-temurin:25-jre-alpine`.
- Non-root user: UID/GID `10001` (user `app`).
- STOPSIGNAL: `SIGTERM` for graceful termination.
- Java OOM fast-exit and memory limits: `ENV JAVA_TOOL_OPTIONS="-XX:+ExitOnOutOfMemoryError -XX:MaxRAMPercentage=70"`.
- OCI labels present: `org.opencontainers.image.*` (title, description, version, license, vendor, source).
- Pinned base image digest to reduce supply-chain variance:
  `eclipse-temurin:25-jre-alpine@sha256:bf9c91071c4f90afebb31d735f111735975d6fe2b668a82339f8204202203621`.
- Build image:
    - `podman build -t crypto-scout-client:0.0.1 .`
- Run container:
    - `podman run --rm -p 8081:8081 --name crypto-scout-client crypto-scout-client:0.0.1`
- Docker (alternative):
    - `docker build -t crypto-scout-client:0.0.1 .`
    - `docker run --rm -p 8081:8081 --name crypto-scout-client crypto-scout-client:0.0.1`
- Note: The image contains bundled defaults from `application.properties`. You can override any value at runtime using
  environment variables or JVM system properties (e.g., `-Dserver.port=9090`, `-Damqp.rabbitmq.host=rmq`). No image
  rebuild is required—update your env and restart the container.
- Build context optimization: repository includes `.dockerignore` to reduce build context size (excludes `.git/`,
  `.idea/`, `.vscode/`, `secret/`, `doc/`, `dev/`, `*.iml`, `.mvn/`, `*.log`, `dependency-reduced-pom.xml`,
  `target/*` with `!target/*.jar`).

## Podman Compose (with secrets)

Use `podman-compose.yml` with an env file for a production-like run with secrets separated.

Steps:

1. Build the image:
    - `podman build -t crypto-scout-client:0.0.1 .`
2. Create external network (once):
    - `podman network create crypto-scout-bridge`
3. Prepare env file:
    - `cp secret/client.env.example secret/parser-client.env`
    - Edit `secret/parser-client.env` and set RabbitMQ host/credentials, stream port, and API keys as needed.
      `SERVER_PORT=8081` is the default (matches Dockerfile EXPOSE and compose healthcheck). Set module toggles as needed
      (e.g., `CMC_PARSER_MODULE_ENABLED=true`, `BYBIT_PARSER_MODULE_ENABLED=true`).
4. Start the service:
    - `podman-compose -f podman-compose.yml up -d`
5. Verify:
    - Readiness (parser): `podman inspect --format='{{.State.Health.Status}}' crypto-scout-parser-client` -> `healthy`
    - Logs: `podman logs -f crypto-scout-parser-client`
    - Note: Port 8081 is not exposed to the host; the service is accessible only within the `crypto-scout-bridge`
      network. Other containers can reach it via `crypto-scout-parser-client:8081`.

Security hardening in `podman-compose.yml`:

- `read_only: true`, `tmpfs: /tmp (nodev,nosuid)` for writable scratch only.
- `security_opt: no-new-privileges:true`, `cap_drop: ALL`.
- Non-root user: `user: "10001:10001"`.
- `init: true` to reap zombie processes.
- `pids_limit: 256` and `ulimits.nofile: 4096` to constrain resources.
- `stop_signal: SIGTERM`, `stop_grace_period: 30s` for graceful shutdown.
- Healthcheck `start_period: 30s` for safer warm-up time.
- Resource limits: `cpus: "0.5"`, `mem_limit: 256m`, `mem_reservation: 128m`.
- Restart policy: `restart: unless-stopped`.
- Timezone: `environment: TZ=UTC`.

Notes on configuration:

- The app reads defaults via `AppConfig` from `src/main/resources/application.properties`, then applies runtime
  overrides from environment variables and JVM system properties. With Podman Compose, `secret/parser-client.env` is
  injected for the parser service.
- No rebuild is required when changing configuration via env vars/system properties; restart the service to apply.
- Property-to-env examples: `dns.address` → `DNS_ADDRESS`, `dns.timeout.ms` → `DNS_TIMEOUT_MS` (dot → underscore,
  uppercased).

## Observability & operations

- **Logging:** Uses the SLF4J API with a binding provided transitively by `jcryptolib`, so logs are emitted by default.
  To change levels/format or switch backend, include your preferred SLF4J binding and its configuration (for example,
  provide `src/main/resources/logback.xml` if using Logback).
- **Liveness:** `GET /health` returns `ok` and HTTP 200 on the configured `server.port`.
- **Readiness:** `GET /ready` returns `ok` when RabbitMQ Streams environment and producers are initialized; otherwise
  HTTP 503 `not-ready`. Use `/health` for liveness and `/ready` for readiness in orchestrators.
- **JVM tuning:** Image sets `-XX:MaxRAMPercentage=70` by default. To enable heap dumps on OOM, add
  `-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/tmp` to `JAVA_TOOL_OPTIONS` via env. Ensure `/tmp` tmpfs in
  `podman-compose.yml` is large enough (e.g., bump to `size=1g`).

## Security notes

- Do not commit real API keys. Keep `bybit.api.key`, `bybit.api.secret`, and `cmc.api.key` empty in VCS and provide them
  securely for your environment.
- Scope RabbitMQ credentials to only required streams and operations.

## Production checklist

- Properties audited and provided for your environment (Bybit/CMC keys if required, RabbitMQ host/port/streams, server
  port).
- DNS resolver and timeout configured (`DNS_ADDRESS`, `DNS_TIMEOUT_MS`) and reachable from the runtime environment.
- Module flags set per deployment needs (`bybit.stream.module.enabled`, `bybit.parser.module.enabled`,
  `cmc.parser.module.enabled`).
- RabbitMQ Streams available and streams pre-created with correct permissions.
- Outbound connectivity allowed to Bybit and CoinMarketCap endpoints.
- Container built and started with port `8081` mapped (or custom `server.port`).
- Health endpoint monitored; logs collected at INFO level (adjust as needed in `logback.xml`).

## Summary of documentation updates

- `README.md`: Added description, features, architecture, configuration keys (including module toggles), build/run,
  Podman, health check, and logging sections.
- `README.md`: Added DNS configuration and env mappings (`DNS_ADDRESS`, `DNS_TIMEOUT_MS`).
- `doc/client-production-setup.md`: This report consolidates configuration, deployment steps, and operational guidance
  for production use, including DNS configuration details.

## Appendix A: Repository Review Summary (merged)

- **Tech stack (`pom.xml`):** Java 25 (`java.version`, compiler source/target 25), ActiveJ 6.0-rc2, RabbitMQ Stream
  Client 1.4.0, `jcryptolib` 0.0.3, shaded JAR main `com.github.akarazhev.cryptoscout.Client`.
- **Runtime architecture:** Modules `CoreModule`, `ClientModule`, `BybitSpotModule`, `BybitLinearModule`,
  `BybitParserModule`, `CmcParserModule`, `WebModule` + `JmxModule`, `ServiceGraphModule`.
  Endpoints: liveness `GET /health` -> `ok`; readiness `GET /ready` -> `ok` when RabbitMQ Streams environment and
  producers are initialized; otherwise HTTP 503 `not-ready`.
- **Module toggles:** `cmc.parser.module.enabled` (default `true`), `bybit.parser.module.enabled` (default `false`),
  `bybit.stream.module.enabled` (default `false`) in `application.properties`. Evaluated by `Client.getModule()` via
  `AppConfig.getAsBoolean(...)`.
- **Configuration:** `server.port`, RabbitMQ Streams host/credentials/port and stream names `amqp.bybit.stream`,
  `amqp.crypto.scout.stream`; DNS resolver and timeout (`dns.address`, `dns.timeout.ms`); Bybit/CMC timings and API
  keys via `AppConfig`.
- **Containerization:** Base image `eclipse-temurin:25-jre-alpine`; copies shaded JAR and runs `java -jar`.

## Appendix B: Validation Checklist (merged)

- Test: `mvn test`
- Build: `mvn clean package -DskipTests`
- Run locally: `java -jar target/crypto-scout-client-0.0.1.jar`
- Health check: `curl -fsS http://localhost:8081/health` -> `ok`
- Readiness check: `curl -fsS -o /dev/null -w "%{http_code}\n" http://localhost:8081/ready` -> `200`
- Container (Podman):
    - `podman build -t crypto-scout-client:0.0.1 .`
    - `podman run --rm -p 8081:8081 --name crypto-scout-client crypto-scout-client:0.0.1`
- Container (Docker):
    - `docker build -t crypto-scout-client:0.0.1 .`
    - `docker run --rm -p 8081:8081 --name crypto-scout-client crypto-scout-client:0.0.1`
- RabbitMQ Streams: ensure two streams exist (`bybit-stream`, `crypto-scout-stream`) and user can publish to them.
- Module toggles: prefer runtime overrides via env/JVM (e.g., `BYBIT_STREAM_MODULE_ENABLED=true`). Rebuild only if you
  change the bundled defaults in `src/main/resources/application.properties`.

## Appendix C: Next Steps (merged)

- Runtime configuration overrides via environment variables and JVM system properties are supported and documented.
  Ensure your deployment passes required values through the env file (`secret/parser-client.env`) in Podman Compose,
  or your Orchestrator's secret/config mechanism. Rebuilds are not
  necessary for config changes; restart with updated env.
- Prepare 0.0.2 changes: extend HTTP client timeouts, add optional AMQP TLS, optionally add an explicit logging
  binding and sample config if customization is required, and add CI integration for automated testing.
