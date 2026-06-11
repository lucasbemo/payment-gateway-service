# Implementation Tasks: Update the project to JDK 26

Status: draft

> Sequencing matters: do the framework upgrade and the JDK switch as coordinated steps,
> verifying after each. Resolve the blocking Open Questions (spec.md) before starting.

## 0. Pre-work
- [x] Blocking questions resolved (decisions.md): **JDK 26**, **Spring Boot 4 in scope**,
      **big-bang PR**, DoD includes perf/load + staging + real-request validation.
- [ ] Install **JDK 26** via sdkman; confirm `java -version`.
- [ ] Capture JDK 21 baselines (test counts, coverage, Postman run, perf numbers) for diffing.

## 1. Build & toolchain
- [ ] `pom.xml`: set `<java.version>` → 26 (prefer `<maven.compiler.release>26`).
- [ ] Bump `maven-compiler-plugin` to 3.15.0 (compile `--release 26`).
- [ ] Bump JaCoCo 0.8.11 → 0.8.15.
- [ ] Verify Spotless 3.6.0 + palantir-java-format on JDK 26; if it fails on internal
      javac access, add the required `--add-exports jdk.compiler/...=ALL-UNNAMED` (or bump
      palantir-java-format) and document it.

## 2. Framework upgrade (the blast radius)
- [ ] Parent: Spring Boot 3.2.0 → **4.1.x** (Spring Framework 7).
- [ ] Migrate Spring Framework 6→7 breaking changes (deprecated/removed APIs, Jakarta
      baseline). Run Spring Boot's migration tooling/openrewrite if available.
- [ ] Spring Security major upgrade — update the `SecurityFilterChain` DSL.
- [ ] Hibernate/Spring Data major — verify JPA mappings & repositories compile and run.
- [ ] springdoc-openapi 2.3.0 → 3.0.3.
- [ ] Micrometer / tracing bumps — keep Prometheus + Brave/Zipkin wiring working.

## 3. Dependency bumps (JDK 26 compatibility)
- [ ] Lombok 1.18.30 → 1.18.46+ (update both the dependency and the
      `annotationProcessorPaths` version).
- [ ] Override transitive **byte-buddy** to ≥1.17.8 (for Mockito on JDK 26).
- [ ] MapStruct 1.5.5 → 1.6.3 (dependency + processor path).
- [ ] Testcontainers 1.19.3 → 2.0.2+; re-check the `api.version` workaround in
      `ContainerConfig`/docs — ideally removable.
- [ ] Flyway 9.22.3 → 11.x/12.x; confirm V1–V19 still validate.
- [ ] Redisson 3.24.3 → 4.x.
- [ ] Resilience4j 2.2.0 → 2.4.0.
- [ ] Review remaining libs (stripe-java, itext7, bucket4j, spring-cloud-aws) for Boot 4
      compatibility; bump where required.

## 4. Runtime & CI
- [ ] Dockerfile: builder `maven:...-eclipse-temurin-21` → `-26`; runtime
      `eclipse-temurin:21-jre-alpine` → `26-jre-alpine` (confirm the tag exists; fall back
      to a non-alpine 26 JRE if not).
- [ ] CI `.github/workflows/ci.yml`: `JAVA_VERSION: '21'` → `'26'` (all jobs).
- [ ] Confirm `actions/setup-java` resolves Temurin 26.

## 5. Documentation
- [ ] `CLAUDE.md`: update Tech Stack (Java 26, Spring Boot 4.x) and **rewrite the Gotchas**
      — the "Temurin 26 breaks the build / use JDK 21" and `-Dapi.version=1.44` notes are
      now obsolete or changed.
- [ ] `GUIDE_WORKFLOW.md`, `CONTRIBUTING.md`, `README.md`, `docs/*`: bump all "Java 21"
      prerequisites and the JDK warning callout.

## 6. Verification (see test-plan.md)
- [ ] Unit/slice tests, e2e tests, Spotless, JaCoCo, ArchUnit all green on JDK 26.
- [ ] Postman/newman collection green.
- [ ] Docker image builds + container healthcheck passes.
- [ ] OWASP dependency-check re-run.
- [ ] **Real-request validation:** run the app and exercise every feature, validating
      responses vs the JDK 21 baseline.
- [ ] **Performance/load** run — no regression vs baseline.
- [ ] **Staging soak** — runs clean under representative traffic.
- [ ] `/spring-boot-review` then `/create-pr`.

## Verification commands
- [ ] `./mvnw test -Dtest='!com.payment.gateway.e2e.**'`
- [ ] `./mvnw test -Dtest='com.payment.gateway.e2e.**'`
- [ ] `./mvnw spotless:check`
- [ ] `./mvnw clean verify`
- [ ] `docker build -t payment-gateway:jdk26 .`
