---
name: spring-boot-review
description: Review Spring Boot changes before opening a PR. Use for adversarial review of Java/Spring Boot diffs, tests, API behavior, security, observability, configuration, and production readiness.
disable-model-invocation: true
effort: high
allowed-tools:
  - Read
  - Grep
  - Glob
  - Bash(git status*)
  - Bash(git diff*)
  - Bash(git log*)
  - Bash(./mvnw*)
  - Bash(mvn*)
  - Bash(./gradlew*)
  - Bash(gradle*)
---

# Spring Boot Review

Review the current Spring Boot changes.

## Live diff context

* Branch: !`git branch --show-current 2>/dev/null || true`
* Status: !`git status --short 2>/dev/null || true`
* Diff stat: !`git diff --stat HEAD 2>/dev/null || true`
* Changed files: !`git diff --name-only HEAD 2>/dev/null || true`

## Review mode

Act as a strict but practical Staff-level Spring Boot reviewer.

Your job is not to praise the code. Your job is to find defects before the PR reaches humans.

Do not make code changes unless the user explicitly asks you to fix the findings. Prefer review comments with concrete fixes.

## Review priorities

Review in this order:

1. Correctness and acceptance criteria.
2. Regressions and backward compatibility.
3. Security and authorization.
4. Transactionality, idempotency, and concurrency.
5. API contract and error handling.
6. Persistence and database migration safety.
7. Test coverage and test quality.
8. Observability and operability.
9. Configuration and environment safety.
10. Maintainability and project conventions.

## Spring Boot checklist

Check for:

* Controllers: correct status codes, request validation, response shape, error mapping, authentication/authorization, no business logic leakage.
* Services/use cases: clear orchestration, transaction boundaries, idempotency, retry safety, no hidden side effects.
* Repositories/data access: correct queries, indexes/migrations considered, no N+1 surprises, pagination where needed.
* DTOs/mappers: no leaking entities through public APIs unless project convention allows it.
* Configuration: no hardcoded environment values, safe defaults, typed config if project uses it.
* Security: no missing authorization on new endpoints, no token/secret exposure, no unsafe logging of PII or credentials.
* Observability: important flows have useful logs/metrics/traces without noisy logs.
* Tests: prove behavior, include negative/edge cases, avoid brittle implementation-only assertions.
* Build quality: formatting, static analysis, dependency changes, unused code/imports.

## Severity labels

Use these labels:

* BLOCKER: likely production bug, security issue, data corruption, broken build, or unacceptable regression.
* IMPORTANT: should be fixed before merge but not immediately catastrophic.
* NIT: readability, naming, small maintainability issue.
* QUESTION: unclear intent or possible missing requirement.
* PRAISE: only for unusually good decisions worth preserving.

## Verification

If a relevant check is cheap and available, run it.

Prefer:

* `./mvnw test` or `mvn test`
* `./mvnw verify` or `mvn verify`
* `./gradlew test` or `./gradlew check`
* configured formatting/static-analysis checks

If checks fail, include the failing command and the important error summary.

## Output format

Return:

1. Overall verdict: READY, READY WITH NITS, NEEDS CHANGES, or BLOCKED.
2. Findings grouped by severity.
3. Missing tests or weak tests.
4. Suggested concrete fixes.
5. Verification commands run and results.
6. Final merge recommendation.

Do not invent issues. Tie every finding to a file, symbol, behavior, or diff evidence.
