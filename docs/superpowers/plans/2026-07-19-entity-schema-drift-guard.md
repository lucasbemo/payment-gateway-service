# Entity↔Schema Drift Guard (Stage A) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add `EntitySchemaDriftTest` — a Testcontainers test that compares every JPA entity's `@Column` metadata against the real Flyway schema (V1→V19) and fails on any drift not recorded in a checked-in allowlist debt register.

**Architecture:** Plain JUnit 5 test (no Spring context) in `com.payment.gateway.architecture`. It starts its own pristine `postgres:15-alpine` container, runs `Flyway.migrate()`, reads the schema via JDBC `DatabaseMetaData`, builds Hibernate boot `Metadata` from all `@Entity` classes (classpath-scanned, so future entities are auto-covered), and diffs the two. Drift not in `known-schema-drift.txt` fails; stale allowlist entries also fail — the register can only shrink truthfully. Stages B/C (separate plans, written after this lands) empty the register.

**Tech Stack:** Java 26, JUnit 5, AssertJ, Testcontainers 2.x (`PostgreSQLContainer`), Flyway (already a prod dependency), Hibernate 7 boot metadata (`MetadataSources`), Spring `ClassPathScanningCandidateComponentProvider` (from spring-context, already on test classpath).

## Global Constraints

- Build/run with JDK 26 (Temurin); Docker required for Testcontainers.
- Spec: `docs/superpowers/specs/2026-07-19-entity-flyway-schema-drift-design.md` — this plan implements §3 (Stage 1 / PR A) only.
- No production code changes; no new Maven dependencies; no new migrations.
- Nullability policy: entity may be stricter than schema, never looser.
- Unmapped schema columns are allowed iff nullable or DB-defaulted.
- Unique constraints compared as **column-name sets**, never by constraint name.
- AssertJ only; test naming per repo conventions.
- `make format` before every commit (spotless is the CI gate); Conventional Commits; end commit messages with `Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>`.
- Do not stage `specs/INDEX.md`.

---

### Task 1: `EntitySchemaDriftTest` harness (RED — reports real drift)

**Files:**
- Create: `src/test/java/com/payment/gateway/architecture/EntitySchemaDriftTest.java`
- Create: `src/test/resources/known-schema-drift.txt` (header comment only, no entries yet)

**Interfaces:**
- Consumes: `src/main/resources/db/migration/V*.sql` (Flyway), all `@Entity` classes under `com.payment.gateway`.
- Produces: drift records formatted `table.column | KIND | detail` (KIND ∈ `LENGTH`, `SCALE`, `NULLABLE`, `TYPE`, `MISSING_IN_SCHEMA`, `UNMAPPED_REQUIRED`, `UNIQUE_MISSING`; unique-key rows use `table.<uk:colA+colB> | UNIQUE_MISSING`). Task 2 and the Stage B/C plans rely on this exact format.

- [ ] **Step 1: Create the empty allowlist file**

`src/test/resources/known-schema-drift.txt`:

```text
# Known entity<->schema drift register. Format: table.column | KIND | note
# The EntitySchemaDriftTest fails on drift missing from this file AND on
# stale entries whose drift no longer exists. This file may only shrink.
```

- [ ] **Step 2: Write the test**

`src/test/java/com/payment/gateway/architecture/EntitySchemaDriftTest.java`:

