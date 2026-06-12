# Test Plan: Refactor README.md

This is a **documentation-only** change. There is no executable code to test;
"testing" here means content correctness, link integrity, and no source drift.

## Unit Tests

N/A — no code paths changed.

## Integration Tests

N/A — no code paths changed.

## API Tests

N/A — no endpoints changed. (Endpoint *descriptions* in the README are verified
against controllers during the correctness pass, not via automated API tests.)

## Regression Tests

- **No source/test/config drift:** `git diff --name-only` must list only `*.md`
  files plus the new `LICENSE` file. Any `.java`, `.yml`, `.xml` change is a
  regression for this task.
- **Build unaffected:** because only markdown changes, CI build/test/spotless
  gates are unaffected; optionally run `./mvnw spotless:check` to confirm nothing
  non-md was touched.

## Manual Verification

- [ ] **Link integrity:** every relative link in the new README resolves to an
      existing file (`docs/README.md`, `docs/API_REFERENCE.md`,
      `docs/TESTING_GUIDE.md`, `docs/SECURITY_GUIDE.md`, `CONTRIBUTING.md`, etc.).
      No links to `PAYMENT_GATEWAY_PROJECT_PLAN.md` or `CHECKPOINT.md` remain.
- [ ] **License present:** a `LICENSE` file exists and the README License stub
      links to it; GitHub recognizes the license in the repo sidebar.
- [ ] **Stubs kept:** Security Notice, Contributing (→ `CONTRIBUTING.md`), License
      (→ `LICENSE`) are present as short sections; Support is gone.
- [ ] **Fact accuracy:** tech-stack table matches `pom.xml` / `CLAUDE.md`
      (Spring Boot 4.1, Java 26, stripe-java, Testcontainers 2.x, etc.).
- [ ] **Structure:** section order is Overview → Features → Architecture →
      Quick Start (config inside) → Documentation.
- [ ] **Content preservation:** sections removed from README (Testing, Security
      Best Practices, Contributing, Project Structure) are present in their
      destination docs — open each destination and confirm.
- [ ] **First-time reader check:** a reader who has never seen the repo can tell,
      from the top of the README, what the service does and how to run it.
- [ ] **Rendering:** README renders correctly on GitHub (tables, code fences,
      diagram block, emoji headings as desired).
