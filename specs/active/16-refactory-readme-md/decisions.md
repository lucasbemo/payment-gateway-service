# Decisions: Refactor README.md

## Assumptions (proposed — confirm or override)

1. **Reuse the existing `docs/` tree as the destination** for moved-out sections
   rather than inventing a parallel structure. `docs/` already has
   `TESTING_GUIDE.md`, `SECURITY_GUIDE.md`, `DEVELOPMENT_GUIDE.md`,
   `API_REFERENCE.md`, `GETTING_STARTED.md`, etc., plus an index `docs/README.md`.
2. **Keep the Security Notice in the README.** It warns first-time users about
   demo/default credentials; that safety callout belongs up front, not buried in
   a moved doc. The longer *Security Best Practices* list moves to
   `docs/SECURITY_GUIDE.md`.
3. **Contributing/Code Style → `CONTRIBUTING.md`** (already exists and is richer).
   The README keeps at most a one-line "See CONTRIBUTING.md" pointer.
4. **Correct stale facts as part of this change.** Spring Boot 3.2 → 4.1 and any
   other tech-stack drift, validated against `pom.xml` and `CLAUDE.md`. This is in
   scope because leaving known-wrong facts would defeat the "useful README" goal.
5. **Fix/remove the two dead links** (`PAYMENT_GATEWAY_PROJECT_PLAN.md`,
   `CHECKPOINT.md`). Replace with valid `docs/` targets or drop them.
6. **No new top-level marketing files.** New docs (if any) live under `docs/`.
7. **Docs-only diff.** No `.java`, `.yml`, `pom.xml`, or test changes. Rollback is
   a trivial `git revert` of the markdown commit.

## Resolved blocking questions (confirmed by Lucas, 2026-06-12)

1. **Testing section → merge into `docs/TESTING_GUIDE.md`.** README keeps no
   Testing section (at most a one-line link). The full matrix/CI advice moves out.
2. **Project Structure tree → remove from README entirely.** Not even a trimmed
   version stays; it lives in dev docs (`docs/DEVELOPMENT_GUIDE.md`) if anywhere.
3. **Reuse existing `docs/*` only — no new doc files.** Every moved section maps
   onto an existing file (`TESTING_GUIDE.md`, `SECURITY_GUIDE.md`,
   `DEVELOPMENT_GUIDE.md`, `CONTRIBUTING.md`).
4. **Fix stale facts + dead links as part of #16.** Spring Boot 3.2 → 4.1, other
   tech-stack drift, and removal/replacement of the broken
   `PAYMENT_GATEWAY_PROJECT_PLAN.md` / `CHECKPOINT.md` links are in scope.

## Resolved blocking questions — round 2 (Lucas, 2026-06-12)

5. **License stays in the README** as a short stub (corrects the earlier "remove"
   criterion). License is a trust/legal signal expected at README top level.
6. **Add a real `LICENSE` file (MIT).** The repo has none today and the current
   License section links to a missing file. README License stub points to it.
   *Minor open item:* confirm the copyright holder name (default: repo owner).
7. **Contributing stays as a short stub** → `CONTRIBUTING.md`. **Support section
   is dropped** (its content was only broken links).

Refined principle: **move depth, keep signposts.** Reference detail (test matrix,
dir tree, security checklist) → `docs/`; trust/orientation signals (License,
Contributing pointer, Security Notice) stay as short README stubs.

## Canonical structure & badges (Lucas, 2026-06-12)

8. **Adopt the tiered canonical README structure** in `spec.md` ("Target README
   Structure"). The section *order* is binding (order = UX); sections are tiered
   Must / Strongly-recommended. Target ~150–200 lines.
9. **Badges:** include only badges backed by something real — CI build status
   (GitHub Actions `ci.yml` exists), License (MIT), Java 26, Spring Boot 4.1
   (static). **Skip the coverage badge** — no Codecov/Coveralls is configured, so
   it would be fake; revisit when a coverage service is wired up.
10. **No coverage follow-up tracked (Lucas, 2026-06-12).** JaCoCo runs locally
    (`make coverage`) but CI doesn't publish it. Decision: leave it — no coverage
    badge and **no backlog item** for now. To add one later it needs CI to run
    `jacoco:report` + upload to Codecov/Coveralls (or a self-contained badge
    action); that's deliberately **out of scope** for #16 (which stays docs-only).

## Decision Log

| Date | Decision | Reason | Alternatives |
|---|---|---|---|
| 2026-06-12 | Treat issue #16 as documentation-only; spec sections API/Domain/Data marked N/A | The issue is purely about README usefulness and structure; no code surface | Could have bundled doc-cleanup of `docs/` too — deferred to keep scope tight |
| 2026-06-12 | Target README order: Overview → Features → Architecture → Quick Start (config inside) → Documentation | Directly matches the issue's requested structure | Keep current order — rejected, it's the problem being fixed |
| 2026-06-12 | Move Testing/Security/Contributing/Project-Structure/License/Support out of README | Issue explicitly asks to move "all other current sections" to separate docs | Inline-but-collapsed sections — rejected, still bloats README |
| 2026-06-12 | Testing matrix merged into `docs/TESTING_GUIDE.md`; no Testing section retained in README | Confirmed by Lucas | Short README subsection + link — declined |
| 2026-06-12 | Project Structure tree removed from README entirely | Confirmed by Lucas | Keep trimmed under Architecture — declined |
| 2026-06-12 | Reuse existing `docs/*` only; create no new doc files | Confirmed by Lucas | Allow new `docs/` files — declined |
| 2026-06-12 | Fix Spring Boot 3.2→4.1 and dead links within #16 | Confirmed by Lucas | Structure-only, defer fixes — declined |
| 2026-06-12 | Keep License + Contributing as short README stubs; drop Support | License/Contributing are expected trust signals; Support was only broken links | Remove License — declined (Lucas corrected this) |
| 2026-06-12 | Add an MIT `LICENSE` file and link the README stub to it | Repo had no license; broken link in README | Inline license text only / defer — declined |
| 2026-06-12 | Adopt tiered canonical README structure; order is binding | Order is the README's UX; tiering keeps it lean (~150–200 lines) | Free-form section set — rejected |
| 2026-06-12 | Add badges: CI build, License, Java 26, Spring Boot 4.1; skip coverage | Only show badges backed by something real; no coverage service exists | Add coverage badge anyway — rejected (would be fake) |
