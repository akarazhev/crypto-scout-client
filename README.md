# crypto-scout-client

Java 25 microservice that collects crypto market data from Bybit and CoinMarketCap, publishing to RabbitMQ Streams. Built on ActiveJ for async I/O.

**Status:** ✅ Production Ready

## Features

- **Bybit Streams**: Spot (PMST) and Linear (PML) market data for BTCUSDT/ETHUSDT (tickers, trades, order books, klines)
- **CoinMarketCap**: Fear & Greed Index, BTC/USD quotes (1D, 1W)
- **AMQP Publishing**: Routes data to RabbitMQ Streams based on provider/source
- **Health Endpoint**: `GET /health` returns `ok` (200) when ready, 503 otherwise

## Quick Start

### Build
```bash
mvn clean package -DskipTests
```

### Run
```bash
java -jar target/crypto-scout-client-0.0.1.jar
```

### Health Check
```bash
curl http://localhost:8081/health
```

## Configuration

Configure via environment variables (recommended) or system properties:

| Property | Env Var | Default | Description |
|----------|---------|---------|-------------|
| `server.port` | `SERVER_PORT` | 8081 | HTTP server port |
| `amqp.rabbitmq.host` | `AMQP_RABBITMQ_HOST` | localhost | RabbitMQ host |
| `amqp.rabbitmq.username` | `AMQP_RABBITMQ_USERNAME` | crypto_scout_mq | RabbitMQ user |
| `amqp.rabbitmq.password` | `AMQP_RABBITMQ_PASSWORD` | - | **Required** |
| `amqp.stream.port` | `AMQP_STREAM_PORT` | 5552 | RabbitMQ Streams port |
| `cmc.api.key` | `CMC_API_KEY` | - | CoinMarketCap API key |
| `bybit.api.key` | `BYBIT_API_KEY` | - | Bybit API key |
| `bybit.api.secret` | `BYBIT_API_SECRET` | - | Bybit API secret |
| `cmc.parser.module.enabled` | `CMC_PARSER_MODULE_ENABLED` | true | Enable CMC parser |
| `bybit.stream.module.enabled` | `BYBIT_STREAM_MODULE_ENABLED` | false | Enable Bybit streams |

### Example
```bash
export AMQP_RABBITMQ_PASSWORD="secure-password"
export CMC_API_KEY="your-cmc-key"
java -jar target/crypto-scout-client-0.0.1.jar
```

## Security

**Critical:** Never commit credentials to version control.

- Credentials must be provided via environment variables or system properties
- `application-local.properties` is gitignored for local development
- All sensitive fields in `application.properties` have warning comments
- Configuration is validated at startup (hostname, port ranges, required fields)

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                        Client Launcher                       │
├─────────────┬─────────────┬──────────────┬──────────────────┤
│ CoreModule  │ WebModule   │ ClientModule │ Bybit/CMC Modules│
│ (reactor)   │ (HTTP/API)  │ (publisher)  │ (consumers)      │
└─────────────┴─────────────┴──────────────┴──────────────────┘
                            │
                    ┌───────┴───────┐
                    ▼               ▼
            Bybit WebSocket    CMC HTTP API
                    │               │
                    └───────┬───────┘
                            ▼
                    AmqpPublisher
                            │
                            ▼
                    RabbitMQ Streams
```

**Key Components:**
- `AmqpPublisher` - Thread-safe publisher to RabbitMQ Streams with consistent health checks
- `AbstractBybitStreamConsumer` - Base class for Bybit stream consumers
- `CmcParserConsumer` - Processes CMC data with null-safe quote selection
- `ConfigValidator` - Validates all config at startup with descriptive errors

## Container Deployment

```bash
# Build image
podman build -t crypto-scout-client:0.0.1 .

# Run with compose
podman-compose up -d
```

Features:
- Non-root user (UID 10001)
- Read-only rootfs with tmpfs
- `cap_drop: ALL`, `no-new-privileges`
- Resource limits configured
- Health checks enabled

## Testing

```bash
mvn test
```

## License

MIT License. See `LICENSE`.
