# CLAUDE.md — payment-gateway-service

Guidance for Claude Code when working in this repository. Project-specific facts
live in the lower half; the working-style rules at the top are mandatory.

---

## Working Style (mandatory)

### 1. Plan Mode Default

* Enter plan mode for ANY non-trivial task (3+ steps or architectural decisions)
* If something goes sideways, STOP and re-plan immediately – don't keep pushing
* Use plan mode for verification steps, not just building
* Write detailed specs upfront to reduce ambiguity

### 2. Subagent Strategy

* Use subagents liberally to keep main context window clean
* Offload research, exploration, and parallel analysis to subagents
* For complex problems, throw more compute at it via subagents
* One task per subagent for focused execution

### 3. Self-Improvement Loop

* After ANY correction from the user: update tasks/lessons.md with the pattern
* Write rules for yourself that prevent the same mistake
* Ruthlessly iterate on these lessons until mistake rate drops
* Review lessons at session start for relevant project

### 4. Verification Before Done

* Never mark a task complete without proving it works
* Diff behavior between main and your changes when relevant
* Ask yourself: "Would a staff engineer approve this?"
* Run tests, check logs, demonstrate correctness

### 5. Demand Elegance (Balanced)

* For non-trivial changes: pause and ask "is there a more elegant way?"
* If a fix feels hacky: "Knowing everything I know now, implement the elegant solution"
* Skip this for simple, obvious fixes – don't over-engineer
* Challenge your own work before presenting it

### 6. Autonomous Bug Fixing

* When given a bug report: just fix it. Don't ask for hand-holding
* Point at logs, errors, failing tests – then resolve them
* Zero context switching required from the user
* Go fix failing CI tests without being told how

### Task Management

* Plan First: Write plan to tasks/todo.md with checkable items
* Verify Plan: Check in before starting implementation
* Track Progress: Mark items complete as you go
* Explain Changes: High-level summary at each step
* Document Results: Add review section to tasks/todo.md
* Capture Lessons: Update tasks/lessons.md after corrections

### Core Principles

* Simplicity First: Make every change as simple as possible. Impact minimal code.
* No Laziness: Find root causes. No temporary fixes. Senior developer standards.

### Review rules

* Add tests for new behavior
* Avoid unrelated refactors
* Mention risk and rollback strategy in the PR body

### Git workflow

* Never work directly on main or master
* Always check the working tree before starting
* If the working tree is dirty, stop and ask the user
* Branch naming:
  * feature/{ticket-id}-{kebab-title}
  * fix/{ticket-id}-{kebab-title}
  * chore/{ticket-id}-{kebab-title}
  * refactor/{ticket-id}-{kebab-title}
* Use Conventional Commits
* Always open PRs as draft
* Never auto-merge

### Product Backlog Source of Truth

GitHub Project is the product backlog source of truth.

For feature implementation:

1. Do not implement directly from a GitHub Project item.
2. First import the item with `/import-github-project-item <issue-number>`.
3. Create or update `specs/active/<feature>/`.
4. Ask blocking questions.
5. Wait for explicit human approval.
6. Implement only from the approved local spec.
7. Current source code and tests override stale docs.

Implementation runs in an **auto-created git worktree per feature** (default): branch
`feature/<n>-<slug>`, its own app `SERVER_PORT`, sharing one local infra stack. The spec
folder travels on the feature branch; `specs/INDEX.md` is edited **only in the main
checkout** (never on feature branches). After merge, `/finish-feature <n>-<slug>`
archives the spec and removes the worktree. See [`GUIDE_WORKFLOW.md`](docs/GUIDE_WORKFLOW.md).

Other available commands (in `.claude/skills/`): `/implement-feature`,
`/list-github-project-items`, `/spring-boot-review`, `/create-pr`.

### Spec Context Policy

Do not scan all `specs/` folders by default.

Read in this order:

1. `CLAUDE.md`
2. `specs/INDEX.md`
3. The relevant `specs/active/<feature>/` folder
4. Source code and tests
5. Historical specs only when explicitly relevant

---

## Project Overview

A payment gateway service: processes payments, refunds, transactions, and
merchant/customer management, with settlement reconciliation and webhook
delivery. Stripe is the external payment provider. Events are propagated via a
transactional **outbox** to Kafka.

Root Java package: `com.payment.gateway`. Single-module Maven project.

## Tech Stack

