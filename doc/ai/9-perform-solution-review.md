# Issue 9: Perform solution review of the `crypto-scout-client` project

The first version of the `crypto-scout-client` project has been done now. Let's perform the solution review to be sure
that the project is ready for production and there are no issues. Let's check if there is anything that can be optimized
and what can be done better.

## Roles

Take the following roles:

- Expert solution architect.
- Expert technical writer.

## Conditions

- Use the best practices and design patterns.
- Use the current technological stack, that's: `ActiveJ 6.0`, `Java 25`, `maven 3.9.1`, `podman 5.6.2`,
  `podman-compose 1.5.0`.
- Do not hallucinate.

## Tasks

- As the `expert solution architect` perform solution review of the `crypto-scout-client` project and verify if this is
  ready for production and there are no issues. Check if there is anything that can be optimized and what can be done
  better.
- As the `expert solution architect` recheck your proposal and make sure that they are correct and haven't missed any
  important points.
- As the `expert technical writer` update the `README.md` and `client-production-setup.md` files with your results.
- As the `expert technical writer` update the `9-perform-solution-review.md` file with your resolution.

## Resolution (0.0.1)

- Ready for production: Yes, under documented prerequisites (RabbitMQ Streams enabled and reachable; required streams
  and credentials configured; secrets injected via env/JVM; outbound access to Bybit/CMC).
- Documentation updated: README remains concise without a dedicated Solution review section.
  `doc/0.0.1/client-production-setup.md` contains the solution review and recommendations for 0.0.2.

## Findings

- Health and readiness: `WebModule` serves `GET /health` -> `ok` and `GET /ready` -> `ok` only when
  `AmqpPublisher.isReady()` is true; otherwise HTTP 503 `not-ready`.
- AMQP publisher readiness: `AmqpPublisher` initializes RabbitMQ Streams `Environment` and three producers; `isReady()`
  checks all are non-null.
- Configuration precedence: Defaults in `src/main/resources/application.properties` are read via `AppConfig`;
  environment variables and JVM system properties override at runtime. `podman-compose.yml` injects env files per
  service: `secret/bybit-client.env` (Bybit streams) and `secret/parser-client.env` (parser).
- DNS configuration: `WebConfig` uses `dns.address` and `dns.timeout.ms` to configure `DnsClient`.
- Containerization: `Dockerfile` uses Temurin JRE 25, non-root user, pinned base digest, `JAVA_TOOL_OPTIONS` with OOM
  fast-exit; compose hardens with `read_only`, `tmpfs /tmp` (nodev,nosuid), `cap_drop: ALL`, `no-new-privileges`,
  healthcheck to `/ready`, resource limits.
- Entry point visibility: `Client` and `main` are not declared `public` (package-private). This is acceptable because
  the app launches via a shaded JAR manifest; no change required.
- Logging: Provided transitively by `jcryptolib`; explicit SLF4J binding is optional if customization is needed.

## Recommendations for 0.0.2

- HTTP client timeouts: Add read/write/request timeouts in `WebModule` `HttpClient` builder.
- AMQP TLS: Add optional TLS configuration for RabbitMQ Streams.
- Logging (optional): Keep `jcryptolib`-provided logging by default; add an explicit SLF4J binding with
  `src/main/resources/logback.xml` only if customization is required.
- Tests & CI: Add injector + `/health` smoke test and publisher unit test; integrate into CI (`mvn verify`) and optional
  image build.

## Changes applied

- `README.md`: Kept concise; no dedicated Solution review section per decision. Production notes retained (including
  readiness semantics).
- `doc/0.0.1/client-production-setup.md`: Added solution review, recommendations; updated Appendix C.

## Status

- Version `0.0.1` solution review completed. Service is production-ready given the documented assumptions and
  configuration.