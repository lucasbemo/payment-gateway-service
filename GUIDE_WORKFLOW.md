# Workflow Guide — Spec-Driven Development

This project uses a **spec-driven, human-in-the-loop workflow** for feature work,
assisted by Claude Code skills. The goal: every non-trivial change flows from a
GitHub Project backlog item → an approved local spec → implementation → review →
PR, with explicit human approval gates along the way.

> **Source-of-truth rule:** GitHub Project is the product backlog source of truth.
> Once imported and approved, `specs/active/<feature>/` becomes the engineering
> contract for implementation. **Current source code and tests always override
> older specs.** (See also the policy sections in [`CLAUDE.md`](CLAUDE.md).)

---

## Overview

```
GitHub Project issue
  │
  ▼
/list-github-project-items        browse the backlog
  │
  ▼
/import-github-project-item <n>   scaffold specs/active/<n>-<slug>/, enrich, ask blocking questions
  │
  ▼
  ── human review & approval ──   answer blocking questions, edit the spec
  │
  ▼
/implement-feature specs/active/<n>-<slug>
  │                               Phase 1: plan + blocking questions → STOP
  │                               (you reply APPROVE)
  │                               Phase 2: implement + tests + verification
  ▼
/spring-boot-review               adversarial pre-PR review (BLOCKER/IMPORTANT/NIT)
  │
  ▼
/create-pr                        draft PR: conventional title, risk + rollback body
```

Two hard approval gates keep a human in control: **(1)** after import you review
the generated spec before implementing; **(2)** `/implement-feature` will not write
any code until you reply `APPROVE` to its Phase 1 plan.

---

## Prerequisites

Run these once before using the workflow:

1. **GitHub CLI with `project` scope.** The backlog scripts call
   `gh project item-list`, which needs the `project` (or `read:project`) scope —
   the default `repo` token does **not** include it.
   ```bash
   gh auth status            # check current scopes
   gh auth refresh -s project
   ```
2. **`jq`** installed (`brew install jq`) — used to parse issue JSON into specs.
3. **JDK 21** for any build/verify steps the skills run. The host default JDK
   may be newer and break the build — see the *Gotchas* section in
   [`CLAUDE.md`](CLAUDE.md) for the exact `JAVA_HOME` recipe.
4. **Fill in the project config** (gitignored, never committed):
   ```bash
   cp .claude/config/github-project.env.example .claude/config/github-project.env
   # then edit .claude/config/github-project.env with your real values
   ```

---

## The skills

| Skill | Invoke | Purpose | Backing script |
|---|---|---|---|
| `list-github-project-items` | `/list-github-project-items [filter]` | List backlog items from the configured GitHub Project | `.claude/scripts/github-project/list-items.sh` |
| `import-github-project-item` | `/import-github-project-item <issue-number>` | Turn an issue into `specs/active/<n>-<slug>/`, enrich the spec, ask blocking questions, update `specs/INDEX.md` | `get-item.sh`, `create-spec-from-item.sh` |
| `implement-feature` | `/implement-feature specs/active/<folder>` | Two-phase: plan (stops for `APPROVE`) → implement + test + verify + self-review | — |
| `spring-boot-review` | `/spring-boot-review` | Staff-level adversarial review of the current diff before PR | — |
| `create-pr` | `/create-pr` | Validate, summarize, and open a reviewer-friendly draft PR | — |

All five are manual (`disable-model-invocation: true`) — Claude won't trigger them
on its own; you invoke them by name.

These project skills are distinct from the **superpowers** plugin skills
(brainstorming, writing-plans, test-driven-development, systematic-debugging,
requesting-code-review, etc.), which are general-purpose engineering aids. Use
superpowers skills *within* a step (e.g. TDD while implementing); use the project
skills above to drive the GitHub-Project → PR pipeline.

---

## Step-by-step walkthrough

Example: implementing issue **#123 "Reconciliation rerun idempotency"**.

**1. Browse the backlog**
```
/list-github-project-items is:issue is:open -status:Done
```
Returns a table of items with issue numbers, titles, status, labels, and the
recommended next command.

**2. Import the chosen item**
```
/import-github-project-item 123
```
This scaffolds `specs/active/123-reconciliation-rerun-idempotency/` with:
`source.md` (raw issue), `spec.md`, `tasks.md`, `test-plan.md`, `decisions.md`.
The skill enriches the spec from the codebase, lists **blocking questions**, and
adds a row to `specs/INDEX.md`. It does **not** write production code.

**3. Review & approve the spec.** Answer the blocking questions, edit `spec.md` /
`tasks.md` as needed. The spec is now the contract.

