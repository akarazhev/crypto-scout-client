# crypto-scout-client

Production-ready Java microservice that collects crypto market data from Bybit and metrics from CoinMarketCap, then
publishes structured events to RabbitMQ Streams. Built on ActiveJ for fully async I/O.

Note: This project was authored using AI-driven tools and curated by the maintainer.

**Production Status:** ✅ Production Ready (as of January 26, 2026)

## Recent Changes

### Code Review Fixes (February 2, 2026)

Thread-safety and resource management improvements:

- **Thread-Safety in `AmqpPublisher`**: Fixed race condition in `isReady()` by capturing consistent snapshot of volatile fields
- **Resource Leak Prevention**: Fixed `stop()` to ensure all resources close even if individual close operations throw exceptions
- **Null Safety in `CmcParserConsumer`**: Added null/empty checks in `selectLatestQuote()` with proper logging to prevent NPE
- **Test Improvements**: Fixed `AmqpPublisherTest` to properly shut down all ExecutorService instances with timeout handling

### Code Review Fixes (January 26, 2026)

All critical security and validation issues have been successfully resolved:

- **Security Enhancements**: Comprehensive null safety improvements, exception handling standardization, and configuration validation
- **Constants Organization**: All magic numbers consolidated into `Constants.java` for better maintainability
- **Enhanced Validation**: Hostname, port, and timeout validation with descriptive error messages
- **Code Quality**: Consistent use of `IllegalStateException`, improved constructor validation, removed unused methods
- **Test Coverage**: All 28 tests passing (100% success rate) with lifecycle methods and proper naming conventions
- **Documentation**: New `SECURITY.md` with comprehensive security guidelines and validation rules

For complete details on all improvements, see the [SECURITY.md](SECURITY.md) file.

## Features

- **Bybit streams (public):** Spot (PMST) and Linear (PML) channels for `BTCUSDT` and `ETHUSDT` with tickers, public
  trades, and order books (depths 50, 200, 1000). Spot subscribes to 15m/60m/240m/D klines; Linear subscribes to
  15m/60m/240m/D klines and all-liquidations. Implemented via jcryptolib `BybitStream` and published to
  `amqp.bybit.stream`.
- **CoinMarketCap metrics:** Retrieves Fear & Greed Index (API Pro Latest) and BTC/USD quotes (1D, 1W) via `CmcParser`
  and publishes to `amqp.crypto.scout.stream`.
- **AMQP (RabbitMQ Streams):** Publishes messages to two streams configured in `application.properties`.

## Production Setup

The service is production-ready with comprehensive security and validation features. Before deploying to production:

### Prerequisites

- Java 25 JDK (Temurin recommended)
- RabbitMQ Streams (version 3.9+) with streams pre-created
- Outbound network access to Bybit and CoinMarketCap endpoints
- Proper credentials configured via environment variables or system properties

### Configuration Validation

