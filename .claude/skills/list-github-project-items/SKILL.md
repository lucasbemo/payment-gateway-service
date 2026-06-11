---
name: list-github-project-items
description: List GitHub Project backlog items from the configured project. Use to inspect available product backlog items before importing or implementing a feature.
disable-model-invocation: true
argument-hint: "[optional project filter query]"
allowed-tools:
  - Bash(.claude/scripts/github-project/list-items.sh*)
  - Bash(gh auth status*)
---

# List GitHub Project Items

List items from the configured GitHub Project.

User filter: `$ARGUMENTS`

## Rules

- Use `.claude/config/github-project.env` for project configuration.
- If the user provides a filter, pass it to the script.
- If no filter is provided, use the default query from config.
- Do not edit files.
- Do not update GitHub Project items.
- Do not start implementation.

## Commands

First check GitHub auth:

!`gh auth status 2>/dev/null || true`

Then list items:

!`.claude/scripts/github-project/list-items.sh "$ARGUMENTS" 30 2>/dev/null || .claude/scripts/github-project/list-items.sh`

## Output

Return a short table with:

- Issue number or item identifier
- Title
- Status
- Labels
- Assignees
- URL
- Recommended next command

Recommended next command format:

`/import-github-project-item <issue-number>`