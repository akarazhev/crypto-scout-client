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

Configuration is loaded from `src/main/resources/application.properties` via `AppConfig`.

- **Server**
    - `server.port=8080`
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
curl -fsS http://localhost:8080/health
# ok
```

## Container image (Podman or Docker)

The provided `Dockerfile` uses Temurin JRE 25 and runs the shaded JAR.

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
podman run --rm -p 8080:8080 --name crypto-scout-client crypto-scout-client:0.0.1
```

- Run (Docker, map HTTP port):

```bash
docker run --rm -p 8080:8080 --name crypto-scout-client crypto-scout-client:0.0.1
```

Configuration is bundled from `src/main/resources/application.properties` at build-time. To change any values (e.g.,
RabbitMQ host/credentials, Bybit/CMC API keys), update that file and rebuild the image.

## Production notes

- **Java version alignment:** Build targets Java 25 and Docker image uses JRE 25 — aligned.
- **RabbitMQ prerequisites:** Ensure Streams exist and the configured user can publish to:
  `amqp.crypto.bybit.stream`, `amqp.metrics.bybit.stream`, `amqp.metrics.cmc.stream`.
- **Secrets:** Do not commit secrets. Keep API keys/passwords empty in the repository and inject values securely during
  your image build process within CI, then distribute the built image.
- **Health endpoint:** `GET /health` returns `ok` for liveness checks.
- **Observability:** Console logging via `src/main/resources/logback.xml` (INFO). JMX is enabled via ActiveJ
  `JmxModule`.

## Logging

Configured via `src/main/resources/logback.xml` (console, INFO level by default).

## License

MIT License. See `LICENSE`.