**4. Implement**
```
/implement-feature specs/active/123-reconciliation-rerun-idempotency
```
*Phase 1* — the skill reads the spec, explores the code, and returns a plan +
risk review, then **stops**. Reply `APPROVE` (or `GO`/`PROCEED`/"looks good,
implement it"). *Phase 2* — it implements, adds the smallest useful tests, and
runs verification (`./mvnw test` / `verify`, `spotless:check`).

**5. Review before PR**
```
/spring-boot-review
```
Produces findings grouped by severity (BLOCKER / IMPORTANT / NIT / QUESTION /
PRAISE) and a verdict (READY / NEEDS CHANGES / BLOCKED). Address blockers.

**6. Open the PR**
```
/create-pr
```
Creates a focused draft PR with a Conventional-Commits title and a body covering
summary, tests run, risk, and rollback. Follows the repo's
[`CONTRIBUTING.md`](CONTRIBUTING.md) and CLAUDE.md Git rules (never on `main`,
draft PRs, no auto-merge).

---

## The `specs/` lifecycle

```
specs/
  INDEX.md            ← the cheap map; read this first, not every spec
  active/             ← work being prepared or implemented
    123-reconciliation-rerun-idempotency/
      source.md       ← raw imported issue (do not treat as final truth)
      spec.md         ← the contract: problem, behavior, acceptance criteria, API, data, security
      tasks.md        ← implementation checklist
      test-plan.md    ← unit / integration / API / regression tests
      decisions.md    ← decision log + assumptions
  completed/YYYY/MM/  ← merged work, compressed to summary + decisions
  archived/           ← obsolete / superseded specs (history only)
```

**Status metadata.** Each spec carries a status: `draft` → `active` → `completed`
(or `superseded`). When a PR merges, update the spec's status + merged-PR link,
move the folder to `completed/YYYY/MM/`, and update `specs/INDEX.md`. When a spec
is replaced, mark it `superseded` and point to the replacement.

**Why the discipline:** a growing `specs/` folder is fine — *auto-loading all of
it into every session is not*. Keep stable rules small, keep active specs visible,
archive completed ones, and let the index be the map.

---

## Spec context policy

When working on a task, read in this order (also enforced in `CLAUDE.md`):

1. `CLAUDE.md`
2. `specs/INDEX.md`
3. The relevant `specs/active/<feature>/` folder
4. Source code and tests
5. Historical (`completed/`, `archived/`) specs **only** when explicitly relevant

**Do not scan all of `specs/` by default.** Old specs describe decisions that may
have been changed by later PRs — when in doubt, the current code and tests win.

---

## Configuration reference

`.claude/config/github-project.env` (copy from `.example`; the real file is
gitignored):

| Variable | Meaning | Example |
|---|---|---|
| `GITHUB_PROJECT_OWNER` | Org or user that owns the Project | `lucasbemo` |
| `GITHUB_PROJECT_NUMBER` | Project number (from its URL) | `3` |
| `GITHUB_PROJECT_OWNER_TYPE` | `org` or `user` | `user` |
| `GITHUB_PROJECT_DEFAULT_QUERY` | Default filter when none is passed | `is:issue is:open -status:Done` |
| `GITHUB_REPOSITORY` | `owner/repo` for issue lookup & PRs | `lucasbemo/payment-gateway-service` |

**Filter syntax** for `/list-github-project-items` (GitHub Projects filter
grammar):
```
is:issue is:open -status:Done
assignee:@me is:issue is:open
label:feature is:issue is:open -status:Done
```

---

## Troubleshooting

| Symptom | Cause | Fix |
|---|---|---|
| `/list-github-project-items` fails with a scope error | `gh` token lacks `project`/`read:project` | `gh auth refresh -s project` |
| Scripts resolve a project named `YOUR_ORG_OR_USER` / find nothing | `github-project.env` still has placeholder values | Edit `.claude/config/github-project.env` with real owner/number/repo |
| "skill not found" when following a recommended next command | Skill renamed/moved; a reference drifted | Confirm the skill dir under `.claude/skills/`; the implement skill is `implement-feature` |
| A skill prompts for permission on every command | Malformed `SKILL.md` frontmatter (blank line after `---`, unindented `allowed-tools`) | Match the frontmatter shape of `implement-feature/SKILL.md` |
| `create-spec-from-item.sh: jq: command not found` | `jq` not installed | `brew install jq` |
| `permission denied` running a script | Lost the executable bit | `chmod +x .claude/scripts/github-project/*.sh` |

---

## Roadmap / not yet built

These are referenced in the design rationale but **do not exist yet** — treat them
as future work, not current capability:

- **`_bmad-output/project-context.md`** — a stable "constitution" file. This repo
  is *BMAD-inspired* but does not use native BMAD output; project-wide rules
  currently live in `CLAUDE.md`.
- **`archive-feature-spec` skill** — to compress a merged spec to summary +
  decisions and move it to `completed/`. For now, do this step manually.
- **Type-specialized skills** — `fix-bug`, `investigate-incident`,
  `performance-investigation`, `security-fix`. For now, `implement-feature` covers
  general feature work.
