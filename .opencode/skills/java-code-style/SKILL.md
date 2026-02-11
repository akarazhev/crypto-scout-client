---
name: java-code-style
description: Java 25 code style conventions for crypto-scout-client including naming, imports, error handling, and testing patterns
license: MIT
compatibility: opencode
metadata:
  language: java
  version: "25"
  project: crypto-scout-client
  domain: style-guide
---

## What I Do

Enforce consistent code style across the crypto-scout-client microservice following established Java 25 conventions.

## File Structure

```
1-23:   MIT License header (see below)
25:     Package declaration
26:     Blank line
27+:    Imports (java.* → third-party → static, blank lines between groups)
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

## Import Organization

```java
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.LinkedList;

import com.github.akarazhev.jcryptolib.config.AppConfig;
import io.activej.inject.module.Module;
import io.activej.launcher.Launcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.github.akarazhev.cryptoscout.Constants.Module.CMC_PARSER_MODULE_ENABLED;
import static io.activej.inject.module.Modules.combine;
import static org.junit.jupiter.api.Assertions.assertEquals;
```

## Naming Conventions

| Element | Convention | Example |
|---------|------------|---------|
| Classes | PascalCase | `AmqpPublisher`, `BybitSpotBtcUsdtConsumer` |
| Methods | camelCase with verb | `publish`, `consume`, `validate` |
| Constants | UPPER_SNAKE_CASE | `AMQP_RABBITMQ_HOST`, `SERVER_PORT` |
| Parameters/locals | `final var` | `final var timeout`, `final var data` |
| Test classes | `<ClassName>Test` | `AmqpPublisherTest` |
| Test methods | `should<Subject><Action>` | `shouldPublishPayloadToStream` |

## Access Modifiers

### Utility Classes
```java
final class Constants {
    private Constants() {
        throw new UnsupportedOperationException();
    }

    static final String PATH_SEPARATOR = "/";

    final static class AmqpConfig {
        private AmqpConfig() {
            throw new UnsupportedOperationException();
        }

        static final String AMQP_RABBITMQ_HOST = "amqp.rabbitmq.host";
        static final String AMQP_STREAM_PORT = "amqp.stream.port";
    }
}
```

### Factory Pattern
```java
public final class AmqpPublisher extends AbstractReactive implements ReactiveService {
    private final Executor executor;
    private volatile Producer producer;

    public static AmqpPublisher create(final NioReactor reactor, final Executor executor) {
        return new AmqpPublisher(reactor, executor);
    }

    private AmqpPublisher(final NioReactor reactor, final Executor executor) {
        super(reactor);
        this.executor = executor;
    }
}
```

## Error Handling

### Unchecked Exceptions
```java
if (resource == null) {
    throw new IllegalStateException("Resource not found: " + path);
}
```

### Try-with-Resources
```java
try (final var conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);
     final var st = conn.createStatement();
     final var rs = st.executeQuery(SELECT_ONE)) {
    return rs.next();
} catch (final SQLException e) {
    return false;
}
```

### Interrupt Handling
```java
try {
    Thread.sleep(duration.toMillis());
} catch (final InterruptedException e) {
    Thread.currentThread().interrupt();
}
```

### Exception Chaining
```java
throw new IllegalStateException("Failed to initialize service", e);
```

## Logging

```java
private static final Logger LOGGER = LoggerFactory.getLogger(ClassName.class);

LOGGER.info("Connected to stream: {}", streamName);
LOGGER.warn("Connection lost, retrying...");
LOGGER.error("Failed to start publisher", ex);
```

## Testing (JUnit 6/Jupiter)

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
        final var publisher = createPublisher();
        final var result = publisher.publish(payload);
        assertTrue(result);
    }
}
```

## Configuration Pattern

```java
static final String VALUE = System.getProperty("property.key", "defaultValue");
static final int PORT = Integer.parseInt(System.getProperty("port.key", "8081"));
static final long TIMEOUT_MS = Long.getLong("timeout.key", 3000L);
static final Duration TIMEOUT = Duration.ofMinutes(Long.getLong("timeout.min.key", 3L));
```

## Module-Specific Patterns

### Module Constants Organization
```java
final class Constants {
    private Constants() {
        throw new UnsupportedOperationException();
    }

    final static class Module {
        private Module() {
            throw new UnsupportedOperationException();
        }

        static final String CMC_PARSER_MODULE_ENABLED = "cmc.parser.module.enabled";
        static final String BYBIT_STREAM_MODULE_ENABLED = "bybit.stream.module.enabled";
    }
}
```

### Config Constants Organization
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
        static final String AMQP_RABBITMQ_USERNAME = "amqp.rabbitmq.username";
        static final String AMQP_RABBITMQ_PASSWORD = "amqp.rabbitmq.password";
        static final String AMQP_STREAM_PORT = "amqp.stream.port";
        static final String AMQP_BYBIT_STREAM = "amqp.bybit.stream";
        static final String AMQP_CRYPTO_SCOUT_STREAM = "amqp.crypto.scout.stream";
    }

    final static class WebConfig {
        private WebConfig() {
            throw new UnsupportedOperationException();
        }

        static final String SERVER_PORT = "server.port";
        static final String DNS_ADDRESS = "dns.address";
        static final String DNS_TIMEOUT_MS = "dns.timeout.ms";
        static final int PORT_MIN = 1;
        static final int PORT_MAX = 65535;
        static final int DNS_TIMEOUT_MIN_MS = 100;
        static final int DNS_TIMEOUT_MAX_MS = 60000;
        static final String HOSTNAME_PATTERN = "^(([0-9]{1,3}\\.){3}[0-9]{1,3})|([a-zA-Z0-9.-]+)$";
    }
}
```

## When to Use Me

Use this skill when:
- Writing new Java classes or methods
- Reviewing code for style compliance
- Understanding project conventions
- Organizing imports and file structure
- Implementing error handling patterns
- Creating configuration constants
