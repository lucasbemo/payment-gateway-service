# Implementation Tasks: Refactor README.md

Status: draft
Type: Documentation only — no Java/source/test changes.

## Tasks

### 1. Inventory & destination mapping
- [ ] List every current README section and decide: keep-stub / rewrite / move-out / drop.
- [ ] Map each moved-out section to a destination doc (reuse existing `docs/*` only):
      - Testing matrix → `docs/TESTING_GUIDE.md` (merge, dedupe stale counts)
      - Security Best Practices → `docs/SECURITY_GUIDE.md`
      - Project Structure tree → removed from README (dev docs if anywhere)
- [ ] Keep as short README stubs: **Security Notice**, **Contributing** (→
      `CONTRIBUTING.md`), **License** (→ `LICENSE`).
- [ ] Drop the **Support** section.
- [ ] Confirm no content is lost in the move (each destination already covers it
      or receives the moved text).

### 2. Author the new README (follow the canonical structure in spec.md)
- [ ] **Title + one-line description** and **badges** row: CI build (GitHub
      Actions `ci.yml`), License (MIT), Java 26, Spring Boot 4.1. No coverage badge.
- [ ] **Overview** paragraph: what the payment gateway does.
- [ ] **Features / Functionalities** section grouped by capability:
      payments, refunds, transactions, merchants, customers, reconciliation,
      Stripe webhooks, idempotency (Redis), transactional outbox → Kafka,
      resilience (Resilience4j/Bucket4j), observability (Prometheus/Zipkin).
- [ ] **Architecture** section: concise DDD + Hexagonal summary, corrected
      tech-stack table, link to architecture docs / ADRs.
- [ ] **Quick Start** section with **configuration nested inside**:
      prerequisites → clone → `docker-compose up -d` → run app → access URLs →
      env vars (`.env.example`) → profiles (`local`/`dev`/`prod`).
- [ ] **Documentation** section: curated index linking `docs/README.md` and key
      guides. Retain the **Security Notice** callout near the top.
- [ ] **Contributing** stub: 2–3 lines → `CONTRIBUTING.md`.
- [ ] **License** stub: 1–2 lines → `LICENSE`.

### 3. Add LICENSE file
- [ ] Create an **MIT `LICENSE`** file (confirm copyright holder name/year).
- [ ] Point the README License stub at it; remove the old broken license text.

### 4. Correctness pass
- [ ] Fix tech-stack drift: **Spring Boot 3.2 → 4.1**; verify each row against
      `pom.xml` / `CLAUDE.md`.
- [ ] Remove/replace dead links: `PAYMENT_GATEWAY_PROJECT_PLAN.md`, `CHECKPOINT.md`.
- [ ] Verify the jar/run commands and access-table ports against
      `docker-compose.yml` / `CLAUDE.md` (Postgres 5433, Kafka 19092, Redis 6380,
      app 8080, etc.).
- [ ] Verify feature/endpoint list against actual controllers.

### 5. Verification
- [ ] Markdown link check across README + any edited docs (no broken relative links).
- [ ] Re-read README as a first-time visitor: capabilities are clear before setup.
- [ ] `git diff --stat` shows only `.md` files + the new `LICENSE` (no source/test/config).

### 6. Wrap-up
- [ ] Update `specs/INDEX.md` status when implementation starts/completes.
- [ ] Open a **draft** PR with risk + rollback notes (docs-only; rollback = revert).

## Verification

- [ ] Markdown link validation (manual or tool) — zero broken links.
- [ ] `git diff --name-only` contains only markdown files.
- [ ] `./mvnw spotless:check` not required for docs, but run if any non-md file
      is unexpectedly touched (should be none).
- [ ] No build/test run needed — confirm no `.java` / `.yml` / `pom.xml` changes.
