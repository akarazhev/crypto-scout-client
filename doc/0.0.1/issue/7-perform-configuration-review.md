# Issue 7: Perform configuration review of the `crypto-scout-client` project

The first version of the `crypto-scout-client` project has been done now. Let's perform the configuration review to be
sure that the project is ready for production and there are no issues. Let's check if there is anything that can be
optimized and what can be done better.

## Roles

Take the following roles:

- Expert dev opts engineer.
- Expert technical writer.

## Conditions

- Use the best practices and design patterns.
- Use the current technological stack, that's: `podman 5.6.2`, `podman-compose 1.5.0`, `maven 3.9.1`.
- Configuration is `pom.xml`, `Dockerfile`, `podman-compose.yml`, `secret/client.env.example`, `secret/README.md`.
- Do not hallucinate.

## Tasks

- As the `expert dev opts engineer` perform configuration review of the `crypto-scout-client` project and verify if this
  is ready for production and there are no issues. Check if there is anything that can be optimized and what can be done
  better.
- As the `expert dev opts engineer` recheck your proposal and make sure that they are correct and haven't missed any
  important points.
- As the `expert technical writer` update the `README.md` and `client-production-setup.md` files with your results.
- As the `expert technical writer` update the `7-perform-configuration-review.md` file with your resolution.

---

## Resolution – Configuration Review Results

### Scope

- Reviewed `pom.xml`, `Dockerfile`, `podman-compose.yml`, `src/main/resources/application.properties`,
  `secret/README.md`, `secret/client.env.example`, and documentation.

### Findings

- **Container image (`Dockerfile`)**
    - Pinned base image digest `eclipse-temurin:25-jre-alpine@sha256:...`.
    - Non-root user `10001:10001`, `STOPSIGNAL SIGTERM`, `JAVA_TOOL_OPTIONS=-XX:+ExitOnOutOfMemoryError`.
    - `curl` installed to support container-internal healthcheck.
    - Exposes `8081`; copies shaded JAR; minimal surface. Production-ready.

- **Compose hardening (`podman-compose.yml`)**
    - `read_only: true`, `tmpfs: /tmp (nodev,nosuid)`, `cap_drop: ALL`, `security_opt: no-new-privileges=true`.
    - Resource constraints: `cpus: "1.00"`, `memory: 1G`; `pids_limit: 256`, `ulimits.nofile: 4096`.
    - Healthcheck using `curl` to the readiness endpoint (`/ready`) with `start_period: 30s`.
    - `user: "10001:10001"`, `init: true`, `restart: unless-stopped`, `TZ=UTC`. Solid defaults.

- **Secrets & env (`secret/`)**
    - `client.env.example` covers RabbitMQ creds/port, DNS, API keys. `.gitignore` excludes `secret/*.env`.
    - Compose injects env files per service: `env_file: secret/bybit-client.env` (Bybit streams service) and
      `env_file: secret/parser-client.env` (parser service). Aligns with best practices.

- **Configuration precedence**
    - Defaults from `src/main/resources/application.properties` with runtime overrides via env vars and JVM system
      properties (through `AppConfig`).
    - Documented in `README.md` and `doc/0.0.1/client-production-setup.md`. Clear and correct.
- **Health & readiness endpoints**
    - `WebModule` serves `GET /health` -> `ok` on `server.port` (`WebConfig`) for liveness.
    - `WebModule` serves `GET /ready` -> `ok` when RabbitMQ Streams environment and producers are initialized; otherwise
      HTTP 503 `not-ready`.

- **Observability / logging**
    - Code uses SLF4J API (e.g., `AmqpPublisher`) with a logging binding provided transitively by `jcryptolib`; logs
      are emitted by default.
    - No `logback.xml` is bundled; customize levels/format or swap backend by adding your preferred SLF4J binding and
      configuration if desired.

- **Build (`pom.xml`)**
    - Java 25, pinned plugin versions, shaded JAR with `mainClass` `com.github.akarazhev.cryptoscout.Client`.
    - Dependencies: ActiveJ, RabbitMQ Stream client, `jcryptolib`. Meets requirements with Maven 3.9.1.

### Optimizations applied in this issue

- **Docs – Logging/Observability:**
    - Updated `README.md` to reflect that `jcryptolib` provides a logging binding; added guidance on customizing levels,
      formats, or switching backend (e.g., providing `logback.xml` when using Logback).
    - Updated `doc/0.0.1/client-production-setup.md` accordingly.

- **Docs – Compose details:**
    - Added explicit notes about `cpus`, `memory`, `restart: unless-stopped`, and `TZ=UTC` to both docs.

- **Runtime – Readiness endpoint:**
    - Implemented `/ready` check using `AmqpPublisher.isReady()` and exposed via `WebModule` (200 when ready, 503
      otherwise).

### Recommendations (optional, future hardening)

- **.dockerignore:** Already implemented at project root to minimize build context (excludes `target/*` with
  `!target/*.jar`, `.git/`, `secret/`, `doc/`, `dev/`, IDE files). Keep it updated as the repo evolves.
- **Logging configuration:** Optionally provide a `logback.xml` (if using Logback) or another backend configuration to
  tune formats/levels beyond defaults.
- **Orchestrator checks:** Use `GET /ready` for readiness and `GET /health` for liveness in your orchestrator.
- **JVM container tuning:** Optionally set `-XX:MaxRAMPercentage=70` and heap dump/location flags via
  `JAVA_TOOL_OPTIONS` if you want more predictable memory behavior. Ensure `/tmp` size can accommodate dumps if enabled.
- **TLS to RabbitMQ Streams:** If your environment mandates encryption, add TLS configuration to the Rabbit Streams
  `Environment` and corresponding cert/key handling via env/secret.

### Re-check summary

- Image and Compose configs align with best practices for Podman 5.6.2 / podman-compose 1.5.0.
- Secrets and configuration precedence are correctly implemented and documented.
- Health, liveness, and readiness are present; logging customization is optional.

### Conclusion

`crypto-scout-client` is production-ready with strong defaults. No blocking issues found. Optional improvements are
documented above.