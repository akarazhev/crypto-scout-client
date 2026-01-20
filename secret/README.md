# Secrets for crypto-scout-client

This folder contains templates and guidance for providing runtime secrets and configuration for Podman Compose
deployments.

## Files

- `client.env.example` – Template with all required keys. Copy this to the env file and fill real values.

## How to create env file

1. Copy the example:

   ```bash
   cp secret/client.env.example secret/parser-client.env
   ```
2. Edit the env file and fill values:
    - RabbitMQ:
        - `AMQP_RABBITMQ_HOST`, `AMQP_RABBITMQ_USERNAME`, `AMQP_RABBITMQ_PASSWORD`
        - `AMQP_STREAM_PORT`
    - DNS:
        - `DNS_ADDRESS` (resolver address, e.g., `8.8.8.8`), `DNS_TIMEOUT_MS` (e.g., `10000`)
    - API keys as needed:
        - `BYBIT_API_KEY`, `BYBIT_API_SECRET` (optional if you do not use authenticated flows)
        - `CMC_API_KEY` (if CMC metrics are enabled)
    - Server port:
        - `SERVER_PORT=8081` (default, matches Dockerfile EXPOSE and compose healthcheck)
    - Module toggles:
        - `CMC_PARSER_MODULE_ENABLED=true` (default) – enable CMC parser
        - `BYBIT_STREAM_MODULE_ENABLED=false` (default) – set to `true` to enable Bybit streams

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

- Verify health via container healthcheck (port 8081 is internal-only, not exposed to host):

  ```bash
  podman inspect --format='{{.State.Health.Status}}' crypto-scout-parser-client
  # 'healthy' when ready
  ```

- View logs:

  ```bash
  podman logs -f crypto-scout-parser-client
  ```

## Notes on configuration

- Defaults are loaded from `src/main/resources/application.properties` using `AppConfig`.
- Runtime overrides are supported: environment variables and JVM system properties take precedence at startup.
- With Podman Compose, `secret/parser-client.env` is injected as environment variables for the parser container.
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
