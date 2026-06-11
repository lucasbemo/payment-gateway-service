# Feature Spec: Update the project to JDK 26

Status: draft
Source: https://github.com/lucasbemo/payment-gateway-service/issues/12

## Problem

The service currently targets **Java 21** (`pom.xml` `<java.version>21</java.version>`,
Spring Boot 3.2.0, Dockerfile on `eclipse-temurin:21`, CI `JAVA_VERSION: '21'`). Issue
#12 asks to move the whole project to **JDK 26** and remain "production ready" — all
features, all tests, and the Postman collection must pass on the new toolchain.

The key reality: **JDK 26 is not reachable by a property edit.** No Spring Boot 3.x line
supports Java 26 (the 3.x family caps at Java 25); only **Spring Boot 4.x** declares Java
26 support. Spring Boot 4 runs on **Spring Framework 7** (a 6→7 major jump), which forces
coordinated upgrades of Lombok, Byte Buddy/Mockito, MapStruct, Testcontainers, JaCoCo,
Flyway, Redisson, Resilience4j, springdoc, and the Maven toolchain. This is a **framework
migration**, not a JDK bump.

> ⚠️ **Strategic caveat (must resolve before implementing).** JDK 26 is a **non-LTS**
> feature release (GA 2026-03-17) that loses free public updates when JDK 27 ships
> (~Sept 2026 — ~3 months from now). The current **LTS is JDK 25** (GA 2025-09-16).
> For a payment service whose own issue stresses "production ready," shipping onto a JDK
> months from end-of-updates is a material risk. See Open Questions Q1.

## Desired Behavior

- The project builds, tests, and runs on **JDK 26** (class file major version 70).
- `./mvnw clean verify` (unit + e2e + Spotless + JaCoCo) passes on JDK 26 in CI.
- The Postman/newman collection passes against the app running on JDK 26.
- The Docker image builds on, and runs on, a JDK 26 base.
- Documentation (`CLAUDE.md`, `GUIDE_WORKFLOW.md`, `CONTRIBUTING.md`, `README.md`) is
  updated — the current JDK-21 pin and the `-Dapi.version=1.44` Testcontainers workaround
  become obsolete/changed.

> ✅ **Explicit owner expectation — not optional.** Passing the test suite is **not
> sufficient** on its own. The implementation **must actually start the application on
> JDK 26 and exercise every feature and end-to-end flow** (payments authorize/capture/
> cancel, refunds, transactions, merchants, customers, reconciliation + settlement, the
> outbox→Kafka→webhook pipeline) with real requests, confirming the application is still
> **fully functional** in practice — not just that tests compile and pass. This is a
> hard part of the definition of done (see Acceptance Criteria 12 and the
> *Manual Verification* section of `test-plan.md`).

## Current Behavior

- Java 21, Spring Boot 3.2.0 (Spring Framework 6.1), build pinned to JDK 21.
- `CLAUDE.md` explicitly documents that **Temurin 26 breaks Lombok/Mockito** and that the
  build must use JDK 21 + `-Dapi.version=1.44` for Testcontainers. Removing that
  constraint by upgrading the toolchain is the point of this issue.

## Acceptance Criteria

1. `<java.version>` (or `<maven.compiler.release>`) = 26; build runs on a JDK 26 runtime.
2. `./mvnw test -Dtest='!com.payment.gateway.e2e.**'` (unit/slice) — all green on JDK 26.
3. `./mvnw test -Dtest='com.payment.gateway.e2e.**'` (Testcontainers e2e) — all green on
   JDK 26, with no manual `-Dapi.version` hack required (or the hack re-documented if
   still needed).
4. `./mvnw spotless:check` passes on JDK 26 (palantir-java-format runs cleanly).
5. JaCoCo coverage report generates (no "unsupported class file major version 70").
6. ArchUnit `HexagonalArchitectureTest` still passes (no architectural drift).
7. The Postman collection (newman) passes against the app on JDK 26.
8. Docker image builds (`docker build`) and the container starts & passes its healthcheck.
9. CI pipeline (build, unit, e2e, spotless, docker-build) is green on JDK 26.
10. Prod-profile auth smoke test (`ProdProfileAuthSmokeE2ETest`) passes.
11. No behavior change in any existing feature (payments, refunds, reconciliation,
    outbox/Kafka, webhooks, settlement) — this is a platform upgrade, not a feature change.
12. **Run the app and execute all features/flows:** the application is **started on JDK 26
    and every feature and end-to-end flow is exercised with real requests** (payments
    authorize/capture/cancel, refunds, transactions, merchants, customers, reconciliation
    + settlement, outbox→Kafka→webhook), with each response validated against the JDK 21
    baseline — proving the app is still **fully functional**, not just that tests pass.