```java
package com.payment.gateway.architecture;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.Entity;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.flywaydb.core.Flyway;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.Table;
import org.hibernate.mapping.UniqueKey;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@DisplayName("Entity <-> Flyway schema drift guard")
class EntitySchemaDriftTest {

    private static final String BASE_PACKAGE = "com.payment.gateway";
    private static final String ALLOWLIST_RESOURCE = "/known-schema-drift.txt";

    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
                    DockerImageName.parse("postgres:15-alpine"))
            .withDatabaseName("driftdb")
            .withUsername("drift")
            .withPassword("drift");

    @BeforeAll
    static void migrate() {
        postgres.start();
        Flyway.configure()
                .dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
                .load()
                .migrate();
    }

    @AfterAll
    static void stop() {
        postgres.stop();
    }

    @Test
    @DisplayName("Should have no entity/schema drift outside the known-drift register")
    void shouldHaveNoDriftOutsideRegister() throws Exception {
        Map<String, SchemaTable> schema = readSchema();
        Metadata entityMetadata = buildEntityMetadata();

        Set<String> drift = new TreeSet<>();
        Set<String> mappedColumns = new HashSet<>();

        for (Table table : entityMetadata.collectTableMappings()) {
            String tableName = table.getName().toLowerCase(Locale.ROOT);
            SchemaTable schemaTable = schema.get(tableName);
            assertThat(schemaTable)
                    .as("entity table %s must exist in the migrated schema", tableName)
                    .isNotNull();

            for (Column column : table.getColumns()) {
                String columnName = column.getName().toLowerCase(Locale.ROOT);
                String key = tableName + "." + columnName;
                mappedColumns.add(key);
                SchemaColumn sc = schemaTable.columns.get(columnName);
                if (sc == null) {
                    drift.add(key + " | MISSING_IN_SCHEMA | entity column has no schema column");
                    continue;
                }
                compareColumn(key, column, sc, drift);
            }

            for (UniqueKey uk : table.getUniqueKeys().values()) {
                Set<String> ukCols = new TreeSet<>();
                uk.getColumns().forEach(c -> ukCols.add(c.getName().toLowerCase(Locale.ROOT)));
                schemaTable.uniqueKeys.remove(ukCols);
            }
            for (Set<String> undeclared : schemaTable.uniqueKeys) {
                drift.add(tableName + ".<uk:" + String.join("+", undeclared)
                        + "> | UNIQUE_MISSING | schema unique constraint not declared on entity");
            }
        }

        for (SchemaTable schemaTable : schema.values()) {
            for (SchemaColumn sc : schemaTable.columns.values()) {
                String key = schemaTable.name + "." + sc.name;
                if (!mappedColumns.contains(key) && !sc.nullable && sc.defaultValue == null) {
                    drift.add(key + " | UNMAPPED_REQUIRED | NOT NULL column without default is unmapped");
                }
            }
        }

        Set<String> allowlist = readAllowlist();
        Set<String> driftKeys = new TreeSet<>();
        drift.forEach(d -> driftKeys.add(d.substring(0, d.indexOf(" | ", d.indexOf(" | ") + 3))));

        Set<String> newDrift = new TreeSet<>(drift);
        newDrift.removeIf(d -> allowlist.contains(keyAndKind(d)));
        Set<String> staleEntries = new TreeSet<>(allowlist);
        drift.forEach(d -> staleEntries.remove(keyAndKind(d)));

        assertThat(newDrift)
                .as("NEW drift not in known-schema-drift.txt — fix the entity or (only if truly known debt) register it")
                .isEmpty();
        assertThat(staleEntries)
                .as("STALE register entries — this drift no longer exists; remove the lines from known-schema-drift.txt")
                .isEmpty();
    }

    private static String keyAndKind(String driftLine) {
        int second = driftLine.indexOf(" | ", driftLine.indexOf(" | ") + 3);
        return driftLine.substring(0, second);
    }

    private void compareColumn(String key, Column entity, SchemaColumn sc, Set<String> drift) {
        boolean schemaIsText = sc.typeName.equals("text");
        boolean entityDeclaresText =
                entity.getSqlType() != null && entity.getSqlType().toLowerCase(Locale.ROOT).startsWith("text");

        if (schemaIsText && !entityDeclaresText) {
            drift.add(key + " | TYPE | schema=text entity=varchar(" + entity.getLength() + ")");
        } else if (sc.typeName.contains("varchar") && entity.getLength() != null) {
            if (entity.getLength().intValue() != sc.size) {
                drift.add(key + " | LENGTH | entity=" + entity.getLength() + " schema=" + sc.size);
            }
        } else if (sc.typeName.equals("numeric")) {
            Integer p = entity.getPrecision();
            Integer s = entity.getScale();
            if (p != null && s != null && (p != sc.size || s != sc.decimalDigits)) {
                drift.add(key + " | SCALE | entity=(" + p + "," + s + ") schema=(" + sc.size + ","
                        + sc.decimalDigits + ")");
            }
        }

        // Policy: entity may be stricter than schema, never looser.
        if (entity.isNullable() && !sc.nullable) {
            drift.add(key + " | NULLABLE | entity=nullable schema=NOT NULL");
        }
    }

    private Metadata buildEntityMetadata() throws ClassNotFoundException {
        var registry = new StandardServiceRegistryBuilder()
                .applySetting("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect")
                .applySetting(
                        "hibernate.physical_naming_strategy",
                        "org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy")
                .build();
        MetadataSources sources = new MetadataSources(registry);
        var scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(Entity.class));
        for (var candidate : scanner.findCandidateComponents(BASE_PACKAGE)) {
            sources.addAnnotatedClass(Class.forName(candidate.getBeanClassName()));
        }
        return sources.buildMetadata();
    }

    private Map<String, SchemaTable> readSchema() throws Exception {
        Map<String, SchemaTable> result = new HashMap<>();
        try (Connection conn = DriverManager.getConnection(
                postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())) {
            DatabaseMetaData meta = conn.getMetaData();
            try (ResultSet tables = meta.getTables(null, "public", "%", new String[] {"TABLE"})) {
                while (tables.next()) {
                    String name = tables.getString("TABLE_NAME").toLowerCase(Locale.ROOT);
                    if (name.equals("flyway_schema_history")) {
                        continue;
                    }
                    result.put(name, new SchemaTable(name));
                }
            }
            for (SchemaTable table : result.values()) {
                try (ResultSet cols = meta.getColumns(null, "public", table.name, "%")) {
                    while (cols.next()) {
                        SchemaColumn c = new SchemaColumn(
                                cols.getString("COLUMN_NAME").toLowerCase(Locale.ROOT),
                                cols.getString("TYPE_NAME").toLowerCase(Locale.ROOT),
                                cols.getInt("COLUMN_SIZE"),
                                cols.getInt("DECIMAL_DIGITS"),
                                "YES".equals(cols.getString("IS_NULLABLE")),
                                cols.getString("COLUMN_DEF"));
                        table.columns.put(c.name, c);
                    }
                }
                Set<String> pkColumns = new HashSet<>();
                try (ResultSet pk = meta.getPrimaryKeys(null, "public", table.name)) {
                    while (pk.next()) {
                        pkColumns.add(pk.getString("COLUMN_NAME").toLowerCase(Locale.ROOT));
                    }
                }
                Map<String, Set<String>> indexes = new HashMap<>();
                try (ResultSet idx = meta.getIndexInfo(null, "public", table.name, true, false)) {
                    while (idx.next()) {
                        String idxName = idx.getString("INDEX_NAME");
                        String col = idx.getString("COLUMN_NAME");
                        if (idxName != null && col != null) {
                            indexes.computeIfAbsent(idxName, k -> new TreeSet<>())
                                    .add(col.toLowerCase(Locale.ROOT));
                        }
                    }
                }
                for (Set<String> cols : indexes.values()) {
                    if (!cols.equals(pkColumns)) {
                        table.uniqueKeys.add(cols);
                    }
                }
            }
        }
        return result;
    }

    private Set<String> readAllowlist() throws Exception {
        Set<String> entries = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                getClass().getResourceAsStream(ALLOWLIST_RESOURCE), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                String[] parts = line.split("\\|");
                entries.add(parts[0].trim() + " | " + parts[1].trim());
            }
        }
        return entries;
    }

    private static final class SchemaTable {
        final String name;
        final Map<String, SchemaColumn> columns = new HashMap<>();
        final List<Set<String>> uniqueKeys = new ArrayList<>();

        SchemaTable(String name) {
            this.name = name;
        }
    }

    private record SchemaColumn(
            String name, String typeName, int size, int decimalDigits, boolean nullable, String defaultValue) {}
}
```

