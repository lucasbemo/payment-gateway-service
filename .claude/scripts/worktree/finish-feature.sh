#!/usr/bin/env bash
#
# finish-feature.sh <n>-<slug>
#
# Remove a feature worktree after its PR has merged. Handles only the git
# worktree mechanics; moving the spec active -> completed and updating
# specs/INDEX.md is done by the /finish-feature skill (it knows the date).
#
# Refuses to remove a worktree with uncommitted changes (pass FORCE=1 to override).
set -euo pipefail

SLUG="${1:-}"
if [ -z "$SLUG" ]; then
  echo "Usage: $0 <n>-<slug>" >&2
  exit 1
fi

ROOT="$(git rev-parse --show-toplevel)"
WT="$ROOT/.worktrees/$SLUG"

if [ ! -d "$WT" ]; then
  echo "No worktree at $WT (already removed?). Pruning stale entries." >&2
  git -C "$ROOT" worktree prune
  exit 0
fi

if [ "${FORCE:-0}" != "1" ] && [ -n "$(git -C "$WT" status --porcelain)" ]; then
  echo "Worktree $WT has uncommitted changes; not removing." >&2
  echo "Commit/push first, or re-run with FORCE=1 to discard." >&2
  exit 1
fi

if [ "${FORCE:-0}" = "1" ]; then
  git -C "$ROOT" worktree remove --force "$WT"
else
  git -C "$ROOT" worktree remove "$WT"
fi
git -C "$ROOT" worktree prune
echo "Removed worktree $WT" >&2
