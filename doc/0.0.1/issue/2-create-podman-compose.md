# Issue 2: Create a `podman-compose.yml` file for the `crypto-scout-client` project

In this `crypto-scout-client` project we are going to create a `podman-compose.yml` file to run the service in a
container. Create a secret file `secret/client.env.example` with key settings and create a documentation file
`secret/README.md` as a guide how to create a `client.env` file.

## Roles

Take the following roles:

- Expert dev-opts engineer.
- Expert technical writer.

## Conditions

- Use the best practices and design patterns.
- Use the minimal production image with `java 25`.
- Do not hallucinate.

## Tasks

- As the expert dev-opts engineer create `podman-compose.yml` in the root directory for the `crypto-scout-client`
  project. Define everything that is needed in the file to run the service in a container and to be ready for production.
- As the expert dev-opts engineer create `secret/client.env.example` file for the `crypto-scout-client` project. 
  Define everything that is needed in the file to run the service in a container and to be ready for production.
- As the expert dev-opts engineer recheck your proposal and make sure that they are correct and haven't missed any
  important points.
- As the technical writer update the `README.md` and `client-production-setup.md` files with your results.
- As the technical writer update the `2-create-podman-compose.md` file with your resolution.

## Resolution

### Created/Updated files

- `podman-compose.yml` – Production-ready compose definition for the service.
- `secret/client.env.example` – Template with all required keys to run in a container.
- `secret/README.md` – Guide to create and use `secret/client.env`.
- `.gitignore` – Ignore real secret env files while keeping the example tracked.
- `README.md` – Added "Podman Compose (with secrets)" section.
- `doc/0.0.1/client-production-setup.md` – Added compose deployment steps and security notes.

### Key decisions and best practices

- Minimal runtime base: `eclipse-temurin:25-jre` in `Dockerfile` (Java 25).
- Hardened container in `podman-compose.yml`:
    - `read_only: true`, `tmpfs: /tmp`, `no-new-privileges`, `cap_drop: ALL`, non-root `user: "10001:10001"`.
- Configuration via `JAVA_TOOL_OPTIONS` system properties (e.g., `-Dserver.port`, `-Damqp.rabbitmq.host`). If
  `AppConfig` does not honor overrides at runtime, update `src/main/resources/application.properties` and rebuild the
  image.
- Ports: default `8080:8080`. If you change `SERVER_PORT` in `secret/client.env`, also adjust the compose ports mapping.

### Secrets schema (`secret/client.env.example`)

- `SERVER_PORT=8080`
- `AMQP_RABBITMQ_HOST`, `AMQP_RABBITMQ_USERNAME`, `AMQP_RABBITMQ_PASSWORD`
- `AMQP_STREAM_PORT=5552`
- `AMQP_CRYPTO_BYBIT_STREAM`, `AMQP_METRICS_BYBIT_STREAM`, `AMQP_METRICS_CMC_STREAM`
- `BYBIT_API_KEY`, `BYBIT_API_SECRET`
- `CMC_API_KEY`

### How to run with Podman Compose

```bash
mvn clean package -DskipTests
podman build -t crypto-scout-client:0.0.1 .
cp secret/client.env.example secret/client.env
$EDITOR secret/client.env
podman-compose -f podman-compose.yml up -d
curl -fsS http://localhost:8080/health
podman logs -f crypto-scout-client
```

### Verification checklist

- Image builds and runs on Java 25.
- Health endpoint `GET /health` returns `ok` on `SERVER_PORT`.
- RabbitMQ Streams host/port reachable; three streams pre-created and user has publish permissions.
- Secrets not committed to VCS (`secret/client.env` ignored).