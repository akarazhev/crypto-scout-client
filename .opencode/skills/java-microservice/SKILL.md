---
name: java-microservice
description: Java 25 microservice development patterns for crypto-scout-client including modules, AMQP publishing, and WebSocket consumers
license: MIT
compatibility: opencode
metadata:
  language: java
  framework: activej
  version: "0.0.1"
  domain: microservice
---

## What I Do

Provide guidance for developing and maintaining the crypto-scout-client microservice, a Java 25 Maven application that collects crypto market data and publishes to RabbitMQ Streams.

## Architecture

```
Client.java (Launcher)
├── CoreModule - Reactor and executor (virtual threads)
├── ServiceGraphModule - ActiveJ service lifecycle
├── JmxModule - JMX monitoring
├── ClientModule - AMQP publisher lifecycle
├── BybitSpotModule/BybitLinearModule - WebSocket consumers (conditional on bybit.stream.module.enabled)
├── CmcParserModule - HTTP parser consumer (conditional on cmc.parser.module.enabled)
└── WebModule - HTTP server, health endpoint, DNS
```

## Core Components

### Modules
ActiveJ DI modules for separation of concerns:

| Module | Purpose |
|--------|---------|
| `CoreModule` | Reactor and executor (virtual threads) |
| `WebModule` | HTTP server, health routes, DNS configuration |
| `ClientModule` | AMQP publisher lifecycle |
| `BybitSpotModule` | Spot WebSocket streams + consumers (conditional) |
| `BybitLinearModule` | Linear WebSocket streams + consumers (conditional) |
| `CmcParserModule` | CMC HTTP parser + consumer (conditional) |

### AmqpPublisher
Thread-safe publisher that routes structured events to RabbitMQ Streams:
```java
// Routes payloads to configured streams based on provider/source
// Bybit data -> bybit-stream (configured via amqp.bybit.stream)
// CMC data -> crypto-scout-stream (configured via amqp.crypto.scout.stream)
amqpPublisher.publish(payload);
```

### Bybit Stream Consumers
WebSocket consumers extending `AbstractBybitStreamConsumer`:
```java
// AbstractBybitStreamConsumer base class provides common lifecycle
BybitSpotBtcUsdtConsumer   // Spot BTC/USDT market data
BybitSpotEthUsdtConsumer   // Spot ETH/USDT market data
BybitLinearBtcUsdtConsumer // Linear BTC/USDT market data
BybitLinearEthUsdtConsumer // Linear ETH/USDT market data
```

### CmcParserConsumer
HTTP-based data collection from CoinMarketCap:
```java
// Retrieves Fear & Greed Index (API Pro Latest)
// Retrieves BTC/USD quotes (1D, 1W timeframes)
// Publishes to crypto-scout-stream
```

### Health Endpoint
```java
// GET /health - returns "ok" (200) when ready, 503 otherwise
// Used for liveness and readiness checks in container orchestration
```

### ConfigValidator
Validates all configuration at startup:
```java
@Override
protected void onStart() throws Exception {
    ConfigValidator.validate(AppConfig.getAsBoolean(CMC_PARSER_MODULE_ENABLED));
}
```

## Configuration

All settings via system properties with environment variable mapping:

| Property | Env Var | Default | Description |
|----------|---------|---------|-------------|
| `server.port` | `SERVER_PORT` | `8081` | HTTP server port |
| `amqp.rabbitmq.host` | `AMQP_RABBITMQ_HOST` | `localhost` | RabbitMQ host |
| `amqp.stream.port` | `AMQP_STREAM_PORT` | `5552` | RabbitMQ Streams port |
| `amqp.rabbitmq.username` | `AMQP_RABBITMQ_USERNAME` | `crypto_scout_mq` | RabbitMQ user |
| `amqp.rabbitmq.password` | `AMQP_RABBITMQ_PASSWORD` | - | **Required** |
| `amqp.bybit.stream` | - | `bybit-stream` | Bybit data stream name |
| `amqp.crypto.scout.stream` | - | `crypto-scout-stream` | CMC data stream name |
| `cmc.api.key` | `CMC_API_KEY` | - | **Required** - CMC API key |
| `bybit.api.key` | `BYBIT_API_KEY` | - | Bybit API key |
| `bybit.api.secret` | `BYBIT_API_SECRET` | - | Bybit API secret |
| `cmc.parser.module.enabled` | `CMC_PARSER_MODULE_ENABLED` | `true` | Enable CMC module |
| `bybit.stream.module.enabled` | `BYBIT_STREAM_MODULE_ENABLED` | `false` | Enable Bybit modules |

## Module Loading Pattern

```java
@Override
protected Module getModule() {
    final var modules = new LinkedList<Module>();
    modules.add(JmxModule.create());
    modules.add(ServiceGraphModule.create());
    modules.add(CoreModule.create());
    modules.add(ClientModule.create());

    if (AppConfig.getAsBoolean(BYBIT_STREAM_MODULE_ENABLED)) {
        modules.add(BybitSpotModule.create());
        modules.add(BybitLinearModule.create());
    }

    if (AppConfig.getAsBoolean(CMC_PARSER_MODULE_ENABLED)) {
        modules.add(CmcParserModule.create());
    }

    modules.add(WebModule.create());
    return combine(modules);
}
```

## Key Dependencies

| Dependency | Version | Purpose |
|------------|---------|---------|
| jcryptolib | 0.0.4 | JSON utilities, Bybit/CMC clients |
| activej | 6.0-rc2 | Async I/O framework |
| stream-client | 1.4.0 | RabbitMQ Streams protocol |
| junit-jupiter | 6.1.0-M1 | Testing |
| mockito | 5.21.0 | Mocking |

## When to Use Me

Use this skill when:
- Implementing new modules or consumers
- Understanding the microservice architecture
- Configuring AMQP publishing and streams
- Working with Bybit WebSocket or CMC HTTP APIs
- Adding health checks or observability features
- Implementing configuration validation