The application validates all required configuration at startup using `ConfigValidator`. Invalid or missing configuration
will cause the application to fail to start with descriptive error messages. See [Configuration Validation](#configuration)
and [SECURITY.md](SECURITY.md) for complete validation rules.

### Security Considerations

- **Never commit credentials** - Use environment variables or system properties
- **API key management** - Rotate credentials regularly and limit permissions
- **Configuration protection** - Local configuration files are excluded via `.gitignore`
- **Validation** - Hostnames, ports, and timeouts are validated at startup

For complete security guidelines, see [SECURITY.md](SECURITY.md).

### Health & Readiness

- **Liveness**: `GET /health` returns `ok` when the application is running
- **Readiness**: `GET /health` returns HTTP 200 when RabbitMQ Streams is initialized; HTTP 503 otherwise
- Use the health endpoint for orchestration probes and monitoring

### Monitoring

- SLF4J API logging (binding provided transitively by `jcryptolib`)
- JMX enabled via ActiveJ `JmxModule`
- Health endpoint for orchestration

See [Production notes](#production-notes) for additional operational guidance.

## Architecture

- **Launcher:** `com.github.akarazhev.cryptoscout.Client` wires modules and awaits shutdown.
- **Modules:**
    - `CoreModule` – reactor and executor (virtual threads).
    - `WebModule` – HTTP server, HTTP/WebSocket clients, health and readiness routes, DNS.
    - `ClientModule` – AMQP publisher lifecycle.
    - `BybitSpotModule` – provides two Spot `BybitStream` beans `@Named("bybitSpotBtcUsdtStream")`,
      `@Named("bybitSpotEthUsdtStream")` + consumers `BybitSpotBtcUsdtConsumer`, `BybitSpotEthUsdtConsumer`.
    - `BybitLinearModule` – provides two Linear `BybitStream` beans `@Named("bybitLinearBtcUsdtStream")`,
      `@Named("bybitLinearEthUsdtStream")` + consumers `BybitLinearBtcUsdtConsumer`, `BybitLinearEthUsdtConsumer`.
    - `CmcParserModule` – CMC HTTP parser + consumer.
- **Publishing:** `AmqpPublisher` routes payloads to configured streams based on provider/source. Thread-safe
  `isReady()` check ensures consistent state visibility across threads.
- **Consumer abstraction:** `AbstractBybitStreamConsumer` base class provides common lifecycle logic for all Bybit
  stream consumers.

## Requirements

- Java 25 JDK (Temurin recommended) to build
- Maven 3.6+
- RabbitMQ Streams 3.9+ (for production deployment)
- Outbound network access to Bybit and CoinMarketCap APIs

**Note**: The service includes comprehensive configuration validation and security features that are enforced at startup.
All sensitive credentials must be provided via environment variables or system properties. See [SECURITY.md](SECURITY.md) for details.

## Configuration

Defaults are loaded from `src/main/resources/application.properties` via `AppConfig`.

- Environment variables and JVM system properties override the bundled defaults at startup.
- Podman Compose loads environment variables from env_file entries. This repository's compose uses:
  `secret/parser-client.env` (see the "Podman Compose (with secrets)" section).
- No rebuild is required when changing configuration via env vars or `-D` system properties; a restart is sufficient.
- **Configuration Validation**: All configuration is validated at startup by `ConfigValidator`. Invalid or missing
  configuration will prevent the application from starting with descriptive error messages. Validation includes:
  - Hostname/IP address format validation
  - Port range validation (1-65535)
  - Timeout range validation (100-60000ms)
  - Required field validation (non-null, non-blank)

See [SECURITY.md](SECURITY.md) for complete validation rules and security best practices.

  Property-to-env mapping (dot to underscore, uppercased) examples:

    - `server.port` → `SERVER_PORT`
    - `amqp.rabbitmq.host` → `AMQP_RABBITMQ_HOST`
    - `amqp.rabbitmq.username` → `AMQP_RABBITMQ_USERNAME`
    - `amqp.rabbitmq.password` → `AMQP_RABBITMQ_PASSWORD`
    - `amqp.stream.port` → `AMQP_STREAM_PORT`
    - `dns.address` → `DNS_ADDRESS`
    - `dns.timeout.ms` → `DNS_TIMEOUT_MS`
    - `bybit.stream.module.enabled` → `BYBIT_STREAM_MODULE_ENABLED`
    - `cmc.parser.module.enabled` → `CMC_PARSER_MODULE_ENABLED`

- **Server**
    - `server.port=8081`

- **Modules**
    - `cmc.parser.module.enabled=true` – Enable CoinMarketCap metrics parser (`CmcParserModule`). Set to `false`
      to disable.
    - `bybit.stream.module.enabled=false` – Enable Bybit public streams publishers (`BybitSpotModule` and
      `BybitLinearModule`). Set to `true` to enable both Spot and Linear stream modules.

- **DNS**
    - `dns.address=8.8.8.8`
    - `dns.timeout.ms=10000`
- **RabbitMQ (Streams)**
    - `amqp.rabbitmq.host=localhost`
    - `amqp.rabbitmq.username=crypto_scout_mq`
    - `amqp.rabbitmq.password=`
    - `amqp.stream.port=5552`
    - `amqp.bybit.stream=bybit-stream` (Bybit WebSocket stream data)
    - `amqp.crypto.scout.stream=crypto-scout-stream` (CMC parser data)
- **Bybit connection**
    - `bybit.connect.timeout.ms=10000`
    - `bybit.initial.reconnect.interval.ms=100`
    - `bybit.max.reconnect.interval.ms=30000`
    - `bybit.max.reconnect.attempts=15`
    - `bybit.backoff.multiplier=1.5`
    - `bybit.ping.interval.ms=20000`
    - `bybit.pong.timeout.ms=15000`
    - `bybit.fast.reconnect.attempts=3`
    - `bybit.fetch.start.at=04:00`
    - `bybit.fetch.interval.min=1440`
    - `bybit.fetch.attempts=4`
    - `bybit.fetch.attempts.delay=15`
    - `bybit.circuit.breaker.threshold=5`
    - `bybit.circuit.breaker.timeout.ms=30000`
    - `bybit.reconnect.rate.limit.ms=1000`
    - `bybit.rest.rate.limit.ms=100`
    - `bybit.auth.expires.ms=10000`
    - `bybit.api.key=`
    - `bybit.api.secret=`
- **CoinMarketCap**
    - `cmc.connect.timeout.ms=10000`
    - `cmc.fetch.start.at=04:00`
    - `cmc.fetch.interval.min=1440`
    - `cmc.fetch.attempts=4`
    - `cmc.fetch.attempts.delay=15`
    - `cmc.circuit.breaker.threshold=5`
    - `cmc.circuit.breaker.timeout.ms=30000`
    - `cmc.rate.limit.ms=100`
    - `cmc.api.key=`

## DNS configuration

Configure the DNS client with `dns.address` (resolver address) and `dns.timeout.ms` (milliseconds).

## Build

```bash
mvn clean package -DskipTests
```

This produces a shaded JAR at `target/crypto-scout-client-0.0.1.jar`.

## Testing

Run unit tests:

```bash
mvn test
```

**Test Status**: All tests passing (28/28, 100% success rate) as of January 26, 2026.

The test suite includes comprehensive validation of the enhanced security and code quality improvements:

- **Config tests:** `AmqpConfigTest`, `CmcConfigTest`, `WebConfigTest` – verify configuration loading and validation.
- **Publisher tests:** `AmqpPublisherTest` – verify creation, readiness state, publish routing, and validation.
- **Consumer tests:** `BybitSpotBtcUsdtConsumerTest`, `BybitSpotEthUsdtConsumerTest`, `BybitLinearBtcUsdtConsumerTest`,
  `BybitLinearEthUsdtConsumerTest`, `CmcParserConsumerTest` – verify constructor validation and null safety.

All test classes follow JUnit 6/Jupiter patterns with `@BeforeAll` and `@AfterAll` lifecycle methods, and test methods
follow the `should<Subject><Action>` naming convention for clarity.

## Run (local)

```bash
java -jar target/crypto-scout-client-0.0.1.jar
```

Health check:

```bash
curl -fsS http://localhost:8081/health
# ok
```

Readiness check:

```bash
curl -fsS -o /dev/null -w "%{http_code}\n" http://localhost:8081/health
# 200 when RabbitMQ Streams is initialized; 503 otherwise
```

## Container image (Podman or Docker)

The provided `Dockerfile` uses Temurin JRE 25 and runs the shaded JAR.

- Non-root user: UID/GID `10001` (user `app`).
- STOPSIGNAL: `SIGTERM` for graceful shutdown.
- JVM options: `ENV JAVA_TOOL_OPTIONS="-XX:+ExitOnOutOfMemoryError -XX:MaxRAMPercentage=70"`.
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

3) Create and populate env file:

```bash
cp secret/client.env.example secret/parser-client.env
$EDITOR secret/parser-client.env
```

4) Start with compose:

