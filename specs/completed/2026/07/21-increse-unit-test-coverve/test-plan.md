# Test Plan: Increase unit test coverage + domain coverage gate

This feature *is* tests + a build gate, so the "test plan" is primarily the set
of new unit tests to write and how the gate is verified.

## Unit Tests (the bulk of the work)

Target the in-scope business packages (`domain.*`, `application.*`, and
`commons.model`). Follow existing conventions: Mockito BDD (`given`/`then`/
`should`), AssertJ only (`assertThat`, `assertThatThrownBy`), `@Nested` +
`@DisplayName`, method names `shouldDoX...`, explicit `// Given / // When /
// Then`, aggregates via domain factory methods.

Priority areas (branch-heavy, business-critical, likely under-covered):

* **Domain models with behavior** — state machines and invariants, e.g.
  `Payment` (`create`, `authorize`, capture/cancel transitions and illegal-state
  guards), refund models, reconciliation models, `PaymentStatus`/enum transitions.
* **Domain services** (`*DomainService`) across payment, refund, transaction,
  reconciliation, outbox, idempotency, merchant, customer.
* **Application services** (`*Service`, `@Transactional`) — orchestration,
  including outbox publish-in-transaction, error/exception mapping, idempotency
  handling. Mock the outbound `*Port`s.
* **`commons.model.Money`** — cents arithmetic, rounding, currency guards,
  equality/edge cases (zero, negative, overflow if applicable).
* **Exception/validation paths** — assert typed domain exceptions are thrown
  (`PaymentDeclinedException`, `ValidationException`, `NotFoundException`, …).

Assertions should verify **behavior and branches**, not just that a getter
returns a value — the goal is meaningful coverage that satisfies the counter
chosen for the gate.

## Slice Tests

Existing `@WebMvcTest` controller tests remain. Only add/adjust if a controller
falls in an in-scope package for the gate (default scope excludes infrastructure,
so likely no new slice tests are required — confirm against the final gate scope).

## Integration / E2E Tests

No new e2e tests required for this issue. The coverage gate deliberately measures
the **unit/slice** run (e2e coverage is not counted toward the domain gate).
Keep the existing e2e suite green; do not regress `ProdProfileAuthSmokeE2ETest`.

## Gate Verification (build behavior)

* **Negative test:** temporarily raise the threshold above baseline → confirm
  `./mvnw jacoco:check` / `verify` **fails** with a clear "coverage below X"
  message scoped to the in-scope packages. Restore afterward.
* **Positive test:** with tests added and threshold at the agreed value →
  `./mvnw clean test -Dtest='!com.payment.gateway.e2e.**'` + gate is **green**.
* **Excludes test:** confirm DTOs/records/enums/mappers/config are absent from
  the coverage denominator (spot-check the JaCoCo HTML report).

## Regression

* `make test` (full suite) passes — no behavior changed in production code.
* `./mvnw spotless:check` passes.
* CI: unit job (with gate), e2e job, spotless gate all green on the PR.

## Manual Verification

* Open `target/site/jacoco/index.html`, confirm in-scope package %s meet the
  threshold and excluded packages are not counted.
* Confirm a deliberately-untested new business method drops the % and trips the
  gate (sanity check the gate actually protects new code).
