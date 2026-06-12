# Decisions: Update the project to JDK 26

## Outcome (2026-06-12)

**Completed and merged** via PR #13 (squash `99e5b97`). It was a genuine Spring Boot
3.2 → 4.1 / Framework 6 → 7 major migration (Jackson 2→3 bridge, Boot 4 package/starter
relocations, ~12 dependency bumps, test-API migration, Dockerfile + CI + docs). Verified:
1081 unit + 128 e2e green, CI green, Postman 81/81 (concurrency-safe), Docker image,
real-request validation, observability (metrics→Prometheus→Grafana, traces→Zipkin), and a
local ~1h staging soak PASS. Four pre-existing observability bugs were surfaced and fixed
(Zipkin endpoint key, dead processing-duration timer, Grafana datasource uid, avg-panel
NaN). **Remaining:** a production-sized staging soak (`DURATION=8h make soak`) on real
staging infra — harness committed; not doable locally.

## Decision Log

| Date | Decision | Reason | Alternatives |
|---|---|---|---|
| 2026-06-11 | Treat this as a Spring Boot 4 / Spring Framework 7 migration, not a JDK bump | No Spring Boot 3.x line supports Java 26; 3.x caps at Java 25 | Stay on 3.5 + JDK 25 (LTS) |
| 2026-06-11 | (proposed) Override transitive `byte-buddy` to ≥1.17.8 rather than wait for Mockito | No Mockito release bundles a JDK-26-capable Byte Buddy yet | Wait for a Mockito release (blocks the upgrade) |
| 2026-06-11 | **Target JDK 26 (non-LTS)** — accept the ~6-month update window | Owner decision (issue #12) | JDK 25 LTS (considered, not chosen) |
| 2026-06-11 | **Single big-bang PR** — framework + JDK + all deps + docs together | Owner decision | Phased (JDK 25 LTS first) |
| 2026-06-11 | **"Production ready" = green CI + Postman + perf/load + staging soak + real-request validation against a running app** | Owner decision | CI + Postman only |

## Assumptions (used unless corrected)

1. **Scope = platform upgrade only.** No feature/API/schema changes; success = the
   existing suite + Postman pass unchanged on the new JDK.
2. The full framework upgrade (Spring Boot 4.1.x / Spring Framework 7, Spring Security,
   Hibernate, springdoc 3.x, Micrometer) is **in scope** because JDK 26 requires it.
3. **"Production ready" (confirmed) = green CI (unit + e2e + Spotless + JaCoCo) + green
   Postman run + building/running Docker image + perf/load testing + a staging soak +
   manual real-request validation** (run the app and validate real responses across all
   features).
4. Target versions follow the matrix in `spec.md`; exact patch versions are confirmed at
   implementation time against current release notes.
5. The deployment/runtime environment can run JDK 26 (base image + hosting).
6. Docs (`CLAUDE.md` gotchas, all "Java 21" prereqs) will be updated as part of this work.
7. **Delivery = a single big-bang PR** (no intermediate JDK 25 step).

## Key risks

- **Non-LTS target.** JDK 26 loses free updates ~Sept 2026. For a payment service this is
  a real operational risk — see Q1. Strong recommendation: target **JDK 25 LTS** unless a
  specific JDK 26 feature is required.
- **Spring Framework 6→7 is the real blast radius** — removed/deprecated APIs, Spring
  Security & Hibernate majors. Highest chance of breakage.
- **palantir-java-format on JDK 26** is unverified (relies on internal javac APIs); may
  need `--add-exports`. Could break the CI Spotless gate.
- **Many libs lack an explicit "JDK 26 supported" statement** (MapStruct, Flyway,
  Redisson, Resilience4j) — versions inferred; must be validated by the test suite.

## Resolved questions (2026-06-11)

- **Q1 — RESOLVED:** Target **JDK 26 (non-LTS)** as the issue requests. The ~6-month
  update window and operational risk are acknowledged and accepted by the owner.
- **Q2 — RESOLVED:** The **Spring Boot 4 / Framework 7** migration is in scope (mandatory
  for JDK 26); breaking-change blast radius accepted.
- **Q3 — RESOLVED:** "Production ready" = green CI + Postman **plus** perf/load testing,
  a staging soak, and manual real-request validation against a running app.
- **Q4 — RESOLVED:** **Single big-bang PR** (framework + JDK + deps + docs together).

## Remaining (non-blocking) to confirm at implementation time

- Exact patch versions per the `spec.md` matrix (validate against current release notes).
- Whether the `-Dapi.version=1.44` Testcontainers workaround is still needed on 2.x.
- Whether palantir-java-format needs `--add-exports` on JDK 26, or a newer version.
- Confirm the deployment target/base image (`eclipse-temurin:26-jre*`) is available.
