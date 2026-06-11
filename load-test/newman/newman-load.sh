#!/usr/bin/env bash
#
# Functional load loop — runs the Postman collection repeatedly across several
# concurrent workers to validate API correctness under sustained load (it checks
# *every response* against the collection's assertions, which a pure throughput
# tool does not). Complements load-test/k6 (latency/throughput).
#
# Run:
#   load-test/newman/newman-load.sh
#   BASE_URL=http://localhost:8080 CONCURRENCY=8 ITERATIONS=25 load-test/newman/newman-load.sh
#
# Env: BASE_URL, CONCURRENCY (parallel workers), ITERATIONS (collection runs per
#      worker), COLLECTION, ENVIRONMENT.
set -uo pipefail

ROOT="$(git rev-parse --show-toplevel)"
BASE_URL="${BASE_URL:-http://localhost:8080}"
CONCURRENCY="${CONCURRENCY:-5}"
ITERATIONS="${ITERATIONS:-20}"
COLLECTION="${COLLECTION:-$ROOT/postman/Payment Gateway.postman_collection.json}"
ENVIRONMENT="${ENVIRONMENT:-$ROOT/postman/Payment Gateway Local.postman_environment.json}"

NEWMAN=(npx --yes newman)
command -v newman >/dev/null 2>&1 && NEWMAN=(newman)

OUT="$(mktemp -d)"
echo "Newman load loop"
echo "  base URL    : $BASE_URL"
echo "  concurrency : $CONCURRENCY workers"
echo "  iterations  : $ITERATIONS collection runs / worker  (= $((CONCURRENCY * ITERATIONS)) total)"
echo

start=$(date +%s)
pids=()
for w in $(seq 1 "$CONCURRENCY"); do
  "${NEWMAN[@]}" run "$COLLECTION" \
    -e "$ENVIRONMENT" \
    --env-var "baseUrl=$BASE_URL" \
    -n "$ITERATIONS" \
    --reporters cli --reporter-cli-no-banner \
    > "$OUT/worker-$w.log" 2>&1 &
  pids+=("$!")
done

fail=0
for i in "${!pids[@]}"; do
  wait "${pids[$i]}" || fail=$((fail + 1))
done
end=$(date +%s)

# Aggregate assertion totals from each worker's summary table.
total_assert=0
failed_assert=0
for f in "$OUT"/worker-*.log; do
  line=$(grep -E "assertions" "$f" | grep -E "│" | tail -1)
  exec_n=$(echo "$line" | grep -oE "[0-9]+" | sed -n '1p')
  fail_n=$(echo "$line" | grep -oE "[0-9]+" | sed -n '2p')
  total_assert=$((total_assert + ${exec_n:-0}))
  failed_assert=$((failed_assert + ${fail_n:-0}))
done

echo "=== Newman load loop result ==="
echo "  workers failed     : $fail / $CONCURRENCY"
echo "  assertions total   : $total_assert"
echo "  assertions failed  : $failed_assert"
echo "  wall-clock         : $((end - start))s"
echo "  per-worker logs    : $OUT"

if [ "$fail" -eq 0 ] && [ "$failed_assert" -eq 0 ]; then
  echo "RESULT: PASS"
  exit 0
fi
echo "RESULT: FAIL (inspect $OUT/worker-*.log)"
exit 1
