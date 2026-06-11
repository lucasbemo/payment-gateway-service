---
name: implement-feature
description: Plan and implement a Spring Boot feature from a ticket, issue, or requirement. Use when the user wants a human-approved workflow with discovery, blocking questions, implementation plan, tests, verification, and self-review.
disable-model-invocation: true
effort: high
allowed-tools:
  - Read
  - Grep
  - Glob
  - Edit
  - MultiEdit
  - Bash(git status*)
  - Bash(git diff*)
  - Bash(git branch*)
  - Bash(git checkout*)
  - Bash(git switch*)
  - Bash(git fetch*)
  - Bash(git worktree*)
  - Bash(.claude/scripts/worktree/*)
  - Bash(gh issue view*)
  - Bash(gh pr view*)
  - Bash(./mvnw*)
  - Bash(mvn*)
  - Bash(./gradlew*)
  - Bash(gradle*)
---

# Implement Feature

Implement the requested ticket or feature using a human-approved workflow.

User input: `$ARGUMENTS`

## Live repository context

- Current branch: !`git branch --show-current 2>/dev/null || true`
- Git status: !`git status --short 2>/dev/null || true`
- Build files: !`ls -1 pom.xml build.gradle build.gradle.kts settings.gradle settings.gradle.kts 2>/dev/null || true`
- Existing diff: !`git diff --stat HEAD 2>/dev/null || true`

## Source input rule

This skill should normally receive a local spec folder, not a raw GitHub Project item.

Preferred input:

`/implement-feature specs/active/<feature-folder>`

If the user provides only a GitHub Issue number, stop and recommend:

`/import-github-project-item <issue-number>`

Do not implement directly from a GitHub Project item unless the user explicitly overrides this rule.

## Spec discovery

If `$ARGUMENTS` points to `specs/active/<feature-folder>`:

Read:

- `source.md`
- `spec.md`
- `tasks.md`
- `test-plan.md`
- `decisions.md`

Treat `spec.md` and `tasks.md` as the implementation contract.

If the spec has unresolved blocking questions, stop and ask the user to resolve them before implementation.

## Critical rule: do not code before approval

This skill has two phases.

Phase 1:
- Understand the requirement.
- Explore the codebase.
- Ask blocking questions.
- State assumptions.
- Produce an implementation plan.
- Stop and wait for explicit user approval.

Phase 2:
- Start implementation only after the user explicitly approves.

Explicit approval means the user says one of:

- `APPROVE`
- `APPROVED`
- `GO`
- `IMPLEMENT`
- `PROCEED`
- `START CODING`
- clear natural language equivalent, such as “looks good, implement it”

Do not edit files, create branches, commit, or run destructive commands during Phase 1.

Reading files, searching code, checking git status, and inspecting build files are allowed during Phase 1.

## Phase 1 — Discovery and planning

### 1. Understand the ticket

If `$ARGUMENTS` looks like a GitHub issue number or URL and `gh` is available, read the issue before planning.

If `$ARGUMENTS` is plain text, treat it as the source of truth.

Extract:

- Goal
- User-visible behavior
- API changes
- Data/model changes
- Non-functional requirements
- Acceptance criteria
- Out of scope items
- Risk areas

### 2. Inspect the codebase before planning

Read relevant project context before proposing the plan:

- `README*`
- `CLAUDE.md`
- `REVIEW.md`
- `pom.xml` or Gradle files
- package structure under `src/main/java`
- tests under `src/test`
- controllers, services, repositories, mappers, DTOs, configuration, migrations, and exception handling related to the feature

Prefer existing project conventions over generic best practices.

### 3. Ask questions correctly

Ask only blocking questions.

A blocking question is one where implementing without the answer could cause:

- wrong business behavior
- incompatible API behavior
- wrong data model
- security issue
- destructive migration
- unclear acceptance criteria
- wrong integration contract

Do not ask questions about things that can be safely inferred from the existing codebase.

For non-blocking uncertainty, write an assumption instead.

### 4. Produce the plan and stop

Your Phase 1 response must use this format:

## Understanding

Summarize the ticket in your own words.

## Discovered Context

List the relevant files, classes, endpoints, tests, and conventions found during exploration.

## Blocking Questions

List only truly blocking questions.

If there are no blocking questions, write:

`No blocking questions.`

## Assumptions

List assumptions you will use if the user does not correct them.

## Implementation Plan

Give a step-by-step plan.

Each step should mention:

- what will change
- where it will change
- why it is needed
- what test will prove it

## Test Plan

List the tests/checks you intend to add or run.

## Risk Review

Call out risk around:

- API compatibility
- data/database changes
- transactionality
- idempotency
- concurrency
- security
- observability
- rollback

## Approval Request

End with:

`Reply APPROVE to implement this plan, or reply with changes/questions.`

Then stop.

Do not continue to implementation in the same response.

## Phase 2 — Implementation

Start Phase 2 only after explicit approval.

### Step 0 — Create or enter the feature worktree (default)

This project uses **worktree-per-feature** (shared infra + app port offset). Before
writing any code:

1. Derive the slug from the spec path: `specs/active/<n>-<slug>` → slug `<n>-<slug>`.
2. If the current working directory is already under `.worktrees/`, you are already in
   the worktree — skip creation.
3. Otherwise create/enter it:
   `.claude/scripts/worktree/create-worktree.sh <n>-<slug>`
   Read the `WORKTREE_PATH=` and `SERVER_PORT=` lines from its output. Use
   `WORKTREE_PATH` as the **working root for ALL subsequent edits, builds, and git
   commands** (absolute paths under it, or `git -C <WORKTREE_PATH>`). The script
   creates branch `feature/<n>-<slug>`, moves the spec onto it, copies local config
   (`.claude/config/github-project.env`, `.env*`), and assigns a unique app port.
4. Infrastructure (DB/Kafka/Redis) is a **single shared stack** started once from the
   main checkout (`make docker-up`). Do NOT start a second stack. To run the app, use
   `<WORKTREE_PATH>/run-app.sh`, which binds the assigned `SERVER_PORT` against the
   shared infra. Testcontainers-based tests are already isolated and need no port handling.

Never create the worktree during Phase 1.

### Before editing (inside the worktree)

1. Re-check `git -C <WORKTREE_PATH> status`.
2. Confirm whether there are existing uncommitted changes.
3. Avoid overwriting user changes.
4. If the working tree has unrelated changes, preserve them and work around them.

## Spring Boot implementation checklist

When changing Spring Boot code, check for:

- Clear package placement and dependency direction
- Constructor injection, not field injection
- Validation at API boundaries
- Consistent DTO, error response, and exception handling patterns
- Transaction boundaries where persistence changes occur
- Idempotency for retryable commands or payment-like operations
- No secrets, credentials, tokens, or environment-specific values in code
- Configuration through properties/yaml and typed configuration when the project already uses it
- Observability for important business flows: logs, metrics, traces, correlation IDs, or audit events when appropriate
- Backward-compatible API behavior unless the ticket explicitly requires a breaking change
- Database migration safety if schema changes are required
- Security and authorization checks for new or changed endpoints
- No unnecessary broad refactors

## Testing strategy

Add the smallest useful test set that proves the acceptance criteria.

Prefer, in this order:

1. Unit tests for pure business logic.
2. MVC/API tests for controller behavior.
3. Repository/data tests for persistence behavior.
4. Integration tests with Testcontainers when behavior depends on a real database, broker, or external service.
5. End-to-end or application tests only when lower-level tests cannot prove the behavior.

Do not delete or weaken existing tests to make the build pass.

## Verification commands

Detect the build tool and run the strongest reasonable checks.

For Maven projects, prefer:

- `./mvnw test`
- `./mvnw verify`
- `./mvnw spotless:check` if Spotless is configured
- `./mvnw checkstyle:check` if Checkstyle is configured
- `./mvnw spotbugs:check` if SpotBugs is configured

For Gradle projects, prefer:

- `./gradlew test`
- `./gradlew check`

If a command fails, read the error, fix the root cause, and rerun the relevant command.

Do not suppress checks unless the project already has an accepted convention for doing so.

## Self-review before final response

Before finishing, inspect:

- `git diff --stat HEAD`
- `git diff HEAD`
- changed tests
- changed configuration
- changed public API contracts
- possible missing edge cases

Look specifically for:

- broken compatibility
- missing validation
- missing tests
- accidental formatting-only churn
- null handling issues
- transaction and concurrency issues
- security gaps
- poor naming
- unnecessary abstractions

## Final response after implementation

Return:

1. Summary of what was implemented.
2. Files changed.
3. Tests/checks run with pass/fail status.
4. Important design decisions.
5. Risks or follow-up items.
6. Suggested PR title and PR body draft.

Never claim verification passed unless you actually ran the command and saw it pass.