Implementation notes for the engineer (deviations are expected, semantics are not):
- Hibernate 7 API: `Column.getLength()` returns `Long`, `getPrecision()`/`getScale()` return `Integer`; if method names differ in the pinned version, adapt — the *checks* (exact length match on varchar, exact precision/scale on numeric, no-looser nullability, UK column-set match, unmapped-required, missing-in-schema) are the contract.
- `SchemaColumn.nullable`/`defaultValue` are record components accessed as `sc.nullable`/`sc.defaultValue` — if the record accessor style clashes, use `sc.nullable()` form consistently.
- The physical naming strategy `CamelCaseToUnderscoresNamingStrategy` mirrors Spring Boot's default so fields without explicit `@Column(name=...)` resolve identically.
- JDBC `getIndexInfo(..., unique=true, ...)` returns the PK index too — it is filtered by comparing column sets against `getPrimaryKeys`.

- [ ] **Step 3: Run the test — expect FAIL with the full real drift report**

Run: `./mvnw test -Dtest='EntitySchemaDriftTest'`
Expected: FAIL. The `NEW drift not in known-schema-drift.txt` assertion lists every real mismatch (per the 2026-07-18 audit: ~36 lines — 9 LENGTH, 1 SCALE, ~20 NULLABLE, 3 TYPE, 3 UNIQUE_MISSING; spec §6). **Save this output — it is the ground truth for Task 2.** If instead it errors before the assertion (API mismatch, naming issue), fix the harness first; the test must fail on the *assertion*, not on an exception.

