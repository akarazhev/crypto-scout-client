# AGENTS.md

This document provides guidelines for agentic coding contributors to the crypto-scout-client module.

## Project Overview

**crypto-scout-client** is a Java 25 Maven microservice that collects cryptocurrency market data from Bybit (WebSocket streaming) and CoinMarketCap (REST API), then publishes structured events to RabbitMQ Streams. Built on ActiveJ for fully async I/O.

## MCP Server Configuration

This module uses the **Context7 MCP server** for enhanced code intelligence and documentation retrieval.

### Available MCP Tools

When working with this codebase, you can use the following MCP tools via the context7 server:

- **resolve-library-id**: Resolve a library name to its Context7 library ID
- **get-library-docs**: Retrieve up-to-date documentation for a library by its ID

### Configuration

The MCP server is configured in `.opencode/package.json`:

```json
{
  "mcp": {
    "context7": {
      "type": "remote",
      "url": "https://mcp.context7.com/mcp",
      "headers": {
        "CONTEXT7_API_KEY": "ctx7sk-4cec80b8-d947-4ff4-a29a-d00bea5a2fac"
      },
      "enabled": true
    }
  }
}
```

### Usage Guidelines

1. **ActiveJ Framework**: Use `resolve-library-id` for "activej" to get async HTTP client patterns, WebSocket implementation guides, and Promise-based programming models.

2. **RabbitMQ Streams**: Retrieve publisher documentation for stream routing, message confirmation patterns, and producer configuration.

3. **WebSocket APIs**: Access WebSocket client documentation for connection management, ping/pong handling, and reconnection strategies.

4. **REST API Integration**: Get HTTP client best practices for rate-limited API consumption (CoinMarketCap integration).

### Module Structure

```
crypto-scout-client/
├── src/main/java/com/github/akarazhev/cryptoscout/
│   ├── Client.java                    # Launcher (ActiveJ)
│   ├── Constants.java                 # Module constants
│   ├── client/                        # Data consumers
│   │   ├── AbstractBybitStreamConsumer.java
│   │   ├── AmqpPublisher.java
│   │   ├── BybitLinearBtcUsdtConsumer.java
│   │   ├── BybitLinearEthUsdtConsumer.java
│   │   ├── BybitSpotBtcUsdtConsumer.java
│   │   ├── BybitSpotEthUsdtConsumer.java
│   │   └── CmcParserConsumer.java
│   ├── config/                        # Configuration
│   │   ├── AmqpConfig.java
│   │   ├── CmcApiConfig.java
│   │   ├── ConfigValidator.java
│   │   ├── Constants.java
│   │   └── WebConfig.java
│   └── module/                        # ActiveJ DI modules
│       ├── BybitLinearModule.java
│       ├── BybitSpotModule.java
│       ├── ClientModule.java
│       ├── CmcParserModule.java
│       ├── Constants.java
│       ├── CoreModule.java
│       └── WebModule.java
├── src/main/resources/
│   └── application.properties         # Configuration defaults
└── src/test/java/...                  # Unit tests
```

### Key Dependencies

| Dependency | Version | Purpose |
|------------|---------|---------|
| Java | 25 | Language |
| ActiveJ | 6.0-rc2 | Async I/O framework |
| jcryptolib | 0.0.4 | JSON utilities, Bybit/CMC clients |
| RabbitMQ Stream Client | 1.4.0 | Streams protocol |
| AMQP Client | 5.28.0 | AMQP protocol |
| JUnit | 6.1.0-M1 | Testing |

### Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    crypto-scout-client                       │
├─────────────────────────────────────────────────────────────┤
│  Bybit WebSocket  │  CMC REST API   │  HTTP Health Endpoint │
│     (4 streams)   │   (scheduled)   │       (/health)       │
└─────────┬─────────┴────────┬────────┴───────────┬───────────┘
          │                  │                    │
          └──────────────────┼────────────────────┘
                             ▼
                    ┌─────────────────┐
                    │  AmqpPublisher  │
                    │  (RabbitMQ      │
                    │   Streams)      │
                    └────────┬────────┘
                             │
              ┌──────────────┴──────────────┐
              ▼                             ▼
    ┌─────────────────────┐    ┌─────────────────────┐
    │   bybit-stream      │    │  crypto-scout-stream│
    │  (Bybit market data)│    │  (CMC/FGI data)     │
    └─────────────────────┘    └─────────────────────┘
```

## Build, Test, and Lint Commands

### Build
```bash
mvn clean install              # Full build with tests
mvn -q -DskipTests install     # Quick build without tests
```

### Run All Tests
```bash
mvn test
mvn -q test
```

### Run Single Test
```bash
mvn test -Dtest=AmqpPublisherTest
mvn test -Dtest=AmqpPublisherTest#shouldPublishPayloadToStream
mvn -q test -Dtest=BybitSpotBtcUsdtConsumerTest
```

### Run Tests with System Properties
```bash
mvn -q -Dpodman.compose.up.timeout.min=5 test
mvn -q -Damqp.rabbitmq.host=localhost test
```

### Clean
```bash
mvn clean
```

## Code Style Guidelines

### File Structure
```
1-23:   MIT License header
25:     Package declaration
26:     Blank line
27+:    Imports: java.* → third-party → static imports (blank lines between)
        Blank line
        Class/enum/interface declaration
