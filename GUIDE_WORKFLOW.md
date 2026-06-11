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
MAIN checkout  (control plane: intake + registry)
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
  │   Phase 1 (in main): plan + blocking questions → STOP → you reply APPROVE
  │   Phase 2, step 0: AUTO-CREATES a git worktree ──────────────┐
  │                                                              ▼
  │                              .worktrees/<n>-<slug>/  (branch feature/<n>-<slug>, own SERVER_PORT)
  │                                  │   implement + tests + verification (isolated)
  │                                  ▼
  │   /spring-boot-review            adversarial pre-PR review (BLOCKER/IMPORTANT/NIT)
  │                                  │
  │                                  ▼
  │   /create-pr                     draft PR: conventional title, risk + rollback body
  │                                  │
  └────────────── after merge ───────┘
  │
  ▼
/finish-feature <n>-<slug>        archive spec → specs/completed, update INDEX, remove worktree
```

Each feature is implemented in its **own git worktree** (default), so you can start one
feature and switch to another without stashing — see [Worktrees (default)](#worktrees-default).

Two hard approval gates keep a human in control: **(1)** after import you review
the generated spec before implementing; **(2)** `/implement-feature` will not write
any code (or create the worktree) until you reply `APPROVE` to its Phase 1 plan.

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
3. **Git ≥ 2.x** (worktree support) — features are implemented in isolated worktrees.
4. **JDK 21** for any build/verify steps the skills run. The host default JDK
   may be newer and break the build — see the *Gotchas* section in
   [`CLAUDE.md`](CLAUDE.md) for the exact `JAVA_HOME` recipe.
5. **Shared infra running** — start the single docker-compose stack once from the main
   checkout (`make docker-up`); all worktrees share it.
6. **Fill in the project config** (gitignored, never committed):
   ```bash
   cp .claude/config/github-project.env.example .claude/config/github-project.env
   # then edit .claude/config/github-project.env with your real values
   ```
   (The worktree tooling copies this file into each new worktree automatically.)

---

## The skills

| Skill | Invoke | Purpose | Backing script |
|---|---|---|---|
| `list-github-project-items` | `/list-github-project-items [filter]` | List backlog items from the configured GitHub Project | `.claude/scripts/github-project/list-items.sh` |
| `import-github-project-item` | `/import-github-project-item <issue-number>` | Turn an issue into `specs/active/<n>-<slug>/`, enrich the spec, ask blocking questions, update `specs/INDEX.md` | `get-item.sh`, `create-spec-from-item.sh` |
| `implement-feature` | `/implement-feature specs/active/<folder>` | Two-phase: plan (stops for `APPROVE`) → **auto-creates the feature worktree** → implement + test + verify + self-review | `.claude/scripts/worktree/create-worktree.sh` |
| `spring-boot-review` | `/spring-boot-review` | Staff-level adversarial review of the current diff before PR | — |
| `create-pr` | `/create-pr` | Validate, summarize, and open a reviewer-friendly draft PR | — |
| `finish-feature` | `/finish-feature <n>-<slug>` | After merge: archive spec → `specs/completed/`, update `INDEX.md`, remove the worktree | `.claude/scripts/worktree/finish-feature.sh` |

All are manual (`disable-model-invocation: true`) — Claude won't trigger them
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
*Phase 1* (runs in the main checkout) — the skill reads the spec, explores the code, and
returns a plan + risk review, then **stops**. Reply `APPROVE` (or `GO`/`PROCEED`/"looks
good, implement it"). *Phase 2* — it first **auto-creates an isolated worktree** at
`.worktrees/123-reconciliation-rerun-idempotency/` on branch
`feature/123-reconciliation-rerun-idempotency` (moving the spec onto that branch and
assigning it its own app port), then implements there, adds the smallest useful tests, and
runs verification (`./mvnw test` / `verify`, `spotless:check`).

**5. Review before PR** (from the worktree)
```
/spring-boot-review
```
Produces findings grouped by severity (BLOCKER / IMPORTANT / NIT / QUESTION /
PRAISE) and a verdict (READY / NEEDS CHANGES / BLOCKED). Address blockers.

**6. Open the PR** (from the worktree)
```
/create-pr
```
Creates a focused draft PR with a Conventional-Commits title and a body covering
summary, tests run, risk, and rollback. Follows the repo's
[`CONTRIBUTING.md`](CONTRIBUTING.md) and CLAUDE.md Git rules (never on `main`,
draft PRs, no auto-merge).

**7. After merge — clean up** (from the main checkout)
```
/finish-feature 123-reconciliation-rerun-idempotency
```
Archives the spec to `specs/completed/YYYY/MM/`, updates `specs/INDEX.md`, and removes
the worktree.

---

## Worktrees (default)

Every feature is implemented in its own **git worktree** — a second working directory
checked out to the feature branch, sharing the same `.git`. This is what lets you start
one feature, get interrupted, and switch to another **without stashing**: each
in-progress feature lives in its own folder under `.worktrees/`.

### Shared infra + app port offset

This service's `docker-compose.yml` uses fixed host ports (Postgres 5433, Kafka 19092,
Redis 6380) and fixed container names, so two full stacks can't run side by side. The
workflow therefore uses one **shared infra stack** plus a **per-worktree app port**:

| What | Where | Isolation |
|---|---|---|
| DB / Kafka / Redis / monitoring | one docker-compose stack (start once from main) | **shared** across worktrees |
| App instance | `<worktree>/run-app.sh` on its own `SERVER_PORT` | **isolated** (8080 main, 8081, 8082, …) |
| Source + `target/` + branch | `.worktrees/<n>-<slug>/` | **isolated** |
| Testcontainers (e2e) | ephemeral containers per JVM | **isolated** automatically |

This works with **zero changes to `docker-compose.yml` or application code** because the
app reads `SERVER_PORT` and all infra hosts from environment variables
(`application.yml`). The `create-worktree.sh` script assigns a free port (8081–8099) and
writes a `run-app.sh` that binds it.

> **Caveat (accepted):** parallel features share **one dev database**. If two open
> features both mutate data, they see each other's state. Fine for typical feature
> switching; if you need full DB isolation, use separate schemas or run features
> sequentially.

### Day-to-day

```bash
make docker-up                         # start the shared infra once (from main)

