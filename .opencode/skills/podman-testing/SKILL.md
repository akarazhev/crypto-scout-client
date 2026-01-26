---
name: podman-testing
description: Podman Compose integration testing patterns for TimescaleDB and RabbitMQ container management
license: MIT
compatibility: opencode
metadata:
  tools: podman
  services: timescaledb,rabbitmq
  domain: integration-testing
---

## What I Do

Guide integration testing with Podman-managed containers for TimescaleDB (PostgreSQL 17) and RabbitMQ 4.1.4.

## Container Services

### TimescaleDB (crypto-scout-collector-db)
- **Image**: `timescale/timescaledb:latest-pg17`
- **Port**: `127.0.0.1:5432:5432`
- **Database**: `crypto_scout`
- **User/Password**: `crypto_scout_db` / `crypto_scout_db`
- **Schema**: `crypto_scout`

### RabbitMQ (crypto-scout-mq)
- **Image**: `rabbitmq:4.1.4-management`
- **AMQP Port**: `127.0.0.1:5672:5672`
- **Streams Port**: `127.0.0.1:5552:5552`
- **User/Password**: `crypto_scout_mq` / `crypto_scout_mq`
- **Default Stream**: `bybit-stream`

## Usage in Tests

```java
import com.github.akarazhev.cryptoscout.test.PodmanCompose;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

final class IntegrationTest {
    
    @BeforeAll
    static void setUp() {
        PodmanCompose.up();  // Starts containers, waits for readiness
    }
    
    @AfterAll
    static void tearDown() {
        PodmanCompose.down();  // Stops containers, removes volumes
    }
    
    @Test
    void shouldConnectToDatabase() throws Exception {
        assertTrue(DBUtils.canConnect());
    }
}
```

## Configuration Properties

| Property | Default | Description |
|----------|---------|-------------|
| `podman.compose.cmd` | `podman-compose` | Podman Compose binary |
| `podman.cmd` | `podman` | Podman binary |
| `podman.compose.up.timeout.min` | `3` | Startup timeout (minutes) |
| `podman.compose.down.timeout.min` | `1` | Shutdown timeout (minutes) |
| `podman.compose.ready.interval.sec` | `2` | Readiness check interval |

## Database Tables

### Bybit Spot
- `crypto_scout.bybit_spot_tickers`
- `crypto_scout.bybit_spot_kline_1m`, `_5m`, `_15m`, `_60m`, `_240m`, `_1d`
- `crypto_scout.bybit_spot_public_trade`
- `crypto_scout.bybit_spot_order_book_1`, `_50`, `_200`, `_1000`

### Bybit Linear
- `crypto_scout.bybit_linear_tickers`
- `crypto_scout.bybit_linear_kline_1m`, `_5m`, `_15m`, `_60m`, `_240m`, `_1d`
- `crypto_scout.bybit_linear_public_trade`
- `crypto_scout.bybit_linear_order_book_1`, `_50`, `_200`, `_1000`
- `crypto_scout.bybit_linear_all_liquidation`

### Crypto Scout
- `crypto_scout.cmc_fgi`
- `crypto_scout.cmc_kline_1d`, `_1w`
- `crypto_scout.bybit_lpl`
- `crypto_scout.btc_price_risk`
- `crypto_scout.btc_risk_price`

## Resource Files

Located in `src/main/resources/podman/`:
- `podman-compose.yml` - Service definitions
- `script/init.sql` - Database initialization
- `script/bybit_spot_tables.sql` - Spot table schemas
- `script/bybit_linear_tables.sql` - Linear table schemas
- `script/crypto_scout_tables.sql` - Crypto scout table schemas
- `rabbitmq/enabled_plugins` - RabbitMQ plugins
- `rabbitmq/rabbitmq.conf` - RabbitMQ configuration
- `rabbitmq/definitions.json` - RabbitMQ definitions

## Running Tests

```bash
# Standard test run
mvn test

# With extended timeout for slow environments
mvn -q -Dpodman.compose.up.timeout.min=5 test

# Custom database URL
mvn -q -Dtest.db.jdbc.url=jdbc:postgresql://localhost:5432/crypto_scout test
```

## Troubleshooting

### Container not starting
- Verify Podman is installed: `podman --version`
- Check podman-compose: `podman-compose --version`
- Increase timeout: `-Dpodman.compose.up.timeout.min=10`

### Database not reachable
- Confirm port 5432 is free
- Check credentials match defaults
- Verify container is running: `podman ps`

### RabbitMQ Streams not reachable
- Confirm port 5552 is free
- Check `rabbitmq.conf` advertises `localhost`
- Verify Streams plugin is enabled

## When to Use Me

Use this skill when:
- Setting up integration tests with containers
- Troubleshooting container startup issues
- Understanding the test infrastructure
- Configuring Podman for CI/CD environments
- Managing database state between tests
