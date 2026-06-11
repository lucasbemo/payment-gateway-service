# Test Plan: Update the project to JDK 26

The goal is **zero behavior change** on a new platform. The existing test suite is the
primary safety net; this is a regression-focused upgrade. Build & run everything on the
target JDK.

## Unit Tests
- Run all non-e2e tests: `./mvnw test -Dtest='!com.payment.gateway.e2e.**'`.
- Focus risk areas after the framework upgrade:
  - Mockito-based service tests (Byte Buddy must support JDK 26 bytecode) — e.g.
    `ProcessPaymentServiceTest`.
  - MapStruct mappers (regenerate cleanly under 1.6.x).
  - Lombok-generated code compiles under 1.18.46+.
- Expected: identical pass count to the JDK 21 baseline; capture before/after counts.

## Integration / Slice Tests
- `@WebMvcTest` controller tests (`*ControllerTest`) — assert status codes, the
  `ApiResponse<T>` envelope, and validation messages are unchanged after Spring Boot 4 /
  springdoc 3.x.
- ArchUnit `HexagonalArchitectureTest` — layering rules still hold.

## E2E Tests (Testcontainers)
- `./mvnw test -Dtest='com.payment.gateway.e2e.**'`.
- Verify Testcontainers 2.x starts Postgres/Kafka/Redis on JDK 26 **without** the manual
  `-Dapi.version=1.44` flag; if still needed, document it.
- `ProdProfileAuthSmokeE2ETest` — real prod `SecurityFilterChain` (API-key + JWT) passes
  after the Spring Security major upgrade.
- Confirm the outbox→Kafka propagation e2e flows still pass (event listeners, webhook
  fan-out).

## API Tests (Postman / newman)
- Run the Postman collection against the app on JDK 26 (newman). All requests pass
  (payments, refunds, transactions, merchants, customers, reconciliation, webhooks).
- Diff response bodies/status codes vs the JDK 21 baseline — no contract drift.

## Regression Tests
- `./mvnw clean verify` end-to-end (unit + e2e + JaCoCo report generates on v70).
- `./mvnw spotless:check` passes on JDK 26.
- OWASP dependency-check — review new findings from the version bumps.
- Flyway migrations V1–V19 validate against a fresh Postgres.

## Build / Runtime Verification
- `docker build -t payment-gateway:jdk26 .` succeeds on the JDK 26 base.
- Container starts; `/actuator/health` healthcheck passes; `/actuator/prometheus` emits
  metrics; Swagger UI renders (springdoc 3.x).
- App boots on the `dev` profile against the docker-compose stack and serves a sample
  payment request.

## Manual Verification (real-request validation — required for "production ready")
- Start the app on JDK 26 (`run-app.sh` in the worktree) against the docker-compose stack.
- Exercise **every feature area with real requests** and validate each response (status,
  body shape, persisted state) against the JDK 21 baseline:
  - Payments: authorize → capture → cancel; idempotency via `X-Idempotency-Key`.
  - Refunds (full + partial), transactions, merchants, customers.
  - Reconciliation: run for a merchant/date; re-run (idempotency); settlement report (S3/MinIO).
  - Webhooks: confirm outbox → Kafka → listener → webhook delivery fan-out.
- Confirm traces appear in Zipkin and metrics in Prometheus/Grafana, and the
  `/actuator/health` + `/actuator/prometheus` endpoints behave as before.

## Performance / Load (required)
- Run a load profile (e.g. via the Postman/newman runner or a load tool) against the
  JDK 26 build and compare latency/throughput/error-rate to a JDK 21 baseline.
- Acceptance: no meaningful regression (define threshold with the owner, e.g. p95 within
  ±10% of baseline).

## Staging soak (required before "production ready")
- Deploy the upgraded build to staging and let it run under representative traffic without
  errors (define soak window with the owner). Watch logs, metrics, and error rates.

## Baselines to capture before starting (for diffing)
- JDK 21 test pass/fail counts (unit + e2e).
- JaCoCo coverage %.
- Postman collection run result.
- `/actuator/prometheus` sample + a sample OpenAPI doc.
