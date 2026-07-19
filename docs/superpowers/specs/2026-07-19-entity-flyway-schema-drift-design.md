# Entity ↔ Flyway Schema Drift — Design

**Date:** 2026-07-19
**Status:** Approved
**Origin:** Found while fixing the e2e shared-container Flyway failure (PR #26): JPA
entity annotations disagree with the real migrated schema, and Hibernate
`ddl-auto=validate` cannot see length, nullability, or scale drift. A full audit found
drift in **11 of 12 tables**.

## 1. Problem

Entity `@Column` annotations and the Flyway schema (V1→V19) have diverged. `validate`
only catches missing tables/columns and gross type mismatches, so the drift is invisible
to CI and surfaces only at runtime (e.g. `payments.updated_at NOT NULL` violations, the
PR #26 fixture failures). The `test` profile uses Hibernate `create-drop` with Flyway off,
so no test ever exercises the real schema shape.

Audit summary (2026-07-18, full detail in §6):

- 9 string-length mismatches (e.g. payments ids `length=64` vs `VARCHAR(36)`).
- 18 nullability mismatches, entity-nullable vs schema `NOT NULL` — 10 of them the
  `updated_at`/`created_at` pattern; plus `retry_count` ×3 and reconciliation counters ×4.
- 1 reverse (entity stricter): `customers.email` `nullable=false` vs schema nullable.
- 1 precision/scale: `payments.amount` entity `(19,2)` vs schema `DECIMAL(19,4)`.
- 3 type drifts: `error_message` unannotated (→`varchar(255)`) vs schema `TEXT`.
- 3 schema unique constraints undeclared on entities.
- 9 schema columns unmapped by any entity (JSONB `metadata`/`configuration`/
  `provider_response`, `transactions.provider`, `settlement_reports.generated_at`,
  `payment_items.created_at`).
- JPA auditing (`@EnableJpaAuditing` in `JpaConfig`, `AuditorAwareImpl`, `BaseEntity`
  with `@CreatedDate`/`@LastModifiedDate`) is fully wired but used by **zero** entities;
  every entity sets timestamps manually — the root cause of the `updated_at` drift class.
  `BaseEntity` itself has the `@Id length=64` bug.

## 2. Goal & decisions (user-approved)

1. **Fix + prevent:** align entities with the schema AND add an automated guard so drift
   cannot silently return.
2. **Truth source — case-by-case**, defaulting to migrations-as-reality. Outcome of the
   case-by-case review: **zero new migrations** in this task (§4 dispositions).
3. **Timestamps:** adopt the existing-but-dead JPA auditing infrastructure instead of
   manual builder-set timestamps.
4. **Delivery shape:** guard first with an allowlist debt register, then two fix stages
   that shrink it to empty. Three PRs (A/B/C), each independently green and revertible.

## 3. Stage 1 (PR A) — the guard: `EntitySchemaDriftTest`

New Testcontainers-backed test in `com.payment.gateway.architecture` (beside
`HexagonalArchitectureTest`).

**Mechanics**

- Starts its own pristine `postgres:15-alpine` container and runs `Flyway.migrate()`
  programmatically (deliberately NOT the shared `test/ContainerConfig` Postgres: that class
  force-starts Kafka+Redis and keeps its container package-private; a dedicated container
  also guarantees a schema built by migrations alone).
- **Schema side:** JDBC `DatabaseMetaData` per table — column inventory, varchar length,
  nullability, numeric precision/scale, unique indexes.
- **Entity side:** Hibernate mapping model built via `MetadataSources` registering the 13
  `*JpaEntity` classes — `@Column` length/nullable/precision/scale, `@Table` unique
  constraints.

**Invariants checked (per mapped column)**

| Check | Rule |
| --- | --- |
| Length | exact match (varchar) |
| Precision/scale | exact match (numeric) |
| Nullability | entity may be **stricter** than schema, never **looser** (entity-nullable + schema NOT NULL = fail) |
| Unique constraints | every schema unique constraint must be declared on the entity |
| Entity column w/o schema column | fail (validate also catches this; kept for one-stop reporting) |
| Schema column unmapped by entity | allowed **only if** nullable or DB-defaulted; else fail |
| Type class | varchar vs TEXT vs numeric family must agree |

**Allowlist = debt register:** `src/test/resources/known-schema-drift.txt`, one line per
known mismatch: `table.column | kind | note`. The test fails on (a) any drift NOT in the
file and (b) any **stale** entry whose drift no longer exists. The register can only
shrink truthfully. PR A ships green with 38 entries (the guard found 2 real drifts the manual audit in §6 missed: `merchants.<uk:api_key_hash>` and `payment_items.payment_id | LENGTH`); PR C ends with the file empty (file
retained so emptiness is enforced).

## 4. Stage 2 (PR B) — adopt auditing; Stage 3 (PR C) — align annotations

**PR B (the only behavioral PR, isolated on purpose):**

- Fix `BaseEntity` (`@Id` length 64→36) first.
- Entities whose tables have `created_at NOT NULL` + `updated_at` extend `BaseEntity`:
  merchants, payments, customers, payment_methods, idempotency_keys, transactions,
  refunds, reconciliation_batches, discrepancies (9).
- `outbox_events`, `settlement_reports` (no `updated_at` column): `@EntityListeners` +
  `@CreatedDate` directly — extending `BaseEntity` would declare a column the schema
  lacks and break `validate`.
- `payment_items`: unchanged (identity PK; `created_at` DB-defaulted, unmapped).
- Builders/mappers/persistence adapters stop writing timestamps on save (listener owns
  them); entity→domain reads unchanged. Each `*Mapper`/`*PersistenceAdapter` is reviewed
  individually — this is the PR's risk area. Test fixtures that set timestamps manually
  (e.g. `PaymentJpaRepositoryIntegrationTest` after PR #26) drop those setters.
- Removes the 10 timestamp entries from the allowlist.

**PR C (metadata only, zero behavior):** mechanical annotation→schema alignments and the
case-by-case dispositions:

| Item | Disposition |
| --- | --- |
| payments `id`/`merchant_id`/`customer_id`/`payment_method_id` 64→36; `status` 32→50; `idempotency_key` 128→255 | align annotation |
| merchants `api_key_hash`/`api_secret_hash` 255→512, `webhook_secret` 255→512; `api_key` + hashes `nullable=false` | align annotation |
| `payments.amount` scale 2→4 | align annotation to `(19,4)`; values are cents-based (≤2 decimals), schema headroom harmless; **no migration** |
| `retry_count` (outbox, transactions, refunds) & reconciliation counters | `nullable=false`, default 0 |
| `error_message` ×3 | `@Column(columnDefinition = "text")` |
| customers `UNIQUE(merchant_id, token)`, idempotency_keys `UNIQUE(key_hash)`, reconciliation_batches `UNIQUE(merchant_id, batch_date)` | declare via `@Table(uniqueConstraints=...)` |
| `customers.email` (entity stricter) | keep stricter — app-level domain enforcement; a `SET NOT NULL` migration could fail on existing rows. Covered by the guard's stricter-is-OK policy |
| Unmapped JSONB/extra columns (9) | leave unmapped — feature work with no consumer (YAGNI); permanently permitted by the guard's nullable/defaulted policy |
| Remaining allowlist entries | removed; file empty |

**Out of scope:** flipping the whole `test` profile to Flyway (guard makes it redundant;
risks breaking non-DB `test`-profile contexts); mapping JSONB columns; any new
migration; `NotFound→400` HTTP mapping issue; `REDIS_PORT` default drift (separate
one-line fix).

**Considered and deferred — ID strategy (UUID v7 / native `uuid` type):** `length=36`
matches the deployed `VARCHAR(36)` columns and everything `IdGenerator` emits (v4 UUID =
36 chars; `pay_<millis>_...` = 34). Migrating to UUID v7 (b-tree insert locality) and/or
Postgres native `uuid` (16-byte storage) was evaluated and deferred: Java has no built-in
v7 generator, Postgres 15 has no `uuidv7()`, the prefixed `pay_`/`txn_` format is
incompatible with a `uuid` column, and externally-referenced payment IDs make any format
change high-churn. The time-prefixed IDs already give v7-like locality on the hottest
tables. Revisit only when write volume makes index bloat measurable.

## 5. Verification & rollback

Per PR: `./mvnw clean package` green (full suite, single JVM), `spotless:check`, and a
real app boot (`REDIS_PORT=6380`, docker stack) with `/actuator/health` UP plus one
endpoint smoke in the touched area. PR-specific: A — guard green with full register and
demonstrably fails on an injected fake drift (mutation-test it once, manually); B — e2e
suite proves timestamps still populate (rows get non-null `created_at`/`updated_at`);
C — guard green with empty register.

Rollback: each PR is a single revert. A is test-only; C is annotation metadata only; B is
the sole behavioral change and sits between two safe PRs.

## 6. Audit reference (2026-07-18)

Full per-table findings preserved from the audit agent: payments (6 length + scale +
status + updated_at + 2 unmapped), merchants (3 length + 4 nullability + 1 unmapped),
customers (email reverse + updated_at + unique + unmapped), payment_items (created_at
unmapped), idempotency_keys (2 nullability + unique), outbox_events (retry_count +
error_message TEXT), transactions (retry_count + updated_at + error_message + 2
unmapped), refunds (retry_count + updated_at + error_message + 1 unmapped),
reconciliation_batches (4 counters + updated_at + unique), discrepancies (created_at +
updated_at), settlement_reports (generated_at unmapped), payment_methods (clean).
