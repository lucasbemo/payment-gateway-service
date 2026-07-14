# Feature Spec: Increase unit test coverage + enforce a domain coverage threshold

Status: completed
Merged PR: https://github.com/lucasbemo/payment-gateway-service/pull/23
Source: https://github.com/lucasbemo/payment-gateway-service/issues/21

## Problem

The project has broad unit/slice/e2e test suites but **no enforced coverage
floor**. JaCoCo is wired for reporting only (`prepare-agent` + `report`), so
coverage can silently regress and there is no signal when new business logic
ships untested. The issue asks to (1) raise unit test coverage across the
project and (2) enforce a ~70% coverage threshold on the **domain / business**
packages so regressions fail the build.

> **Confirmed decisions (see `decisions.md`):** scope = `domain.* + application.*`
> · counter = **LINE** · threshold = **70%** · granularity = **aggregate (bundle)**
> · the gate is **advisory** — 70% is configured now but `haltOnFailure=false`,
> so a below-threshold run logs a warning and keeps the build/CI green (it does
> not block the workflow).

## Desired Behavior

1. A JaCoCo **coverage rule** (`jacoco:check`) reports when the
   `domain.* + application.*` packages fall below **70% LINE** coverage
   (aggregate across the bundle). It is **advisory** (`haltOnFailure=false`):
   it emits a clear warning to keep developers aware of the 70% target but does
   **not** fail the build.
2. The rule runs on the **unit/slice** test run (the run that excludes
   `e2e.**`), so "unit test coverage" is what is measured.
3. Boilerplate/generated code (records, enums, DTOs, exceptions,
   MapStruct-generated mappers, Lombok-generated accessors, Spring config,
   the `main` class, port interfaces) is **excluded** from the coverage
   denominator so the percentage reflects real behavioral coverage.
4. New unit tests are added to bring the in-scope packages **at or above** the
   threshold, using existing conventions (Mockito BDD, AssertJ, `@Nested`/
   `@DisplayName`, domain factory methods).
5. `make coverage` continues to produce the HTML/CSV/XML report; the gate is
   additive, not a replacement.

## Current Behavior

* `pom.xml` (~lines 347-365): `jacoco-maven-plugin` **0.8.15** with only
  `prepare-agent` and a `report` execution bound to the `test` phase. **No
  `check` execution, no rules, no thresholds, no excludes.**
* `make coverage` = `mvn clean test jacoco:report`. `make test` runs the full
  suite.
* CI (`.github/workflows/ci.yml`): the `test` job runs
  `./mvnw test -Dtest='!com.payment.gateway.e2e.**'` (unit + slice); a separate
  `e2e` job runs `-Dtest='com.payment.gateway.e2e.**'`. `code-quality` runs
  `checkstyle`/`spotbugs` wrapped in `|| true` (no-ops); only `spotless:check`
  is a real gate today.
* The JaCoCo report bound to the `test` phase reflects **only whichever tests
  the current run executed** — so a coverage `check` added to the unit job
  measures unit+slice coverage only (code covered exclusively by e2e will not
  count toward the domain gate).
* Rough source/test inventory: domain 77 main / 32 tests · application 76 / 21 ·
  commons 63 / 15 · infrastructure 152 / 39 · e2e 12.
* **Baseline is currently unknown.** The checked-in
  `target/site/jacoco/jacoco.csv` is stale/partial (reports ~2% domain despite
  32 domain unit-test files) and must not be trusted. A clean full unit run is
  required to establish the real baseline (see `tasks.md`, task 1).

## Acceptance Criteria

* [ ] `jacoco:check` is configured with an **advisory** rule
      (`haltOnFailure=false`) at **70% LINE** aggregate over
      `domain.* + application.*`; a below-threshold run prints a visible warning
      but the build/CI stays green.
* [ ] The rule's `includes`/`excludes` limit measurement to real business logic
      (generated/boilerplate excluded — see Domain Rules).
* [ ] The rule runs on the unit/slice job (not e2e).
* [ ] New unit tests are added to raise the in-scope aggregate LINE coverage
      toward/at 70% (best-effort for "whole project" beyond that).
* [ ] New tests follow existing conventions (Mockito BDD, AssertJ only,
      `@Nested` + `@DisplayName`, `shouldDoX` naming, domain factory methods).
* [ ] `make coverage` still emits the report; README/CLAUDE.md updated if the
      developer workflow changes (e.g. new `make` target or gate description).
* [ ] No production source behavior changes (test-only + build config).

## API Contract

Not applicable — no runtime API changes. This is build/test infrastructure plus
new tests. The only "contract" affected is the Maven/CI build: `jacoco:check`
now enforces coverage.

## Domain Rules

* **In-scope packages (confirmed):** `com.payment.gateway.domain.*` **and**
  `com.payment.gateway.application.*`. (`commons.model` is out of the gate scope
  per the decision, though `Money` may still get tests as best-effort.)
* **Excluded from coverage denominator** (boilerplate / generated / no
  meaningful branches):
  * `**/dto/**`, `**/*Command.*`, `**/*Response.*`, `**/*DTO.*`
  * `**/*Exception.*`
  * MapStruct-generated `**/*MapperImpl.*`, Lombok-generated accessors
  * `**/port/**` (interfaces — no executable code)
  * `PaymentGatewayApplication`, `**/config/**`, `**/*Config.*`
  * enums/records with no behavior (case-by-case)
* **Threshold semantics (confirmed):** **70% LINE**, measured as an **aggregate
  (bundle)** across the in-scope packages — not a per-class floor.
* **Advisory, not blocking (confirmed):** `haltOnFailure=false`. The 70% target
  is set now to keep developers informed, but a below-threshold run does not
  fail the build. Tightening to a hard gate later is a one-line `haltOnFailure`
  flip.

## Data Changes

None. No Flyway migrations, no schema changes, no persistence changes.

## Observability

None at runtime. Build-time signal only: JaCoCo HTML report
(`target/site/jacoco/index.html`), CSV/XML for tooling, and CI job
failure/summary when the gate trips. Optionally surface coverage % in the GitHub
Actions step summary (nice-to-have, non-blocking).

## Security

No security-sensitive surface. New tests must not embed real secrets/keys; use
the existing `TestDataFactory` UUID-randomized fixtures. Keep
`ProdProfileAuthSmokeE2ETest` green (it exercises the real prod security chain);
this work should not touch it.

## Open Questions

None blocking — the five blocking questions were answered by Lucas on
2026-07-09 and are recorded as **Resolved Decisions** in `decisions.md`
(scope `domain.*+application.*` · LINE · 70% · aggregate · advisory/non-blocking).
Ready for implementation approval.
