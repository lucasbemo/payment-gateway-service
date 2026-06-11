#!/usr/bin/env bash
set -euo pipefail

CONFIG_FILE=".claude/config/github-project.env"

if [ ! -f "$CONFIG_FILE" ]; then
  echo "Missing $CONFIG_FILE. Copy .claude/config/github-project.env.example first." >&2
  exit 1
fi

# shellcheck disable=SC1090
source "$CONFIG_FILE"

ISSUE_NUMBER="${1:-}"

if [ -z "$ISSUE_NUMBER" ]; then
  echo "Usage: $0 <issue-number>" >&2
  exit 1
fi

if [ -z "${GITHUB_REPOSITORY:-}" ]; then
  echo "GITHUB_REPOSITORY is required in $CONFIG_FILE" >&2
  exit 1
fi

gh issue view "$ISSUE_NUMBER" \
  --repo "$GITHUB_REPOSITORY" \
  --json number,title,body,state,labels,assignees,milestone,comments,url,createdAt,updatedAt