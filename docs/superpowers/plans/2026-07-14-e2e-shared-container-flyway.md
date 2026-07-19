# e2e Shared-Container Flyway Fix — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make `./mvnw clean package` pass by removing the shared-container schema-management conflict between the `test`-profile persistence integration test and the Flyway-managed e2e tests.

**Architecture:** All container-backed tests share one JVM-wide `static` Postgres container (`ContainerConfig.postgres`). Today `PaymentJpaRepositoryIntegrationTest` uses `create-drop` + Flyway disabled, leaving Hibernate-made tables with no `flyway_schema_history`; the next Flyway-managed e2e context then aborts. Fix: make that one test Flyway-managed (`validate`) so every context sharing the container manages the schema identically — order-independent.

**Tech Stack:** Java 26, Spring Boot 4.1, JUnit 5, Testcontainers 2.x, Flyway, Maven (`./mvnw`).

## Global Constraints

- Build with JDK 26 (Temurin 26). Older JDKs will not compile `--release 26`.
- Docker must be running (Testcontainers 2.x auto-negotiates the Docker API).
- No production code changes — test configuration only.
- Do not stage or commit `specs/INDEX.md`.
- Conventional Commits; end commit messages with the `Co-Authored-By` trailer.

---

### Task 1: Make `PaymentJpaRepositoryIntegrationTest` Flyway-managed

**Files:**
- Modify: `src/test/java/com/payment/gateway/infrastructure/payment/adapter/out/persistence/PaymentJpaRepositoryIntegrationTest.java:21-27` (the `@SpringBootTest(properties = { ... })` block)

**Interfaces:**
- Consumes: `ContainerConfig.postgres` (shared static Postgres), the existing Flyway migrations under `src/main/resources/db/migration`.
- Produces: nothing new — same test class, now loading its schema via Flyway + `validate`.

**Why this works:** the class is already `@Transactional`, so each test method's writes roll back — data isolation never depended on `create-drop`. The two added properties override `application-test.yml`'s `ddl-auto: create-drop` and `flyway.enabled: false` for this class only, leaving the `test` profile untouched for non-DB tests like `Resilience4jConfigTest`.

- [ ] **Step 1: Reproduce the failure (the "failing test")**

Run:
```bash
./mvnw -q test -Dtest='PaymentJpaRepositoryIntegrationTest,RefundFlowE2ETest'
```
Expected: `EXIT=1`. `RefundFlowE2ETest` reports `Errors: 10`, each caused by:
`org.flywaydb.core.api.FlywayException: Found non-empty schema(s) "public" but no schema history table.`

- [ ] **Step 2: Apply the fix**

In `PaymentJpaRepositoryIntegrationTest.java`, extend the existing `properties` array in the `@SpringBootTest` annotation from:

```java
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
            "spring.main.allow-bean-definition-override=true",
            "spring.main.web-application-type=none",
            "spring.main.lazy-initialization=true"
        })
```

to:

```java
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
            "spring.main.allow-bean-definition-override=true",
            "spring.main.web-application-type=none",
            "spring.main.lazy-initialization=true",
            // Share the container's schema with the Flyway-managed e2e contexts:
            // use migrations + validate instead of create-drop, so this context
            // never leaves a non-empty schema without a flyway_schema_history table.
            "spring.flyway.enabled=true",
            "spring.jpa.hibernate.ddl-auto=validate"
        })
```

- [ ] **Step 3: Verify the minimal repro is now green**

Run:
```bash
./mvnw -q test -Dtest='PaymentJpaRepositoryIntegrationTest,RefundFlowE2ETest'
```
Expected: `EXIT=0`, `Failures: 0, Errors: 0`. Both classes pass.

- [ ] **Step 4: Verify the full build end to end**

Run:
```bash
./mvnw clean package
```
Expected: `BUILD SUCCESS`, final summary `... Failures: 0, Errors: 0`, and a single runnable jar:
```bash
ls target/*.jar   # -> target/payment-gateway-0.0.1-SNAPSHOT.jar (exactly one)
```

- [ ] **Step 5: Apply formatting gate**

Run:
```bash
./mvnw spotless:apply && ./mvnw spotless:check
```
Expected: `spotless:check` passes (CI's only hard gate).

- [ ] **Step 6: Commit**

```bash
git add src/test/java/com/payment/gateway/infrastructure/payment/adapter/out/persistence/PaymentJpaRepositoryIntegrationTest.java
git status --short   # confirm only the test file is staged; specs/INDEX.md must NOT be staged
git commit -m "$(cat <<'EOF'
fix(test): make PaymentJpaRepositoryIntegrationTest flyway-managed

The test used create-drop + flyway.enabled=false and shares the
JVM-wide static Postgres container with the Flyway-managed e2e tests.
Its Hibernate-made tables (no flyway_schema_history) contaminated the
shared schema, so the next e2e context aborted with "non-empty schema,
no history table" and the whole e2e suite cascade-failed under
`./mvnw clean package`. Switch this context to Flyway + validate so all
container-sharing contexts manage the schema identically. Data
isolation is unaffected (test is @Transactional; writes roll back).

Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>
EOF
)"
```

---

## Self-Review

- **Spec coverage:** the spec's single fix (§4, scope the change to the culprit class via `spring.flyway.enabled` + `ddl-auto=validate`) is implemented in Task 1 Step 2. The spec's verification plan (§5: repro flips green; full build succeeds; integration test still passes) maps to Steps 3–4. Delivery/rollback (§6: branch, only the test file touched, `specs/INDEX.md` untouched) maps to Step 6.
- **Placeholder scan:** no TBD/TODO/"handle edge cases"; every code and command step shows exact content.
- **Type consistency:** no new types or signatures introduced; only two configuration property strings added to an existing annotation.