13. **Performance/load** testing shows no regression vs the JDK 21 baseline.
14. **Staging soak:** the upgraded build runs in staging without errors before it is
    considered production ready.

## API Contract

No intended API changes. **Risk:** the Spring Boot 4 / Framework 7 and springdoc 3.x
upgrades can change error-response serialization, validation messages, actuator endpoint
shapes, and OpenAPI generation. Acceptance requires the existing `ApiResponse<T>`
envelope, `GlobalExceptionHandler` status mappings, and `/api/v1/**` contracts to remain
compatible (verified by the controller slice tests + Postman collection).

## Domain Rules

No domain-logic changes. Rich domain models, the transactional outbox, idempotency, and
reconciliation behavior must be preserved exactly. Spring Framework 7 transaction/AOP
changes must not alter `@Transactional` boundaries or Resilience4j aspects.

## Data Changes

No schema changes intended. **Risk:** Hibernate ships a new major version with Spring Boot
4 — verify Flyway migrations V1–V19 still validate and that JPA mapping/Postgres dialect
behavior is unchanged. Flyway itself must be upgraded to a JDK-26-capable line.

## Observability

Preserve Micrometer + Prometheus metrics, Brave/Zipkin tracing, and structured logging.
**Risk:** Micrometer / Micrometer-Tracing major bumps come with Spring Boot 4; confirm
`/actuator/prometheus` output and trace propagation still work.

## Security

Preserve the production `SecurityFilterChain` (API-key + JWT) and the prod-profile auth
smoke test. **Risk:** Spring Security ships a new major with Spring Boot 4 — config-DSL
changes are likely; the auth filters and `@WithMockUser` slice tests must still pass.
Re-run OWASP dependency-check; the wholesale bumps change the vulnerability surface.

## Target version matrix (research-backed — verify at implementation time)

| Component | Current | Target for JDK 26 | Notes |
|---|---|---|---|
| JDK | 21 | **26** | class file v70; non-LTS |
| Spring Boot | 3.2.0 | **4.1.x** (≥4.0.4 lists Java 26) | forces Spring Framework 7 |
| Spring Framework | 6.1 | **7.0.8+** | 6→7 major; Jakarta baseline + API removals |
| Lombok | 1.18.30 | **1.18.46+** | JDK 26 support added 2026-04 |
| Byte Buddy (via Mockito) | ~1.17.7 | **≥1.17.8** | override `byte-buddy.version`; Mockito doesn't bundle it yet |
| MapStruct | 1.5.5 | **1.6.3** | no explicit JDK 26 note; low risk (JSR-269) |
| Testcontainers | 1.19.3 | **2.0.2+** | docker-java 3.7.0 defaults API 1.44 |
| JaCoCo | 0.8.11 | **0.8.15** | hard-fails on v70 below this |
| Spotless plugin | 3.6.0 | 3.6.0 | likely fine (orchestrator) |
| palantir-java-format | 2.92.0 | **unverified** | uses `com.sun.tools.javac` internals; may need `--add-exports`; must test |
| Flyway | 9.22.3 | **11.x/12.x** | JDK-26-capable line |
| Redisson | 3.24.3 | **4.x** | old bundled Netty |
| Resilience4j | 2.2.0 | **2.4.0** + byte-buddy pin | AOP/bytecode paths |
| springdoc-openapi | 2.3.0 | **3.0.3** | required for Boot 4 |
| maven-compiler-plugin | Boot-managed | **3.15.0**, `--release 26` | run Maven on JDK 26 |
| Dockerfile base | temurin-21 / 21-jre-alpine | **temurin-26 / 26-jre** | confirm alpine tag exists |
| CI `JAVA_VERSION` | 21 | **26** | `actions/setup-java` supports 26 |

## Decisions (resolved 2026-06-11 — see `decisions.md`)

- **Target = JDK 26** (non-LTS; ~6-month update window accepted by the owner).
- **Spring Boot 4 / Spring Framework 7 migration is in scope** (mandatory for JDK 26).
- **"Production ready"** = green CI + Postman **plus** perf/load testing, a staging soak,
  and manual real-request validation against the running app (criteria 12–14).
- **Delivery = a single big-bang PR.**

Remaining non-blocking confirmations (at implementation time): exact patch versions per
the matrix; whether `-Dapi.version=1.44` is still needed on Testcontainers 2.x; whether
palantir-java-format needs `--add-exports` on JDK 26; availability of the
`eclipse-temurin:26-jre*` base image.
