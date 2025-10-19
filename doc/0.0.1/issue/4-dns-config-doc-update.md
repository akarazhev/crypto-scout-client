# Issue 4: Document updating of the `crypto-scout-client` production ready service to support `DNS` config with `timeout`.

The `crypto-scout-client` project has been configured in a container to get the collect crypto data.
So you will need to update professional documentation of the `crypto-scout-client` project that it is ready for
production. We have added a support of `DNS` config with `timeout` via `application.properties`.

- `dns.port=8.8.8.8`
- `dns.timeout.ms=10000`

Application logic supports it.

## Roles

Take the following roles:

- Expert tech-writer.

## Conditions

- Use the best practices and patterns.
- Do not hallucinate.

## Tasks

- As the expert tech-writer review the `crypto-scout-client` project and update the professional documentation here:
  `README.md`.
- Recheck your proposal and make sure that they are correct and haven't missed any important points.
- Write a report with your proposal and implementation into `doc/0.0.1/client-production-setup.md`.