```

### MIT License Header
```java
/*
 * MIT License
 *
 * Copyright (c) 2026 Andrey Karazhev
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
```

### Import Organization
```java
import java.io.IOException;
import java.time.Duration;
import java.util.Map;

import com.rabbitmq.stream.Environment;
import io.activej.promise.Promise;
import io.activej.reactor.nio.NioReactor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.github.akarazhev.cryptoscout.config.Constants.AmqpConfig.AMQP_RABBITMQ_HOST;
```

### Naming Conventions

| Element | Convention | Example |
|---------|------------|---------|
| Classes | PascalCase | `AmqpPublisher`, `BybitSpotBtcUsdtConsumer` |
| Methods | camelCase with verb prefix | `start()`, `publish()`, `isReady()` |
| Constants | UPPER_SNAKE_CASE in nested classes | `AMQP_RABBITMQ_HOST`, `SERVER_PORT` |
| Parameters/locals | `final var` | `final var payload`, `final var stream` |
| Test classes | `<ClassName>Test` suffix | `AmqpPublisherTest` |
| Test methods | `should<Subject><Action>` pattern | `shouldPublishPayloadToStream` |

### Access Modifiers

**Utility Classes:**
```java
final class Constants {
    private Constants() {
        throw new UnsupportedOperationException();
    }

    final static class AmqpConfig {
        private AmqpConfig() {
            throw new UnsupportedOperationException();
        }

        static final String AMQP_RABBITMQ_HOST = "amqp.rabbitmq.host";
    }
}
```

**ActiveJ Service Pattern:**
```java
public final class AmqpPublisher extends AbstractReactive implements ReactiveService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AmqpPublisher.class);
    private final Executor executor;
    private volatile Environment environment;

    public static AmqpPublisher create(final NioReactor reactor, final Executor executor) {
        return new AmqpPublisher(reactor, executor);
    }

    private AmqpPublisher(final NioReactor reactor, final Executor executor) {
        super(reactor);
        this.executor = executor;
    }

    @Override
    public Promise<Void> start() { /* ... */ }

    @Override
    public Promise<Void> stop() { /* ... */ }
}
```

**ActiveJ Module Pattern:**
```java
public final class WebModule extends AbstractModule {
    private WebModule() {}

    public static WebModule create() {
        return new WebModule();
    }

    @Provides
    private IDnsClient dnsClient(final NioReactor reactor) { /* ... */ }

    @Provides
    @Eager
    private HttpServer server(final NioReactor reactor, final AsyncServlet servlet) { /* ... */ }
}
```

### Error Handling

**Unchecked Exceptions:**
```java
if (reactor == null) {
    throw new IllegalStateException("Reactor cannot be null");
}
```

**Try-with-Resources:**
```java
try (final var conn = dataSource.getConnection();
     final var stmt = conn.prepareStatement(sql)) {
    // Process
} catch (final SQLException e) {
    throw new IllegalStateException("Database error", e);
}
```

**Interrupt Handling:**
```java
try {
    Thread.sleep(duration.toMillis());
} catch (final InterruptedException e) {
    Thread.currentThread().interrupt();
}
```

**Exception Chaining:**
```java
try {
    // Operation
} catch (final Exception ex) {
    LOGGER.error("Failed to start AmqpPublisher", ex);
    throw new IllegalStateException("Failed to start AmqpPublisher", ex);
}
```

### Logging
```java
private static final Logger LOGGER = LoggerFactory.getLogger(ClassName.class);

LOGGER.info("Service started on port {}", port);
LOGGER.warn("Connection lost, retrying...");
LOGGER.error("Failed to process message", exception);
LOGGER.debug("Skipping publish: no stream route for provider={} source={}", provider, source);
```

### Configuration Pattern
```java
// In Constants.java
final static class AmqpConfig {
    private AmqpConfig() {
        throw new UnsupportedOperationException();
    }

    static final String AMQP_RABBITMQ_HOST = "amqp.rabbitmq.host";
    static final String AMQP_RABBITMQ_USERNAME = "amqp.rabbitmq.username";
    static final String AMQP_STREAM_PORT = "amqp.stream.port";
}

// In AmqpConfig.java
public final class AmqpConfig {
    private AmqpConfig() {
        throw new UnsupportedOperationException();
    }

    private static String getAmqpRabbitmqHost() {
        return AppConfig.getAsString(Constants.AmqpConfig.AMQP_RABBITMQ_HOST);
    }

    public static Environment getEnvironment() {
        return Environment.builder()
                .host(getAmqpRabbitmqHost())
                .port(getAmqpStreamPort())
                .username(getAmqpRabbitmqUsername())
                .password(getAmqpRabbitmqPassword())
                .build();
    }
}
```

### Testing (JUnit 6/Jupiter)
```java
final class AmqpPublisherTest {

