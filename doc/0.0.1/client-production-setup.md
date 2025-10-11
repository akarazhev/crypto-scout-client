# crypto-scout-client – Production Setup Report

## GitHub Short Description (proposal)

Async Java 21 service that streams crypto market data (Bybit, CoinMarketCap) and publishes structured events to RabbitMQ
Streams. Built with ActiveJ.

## Overview

This document summarizes the documentation work completed for the `crypto-scout-client` service and provides a
production setup guide.

- Updated `README.md` with a professional, production-ready overview, architecture, configuration, build/run
  instructions, Podman usage, health check, and logging details.
- Added this production setup report to guide deployment and verification.

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

- Server:
    - `server.port=8080`
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

Note: Properties are read via `AppConfig` (from the `jcryptolib` dependency). The repository config file is the source
of truth; no environment override behavior is documented here.

## Build

- Prerequisites: Java 21, Maven, network access to Bybit/CMC endpoints if running with metrics enabled.
- Build the shaded JAR:
    - `mvn clean package -DskipTests`
    - Output: `target/crypto-scout-client-0.0.1.jar`

## Run locally

- Start the service:
    - `java -jar target/crypto-scout-client-0.0.1.jar`
- Health check:
    - `curl -fsS http://localhost:8080/health` -> `ok`

## RabbitMQ Streams prerequisites

- Ensure RabbitMQ Streams is enabled and reachable at `amqp.rabbitmq.host:amqp.stream.port`.
- Pre-create the streams and ensure the user has publish permission:
    - `amqp.crypto.bybit.stream`
    - `amqp.metrics.bybit.stream`
    - `amqp.metrics.cmc.stream`

## Podman

- The `Dockerfile` copies the shaded JAR and runs it on `eclipse-temurin:21-jre-ubi9-minimal`.
- Build image:
    - `podman build -t crypto-scout-client:0.0.1 .`
- Run container:
    - `podman run --rm -p 8080:8080 --name crypto-scout-client crypto-scout-client:0.0.1`
- Note: The image contains the bundled `application.properties`. To change configuration, update the file and rebuild
  the image.

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
- RabbitMQ Streams available and streams pre-created with correct permissions.
- Outbound connectivity allowed to Bybit and CoinMarketCap endpoints.
- Container built and started with port `8080` mapped (or custom `server.port`).
- Health endpoint monitored; logs collected at INFO level (adjust as needed in `logback.xml`).

## Summary of documentation updates

- `README.md`: Added description, features, architecture, configuration keys, build/run, Podman, health check, and
  logging sections.
- `doc/client-production-setup.md`: This report consolidates configuration, deployment steps, and operational guidance
  for production use.
