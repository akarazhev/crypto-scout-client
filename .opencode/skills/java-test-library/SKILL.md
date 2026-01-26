---
name: java-test-library
description: Java 25 test library development patterns for crypto-scout-test including MockData, PodmanCompose, and RabbitMQ utilities
license: MIT
compatibility: opencode
metadata:
  language: java
  framework: junit6
  domain: testing
---

## What I Do

Provide guidance for developing and maintaining the crypto-scout-test library, a Java 25 Maven library offering test support utilities.

## Core Components

### MockData
Typed API for loading bundled JSON fixtures:
```java
// Load SPOT 1m klines
var spotKlines = MockData.get(MockData.Source.BYBIT_SPOT, MockData.Type.KLINE_1);
// Load LINEAR tickers
var linearTickers = MockData.get(MockData.Source.BYBIT_LINEAR, MockData.Type.TICKERS);
// Load crypto-scout FGI
var fgi = MockData.get(MockData.Source.CRYPTO_SCOUT, MockData.Type.FGI);
```

**Sources**: `CRYPTO_SCOUT`, `BYBIT_SPOT`, `BYBIT_LINEAR`
**Types**: `KLINE_1`, `KLINE_5`, `KLINE_15`, `KLINE_60`, `KLINE_240`, `KLINE_D`, `KLINE_W`, `TICKERS`, `PUBLIC_TRADE`, `ORDER_BOOK_1`, `ORDER_BOOK_50`, `ORDER_BOOK_200`, `ORDER_BOOK_1000`, `FGI`, `LPL`, `BTC_PRICE_RISK`, `BTC_RISK_PRICE`, `ALL_LIQUIDATION`

### PodmanCompose
Container lifecycle management:
```java
@BeforeAll
static void setUp() {
    PodmanCompose.up();  // Starts TimescaleDB + RabbitMQ, waits for readiness
}

@AfterAll
static void tearDown() {
    PodmanCompose.down();  // Stops containers, removes volumes
}
```

### RabbitMQ Utilities
**Streams Protocol** (port 5552):
- `StreamTestPublisher.create(reactor, executor, environment, stream)`
- `StreamTestConsumer.create(reactor, executor, environment, stream)`

**AMQP Protocol** (port 5672):
- `AmqpTestPublisher.create(reactor, executor, connectionFactory, queue)`
- `AmqpTestConsumer.create(reactor, executor, connectionFactory, queue)`

### DBUtils
```java
// Check connectivity
DBUtils.canConnect();
// Clean tables for test isolation
DBUtils.deleteFromTables(dataSource, "crypto_scout.bybit_spot_tickers", "crypto_scout.bybit_spot_kline_1m");
```

### Assertions
```java
Assertions.assertTableCount("crypto_scout.bybit_spot_tickers", 5);
```

## Configuration

All settings via system properties:

| Property | Default | Description |
|----------|---------|-------------|
| `test.db.jdbc.url` | `jdbc:postgresql://localhost:5432/crypto_scout` | Database URL |
| `test.db.user` | `crypto_scout_db` | Database user |
| `test.db.password` | `crypto_scout_db` | Database password |
| `test.mq.host` | `localhost` | RabbitMQ host |
| `test.mq.port` | `5552` | RabbitMQ Streams port |
| `test.mq.user` | `crypto_scout_mq` | RabbitMQ user |
| `test.mq.password` | `crypto_scout_mq` | RabbitMQ password |
| `podman.compose.up.timeout.min` | `3` | Container startup timeout |
| `podman.compose.down.timeout.min` | `1` | Container shutdown timeout |

## When to Use Me

Use this skill when:
- Implementing new test utilities or mock data fixtures
- Understanding the library's component architecture
- Configuring test environments with Podman containers
- Working with RabbitMQ Streams or AMQP in tests
- Managing database state in integration tests
