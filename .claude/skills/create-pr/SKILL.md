---
name: create-pr
description: Prepare and create a GitHub pull request for the current branch, including validation, diff summary, risk notes, and a reviewer-friendly PR body.
disable-model-invocation: true
effort: medium
allowed-tools:
  - Read
  - Grep
  - Glob
  - Edit
  - Bash(git status*)
  - Bash(git diff*)
  - Bash(git log*)
  - Bash(git branch*)
  - Bash(git add*)
  - Bash(git commit*)
  - Bash(git push*)
  - Bash(gh pr create*)
  - Bash(gh pr view*)
  - Bash(gh issue view*)
  - Bash(./mvnw*)
  - Bash(mvn*)
  - Bash(./gradlew*)
  - Bash(gradle*)
---

# Create PR

Prepare a pull request for the current branch.

User input: `$ARGUMENTS`

## Live repository context

* Branch: !`git branch --show-current 2>/dev/null || true`
* Status: !`git status --short 2>/dev/null || true`
* Diff stat: !`git diff --stat HEAD 2>/dev/null || true`
* Changed files: !`git diff --name-only HEAD 2>/dev/null || true`
* Recent commits: !`git log --oneline -5 2>/dev/null || true`

## PR principles

Create a reviewer-friendly PR:

* Small and focused.
* Clear title.
* Clear problem statement.
* Clear solution summary.
* Explicit tests/checks.
* Explicit risk and rollback notes.
* Link ticket/issue when available.
* No vague “misc changes” wording.
* No claim that tests passed unless they were actually run and passed.

If the diff appears too broad for one PR, warn the user and propose splitting before creating the PR.

## Pre-PR validation

Before creating the PR:

1. Inspect `git status`.
2. Inspect `git diff HEAD`.
3. Summarize changed files.
4. Run the strongest reasonable verification for the project.
5. If there are unstaged changes, ask before committing unless the user clearly requested commit/PR creation.
6. If there is no commit for the changes, create a focused commit only if the user requested the full PR workflow.

For Maven projects, prefer:

* `./mvnw test`
* `./mvnw verify`
* formatting/static-analysis checks if configured

For Gradle projects, prefer:

* `./gradlew test`
* `./gradlew check`

## PR title

Use one of these formats based on the change:

* `feat: <imperative feature summary>`
* `fix: <imperative bug summary>`
* `test: <test-only summary>`
* `refactor: <safe refactor summary>`
* `chore: <maintenance summary>`

Keep it specific.

## PR body template

Use this structure:

### Summary

* What changed
* Why it changed

### Ticket / Context

* Link or reference to the ticket if available
* Important assumptions

### Implementation Notes

* Main design decisions
* Important files/classes changed
* Any trade-offs

### Tests / Verification

* Commands run
* Result of each command

### Risk

* Runtime risk
* Data risk
* Compatibility risk
* Rollback strategy

### Reviewer Focus

* Areas where reviewer attention is most valuable

## GitHub CLI workflow

If `gh` is available and the user wants the PR created:

1. Push the current branch.
2. Create the PR with `gh pr create`.
3. Return the PR link.

If `gh` is not available, produce the exact PR title and body for manual creation.

## Final response

Return:

1. PR title.
2. PR body.
3. Commands run.
4. PR URL if created.
5. Anything the user must review before merging.