| Area | Choice |
| --- | --- |
| Language / build | Java **26**, Maven (`./mvnw`), Spring Boot **4.1.0** |
| Persistence | PostgreSQL + Spring Data JPA/Hibernate, **Flyway** migrations |
| Messaging | Spring Kafka (transactional outbox pattern) |
| Cache / locks | Redis + Redisson (idempotency, distributed locks) |
| Resilience | Resilience4j (circuit breaker, retry, bulkhead, rate limiter), Bucket4j |
| Mapping / boilerplate | MapStruct 1.6.3, Lombok 1.18.46 |
| Payments | stripe-java 24.0.0 |
| Reports / storage | spring-cloud-aws S3, iText7 (PDF) |
| API docs | springdoc-openapi (Swagger UI) |
| Observability | Micrometer + Prometheus, Brave/Zipkin tracing |
| Testing | JUnit 5, AssertJ, Mockito (BDD), Testcontainers 2.0.5, ArchUnit 1.4.2 |
| Formatting | Spotless 3.6.0 + palantir-java-format 2.92.0 |

## Build & Run Commands

Prefer the **Makefile** targets (note: they call bare `mvn` — see Gotchas about JDK):

```shell
make build            # mvn clean package -DskipTests
make test             # mvn clean test
make coverage         # mvn clean test jacoco:report
make run              # mvn spring-boot:run  (profile: local)
make format           # mvn spotless:apply
make validate         # mvn spotless:check
make docker-up        # docker-compose up -d  (full local stack)
make docker-down / docker-logs / docker-restart
```

Direct Maven equivalents use `./mvnw`. Split used by CI:

```shell
./mvnw test -Dtest='!com.payment.gateway.e2e.**'   # unit + slice tests
./mvnw test -Dtest='com.payment.gateway.e2e.**'    # e2e (Testcontainers)
./mvnw spotless:check                              # hard CI gate
```

> **Note:** `make integration-test` references a `-Pintegration` Maven profile
> that does NOT exist in `pom.xml`. The real unit/e2e split is the `-Dtest=`
> name filter above, not a profile. Don't rely on that target.

## Architecture — Hexagonal (Ports & Adapters) + DDD

Three layers under `com.payment.gateway`, dependency direction
`infrastructure → application → domain`:

* **`domain/`** — pure business core, one sub-package per bounded context
  (`payment`, `refund`, `transaction`, `merchant`, `customer`,
  `reconciliation`, `outbox`, `idempotency`). Each: `model/` (rich entities with
  behavior + factory methods, e.g. `Payment.create(...)`, `payment.authorize()`),
  `service/` (`*DomainService`), `port/` (interfaces the domain owns),
  `exception/`.
* **`application/`** — use-case orchestration. Per context: `port/in/`
  (`*UseCase` inbound interfaces), `port/out/` (`*Port` driven interfaces),
  `service/` (`*Service`, `@Transactional`, implement the use-cases), `dto/`
  (`*Command` inputs, `*Response` outputs).
* **`infrastructure/`** — adapters + Spring config. Per context:
  `adapter/in/{rest,kafka}`, `adapter/out/{persistence,kafka,provider}`. Plus
  `infrastructure/config`, `infrastructure/commons/{rest,persistence,security,
  resilience,monitoring}`, `infrastructure/docs` (OpenAPI interfaces).
* **`commons/`** — cross-cutting: `exception/` (`BusinessException`,
  `NotFoundException`, `ValidationException`, `PaymentDeclinedException`,
  `ExternalServiceException`), `model/Money.java` (cents-based value object),
  `utils/IdGenerator`.

**Key flows:**

* **Transactional outbox:** application services call
  `OutboxEventDomainService.publish(...)` inside the same `@Transactional` method
  that saves the aggregate (e.g. `ProcessPaymentService`). `OutboxPollingScheduler`
  (profile `!test & !e2e`) polls `PENDING` every 5s, publishes via
  `KafkaOutboxEventPublisher` (EventType → topic switch, e.g.
  `PAYMENT_CREATED → "payment.created"`), marks `PUBLISHED`/`FAILED`. Inbound
  `@KafkaListener`s in `adapter/in/kafka` consume and fan out to merchant webhooks.
* **Persistence boundary:** domain models are separate from JPA entities.
  `*JpaEntity` (extends `BaseEntity` with auditing) ↔ `*JpaRepository` ↔
  `*PersistenceAdapter` (implements domain/app ports) ↔ `*Mapper`.
* **Reconciliation:** `ReconcileTransactionsService` matches a merchant's
  transactions for a UTC day; idempotent per merchant/gateway/date (unique
  constraint `V19`, `findOrCreateReconciliationBatch`).

Architecture is enforced by `architecture/HexagonalArchitectureTest.java`
(ArchUnit): domain must not depend on application/infrastructure; no cycles.

## Conventions

* **Naming:** `*UseCase` (inbound port) · `*Service` (app, `@Transactional`) ·
  `*DomainService` · `*Port` (outbound) · `*PersistenceAdapter` / `*Listeners` /
  `*EventPublisher` (adapters) · `*JpaEntity` / `*JpaRepository` / `*Mapper` ·
  `*Command` (input DTO) / `*Response` / `*DTO` (output).
