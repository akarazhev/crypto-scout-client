---
name: podman-deployment
description: Podman Compose deployment patterns for crypto-scout-client containerization and RabbitMQ integration
license: MIT
compatibility: opencode
metadata:
  tools: podman
  services: rabbitmq
  domain: deployment
  version: "0.0.1"
---

## What I Do

Guide containerized deployment of crypto-scout-client with Podman, including RabbitMQ Streams integration.

## Container Configuration

### Service Name
- **Service**: `crypto-scout-parser-client`
- **Image**: `crypto-scout-client:0.0.1`
- **Container Name**: `crypto-scout-parser-client`

### Resource Limits
| Resource | Value |
|----------|-------|
| CPUs | 0.5 |
| Memory Limit | 256m |
| Memory Reservation | 128m |
| PIDs Limit | 256 |
| Open Files (soft/hard) | 4096 |

### Security Hardening
```yaml
security_opt:
  - no-new-privileges=true
read_only: true
cap_drop:
  - ALL
user: "10001:10001"
init: true
```

### tmpfs Configuration
```yaml
tmpfs:
  - /tmp:rw,size=512m,mode=1777,nodev,nosuid
```

### Health Check
```yaml
healthcheck:
  test: [ "CMD-SHELL", "curl -f http://localhost:8081/health || exit 1" ]
  interval: 10s
  timeout: 3s
  retries: 5
  start_period: 30s
```

### Lifecycle Settings
```yaml
restart: unless-stopped
stop_signal: SIGTERM
stop_grace_period: 30s
```

## Build & Deploy

### Build Shaded JAR
```bash
mvn clean package -DskipTests
```

### Build Container Image
```bash
podman build -t crypto-scout-client:0.0.1 .
```

### Create Network (once)
```bash
podman network create crypto-scout-bridge
```

### Run with Compose
```bash
podman-compose up -d
```

### Check Health
```bash
podman inspect --format='{{.State.Health.Status}}' crypto-scout-parser-client
```

## Environment Configuration

Secrets are loaded from `secret/parser-client.env` (gitignored).

### Required Environment Variables
| Variable | Description |
|----------|-------------|
| `AMQP_RABBITMQ_PASSWORD` | **Required** - RabbitMQ password |
| `CMC_API_KEY` | **Required** - CoinMarketCap API key |

### Optional Environment Variables
| Variable | Default | Description |
|----------|---------|-------------|
| `SERVER_PORT` | `8081` | HTTP server port |
| `AMQP_RABBITMQ_HOST` | `localhost` | RabbitMQ host |
| `AMQP_RABBITMQ_USERNAME` | `crypto_scout_mq` | RabbitMQ user |
| `AMQP_STREAM_PORT` | `5552` | RabbitMQ Streams port |
| `BYBIT_API_KEY` | - | Bybit API key |
| `BYBIT_API_SECRET` | - | Bybit API secret |
| `CMC_PARSER_MODULE_ENABLED` | `true` | Enable CMC parser |
| `BYBIT_STREAM_MODULE_ENABLED` | `false` | Enable Bybit streams |
| `TZ` | `UTC` | Timezone |

## Secrets Management

Secrets are managed via env files:
- `secret/parser-client.env` - Runtime secrets (gitignored)
- `secret/client.env.example` - Template for secrets

```bash
cp secret/client.env.example secret/parser-client.env
$EDITOR secret/parser-client.env
```

Example `secret/parser-client.env`:
```bash
AMQP_RABBITMQ_PASSWORD=your-secure-password
CMC_API_KEY=your-cmc-api-key
BYBIT_API_KEY=your-bybit-key
BYBIT_API_SECRET=your-bybit-secret
```

## Local Development

### Run Locally (after build)
```bash
java -jar target/crypto-scout-client-0.0.1.jar
```

### With Environment Variables
```bash
export AMQP_RABBITMQ_PASSWORD="secure-password"
export CMC_API_KEY="your-cmc-key"
java -jar target/crypto-scout-client-0.0.1.jar
```

### Health Check
```bash
curl -fsS http://localhost:8081/health
```

## RabbitMQ Integration

### External RabbitMQ
- **Streams Port**: 5552
- **User/Password**: Configured via env vars
- **Streams**: `bybit-stream`, `crypto-scout-stream`

### For Host RabbitMQ
When RabbitMQ runs on the host (not in container):
```bash
AMQP_RABBITMQ_HOST=host.containers.internal
```

## Troubleshooting

### Container Not Starting
- Verify Podman is installed: `podman --version`
- Check podman-compose: `podman-compose --version`
- Check logs: `podman logs crypto-scout-parser-client`
- Verify secrets file exists: `secret/parser-client.env`

### RabbitMQ Streams Not Reachable
- Confirm port 5552 is accessible
- For host RabbitMQ: `AMQP_RABBITMQ_HOST=host.containers.internal`
- Verify Streams plugin is enabled on RabbitMQ
- Check network connectivity: `podman network inspect crypto-scout-bridge`

### Health Check Failing
- Check RabbitMQ connectivity
- Verify streams exist: `bybit-stream`, `crypto-scout-stream`
- Check credentials in env file
- Check logs for startup errors

### Out of Memory
- Increase tmpfs size if enabling JVM heap dumps:
  ```yaml
  tmpfs:
    - /tmp:rw,size=1g,mode=1777,nodev,nosuid
  ```
- Increase memory limit:
  ```yaml
  mem_limit: "512m"
  ```

## When to Use Me

Use this skill when:
- Building and deploying the container image
- Configuring Podman Compose for production
- Troubleshooting container or connectivity issues
- Setting up CI/CD pipelines
- Managing secrets and environment configuration
- Tuning resource limits and security settings
