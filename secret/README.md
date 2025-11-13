# Secrets for crypto-scout-client

This folder contains templates and guidance for providing runtime secrets and configuration for Podman Compose
deployments.

## Files

- `client.env.example` – Template with all required keys. Copy this to both env files and fill real values.
- `bybit-client.env` – Env file for the Bybit streams service (NOT committed). Contains RabbitMQ credentials, API keys,
  and optional overrides.
- `parser-client.env` – Env file for the Bybit/CMC parser service (NOT committed). Contains RabbitMQ credentials, API
  keys, and optional overrides.

## How to create env files

1. Copy the example:

   ```bash
   cp secret/client.env.example secret/bybit-client.env
   cp secret/client.env.example secret/parser-client.env
   ```
2. Edit the env files and fill values:
    - Common in both files:
        - `AMQP_RABBITMQ_HOST`, `AMQP_RABBITMQ_USERNAME`, `AMQP_RABBITMQ_PASSWORD`
        - `AMQP_STREAM_PORT`
        - `DNS_ADDRESS` (resolver address, e.g., `8.8.8.8`), `DNS_TIMEOUT_MS` (e.g., `10000`)
    - API keys as needed:
        - `BYBIT_API_KEY`, `BYBIT_API_SECRET` (optional if you do not use authenticated flows)
        - `CMC_API_KEY` (if CMC metrics are enabled)
    - Server ports:
        - In `bybit-client.env`, keep `SERVER_PORT=8081` (default) unless you change compose mapping
        - In `parser-client.env`, set `SERVER_PORT=8082` to match `podman-compose.yml`
    - Module toggles:
        - In `bybit-client.env`: `BYBIT_STREAM_MODULE_ENABLED=true`, `BYBIT_PARSER_MODULE_ENABLED=false`,
          `CMC_PARSER_MODULE_ENABLED=false` (recommended)
        - In `parser-client.env`: `BYBIT_PARSER_MODULE_ENABLED=true`, `BYBIT_STREAM_MODULE_ENABLED=false`.
          Leave `CMC_PARSER_MODULE_ENABLED=false` unless you explicitly want CMC metrics in that service

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

- Verify health endpoints:

  ```bash
  # Bybit streams client
  curl -fsS -o /dev/null -w "%{http_code}\n" http://localhost:8081/ready
  # 200 when ready; 503 otherwise

  # Parser client
  curl -fsS -o /dev/null -w "%{http_code}\n" http://localhost:8082/ready
  # 200 when ready; 503 otherwise
  ```

- View logs:

  ```bash
  podman logs -f crypto-scout-bybit-client
  podman logs -f crypto-scout-parser-client
  ```

## Notes on configuration

- Defaults are loaded from `src/main/resources/application.properties` using `AppConfig`.
- Runtime overrides are supported: environment variables and JVM system properties take precedence at startup.
- With Podman Compose, `secret/bybit-client.env` and `secret/parser-client.env` are injected as environment variables
  for their respective containers.
- No rebuild is required for config changes via env vars; edit the env files and restart the compose stack.

Note: RabbitMQ stream names are defined in `src/main/resources/application.properties` and are not configurable via
environment variables.

Property-to-env mapping examples (dot → underscore, uppercased):

- `server.port` → `SERVER_PORT`
- `amqp.rabbitmq.host` → `AMQP_RABBITMQ_HOST`
- `amqp.rabbitmq.username` → `AMQP_RABBITMQ_USERNAME`
- `amqp.rabbitmq.password` → `AMQP_RABBITMQ_PASSWORD`
- `amqp.stream.port` → `AMQP_STREAM_PORT`
- `bybit.api.key` → `BYBIT_API_KEY`
- `bybit.api.secret` → `BYBIT_API_SECRET`
- `cmc.api.key` → `CMC_API_KEY`
- `dns.address` → `DNS_ADDRESS`
- `dns.timeout.ms` → `DNS_TIMEOUT_MS`

## Security

- Do NOT commit env files under `secret/*.env` to VCS. `.gitignore` is configured to ignore them.
- Scope RabbitMQ credentials minimally and rotate API keys periodically.
- Prefer rootless Podman and restricted capabilities (already configured in compose).
