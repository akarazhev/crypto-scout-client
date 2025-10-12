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
  curl -fsS http://localhost:8080/health
  # ok
  ```

- View logs:

  ```bash
  podman logs -f crypto-scout-client
  ```

## Notes on configuration

- The application reads properties from `src/main/resources/application.properties` using `AppConfig`.
- Runtime environment overrides (env vars) are not supported by the application.
- Treat `secret/client.env` as a template and record of your deployment values. Keep it in sync with
  `application.properties` before building, and rebuild the image when changes are needed.

## Security

- Do NOT commit `secret/client.env` to VCS. `.gitignore` is configured to ignore it.
- Scope RabbitMQ credentials minimally and rotate API keys periodically.
- Prefer rootless Podman and restricted capabilities (already configured in compose).
