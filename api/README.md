# Noscam API

Spring Boot API for linking bank accounts, importing transactions, scoring them with deterministic risk factors, and raising dashboard alerts.

## Local setup

1. Start Postgres from `compose.yaml`:

```bash
docker compose up -d
```

2. Copy environment values into `api/.env` (Spring loads `optional:file:.env[.properties]`):

```properties
POSTGRES_HOST=localhost
POSTGRES_PORT=5432
POSTGRES_DB=noscam
POSTGRES_USER=noscam
POSTGRES_PASSWORD=noscam
APP_ENCRYPTION_KEY=MTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTI=
PLAID_CLIENT_ID=
PLAID_SECRET=
PLAID_ENV=sandbox
PLAID_WEBHOOK_URL=https://your-dev-tunnel.example/api/v1/plaid/webhook
PLAID_WEBHOOK_VERIFICATION_ENABLED=false
```

`APP_ENCRYPTION_KEY` must be a 32-byte key, Base64-encoded. Plaid access tokens are encrypted with AES-GCM and never returned to the browser.

3. Run the API:

```bash
./mvnw spring-boot:run
```

OpenAPI is at `/swagger-ui.html`. Health is at `/actuator/health`.

### Plaid Sandbox

Create a sandbox app at [Plaid](https://dashboard.plaid.com/). Use `POST /api/v1/plaid/link-token` from an authenticated session, complete Link, then `POST /api/v1/plaid/exchange` with the public token and an `Idempotency-Key`. The first sync is queued as a durable job and scored through the same ingestion path as manual transactions.

For local webhooks, expose the API with a tunnel and set `PLAID_WEBHOOK_URL` to `/api/v1/plaid/webhook`. Keep verification enabled outside tests.

## Amount convention

Transaction amounts are signed. Negative values are money leaving the account. Positive values are money entering the account. Plaid amounts are inverted on ingest so the API matches the dashboard.

Risk levels are presentation labels only: low `0–39`, medium `40–69`, high `70–100`. Alerts fire when `score >= alertThreshold`, which is independent of those labels.

## Tests

```bash
./mvnw test
```

Tests use Testcontainers PostgreSQL. Docker is required.

## Recovery

- `POST /api/v1/plaid/items/{id}/sync` re-queues a sync (rate limited).
- Failed jobs retry with capped exponential backoff and then move to `DEAD`.
- Disconnecting a Plaid item revokes the access token when possible, marks accounts disconnected, and keeps transaction history.
- Settings changes rescore transactions posted in the last 30 days and only create `SETTINGS_RESCORE` alerts for newly qualifying ones.
