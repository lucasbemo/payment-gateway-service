# Specs Index

This folder contains implementation-ready specs derived from GitHub Project backlog items.

## Context Policy

- GitHub Project is the product backlog source of truth.
- `specs/active/*` contains work currently being prepared or implemented.
- `specs/completed/*` contains historical implementation summaries.
- `specs/archived/*` contains obsolete or superseded specs.
- Claude should read this index first and open only the relevant active spec.
- Current source code and tests override old specs.

## Active

| Issue | Title | Status | Spec Path | GitHub URL | Summary |
|---|---|---|---|---|---|

## Completed

| Issue | Title | Merged PR | Date | Spec Path | Summary |
|---|---|---|---|---|---|
| #12 | Update the project to JDK 26 | [#13](https://github.com/lucasbemo/payment-gateway-service/pull/13) | 2026-06-12 | `specs/completed/2026/06/12-update-the-project-to-use-jdk-26/` | Spring Boot 3.2→4.1 / Framework 6→7 major migration for JDK 26; all tests/CI/Postman green, observability restored, local soak PASS. |
| #16 | Refactor README.md | [#17](https://github.com/lucasbemo/payment-gateway-service/pull/17) | 2026-06-12 | `specs/completed/2026/06/16-refactory-readme-md/` | Docs-only: feature-oriented README (Overview → Features → Architecture → Quick Start w/ config → Documentation), added MIT LICENSE + badges, moved GUIDE_WORKFLOW/PROMPTS into `docs/`, fixed Spring Boot 3.2→4.1, Kafka 9093→19092, and dead links. |
| #21 | Increase unit test coverage + domain coverage gate | [#23](https://github.com/lucasbemo/payment-gateway-service/pull/23) | 2026-07-13 | `specs/completed/2026/07/21-increse-unit-test-coverve/` | Advisory JaCoCo `check` (70% LINE, aggregate BUNDLE over `domain.**`+`application.**`, boilerplate excluded, `haltOnFailure=false`) + targeted unit tests. Business-layer LINE coverage 86.7%→88.0% (~93.5% excl. boilerplate); 22 new tests; no production source changes. |

## Archived

| Issue | Title | Reason | Date | Spec Path |
|---|---|---|---|---|