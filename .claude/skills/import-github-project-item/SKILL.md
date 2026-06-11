---
name: import-github-project-item
description: Import a GitHub Project issue into specs/active as an implementation-ready feature specification. Use before implementing a feature from GitHub Project.
disable-model-invocation: true
argument-hint: "<issue-number>"
allowed-tools:
  - Read
  - Grep
  - Glob
  - Edit
  - MultiEdit
  - Bash(.claude/scripts/github-project/get-item.sh*)
  - Bash(.claude/scripts/github-project/create-spec-from-item.sh*)
  - Bash(git status*)
  - Bash(git diff*)
  - Bash(gh issue view*)
---

# Import GitHub Project Item

Import GitHub Project item `$ARGUMENTS` into a local feature spec.

## Critical rule

Do not implement code.

This skill only:

1. Reads the GitHub Issue / Project item.
2. Creates or updates `specs/active/<issue-number>-<slug>/`.
3. Enriches the raw issue into an implementation-ready spec.
4. Asks blocking questions.
5. Stops for human approval.

## Source of truth policy

- GitHub Project is the product backlog source of truth.
- The local `specs/active/<feature>/` folder becomes the implementation contract after review.
- Current source code and tests override stale docs.
- Do not implement directly from GitHub Project text.

## Step 1 — Import source

Run:

`.claude/scripts/github-project/create-spec-from-item.sh $ARGUMENTS`

Read the generated folder.

## Step 2 — Discover repository context

Inspect only enough code to enrich the spec:

- `README*`
- `CLAUDE.md`
- `pom.xml` or Gradle files
- package structure under `src/main/java`
- related controllers, services, repositories, DTOs, tests
- existing exception handling and validation patterns

Do not edit source code.

## Step 3 — Enrich spec

Update these files:

- `spec.md`
- `tasks.md`
- `test-plan.md`
- `decisions.md`

The enriched `spec.md` must include:

## Problem
## Desired Behavior
## Current Behavior
## Acceptance Criteria
## API Contract
## Domain Rules
## Data Changes
## Observability
## Security
## Open Questions

The enriched `tasks.md` must include concrete implementation tasks.

The enriched `test-plan.md` must include specific test types and expected assertions.

The enriched `decisions.md` must record assumptions and proposed technical decisions.

## Step 4 — Ask blocking questions

Ask only blocking questions.

Blocking questions include:

- ambiguous business behavior
- unclear acceptance criteria
- API compatibility risk
- database migration uncertainty
- idempotency/retry behavior
- security/authorization uncertainty
- external integration contract uncertainty

For non-blocking uncertainty, write assumptions.

## Step 5 — Update specs/INDEX.md

Add or update one row for this feature with:

- issue number
- title
- status: draft
- spec path
- GitHub URL
- short summary

## Final output

Return:

1. Imported GitHub item summary.
2. Spec folder path.
3. Discovered code context.
4. Blocking questions.
5. Assumptions.
6. Recommended next command.

Recommended next command:

`/implement-feature specs/active/<folder-name>`

Do not implement code.