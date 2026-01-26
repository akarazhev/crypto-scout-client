# Security Guidelines

## Credentials Management

All sensitive credentials must be provided via system properties or environment variables to prevent accidental commits of secrets to version control.

### Required Credentials

The following credentials must be configured at runtime:

- `AMQP_RABBITMQ_PASSWORD` - RabbitMQ password (or `amqp.rabbitmq.password`)
- `BYBIT_API_KEY` - Bybit API key (or `bybit.api.key`)
- `BYBIT_API_SECRET` - Bybit API secret (or `bybit.api.secret`)
- `CMC_API_KEY` - CoinMarketCap API key (or `cmc.api.key`)

### Recommended Approach

#### Option 1: Environment Variables

Use environment variables to pass credentials:

```bash
export AMQP_RABBITMQ_PASSWORD="your-secure-password"
export BYBIT_API_KEY="your-bybit-api-key"
export BYBIT_API_SECRET="your-bybit-api-secret"
export CMC_API_KEY="your-cmc-api-key"
```

#### Option 2: System Properties

Pass credentials as system properties when starting the application:

```bash
java -DAMQP_RABBITMQ_PASSWORD="your-secure-password" \
     -DBYBIT_API_KEY="your-bybit-api-key" \
     -DBYBIT_API_SECRET="your-bybit-api-secret" \
     -DCMC_API_KEY="your-cmc-api-key" \
     -jar crypto-scout-client.jar
```

#### Option 3: Docker / Kubernetes

Use environment variables in your Docker Compose or Kubernetes configuration:

```yaml
environment:
  - AMQP_RABBITMQ_PASSWORD=${AMQP_RABBITMQ_PASSWORD}
  - BYBIT_API_KEY=${BYBIT_API_KEY}
  - BYBIT_API_SECRET=${BYBIT_API_SECRET}
  - CMC_API_KEY=${CMC_API_KEY}
```

## Configuration Files

### Local Configuration

For local development, create a `application-local.properties` file to override default values:

```properties
# Do NOT commit this file to version control
amqp.rabbitmq.password=your-local-dev-password
bybit.api.key=your-local-dev-key
bybit.api.secret=your-local-dev-secret
cmc.api.key=your-local-dev-key
```

The `application-local.properties` file is included in `.gitignore` and will not be committed.

### Test Configuration

For testing, create a `application-test.properties` file with test credentials. This file is also included in `.gitignore`.

## Security Best Practices

### 1. Never Commit Credentials to Version Control

- Check `.gitignore` to ensure credential files are excluded
- Use `git diff` before committing to review changes
- Use pre-commit hooks to prevent credential commits
- Review the full git history for accidentally committed secrets

### 2. Use Different Credentials for Different Environments

- **Development**: Use test accounts with limited access
- **Staging**: Use staging API keys with appropriate permissions
- **Production**: Use production credentials with strict access controls

### 3. Rotate Credentials Regularly

- Change API keys and passwords on a regular schedule
- Remove old API keys from provider dashboards
- Update credentials in deployment pipelines immediately after rotation

### 4. Monitor for Unauthorized Access

- Enable audit logs for all API calls
- Set up alerts for unusual activity
- Review access patterns regularly

### 5. Limit API Key Permissions

- Only grant necessary permissions to API keys
- Use separate keys for different environments
- Revoke unused API keys immediately

## Configuration Validation

The application validates all required configuration at startup using `ConfigValidator`. Missing or invalid credentials will cause the application to fail to start with a descriptive error message.

### Validation Rules

- **Hostnames**: Must be valid IPv4 addresses or hostnames
- **Ports**: Must be in range 1-65535
- **Timeouts**: Must be positive integers (typically 100-60000ms)
- **Required Fields**: Cannot be null or blank strings

### Example Startup Failure

```bash
$ java -jar crypto-scout-client.jar
ERROR ConfigValidator - Missing required configuration properties: [amqp.rabbitmq.password, bybit.api.key, bybit.api.secret, cmc.api.key]
java.lang.IllegalStateException: Missing required configuration properties: [...]
```

## Reporting Security Issues

If you discover a security vulnerability in this project, please report it privately:

1. **Do not** create a public issue
2. **Do not** discuss the vulnerability in public channels
3. **Do** send a detailed report to the project maintainers
4. **Do** include steps to reproduce and potential impact

Contact: andrey.karazhev@example.com (replace with actual contact)

## Additional Resources

- [OWASP Configuration Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Configuration_Cheat_Sheet.html)
- [12-Factor App methodology - Config](https://12factor.net/config)
- [Bybit API Security](https://bybit-exchange.github.io/docs/api/)
- [CoinMarketCap API Security](https://coinmarketcap.com/api/documentation)
