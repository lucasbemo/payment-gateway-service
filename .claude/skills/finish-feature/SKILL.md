---
name: finish-feature
description: Finish a merged feature — archive its spec to specs/completed, update specs/INDEX.md, and remove its git worktree. Use after the feature's pull request has merged.
disable-model-invocation: true
argument-hint: "<n>-<slug>"
allowed-tools:
  - Read
  - Grep
  - Glob
  - Edit
  - Bash(git status*)
  - Bash(git log*)
  - Bash(git branch*)
  - Bash(git worktree*)
  - Bash(.claude/scripts/worktree/*)
  - Bash(gh pr view*)
  - Bash(date*)
  - Bash(ls*)
  - Bash(mv*)
  - Bash(mkdir*)
---

# Finish Feature

Wrap up a feature whose PR has merged: archive the spec, update the index, and remove
the worktree. Run this from the **main checkout** (the control plane), not from inside
the worktree.

Feature: `$ARGUMENTS` (the `<n>-<slug>` of the feature).

## Live context

- Worktrees: !`git worktree list 2>/dev/null || true`
- Current branch: !`git branch --show-current 2>/dev/null || true`

## Preconditions

1. You must be in the **main checkout**, not under `.worktrees/`.
2. The feature's PR should be **merged**. Confirm with `gh pr view` (by branch
   `feature/$ARGUMENTS`) when possible. If it is not merged, STOP and tell the user —
   do not archive or remove anything.

## Steps

### 1. Resolve dates and paths

- Slug = `$ARGUMENTS` (e.g. `123-reconciliation-rerun-idempotency`).
- Determine year/month: `date -u +%Y` and `date -u +%m`.
- Source spec (now on `main` after merge): `specs/active/<slug>/`.
- Destination: `specs/completed/<YYYY>/<MM>/<slug>/`.

### 2. Archive the spec

- If `specs/active/<slug>/` exists in the main checkout, move it:
  `mkdir -p specs/completed/<YYYY>/<MM>` then `mv specs/active/<slug> specs/completed/<YYYY>/<MM>/`.
- Update the spec's status metadata to `completed` and record the merged PR number/link.
- (Optional, encouraged) compress drafts: keep `spec.md`/`summary` + `decisions.md`;
  trim large intermediate notes. Do not delete decisions or the PR link.

### 3. Update `specs/INDEX.md` (main checkout only)

- Remove the feature's row from the **Active** table.
- Add a row to the **Completed** table: issue, title, merged PR, date, new spec path, summary.
- `specs/INDEX.md` is edited only here in the main checkout — never on feature branches.

### 4. Remove the worktree

- Run `.claude/scripts/worktree/finish-feature.sh <slug>`.
- It refuses if the worktree has uncommitted changes (rerun with `FORCE=1` only if the
  user confirms those changes are disposable). It also prunes stale worktree entries.
- Optionally delete the merged local branch: `git branch -d feature/<slug>`.

## Final output

Report: the spec's new path, the INDEX.md changes, the removed worktree, and any branch
cleanup. If the PR was not merged, report that and that nothing was changed.
