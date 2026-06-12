# Feature Spec: Refactor README.md

Status: completed
Merged PR: [#17](https://github.com/lucasbemo/payment-gateway-service/pull/17) (merged 2026-06-12)
Source: https://github.com/lucasbemo/payment-gateway-service/issues/16
Type: Documentation (no production code changes)

## Problem

The current `README.md` (337 lines) is not useful for a first-time visitor:

- It opens with a Security Notice and a long Quick Start, but **never explains
  what the service actually does** — there is no feature/functionality overview.
- It mixes top-level concerns (quick start, full testing matrix, security best
  practices, contributing, license, support) into one long file, burying the
  signal a newcomer needs.
- It contains **stale/incorrect facts**: "Spring Boot 3.2" (the project is on
  **4.1**), and it links to **non-existent files** (`PAYMENT_GATEWAY_PROJECT_PLAN.md`,
  `CHECKPOINT.md`) in the Documentation, License, and Support sections.
- A complete `docs/` tree already exists (Getting Started, API Reference, Testing
  Guide, Security Guide, Webhooks, etc.) but the README does not point to it.

## Desired Behavior

A README oriented to **what the service does first**, then how it's built, then
how to run it, then where to go next. Target reading order for a newcomer:

1. **Overview** — one-paragraph description of the payment gateway.
2. **Features / Functionalities** — what the service can do, grouped by domain
   (payments, refunds, transactions, merchants, customers, reconciliation,
   webhooks, idempotency, outbox/events, observability, resilience).
3. **Architecture** — concise DDD + Hexagonal summary + corrected tech stack,
   linking to the deeper architecture docs.
4. **Quick Start** — prerequisites, clone, start infra, run app, access URLs,
   **with configuration covered inside this section** (env vars, profiles).
5. **Documentation** — a curated index linking into the existing `docs/` tree
   (and `docs/README.md`).
6. **Contributing** — a short 2–3 line stub pointing to `CONTRIBUTING.md`.
7. **License** — a short stub pointing to a real `LICENSE` file.

**Move depth, keep signposts.** Reference-heavy sections move to `docs/`; trust/
orientation signals stay as short stubs:

- **Moved out** (detail → existing `docs/*`): full **Testing** matrix →
  `docs/TESTING_GUIDE.md`; **Security Best Practices** → `docs/SECURITY_GUIDE.md`;
  **Project Structure** tree → removed from README (lives in dev docs if anywhere).
- **Kept as short stubs in README** (trust/orientation signals): **Security
  Notice** (safety callout), **Contributing** (pointer → `CONTRIBUTING.md`),
  **License** (pointer → `LICENSE`).
- **Dropped:** **Support** section (its content was broken links).

## Target README Structure (canonical)

This is the authoritative section list and **order** to implement. The *order* is
binding (a README is read top-to-bottom; the flow is the UX). Individual sections
are tiered by necessity.

```
# Payment Gateway Service
> One-line description: payments, refunds, settlement & webhooks —
  Java 26 / Spring Boot 4.1, DDD + Hexagonal.

[badges: CI build · license: MIT · Java 26 · Spring Boot 4.1]   (Tier 2)

⚠️ Security Notice  — short callout: demo/default credentials, don't deploy as-is.

## Overview        — one paragraph: what & why it exists.            (Tier 1, must)
## Features        — grouped by capability: payments · refunds ·     (Tier 1, must)
                     transactions · merchants · customers ·
                     reconciliation · Stripe webhooks · idempotency ·
                     transactional outbox→Kafka · resilience ·
                     observability.
## Architecture    — concise DDD + Hexagonal summary + corrected     (Tier 2)
                     tech-stack table → links to docs/ADRs.
## Quick Start     — prerequisites → docker-compose up → run →       (Tier 1, must)
                     access URLs.
   └─ Configuration — env vars (.env.example) + profiles, nested here.
## Documentation   — curated index → docs/README.md + key guides.    (Tier 2)
## Contributing    — 2–3 line stub → CONTRIBUTING.md.                 (Tier 2)
## License         — 1–2 line stub → LICENSE (MIT).                   (Tier 1, must)
```

**Tiers** — Tier 1 = mandatory content (the agreed must-haves); Tier 2 = strongly
recommended for a service this size (cheap, high trust signal). Target length
~150–200 lines; all deeper detail lives in `docs/`.

**Badges decision (resolved):** include only badges backed by something real —
- ✅ **CI build status** — GitHub Actions `ci.yml` exists; badge is live.
- ✅ **License (MIT)**, **Java 26**, **Spring Boot 4.1** — static shields.io badges.
- ❌ **Coverage** — **skipped**: no Codecov/Coveralls configured, so a coverage
  badge would be fake/broken. Revisit if a coverage service is wired up later.

Rationale captured in `decisions.md` ("move depth, keep signposts"; order-as-UX).

## Current Behavior

`README.md` section order today:

1. Title + tagline
2. ⚠️ Security Notice
3. 🚀 Quick Start (prerequisites, clone, infra, run, access table)
4. 🏗️ Architecture (diagram + tech stack — **stack says Spring Boot 3.2**)
5. 📦 Project Structure
6. 🔧 Configuration (env vars + profiles)
7. 🧪 Testing (large; counts, E2E status, skipped-test breakdown, CI advice)
8. 📚 Documentation (**links to 2 missing files**)
9. 🛡️ Security Best Practices
10. 🤝 Contributing (+ Spotless code style)
11. 📄 License (**links to missing file**)
12. 📞 Support (**links to missing files**)

## Acceptance Criteria

- [ ] README leads with an **Overview** + **Features/Functionalities** section
      describing capabilities before anything else.
- [ ] README contains, in order: Overview → Features → Architecture → Quick Start
      (configuration nested inside) → Documentation → Contributing → License.
- [ ] **Configuration lives inside the Quick Start section** (env vars + profiles),
      not as a separate top-level section.
- [ ] The **Documentation** section links to the existing `docs/` index and the
      relevant guides (Getting Started, API Reference, Testing Guide, Security
      Guide, Deployment Guide, Webhooks, Troubleshooting, ADRs).
- [ ] **Moved out:** full **Testing** matrix → `docs/TESTING_GUIDE.md`; **Security
      Best Practices** → `docs/SECURITY_GUIDE.md`; **Project Structure** tree →
      removed from README. Content preserved in the existing `docs/*` destinations
      (reuse only — no new doc files).
- [ ] **Kept as short stubs:** Security Notice, Contributing (→ `CONTRIBUTING.md`),
      License (→ `LICENSE`).
- [ ] **Support** section removed.
- [ ] A real **`LICENSE` file is added** (MIT) and the README License stub links
      to it; the broken "for educational purposes" link is gone.
- [ ] **Badges** present under the title: CI build status (GitHub Actions),
      License (MIT), Java 26, Spring Boot 4.1. **No coverage badge** (no coverage
      service configured). All badge links resolve / render.
- [ ] README follows the **canonical structure** above and lands ~150–200 lines.
- [ ] **No broken links** remain. Dead links to `PAYMENT_GATEWAY_PROJECT_PLAN.md`
      and `CHECKPOINT.md` are replaced with valid targets or removed.
- [ ] **Stale facts corrected**: Spring Boot version (3.2 → 4.1) and any other
      tech-stack drift relative to `CLAUDE.md` / `pom.xml`.
- [ ] Feature list matches the real surface: REST roots `/api/v1/{payments,
      refunds,transactions,merchants,customers,reconciliation}` and Stripe
      `/api/v1/webhooks`.
- [ ] All markdown links resolve (verified by a link check / manual review).
- [ ] No production source, config, or test files are modified (diff is markdown
      files plus the new `LICENSE` file only).

## API Contract

N/A — documentation-only change. No endpoints added or modified. The README will
*describe* the existing public API surface and link to `docs/API_REFERENCE.md`.

## Domain Rules

N/A — no business logic. Feature descriptions must stay accurate to current
behavior; current source/tests override any older doc text when describing
capabilities.

## Data Changes

None. No Flyway migrations, no schema changes.

## Observability

N/A for runtime. The README's Features section should mention the existing
observability stack (Micrometer + Prometheus + Zipkin/Brave) and link to
`docs/OBSERVABILITY_TEST_REPORT.md`.

## Security

No security-relevant code change. The existing **Security Notice** (demo/default
credentials warning) must be **retained** in the README — it is safety-relevant
for first-time users. Detailed **Security Best Practices** content moves to
`docs/SECURITY_GUIDE.md` and is linked from the README.

**Licensing:** the repo currently has **no `LICENSE` file** and the README's
License section links to a missing file. This change adds an **MIT `LICENSE`**
file (copyright holder: repo owner — confirm name) and points the README License
stub at it. Adding a license is a legal/trust signal expected of a public repo.

## Open Questions

All blocking questions are **resolved** (Lucas, 2026-06-12) — see `decisions.md`:

1. Testing section → **merged into `docs/TESTING_GUIDE.md`**; no Testing section
   kept in README.
2. Project Structure tree → **removed from README entirely**.
3. **Reuse existing `docs/*` only** — no new doc files.
4. **Fix stale facts + dead links** (Spring Boot 3.2→4.1, broken doc links) as
   part of this change.

No remaining blockers.
