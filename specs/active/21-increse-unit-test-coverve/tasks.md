# Implementation Tasks: Increase unit test coverage + domain coverage gate

Status: draft

> Do not start until blocking questions (see spec.md / import output) are
> answered and the spec is human-approved.

## Phase 0 — Establish the real baseline (blocking prerequisite)

- [ ] Run a clean unit/slice coverage pass:
      `./mvnw clean test -Dtest='!com.payment.gateway.e2e.**' jacoco:report`
      (the JDK 26 toolchain — see CLAUDE.md gotchas).
- [ ] Record per-group INSTRUCTION/LINE/BRANCH % from `target/site/jacoco/jacoco.csv`
      for domain, application, commons. This is the trusted baseline (the
      checked-in report is stale — ignore it).
- [ ] Decide gate value vs. baseline (informs Open Question 3 / ratchet plan).

## Phase 1 — Configure the JaCoCo rule (build config) — CONFIRMED

- [ ] Add a `check` execution to `jacoco-maven-plugin` in `pom.xml` with:
      counter=**LINE**, minimum=**0.70**, `element`=BUNDLE (aggregate),
      `haltOnFailure`=**false** (advisory — warns but never fails the build).
- [ ] Scope with `<includes>` to `com/payment/gateway/domain/**` and
      `com/payment/gateway/application/**`, plus `<excludes>` for
      generated/boilerplate (DTOs, `**/dto/**`, `**/*Exception.*`, `**/port/**`,
      `**/*MapperImpl.*`, config, `PaymentGatewayApplication`) — see spec Domain Rules.
- [ ] Confirm the rule **logs a warning** when below 70% and **does not fail**
      the build (verify by temporarily setting minimum high and checking the
      build still returns success with a warning, then restore to 0.70).

## Phase 2 — Raise coverage with new unit tests

- [ ] From the JaCoCo report, list in-scope classes below threshold, ranked by
      uncovered business logic (domain services, application services, rich
      model methods, `Money`).
- [ ] Add unit tests per existing conventions: Mockito BDD (`given`/`then`/
      `should`), AssertJ only, `@Nested` + `@DisplayName`, `shouldDoX...` names,
      `// Given / // When / // Then`, aggregates built via domain factory methods.
- [ ] Cover branch-heavy business rules (state transitions like
      `payment.authorize()`, refund/reconciliation logic, validation, exception
      paths) — not just happy paths.
- [ ] Re-run the coverage pass; iterate until the in-scope packages meet/exceed
      the threshold with the gate enabled.

## Phase 3 — Wire into CI

- [ ] Ensure the coverage gate runs in the unit/slice CI `test` job (not e2e).
- [ ] (Optional, non-blocking) surface coverage % in the GitHub Actions step
      summary.
- [ ] Update `Makefile`/README/CLAUDE.md if the developer workflow changes
      (e.g. a `make coverage-check` target or a note that `verify` now gates).

## Phase 4 — Verify & PR

- [ ] `./mvnw clean test -Dtest='!com.payment.gateway.e2e.**'` green with gate on.
- [ ] `./mvnw spotless:check` green (run `make format` first).
- [ ] Full suite sanity (`make test`) — no regressions.
- [ ] Run `/spring-boot-review`.
- [ ] Open a **draft** PR (Conventional Commit `test:` / `build:` scope); PR body
      notes risk + rollback (revert the `check` execution to disable the gate).

## Verification

- [ ] `./mvnw clean test -Dtest='!com.payment.gateway.e2e.**' jacoco:report`
- [ ] `./mvnw verify` (or `jacoco:check`) — gate enforced
- [ ] `./mvnw spotless:check`
