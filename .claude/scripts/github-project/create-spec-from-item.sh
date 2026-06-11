#!/usr/bin/env bash
set -euo pipefail

ISSUE_NUMBER="${1:-}"

if [ -z "$ISSUE_NUMBER" ]; then
  echo "Usage: $0 <issue-number>" >&2
  exit 1
fi

if ! command -v jq >/dev/null 2>&1; then
  echo "'jq' is required to parse the GitHub issue JSON. Install it (e.g. 'brew install jq')." >&2
  exit 1
fi

SLUG="${2:-}"

RAW_JSON="$(.claude/scripts/github-project/get-item.sh "$ISSUE_NUMBER")"

TITLE="$(echo "$RAW_JSON" | jq -r '.title')"
URL="$(echo "$RAW_JSON" | jq -r '.url')"
BODY="$(echo "$RAW_JSON" | jq -r '.body // ""')"

if [ -z "$SLUG" ]; then
  SLUG="$(echo "$TITLE" \
    | tr '[:upper:]' '[:lower:]' \
    | sed -E 's/[^a-z0-9]+/-/g' \
    | sed -E 's/^-+|-+$//g')"
fi

SPEC_DIR="specs/active/${ISSUE_NUMBER}-${SLUG}"
mkdir -p "$SPEC_DIR"

cat > "$SPEC_DIR/source.md" <<EOF
# Source

Source type: GitHub Project Issue
Issue: #$ISSUE_NUMBER
URL: $URL
Imported at: $(date -u +"%Y-%m-%dT%H:%M:%SZ")

## Title

$TITLE

## Original Description

$BODY

## Raw JSON

\`\`\`json
$RAW_JSON
\`\`\`
EOF

cat > "$SPEC_DIR/spec.md" <<EOF
# Feature Spec: $TITLE

Status: draft
Source: $URL

## Problem

TODO: Describe the business/user problem.

## Desired Behavior

TODO: Describe expected behavior.

## Current Behavior

TODO: Describe current behavior if applicable.

## Acceptance Criteria

TODO: Convert GitHub issue details into clear acceptance criteria.

## API Contract

TODO: Endpoints, request/response, status codes, validation.

## Domain Rules

TODO: Business rules, idempotency, transactionality, retries.

## Data Changes

TODO: DB schema, migrations, persistence changes.

## Observability

TODO: Logs, metrics, traces, audit events.

## Security

TODO: Authentication, authorization, sensitive data, permission model.

## Open Questions

TODO: Blocking questions for Lucas/product/tech lead.
EOF

cat > "$SPEC_DIR/tasks.md" <<EOF
# Implementation Tasks: $TITLE

Status: draft

## Tasks

- [ ] Discover relevant code paths.
- [ ] Confirm acceptance criteria.
- [ ] Plan implementation.
- [ ] Add or update tests.
- [ ] Implement feature.
- [ ] Run verification.
- [ ] Run Spring Boot review.
- [ ] Create PR.

## Verification

- [ ] ./mvnw test
- [ ] ./mvnw verify
- [ ] ./mvnw spotless:check, if configured
EOF

cat > "$SPEC_DIR/decisions.md" <<EOF
# Decisions: $TITLE

## Decision Log

| Date | Decision | Reason | Alternatives |
|---|---|---|---|
EOF

cat > "$SPEC_DIR/test-plan.md" <<EOF
# Test Plan: $TITLE

## Unit Tests

TODO

## Integration Tests

TODO

## API Tests

TODO

## Regression Tests

TODO

## Manual Verification

TODO
EOF

echo "$SPEC_DIR"