- [ ] **Step 4: Commit the RED harness**

```bash
make format
git add src/test/java/com/payment/gateway/architecture/EntitySchemaDriftTest.java src/test/resources/known-schema-drift.txt
git commit -m "$(cat <<'EOF'
test: add entity/Flyway schema drift guard (red - register empty)

Compares Hibernate entity metadata against the real migrated schema
(own pristine Postgres container + Flyway.migrate) and fails on drift
not in the known-schema-drift.txt register. Register is empty, so the
test currently fails, enumerating all real drift. Spec:
docs/superpowers/specs/2026-07-19-entity-flyway-schema-drift-design.md

Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>
EOF
)"
```

---

### Task 2: Populate the register (GREEN) and prove the guard bites

**Files:**
- Modify: `src/test/resources/known-schema-drift.txt`

**Interfaces:**
- Consumes: the drift lines printed by Task 1 Step 3 (`table.column | KIND | detail`).
- Produces: the populated debt register that the Stage B and Stage C plans will shrink to empty.

- [ ] **Step 1: Populate the allowlist from the Task 1 failure output**

Copy every reported drift line into `known-schema-drift.txt` (keep the header comment). Use the `table.column | KIND` part verbatim; put the reported detail as the note. **The test output is ground truth** — the audit predicts these entries (expect ≈36; audit §6 tallies may be off by a couple), but the file must match the output exactly, no more, no less: (Outcome note, post-execution: actual ground truth was 38 lines — the harness found `merchants.<uk:api_key_hash>` and `payment_items.payment_id | LENGTH`, which the manual audit missed.)

