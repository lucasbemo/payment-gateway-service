# Decisions: Increase unit test coverage + domain coverage gate

## Assumptions (non-blocking — will proceed on these unless corrected)

| # | Assumption | Rationale |
|---|---|---|
| A1 | The gate is enforced via JaCoCo's `check` goal in `pom.xml`, not a third-party plugin or external service (Codecov, etc.). | JaCoCo 0.8.15 is already configured; adding a `check` execution is the minimal, in-repo, offline solution. |
| A2 | Generated/boilerplate code (DTOs, records, enums, exceptions, MapStruct `*MapperImpl`, Lombok accessors, config, `port` interfaces, `PaymentGatewayApplication`) is excluded from the coverage denominator. | Otherwise 70% penalizes/rewards trivial getters and inflates effort without improving real coverage. |
| A3 | The gate measures the **unit/slice** run (CI `test` job, `-Dtest='!e2e.**'`), consistent with "unit test coverage". e2e-only coverage is not counted. | Matches the issue wording and the existing CI split; keeps the gate fast and deterministic without Testcontainers. |
| A4 | New tests follow the repo's existing style verbatim (Mockito BDD, AssertJ only, `@Nested`/`@DisplayName`, `shouldDoX`, domain factory methods). | CLAUDE.md testing conventions; consistency with the 89 existing unit/slice tests. |
| A5 | This is test-only + build-config; **no production source behavior changes**. If a class is found to be untestable without a refactor, that is raised separately rather than silently changed. | "Avoid unrelated refactors"; keep the PR scoped and low-risk. |
| A6 | The checked-in `target/site/jacoco/jacoco.csv` is stale/partial and is NOT used as the baseline; a clean run establishes the real numbers. | Report shows implausible ~2% domain despite 32 domain unit-test files. |
| A7 | Rollback = remove/disable the `jacoco:check` execution; reporting stays. | Cheap, reversible; stated in PR body per review rules. |

## Resolved Decisions (confirmed by Lucas, 2026-07-09)

| # | Question | Answer |
|---|---|---|
| Q1 | Gate scope | **`domain.* + application.*`** — the full business layer (pure domain core + use-case services). |
| Q2 | JaCoCo counter | **LINE** — 70% of lines executed. |
| Q3 | Rollout / blocking | **Set the threshold at 70% now, but do NOT hard-block the build.** The gate is **advisory** (`haltOnFailure=false`): JaCoCo logs a clear "coverage below 70%" warning so developers stay informed of the target, but a below-threshold run does **not** fail CI or the local build. (No ratcheting scheme; the number is fixed at 70% immediately, just non-fatal.) |
| Q4 | Granularity | **Aggregate (bundle)** across the in-scope packages — not a per-class floor. |
| Q5 | Whole-project target | Not separately gated. Only the domain/business 70% rule is configured (advisory); the "whole project" coverage increase is best-effort via the new unit tests added in Phase 2. |

### Implication of Q3 (advisory gate)

* Configure `jacoco:check` with `<haltOnFailure>false</haltOnFailure>` so
  violations print a visible warning but return success.
* This means the initial PR can land green regardless of the true baseline, and
  Phase 2 test-writing raises the real number toward 70% without build risk.
* If a hard gate is wanted later, flipping `haltOnFailure` to `true` is a
  one-line change — note this in the PR body as the future tightening path.

## Decision Log

| Date | Decision | Reason | Alternatives |
|---|---|---|---|
| 2026-07-09 | Draft spec proposes JaCoCo `check` on `domain.* + application.*`, boilerplate excluded, measured on the unit run. | Minimal, in-repo, matches conventions and CI split. | Codecov/external gate; per-class floor; include e2e coverage — deferred to blocking answers. |