# implement-feature auto-creates the worktree; to work in it directly:
cd .worktrees/123-reconciliation-rerun-idempotency
./run-app.sh                           # runs the app on its assigned port

# switch to another feature — nothing to stash, just change directory
cd ../124-webhook-retry
```

`.worktrees/`, `.env.worktree`, and `run-app.sh` are git-ignored — they never get
committed. The **spec folder travels on the feature branch** (committed with the PR);
**`specs/INDEX.md` is edited only in the main checkout** to avoid cross-branch conflicts.

### Native alternative

If you prefer Claude Code's built-in worktree feature (`claude --worktree <name>`), the
repo ships a `.worktreeinclude` file so the gitignored config (`github-project.env`,
`.env`) is copied into native worktrees too. The skill-driven `create-worktree.sh` path
is the default because it also handles the spec move and port assignment.

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
| `permission denied` running a script | Lost the executable bit | `chmod +x .claude/scripts/**/*.sh` |
| App fails to start: port 8080 already in use | Two apps on the same port | Each worktree uses its own `SERVER_PORT` (`.env.worktree`); run via `run-app.sh`, not a bare `mvnw spring-boot:run` |
| Worktree missing `github-project.env` / `.env` | Those files are git-ignored, not in a fresh checkout | Re-run `create-worktree.sh <slug>` (it copies them), or use the native `.worktreeinclude` path |
| `git worktree add` fails: branch/worktree already exists | Leftover from a previous run | `create-worktree.sh` is idempotent (reuses it); to reset: `finish-feature.sh <slug>` then `git branch -D feature/<slug>` |
| Stale worktree entries after manual deletion | `.worktrees/<slug>` removed by hand | `git worktree prune` |

---

## Roadmap / not yet built

These are referenced in the design rationale but **do not exist yet** — treat them
as future work, not current capability:

- **`_bmad-output/project-context.md`** — a stable "constitution" file. This repo
  is *BMAD-inspired* but does not use native BMAD output; project-wide rules
  currently live in `CLAUDE.md`.
- **Type-specialized skills** — `fix-bug`, `investigate-incident`,
  `performance-investigation`, `security-fix`. For now, `implement-feature` covers
  general feature work.

Spec archival is handled by `/finish-feature` (move to `specs/completed/` + worktree
cleanup); compressing drafts to summary + decisions is currently a manual step within it.
