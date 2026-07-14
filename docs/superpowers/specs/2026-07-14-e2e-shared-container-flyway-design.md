# Fix: e2e test suite fails under `./mvnw clean package` (shared-container Flyway contamination)

- **Date:** 2026-07-14
- **Status:** Approved — ready for implementation
- **Branch:** `fix/e2e-shared-container-flyway`
- **Scope:** one test class change + verification. No production code changes.

---

## 1. Symptom

Running the command the README advertises:

```bash
./mvnw clean package && java -jar target/*.jar
```

fails during the test phase:

```
[ERROR] Tests run: 1209, Failures: 0, Errors: 128, Skipped: 0
[INFO] BUILD FAILURE
```

All 128 are **errors**, not failures, and every one carries the same message:

```
IllegalState ApplicationContext failure threshold (1) exceeded: skipping
repeated attempt to load context for [ ... activeProfiles = ["e2e"] ... ]
```

Because the test phase fails, Maven never reaches the `package` goal, **no jar is
produced**, and the subsequent `java -jar target/*.jar` also fails ("no such file").

## 2. Investigation & evidence

The threshold message is a *cascade symptom*, not the cause. Spring's context
failure threshold is `1`: once a shared context configuration fails to load once,
every later test that needs the same context is auto-errored without retrying.
Only the **first** genuine load failure has a real cause.

Digging into `target/surefire-reports`, exactly **2 of 12** e2e context configs
recorded a real failure (the `e2e`-profile context via `RefundFlowE2ETest`, and
the `prod`-profile context via `ProdProfileAuthSmokeE2ETest`); the other 10 were
pure threshold skips. Both real failures share one root exception:

```
org.flywaydb.core.api.FlywayException: Found non-empty schema(s) "public"
but no schema history table. Use baseline() or set baselineOnMigrate to true
to initialize the schema history table.
```

### Minimal reproduction (controlled experiment)

| Run | Command | Result |
|-----|---------|--------|
| A | `./mvnw test -Dtest='RefundFlowE2ETest'` | `EXIT=0` — **passes** |
| B | `./mvnw test -Dtest='PaymentJpaRepositoryIntegrationTest,RefundFlowE2ETest'` | `EXIT=1` — e2e class gets **10/10 errors**, all the Flyway "non-empty schema" error |

The same e2e test passes alone and fails when the integration test shares its JVM.
That isolates the bug precisely.

## 3. Root cause

All container-backed tests share **one JVM-wide `static` Postgres container**
(`ContainerConfig.postgres`, a singleton started in a static block). Three test
types use it: `E2ETestBase` (all e2e tests), `ProdProfileAuthSmokeE2ETest`, and
`PaymentJpaRepositoryIntegrationTest`. But they manage that one physical schema in
**two incompatible ways**:

| | `PaymentJpaRepositoryIntegrationTest` | e2e / prod-smoke tests |
|---|---|---|
| Profile | `@ActiveProfiles("test")` | `@ActiveProfiles("e2e")` / `("prod")` |
| Config file | `application-test.yml` | `application-e2e.yml` |
| `ddl-auto` | `create-drop` | `validate` |
| Flyway | `enabled: false` | `enabled: true`, `baseline-on-migrate: false` |

Sequence that breaks the build (single JVM, shared container):

1. `PaymentJpaRepositoryIntegrationTest` loads first (`P` < `R` in default run
   order). With `create-drop` + Flyway disabled, **Hibernate creates all entity
   tables** in `public` — but **never creates `flyway_schema_history`**.
2. Spring **caches** that context. `create-drop` only drops tables when the context
   *closes*, which for a cached context is **JVM shutdown**. So the Hibernate-made
   tables **persist in the shared container** for the rest of the run.
3. An e2e context loads next: `validate` + Flyway enabled + `baseline-on-migrate:
   false`. Flyway inspects the schema, finds **tables present but no history
   table**, and by design refuses to operate on a "foreign" schema → throws.
4. That first failure trips the `ApplicationContext failure threshold (1)`, so all
   remaining e2e tests in the JVM are auto-errored → 128 errors → `BUILD FAILURE`
   → no jar.

### Why CI stays green

CI runs the two families in **separate JVMs**
(`-Dtest='!com.payment.gateway.e2e.**'` then `-Dtest='com.payment.gateway.e2e.**'`),
so each gets its **own fresh static container** and never collides.
`./mvnw clean package` runs everything in **one JVM**, so the two families fight
over the shared container. This asymmetry is why the bug is invisible in CI but
reproducible locally.

## 4. The fix

Eliminate the *incompatible* schema strategy: make `PaymentJpaRepositoryIntegrationTest`
use a Flyway-managed schema with `validate`, exactly like every other context that
touches the shared container. Then **every** container-sharing context manages the
schema the same way — the first to load runs the migrations, the rest validate
against them — and no context ever sees a "foreign" schema, **regardless of test
execution order**.

Mechanism: add two properties **scoped to that one test class**, inside its
existing `@SpringBootTest(properties = { ... })` block:

```java
"spring.flyway.enabled=true",
"spring.jpa.hibernate.ddl-auto=validate",
```

This is safe because the test is already `@Transactional` — each test method's
writes roll back, so data isolation never depended on `create-drop`. The schema is
provided once by Flyway; per-test data still rolls back. The mapping is already
proven consistent with the migrations (the e2e suite runs `validate` against the
same entities successfully).

### Alternatives considered and rejected

- **`@DirtiesContext(AFTER_CLASS)` on the integration test** — order-*fragile*. If an
  e2e context loads before this test, `create-drop` would drop the Flyway-created
  tables and orphan the history table, breaking later contexts a different way.
  Rejected: fixes only one execution order.
- **Editing `application-test.yml` to enable Flyway + `validate` globally** — too
  broad. `Resilience4jConfigTest` also uses `@ActiveProfiles("test")` but has no
  Postgres datasource; enabling Flyway there risks breaking it. Rejected: scope the
  change to the culprit class instead.
- **Separate database/container for the `create-drop` family** — heavier (second
  Postgres container or schema wiring) for no additional benefit over unifying on
  Flyway. Rejected: more moving parts.
- **`baseline-on-migrate: true` in e2e config** — masks the problem; Flyway would
  then try to migrate on top of Hibernate's tables and likely hit "already exists".
  Rejected: hides state, doesn't fix the incompatibility.

## 5. Verification

1. **Minimal repro flips green:** re-run experiment B
   (`-Dtest='PaymentJpaRepositoryIntegrationTest,RefundFlowE2ETest'`) → expect
   `EXIT=0`, 0 errors (was 10/10 errors).
2. **Full build succeeds end to end:** `./mvnw clean package` → expect
   `BUILD SUCCESS`, `Errors: 0`, and a single runnable
   `target/payment-gateway-0.0.1-SNAPSHOT.jar`.
3. **No regression in the integration test itself:** `PaymentJpaRepositoryIntegrationTest`
   still passes (schema now from Flyway, data still rolled back per test).

## 6. Delivery & rollback

- **Branch:** `fix/e2e-shared-container-flyway` off `main` (separate from the README
  docs PR). Draft PR per repo workflow; never auto-merge.
- **Files touched:** `PaymentJpaRepositoryIntegrationTest.java` (2 added properties)
  plus this spec. `specs/INDEX.md` is left untouched.
- **Risk:** low — one test class, no production code. Worst case the entity/migration
  mapping has a latent mismatch that `validate` surfaces; that would be a real bug
  worth knowing, and is already implicitly asserted by the e2e suite.
- **Rollback:** revert the commit; behavior returns to the prior (contaminating)
  state.