```text
# Known entity<->schema drift register. Format: table.column | KIND | note
# The EntitySchemaDriftTest fails on drift missing from this file AND on
# stale entries whose drift no longer exists. This file may only shrink.
# Stage B (auditing adoption) removes the *_at NULLABLE entries.
# Stage C (annotation alignment) removes everything else.
customers.<uk:merchant_id+token> | UNIQUE_MISSING | schema unique constraint not declared on entity
customers.updated_at | NULLABLE | entity=nullable schema=NOT NULL
discrepancies.created_at | NULLABLE | entity=nullable schema=NOT NULL
discrepancies.updated_at | NULLABLE | entity=nullable schema=NOT NULL
idempotency_keys.<uk:key_hash> | UNIQUE_MISSING | schema unique constraint not declared on entity
idempotency_keys.created_at | NULLABLE | entity=nullable schema=NOT NULL
idempotency_keys.updated_at | NULLABLE | entity=nullable schema=NOT NULL
merchants.api_key | NULLABLE | entity=nullable schema=NOT NULL
merchants.api_key_hash | LENGTH | entity=255 schema=512
merchants.api_key_hash | NULLABLE | entity=nullable schema=NOT NULL
merchants.api_secret_hash | LENGTH | entity=255 schema=512
merchants.api_secret_hash | NULLABLE | entity=nullable schema=NOT NULL
merchants.updated_at | NULLABLE | entity=nullable schema=NOT NULL
merchants.webhook_secret | LENGTH | entity=255 schema=512
outbox_events.error_message | TYPE | schema=text entity=varchar(255)
outbox_events.retry_count | NULLABLE | entity=nullable schema=NOT NULL
payments.amount | SCALE | entity=(19,2) schema=(19,4)
payments.customer_id | LENGTH | entity=64 schema=36
payments.id | LENGTH | entity=64 schema=36
payments.idempotency_key | LENGTH | entity=128 schema=255
payments.merchant_id | LENGTH | entity=64 schema=36
payments.payment_method_id | LENGTH | entity=64 schema=36
payments.status | LENGTH | entity=32 schema=50
payments.updated_at | NULLABLE | entity=nullable schema=NOT NULL
reconciliation_batches.<uk:batch_date+merchant_id> | UNIQUE_MISSING | schema unique constraint not declared on entity
reconciliation_batches.discrepancy_count | NULLABLE | entity=nullable schema=NOT NULL
reconciliation_batches.matched_transactions | NULLABLE | entity=nullable schema=NOT NULL
reconciliation_batches.mismatched_transactions | NULLABLE | entity=nullable schema=NOT NULL
reconciliation_batches.total_transactions | NULLABLE | entity=nullable schema=NOT NULL
reconciliation_batches.updated_at | NULLABLE | entity=nullable schema=NOT NULL
refunds.error_message | TYPE | schema=text entity=varchar(255)
refunds.retry_count | NULLABLE | entity=nullable schema=NOT NULL
refunds.updated_at | NULLABLE | entity=nullable schema=NOT NULL
transactions.error_message | TYPE | schema=text entity=varchar(255)
transactions.retry_count | NULLABLE | entity=nullable schema=NOT NULL
transactions.updated_at | NULLABLE | entity=nullable schema=NOT NULL
```

- [ ] **Step 2: Run the test — expect PASS**

Run: `./mvnw test -Dtest='EntitySchemaDriftTest'`
Expected: PASS, `Failures: 0, Errors: 0`.

- [ ] **Step 3: Mutation check 1 — stale entry must fail**

Append a fake line to `known-schema-drift.txt`: `payments.currency | LENGTH | fake stale entry`
Run: `./mvnw test -Dtest='EntitySchemaDriftTest'`
Expected: FAIL on the `STALE register entries` assertion naming `payments.currency | LENGTH`.
Then remove the fake line.

- [ ] **Step 4: Mutation check 2 — new drift must fail**

Temporarily change `length = 3` to `length = 5` on the `currency` column in `src/main/java/com/payment/gateway/infrastructure/payment/adapter/out/persistence/PaymentJpaEntity.java`.
Run: `./mvnw test -Dtest='EntitySchemaDriftTest'`
Expected: FAIL on the `NEW drift` assertion with `payments.currency | LENGTH | entity=5 schema=3`.
Then revert the change (`git checkout -- src/main/java/com/payment/gateway/infrastructure/payment/adapter/out/persistence/PaymentJpaEntity.java`).