```bash
podman-compose -f podman-compose.yml up -d
# or
podman compose -f podman-compose.yml up -d
```

5) Check readiness and logs:

```bash
# Readiness is verified via container healthcheck (no host port exposed)
podman logs -f crypto-scout-parser-client
podman inspect --format='{{.State.Health.Status}}' crypto-scout-parser-client
```

Notes:

- The app reads defaults from `src/main/resources/application.properties`, then applies runtime overrides from
  environment variables and JVM system properties. With Podman Compose, `secret/parser-client.env` is injected as
  env vars.
- Real secrets should be placed in `secret/parser-client.env` (ignored by Git). Never commit real credentials.
- To apply config changes, edit the env files and restart: `podman compose -f podman-compose.yml up -d`.
- The service does not expose ports to the host; it is accessible only within the `crypto-scout-bridge` network.
  Other containers on the same network can reach it via `crypto-scout-parser-client:8081`.
- If RabbitMQ runs on your host machine, set `AMQP_RABBITMQ_HOST=host.containers.internal` in the env file so the
  container can reach the host.
- Build context optimization: see `.dockerignore` (excludes `.git/`, `.idea/`, `.vscode/`, `secret/`, `doc/`, `dev/`,
  `*.iml`, `.mvn/`, `*.log`, `dependency-reduced-pom.xml`, `target/*` with `!target/*.jar`).

