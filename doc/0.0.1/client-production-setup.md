# crypto-scout-client – Production Setup Report

## GitHub Short Description (proposal)

Async Java 25 service that streams crypto market data (Bybit, CoinMarketCap) and publishes structured events to RabbitMQ
Streams. Built with ActiveJ.

## Overview

This document summarizes the documentation work completed for the `crypto-scout-client` service and provides a
production setup guide.

- Updated `README.md` with a professional, production-ready overview, architecture, configuration (including
  module toggles), build/run instructions, Podman usage, health check, and logging details.
- Added this production setup report to guide deployment and verification, and documented how to enable/disable
  modules for production scenarios.
- Documented DNS resolver configuration keys (`dns.address`, `dns.timeout.ms`) and their environment variable mappings
  (`DNS_ADDRESS`, `DNS_TIMEOUT_MS`).

## What the service does

- Consumes crypto data from Bybit public streams (linear and spot) for BTCUSDT and ETHUSDT: tickers, 1m klines, and
  liquidation events.
- Periodically parses Bybit program metrics (Mega Drop, Launch Pool/Pad, ByVotes, ByStarter, Airdrop Hunt).
- Retrieves CoinMarketCap Fear & Greed Index.
- Publishes all collected and parsed payloads to RabbitMQ Streams.
- Exposes a lightweight health endpoint: `GET /health` -> `ok`.

## Key components (by code)

- Entry point: `src/main/java/com/github/akarazhev/cryptoscout/Client.java`
    - Combines modules and runs until shutdown is requested.
- Modules (`src/main/java/com/github/akarazhev/cryptoscout/module/`):
    - `CoreModule` – Single-threaded `NioReactor` and virtual-thread `Executor`.
    - `WebModule` – HTTP server (port from `ServerConfig`), ActiveJ HTTP/WebSocket clients, DNS, `GET /health` route.
    - `ClientModule` – Lifecycle for `AmqpPublisher`.
    - `BybitModule` – Bybit WebSocket streams, Bybit HTTP parser, and corresponding consumers.
    - `CmcModule` – CMC HTTP parser and consumer.
- AMQP publisher: `src/main/java/com/github/akarazhev/cryptoscout/client/AmqpPublisher.java`
    - Routes messages to streams based on provider/source.
- Consumers: `CryptoBybitConsumer`, `MetricsBybitConsumer`, `MetricsCmcConsumer`.
- Configuration readers: `src/main/java/com/github/akarazhev/cryptoscout/config/*`
    - `ServerConfig` (server port), `AmqpConfig` (RabbitMQ Streams parameters).

## Configuration

Default properties: `src/main/resources/application.properties`.

- Modules (enable/disable at startup via flags read by `AppConfig` in `Client.getModule()`):
    - `crypto.bybit.module.enabled=true` – Enable Bybit public streams publisher (`CryptoBybitModule`). Set to `false`
      to disable.
    - `metrics.bybit.module.enabled=true` – Enable Bybit programs metrics parser (`MetricsBybitModule`). Set to `false`
      to disable.
    - `metrics.cmc.module.enabled=true` – Enable CoinMarketCap metrics parser (`MetricsCmcModule`). Set to `false`
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
    - `amqp.crypto.bybit.stream=crypto-bybit-stream`
    - `amqp.metrics.bybit.stream=metrics-bybit-stream`
    - `amqp.metrics.cmc.stream=metrics-cmc-stream`
- Bybit connection and API (used by the Bybit client library):
    - `bybit.connect.timeout.ms`, `bybit.initial.reconnect.interval.ms`, `bybit.max.reconnect.interval.ms`,
      `bybit.max.reconnect.attempts`, `bybit.backoff.multiplier`, `bybit.ping.interval.ms`, `bybit.pong.timeout.ms`,
      `bybit.fast.reconnect.attempts`, `bybit.fetch.interval.ms`, `bybit.circuit.breaker.threshold`,
      `bybit.circuit.breaker.timeout.ms`, `bybit.reconnect.rate.limit.ms`, `bybit.rest.rate.limit.ms`,
      `bybit.auth.expires.ms`
    - `bybit.api.key`, `bybit.api.secret` (empty by default)
- CoinMarketCap:
    - `cmc.connect.timeout.ms`, `cmc.fetch.interval.ms`, `cmc.circuit.breaker.threshold`,
      `cmc.circuit.breaker.timeout.ms`, `cmc.rate.limit.ms`
    - `cmc.api.key` (empty by default)

Note: Defaults are read via `AppConfig` from `src/main/resources/application.properties`. Environment variables and JVM
system properties override these defaults at runtime. With Podman Compose, `secret/client.env` is injected as env vars.
No rebuild is required for config changes applied via env or `-D` properties—restart the application to apply updates.

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
    - `amqp.crypto.bybit.stream`
    - `amqp.metrics.bybit.stream`
    - `amqp.metrics.cmc.stream`

## Container (Podman or Docker)

- The `Dockerfile` copies the shaded JAR and runs it on `eclipse-temurin:25-jre-alpine`.
- Non-root user: UID/GID `10001` (user `app`).
- STOPSIGNAL: `SIGTERM` for graceful termination.
- Java OOM fast-exit: `ENV JAVA_TOOL_OPTIONS="-XX:+ExitOnOutOfMemoryError"`.
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

## Podman Compose (with secrets)

