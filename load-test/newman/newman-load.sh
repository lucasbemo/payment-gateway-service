#!/usr/bin/env bash
#
# Functional load loop — replays the full Postman collection many times and asserts
# every response, validating API correctness under sustained repeated load (a soak
# that a pure throughput tool does not do, since it checks bodies/status of every
# request). Complements load-test/k6 (concurrent throughput/latency).
#
# Each "pass" is an INDEPENDENT `newman run` (fresh variable scope) — we do NOT use
# newman's `-n`, because the collection is a stateful end-to-end flow and `-n` would
# bleed variables (e.g. the idempotency payment id) across iterations.
#
# NOTE: this Postman collection is a *sequential* E2E scenario and is NOT
# concurrency-safe (concurrent passes generate colliding test data, e.g. a merchant
# email, so all-but-one fail). Keep CONCURRENCY=1 for a clean functional soak; use
# the k6 harness for concurrent throughput/latency load.
#
# Run:
#   load-test/newman/newman-load.sh
#   BASE_URL=http://localhost:8080 PASSES=50 load-test/newman/newman-load.sh
#
# Env: BASE_URL, PASSES (sequential collection runs), CONCURRENCY (parallel workers;
#      keep 1 — see note above), COLLECTION, ENVIRONMENT.
set -uo pipefail

ROOT="$(git rev-parse --show-toplevel)"
BASE_URL="${BASE_URL:-http://localhost:8080}"
PASSES="${PASSES:-10}"
CONCURRENCY="${CONCURRENCY:-1}"
COLLECTION="${COLLECTION:-$ROOT/postman/Payment Gateway.postman_collection.json}"
ENVIRONMENT="${ENVIRONMENT:-$ROOT/postman/Payment Gateway Local.postman_environment.json}"

NEWMAN=(npx --yes newman)
command -v newman >/dev/null 2>&1 && NEWMAN=(newman)

OUT="$(mktemp -d)"
echo "Newman functional load loop"
echo "  base URL    : $BASE_URL"
echo "  passes      : $PASSES sequential collection runs / worker"
echo "  concurrency : $CONCURRENCY worker(s)  (= $((CONCURRENCY * PASSES)) total passes)"
echo

# One worker = PASSES independent newman runs, in sequence.
run_worker() {
  local w="$1" p ec
  for p in $(seq 1 "$PASSES"); do
    "${NEWMAN[@]}" run "$COLLECTION" \
      -e "$ENVIRONMENT" \
      --env-var "baseUrl=$BASE_URL" \
      --reporters cli --reporter-cli-no-banner \
      > "$OUT/w${w}-pass${p}.log" 2>&1
    ec=$?
    [ "$ec" -eq 0 ] && echo "PASS" >> "$OUT/w${w}.result" || echo "FAIL" >> "$OUT/w${w}.result"
  done
}

start=$(date +%s)
pids=()
for w in $(seq 1 "$CONCURRENCY"); do run_worker "$w" & pids+=("$!"); done
for pid in "${pids[@]}"; do wait "$pid"; done
end=$(date +%s)

passes_ok=$(grep -rh "PASS" "$OUT"/w*.result 2>/dev/null | wc -l | tr -d ' ')
passes_fail=$(grep -rh "FAIL" "$OUT"/w*.result 2>/dev/null | wc -l | tr -d ' ')
total=$((passes_ok + passes_fail))

echo "=== Newman functional load loop result ==="
echo "  passes total   : $total"
echo "  passes OK      : $passes_ok"
echo "  passes FAILED  : $passes_fail"
echo "  wall-clock     : $((end - start))s"
echo "  per-pass logs  : $OUT"

if [ "$passes_fail" -eq 0 ] && [ "$total" -gt 0 ]; then
  echo "RESULT: PASS"
  exit 0
fi
echo "RESULT: FAIL (inspect $OUT/w*-pass*.log)"
exit 1