- Compose hardening in `podman-compose.yml`:
    - `init: true`
    - `pids_limit: 256`
    - `ulimits.nofile: 4096`
    - `stop_signal: SIGTERM`
    - `stop_grace_period: 30s`
    - healthcheck `start_period: 30s`
    - `read_only` rootfs with `tmpfs: /tmp size=512m (nodev,nosuid)` (increase if enabling JVM heap dumps)
    - `cap_drop: ALL`
    - `security_opt: no-new-privileges=true`
    - `cpus: 0.5`, `mem_limit: 256m`, `mem_reservation: 128m`
    - `restart: unless-stopped`
    - `environment: TZ=UTC`

## Production notes

- **Java version alignment:** Build targets Java 25 and Docker image uses JRE 25 — aligned.
- **Code quality enhancements:** Comprehensive null safety improvements, consistent exception handling using
  `IllegalStateException`, and robust configuration validation with constants organization prevent runtime errors.
- **Security features:** Constructor validation, hostname/IP validation, port and timeout range validation, and
  credential management guidelines ensure production-grade security.
    - `amqp.bybit.stream` (Bybit WebSocket stream data)
    - `amqp.crypto.scout.stream` (CMC parser data)
- **Module toggles:** Control active modules with `cmc.parser.module.enabled` (default `true`) and
  `bybit.stream.module.enabled` (default `false`) in `application.properties`. Evaluated in `Client.getModule()`
  at startup.
- **DNS resolver:** Configure the DNS client with `dns.address` (resolver address) and `dns.timeout.ms` (milliseconds).
- **Secrets:** Do not commit secrets. Keep API keys/passwords empty in the repository and inject values securely at
  runtime via environment variables (e.g., `secret/client.env` with Podman Compose or your orchestrator’s secret store).
  Rebuilds are not required for config/secrets changes—restart with updated env.
- **Health endpoint:** `GET /health` returns `ok` when RabbitMQ Streams environment and producers are initialized;
  otherwise HTTP 503 `not-ready`. The readiness check uses thread-safe volatile field access for accurate state
  reporting. Use for both liveness and readiness checks.
- **Observability:** SLF4J API with a logging binding provided transitively by `jcryptolib`; logs are emitted by
  default. To customize levels/format or switch backend, include your preferred SLF4J binding and configuration on the
  classpath. JMX is enabled via ActiveJ `JmxModule`.
- **JVM tuning:** Image sets `-XX:MaxRAMPercentage=70`. To enable heap dumps on OOM, add
  `-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/tmp` to `JAVA_TOOL_OPTIONS`. Ensure `/tmp` tmpfs in
  `podman-compose.yml` is large enough (for example, `size=1g`).

## Logging

The service uses the SLF4J API with a binding provided transitively by `jcryptolib`, so logs are emitted by default.
If you need different formatting/levels or a different backend, include your preferred SLF4J binding and its
configuration on the classpath. JMX is enabled via ActiveJ `JmxModule`.

## Security & Validation

For comprehensive security guidelines, credential management, and configuration validation documentation,
see [SECURITY.md](SECURITY.md). This document covers:

- Credential management best practices
- Configuration validation rules (hostname, ports, timeouts)
- Null safety and exception handling improvements
- Security best practices and monitoring
- Configuration file security (.gitignore, warning comments)

## License

MIT License. See `LICENSE`.