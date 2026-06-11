#!/usr/bin/env bash
set -euo pipefail

CONFIG_FILE=".claude/config/github-project.env"

if [ ! -f "$CONFIG_FILE" ]; then
  echo "Missing $CONFIG_FILE. Copy .claude/config/github-project.env.example first." >&2
  exit 1
fi

# shellcheck disable=SC1090
source "$CONFIG_FILE"

QUERY="${1:-${GITHUB_PROJECT_DEFAULT_QUERY:-is:issue is:open -status:Done}}"
LIMIT="${2:-30}"

if ! command -v gh >/dev/null 2>&1; then
  echo "GitHub CLI 'gh' is required." >&2
  exit 1
fi

gh project item-list "$GITHUB_PROJECT_NUMBER" \
  --owner "$GITHUB_PROJECT_OWNER" \
  --query "$QUERY" \
  --limit "$LIMIT" \
  --format json