    @BeforeAll
    static void setUp() {
        PodmanCompose.up();
    }

    @AfterAll
    static void tearDown() {
        PodmanCompose.down();
    }

    @Test
    void shouldPublishPayloadToStream() throws Exception {
        final var result = service.doSomething();
        assertNotNull(result);
        assertEquals(expected, result);
    }
}
```

### ActiveJ Patterns

**Reactive Service:**
```java
public final class Consumer extends AbstractReactive implements ReactiveService {
    public static Consumer create(final NioReactor reactor, /* deps */) {
        return new Consumer(reactor, /* deps */);
    }

    private Consumer(final NioReactor reactor, /* deps */) {
        super(reactor);
        // Validate dependencies
        if (dep == null) {
            throw new IllegalStateException("Dep cannot be null");
        }
    }

    @Override
    public Promise<Void> start() {
        return upstream.start().then(stream ->
            stream.streamTo(StreamConsumers.ofConsumer(this::process)));
    }

    @Override
    public Promise<Void> stop() {
        return upstream.stop();
    }
}
```

**Stream Publishing:**
```java
public Promise<Void> publish(final Payload<Map<String, Object>> payload) {
    final var settablePromise = new SettablePromise<Void>();
    try {
        final var message = producer.messageBuilder()
                .addData(JsonUtils.object2Bytes(payload))
                .build();
        producer.send(message, status ->
            reactor.execute(() -> {
                if (status.isConfirmed()) {
                    settablePromise.set(null);
                } else {
                    settablePromise.setException(
                        new IllegalStateException("Publish not confirmed: " + status));
                }
            })
        );
    } catch (final Exception ex) {
        LOGGER.error("Failed to publish", ex);
        settablePromise.setException(ex);
    }
    return settablePromise;
}
```

### Concurrency
- **Volatile fields**: For lazy-initialized singleton-style fields
- **Thread naming**: Provide names for background threads
- **Interruption**: Always restore interrupt status when catching `InterruptedException`
- **Executor**: Use injected `Executor` for blocking operations with `Promise.ofBlocking()`

### Resource Management
- **Try-with-resources**: Required for all closeable resources
- **Null checks**: Throw `IllegalStateException` for null resources
- **Graceful shutdown**: Close resources in `stop()` method, set volatile fields to null

## Configuration Reference

### application.properties

| Property | Default | Description |
|----------|---------|-------------|
| `cmc.parser.module.enabled` | `true` | Enable CMC parser module |
| `bybit.stream.module.enabled` | `false` | Enable Bybit streaming modules |
| `server.port` | `8081` | HTTP server port |
| `dns.address` | `8.8.8.8` | DNS server address |
| `amqp.rabbitmq.host` | `localhost` | RabbitMQ host |
| `amqp.rabbitmq.username` | `crypto_scout_mq` | RabbitMQ username |
| `amqp.rabbitmq.password` | *(required)* | RabbitMQ password |
| `amqp.stream.port` | `5552` | RabbitMQ Streams port |
| `amqp.bybit.stream` | `bybit-stream` | Bybit stream name |
| `amqp.crypto.scout.stream` | `crypto-scout-stream` | CryptoScout stream name |
| `bybit.api.key` | *(required)* | Bybit API key |
| `bybit.api.secret` | *(required)* | Bybit API secret |
| `cmc.api.key` | *(required)* | CoinMarketCap API key |

### Health Endpoint
```bash
curl http://localhost:8081/health
# Returns: "ready" (HTTP 200) or "not ready" (HTTP 503)
```

## Module-Specific Guidelines

### Client Package (`client/`)
- **AbstractBybitStreamConsumer**: Base class for Bybit WebSocket consumers
- **AmqpPublisher**: Routes payloads to appropriate RabbitMQ Streams based on `Provider` and `Source`
- **Bybit*Consumer**: Concrete consumers for spot/linear BTC/ETH markets
- **CmcParserConsumer**: Processes CMC data, selects latest quote for BTC timeframes

### Config Package (`config/`)
- **AmqpConfig**: RabbitMQ Streams configuration
- **WebConfig**: HTTP server configuration
- **CmcApiConfig**: CoinMarketCap API configuration
- **ConfigValidator**: Validates required configuration at startup
- **Constants**: Configuration property keys

### Module Package (`module/`)
- **CoreModule**: Core ActiveJ dependencies (Reactor, Executor)
- **ClientModule**: Client service bindings
- **BybitSpotModule**: Spot market consumer bindings
- **BybitLinearModule**: Linear market consumer bindings
- **CmcParserModule**: CMC parser bindings
- **WebModule**: HTTP server and health endpoint

## Security Best Practices

- **No hardcoded credentials** - Use environment variables or system properties
- **Secret properties** - Mark with `# WARNING: Must be provided via system property`
- **Validation** - Validate all required configuration at startup in `ConfigValidator`

## License

MIT License - See `LICENSE` file.