* **REST:** endpoints under `/api/v1/...`; controllers implement an OpenAPI doc
  interface (e.g. `PaymentApi`) and return the uniform `ApiResponse<T>` envelope
  (`ApiResponse.success(...)` / `.error(...)`); paged results use
  `PagedResponse`/`PageInfo`. Idempotency via `X-Idempotency-Key` header.
* **Errors:** `@RestControllerAdvice GlobalExceptionHandler` maps typed domain
  exceptions to HTTP statuses (NotFound→404, Validation/Business/Declined→400,
  ExternalService→503, else 500), all wrapped in `ApiResponse`.
* **DI:** constructor injection via Lombok `@RequiredArgsConstructor`; logging
  via `@Slf4j`. Money is always cents-based (`Money` value object).
* **Formatting:** run `make format` before committing. CI fails on
  `spotless:check` (palantir-java-format). Spotless is NOT bound to a build
  phase, so a plain `mvn package` won't catch formatting locally.
* **Migrations:** add Flyway scripts in `src/main/resources/db/migration` as
  `V{n}__description.sql`; never edit an applied migration.

## Testing

* One test source root (`src/test/java`), all files named `*Test.java`. Category
  is by **package + suffix**, not tags or Failsafe:
  * Unit: `domain/**`, `application/**`, `commons/**` — Mockito, no Spring.
  * Slice: `infrastructure/**/adapter/in/rest/*ControllerTest` — `@WebMvcTest`.
  * E2E: `com.payment.gateway.e2e.*E2ETest` — `@SpringBootTest` + Testcontainers,
    extend `E2ETestBase` (profile `e2e`), not `@Transactional`; each test calls
    `cleanupDatabase()` (TRUNCATE CASCADE).
* **Style:** AssertJ assertions only (`assertThat`, `assertThatThrownBy`); BDD
  Mockito (`given`/`then`/`should`); `@Nested` + `@DisplayName`; explicit
  `// Given / // When / // Then`. Unit method names `shouldDoX...`; e2e names
  `testFeature_Scenario`.
* **Fixtures:** `e2e/testdata/TestDataFactory.java` (static builders,
  UUID-randomized); REST wrapper `e2e/client/PaymentGatewayClient.java`. Unit
  tests build aggregates via domain factory methods.
* **Testcontainers base:** `test/ContainerConfig.java` — static Postgres 15,
  Kafka 7.5.0, Redis 7; wired via `@DynamicPropertySource`.
* **Prod-profile smoke:** `ProdProfileAuthSmokeE2ETest` runs the real prod
  `SecurityFilterChain` (API-key + JWT) — keep it green when touching security.

## Local Environment

`docker-compose up -d` (`make docker-up`) starts the full stack. Host ports:
Postgres **5433**, Kafka **19092** (internal `kafka:29092`), Redis **6380**,
kafka-ui 8082, pgadmin 8083, Prometheus 9090, Grafana 3000, Zipkin 9411, MinIO
9000/9001, and the app on **8080**.

* Default Spring profile is `local` (no `application-local.yml`; falls back to
  `application.yml` host-port defaults). Docker app container runs profile `dev`.
* Swagger UI: `http://localhost:8080/swagger-ui.html` · health:
  `http://localhost:8080/actuator/health`.
* `application-prod.yml` requires env vars (`DATASOURCE_URL`, etc.).

## Gotchas & Known Issues

* **JDK:** the project targets **JDK 26** (Spring Boot 4.1). Build with a JDK 26
  toolchain (`sdk use java 26.0.1-tem` or any Temurin 26). Older JDKs (≤21) will not
  compile `--release 26`. The previous "use JDK 21" workaround is obsolete.
* **Testcontainers:** uses Testcontainers **2.x**, which negotiates a modern Docker
  API automatically — the old `-Dapi.version=1.44` override is **no longer needed**.
* **E2E locally:** the full e2e suite runs in a single JVM (`./mvnw test
  -Dtest='com.payment.gateway.e2e.**'`). If you hit "ApplicationContext failure
  threshold exceeded" while the local docker-compose stack is also running, run e2e
  classes one per JVM (`-DreuseForks=false`).
* **CI no-ops:** the `code-quality` job runs `checkstyle:check`/`spotbugs:check`
  wrapped in `|| true`, but those plugins aren't configured — effectively no-ops.
  Only `spotless:check` is a real gate.
* When diagnosing config, read the **active** runtime files (`application.yml`,
  `application-dev/prod.yml`), not `.env.example` or templates.

## CI (`.github/workflows/ci.yml`)

Jobs: build → (unit test | e2e test | OWASP dependency-check | spotless gate) →
docker-build (main only) + AI review (PRs). Java 26 / Temurin. Mirror the unit
and e2e `-Dtest=` filters above when reproducing CI locally.