Use `podman-compose.yml` and `secret/client.env` for a production-like run with secrets separated.

Steps:

1. Build the image:
    - `podman build -t crypto-scout-client:0.0.1 .`
2. Create external network (once):
    - `podman network create crypto-scout-bridge`
3. Prepare secrets:
    - `cp secret/client.env.example secret/client.env`
    - Edit `secret/client.env` and set RabbitMQ host/credentials, stream port/names, and API keys (`BYBIT_API_KEY`,
      `BYBIT_API_SECRET`, `CMC_API_KEY`). Optionally set `SERVER_PORT`.
4. Start the service:
    - `podman-compose -f podman-compose.yml up -d`
5. Verify:
    - Health: `curl -fsS http://localhost:8081/health` -> `ok`
    - Logs: `podman logs -f crypto-scout-client`

Security hardening in `podman-compose.yml`:

- `read_only: true`, `tmpfs: /tmp (nodev,nosuid)` for writable scratch only.
- `security_opt: no-new-privileges:true`, `cap_drop: ALL`.
- Non-root user: `user: "10001:10001"`.
- `init: true` to reap zombie processes.
- `pids_limit: 256` and `ulimits.nofile: 4096` to constrain resources.
- `stop_signal: SIGTERM`, `stop_grace_period: 30s` for graceful shutdown.
- Healthcheck `start_period: 30s` for safer warm-up time.

Notes on configuration:

- The app reads defaults via `AppConfig` from `src/main/resources/application.properties`, then applies runtime overrides
  from environment variables and JVM system properties. With Podman Compose, `secret/client.env` is injected.
- No rebuild is required when changing configuration via env vars/system properties; restart the service to apply.
- Property-to-env examples: `dns.address` → `DNS_ADDRESS`, `dns.timeout.ms` → `DNS_TIMEOUT_MS` (dot → underscore, uppercased).

## Observability & operations

- Logging: `src/main/resources/logback.xml` (console, INFO level by default).
- Startup indicators in logs (INFO):
    - `AmqpPublisher started`
    - `CryptoBybitConsumer started`
    - `MetricsBybitConsumer started`
    - `MetricsCmcConsumer started`
- Liveness: `GET /health` should return `ok` and HTTP 200 on the configured `server.port`.

## Security notes

- Do not commit real API keys. Keep `bybit.api.key`, `bybit.api.secret`, and `cmc.api.key` empty in VCS and provide them
  securely for your environment.
- Scope RabbitMQ credentials to only required streams and operations.

## Production checklist

- Properties audited and provided for your environment (Bybit/CMC keys if required, RabbitMQ host/port/streams, server
  port).
- DNS resolver and timeout configured (`DNS_ADDRESS`, `DNS_TIMEOUT_MS`) and reachable from the runtime environment.
- Module flags set per deployment needs (`crypto.bybit.module.enabled`, `metrics.bybit.module.enabled`,
  `metrics.cmc.module.enabled`).
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
  Client 1.2.0, `jcryptolib` 0.0.2, shaded JAR main `com.github.akarazhev.cryptoscout.Client`.
- **Runtime architecture:** Modules `CoreModule`, `ClientModule`, `BybitModule`, `CmcModule`, `WebModule` + `JmxModule`,
  `ServiceGraphModule`. Health route `GET /health` -> `ok`.
- **Module toggles:** `metrics.cmc.module.enabled`, `metrics.bybit.module.enabled`, `crypto.bybit.module.enabled` in
  `application.properties` (default `true`). Evaluated by `Client.getModule()` via `AppConfig.getAsBoolean(...)`.
- **Configuration:** `server.port`, RabbitMQ Streams host/credentials/port and stream names `amqp.crypto.bybit.stream`,
  `amqp.metrics.bybit.stream`, `amqp.metrics.cmc.stream`; DNS resolver and timeout (`dns.address`, `dns.timeout.ms`);
  Bybit/CMC timings and API keys via `AppConfig`.
- **Containerization:** Base image `eclipse-temurin:25-jre-alpine`; copies shaded JAR and runs `java -jar`.

## Appendix B: Validation Checklist (merged)

- Build: `mvn clean package -DskipTests`
- Run locally: `java -jar target/crypto-scout-client-0.0.1.jar`
- Health check: `curl -fsS http://localhost:8081/health` -> `ok`
- Container (Podman):
    - `podman build -t crypto-scout-client:0.0.1 .`
    - `podman run --rm -p 8081:8081 --name crypto-scout-client crypto-scout-client:0.0.1`
- Container (Docker):
    - `docker build -t crypto-scout-client:0.0.1 .`
    - `docker run --rm -p 8081:8081 --name crypto-scout-client crypto-scout-client:0.0.1`
- RabbitMQ Streams: ensure three streams exist and user can publish to them.
- Module toggles: set one or more module flags to `false` in `application.properties`, rebuild the image, and verify
  that corresponding startup log lines are absent (e.g., disabling `metrics.cmc.module.enabled` removes
  `MetricsCmcConsumer started`).

## Appendix C: Next Steps (merged)

- Runtime configuration overrides via environment variables and JVM system properties are supported and documented.
  Ensure your deployment passes required values through `secret/client.env` (Podman Compose) or your orchestrator’s
  secret/config mechanism. Rebuilds are not necessary for config changes; restart with updated env.