- [ ] **Step 5: Run test once more to confirm clean state**

Run: `./mvnw test -Dtest='EntitySchemaDriftTest'`
Expected: PASS.

- [ ] **Step 6: Commit**

```bash
make format
git add src/test/resources/known-schema-drift.txt
git status --short   # only the register (and possibly formatted test) staged; specs/INDEX.md must NOT appear
git commit -m "$(cat <<'EOF'
test: populate schema-drift register with audited debt (green)

Register now lists all real drift found by EntitySchemaDriftTest.
Guard verified to bite both ways: a stale entry fails, and an
injected annotation mutation fails. Stages B/C shrink this file to
empty.

Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>
EOF
)"
```

---

### Task 3: Full verification and draft PR

**Files:** none (verification only).

**Interfaces:**
- Consumes: everything above.
- Produces: pushed branch `chore/entity-flyway-schema-drift` + draft PR (base `main`).

- [ ] **Step 1: Full build**

Run: `./mvnw clean package`
Expected: `BUILD SUCCESS`, 0 failures / 0 errors (≈1209 + 1 new test), exactly one jar in `target/`.

- [ ] **Step 2: Spotless gate**

Run: `./mvnw spotless:apply && ./mvnw spotless:check`
Expected: `BUILD SUCCESS` on check. If apply changed files, amend the Task 2 commit (`git add -u && git commit --amend --no-edit`).

- [ ] **Step 3: Boot the real app and smoke it** (project rule: tests alone don't prove "done")

```bash
docker compose up -d postgres zookeeper kafka redis minio
REDIS_PORT=6380 ./mvnw spring-boot:run &   # note: application.yml default is 6379; compose maps 6380
# wait for startup, then:
curl -sf localhost:8080/actuator/health          # expect overall "status":"UP"
curl -s 'localhost:8080/api/v1/payments/pay-nonexistent?merchantId=merchant-123'
# expect ApiResponse envelope with "Payment not found: pay-nonexistent"
# then stop the app (kill the spring-boot:run process)
```

- [ ] **Step 4: Push and open draft PR**

```bash
git push -u origin chore/entity-flyway-schema-drift
gh pr create --draft --base main --title "test: entity/Flyway schema drift guard with debt register" \
  --body "Stage A of the schema-drift spec (docs/superpowers/specs/2026-07-19-entity-flyway-schema-drift-design.md §3). Adds EntitySchemaDriftTest + known-schema-drift.txt register (~36 audited entries). Guard fails on new drift AND stale entries. Test-only change; rollback = revert. Stages B (auditing adoption) and C (annotation alignment) follow and shrink the register to empty."
```

---

## Self-Review

- **Spec coverage (§3 only, by design):** own-container mechanics ✓ (Task 1 harness; pristine container instead of shared `ContainerConfig` — deliberate deviation from spec's "uses the shared static Postgres" wording, chosen because `ContainerConfig` force-starts Kafka+Redis and keeps `postgres` package-private; the spec's intent — migrate V1→V19 and compare — is unchanged); JDBC schema side ✓; Hibernate entity side ✓; all seven invariant rows ✓ (`compareColumn` + UK set-diff + unmapped/missing checks); allowlist two-way failure ✓ (Task 2 Steps 3–4 prove both directions); "guard demonstrably fails on injected fake drift" from spec §5 ✓ (mutation checks). Stages B/C intentionally deferred to their own plans, fed by this guard's output.
- **Placeholder scan:** none — full test source, full register content, exact commands and expected outputs; Hibernate-API adaptation note is bounded (semantics fixed, method-name drift allowed).
- **Type consistency:** drift-line format `table.column | KIND | detail` is identical in test code, register file, and Task 2 instructions; `keyAndKind` matching uses the first two segments consistently.
