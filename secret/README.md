# Secrets for crypto-scout-client

This folder contains templates and guidance for providing runtime secrets and configuration for Podman Compose
deployments.

## Files

- `client.env.example` – Template with all required keys. Copy this to `client.env` and fill with real values.
- `client.env` – Your local/production secrets file (NOT committed). Contains RabbitMQ credentials, API keys, and
  optional overrides.

## How to create `client.env`

1. Copy the example:

   ```bash
   cp secret/client.env.example secret/client.env
   ```
2. Edit `secret/client.env` and fill values:
    - `AMQP_RABBITMQ_HOST`, `AMQP_RABBITMQ_USERNAME`, `AMQP_RABBITMQ_PASSWORD`
    - `AMQP_STREAM_PORT`, stream names
    - `BYBIT_API_KEY`, `BYBIT_API_SECRET` (optional if you do not use Bybit authenticated flows)
    - `CMC_API_KEY` (if CMC metrics are enabled)
    - `SERVER_PORT` if you need a non-default port

## Using with Podman Compose

- Ensure the application image exists or build it:

  ```bash
  podman build -t crypto-scout-client:0.0.1 .
  ```

- Start with compose:

  ```bash
  podman-compose -f podman-compose.yml up -d
  # or
  podman compose -f podman-compose.yml up -d
  ```

- Verify health endpoint:

  ```bash
  curl -fsS http://localhost:8081/health
  # ok
  ```

- View logs:

  ```bash
  podman logs -f crypto-scout-client
  ```

## Notes on configuration

- Defaults are loaded from `src/main/resources/application.properties` using `AppConfig`.
- Runtime overrides are supported: environment variables and JVM system properties take precedence at startup.
- With Podman Compose, `secret/client.env` is injected as environment variables for the container.
- No rebuild is required for config changes via env vars; edit `secret/client.env` and restart the compose stack.

Property-to-env mapping examples (dot → underscore, uppercased):

- `server.port` → `SERVER_PORT`
- `amqp.rabbitmq.host` → `AMQP_RABBITMQ_HOST`
- `amqp.rabbitmq.username` → `AMQP_RABBITMQ_USERNAME`
- `amqp.rabbitmq.password` → `AMQP_RABBITMQ_PASSWORD`
- `amqp.stream.port` → `AMQP_STREAM_PORT`
- `bybit.api.key` → `BYBIT_API_KEY`
- `bybit.api.secret` → `BYBIT_API_SECRET`
- `cmc.api.key` → `CMC_API_KEY`

## Security

- Do NOT commit `secret/client.env` to VCS. `.gitignore` is configured to ignore it.
- Scope RabbitMQ credentials minimally and rotate API keys periodically.
- Prefer rootless Podman and restricted capabilities (already configured in compose).
