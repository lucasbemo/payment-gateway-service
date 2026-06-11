#!/usr/bin/env bash
#
# Local "staging soak" — drive steady k6 traffic against the full Dockerized stack
# for a sustained window, sample stability metrics to CSV every SAMPLE_INTERVAL, then
# emit a PASS/FAIL report. Catches slow issues a short load test misses: memory creep,
# latency drift, connection-pool exhaustion, Kafka-lag / outbox-backlog growth, restarts.
#
# Run (app + stack must be up — `make docker-up`):
#   load-test/soak/soak.sh                 # ~1h soak (default)
#   DURATION=4h VUS=20 load-test/soak/soak.sh
#
# Env: DURATION (k6 hold, default 56m → ~1h with ramps), VUS, SAMPLE_INTERVAL (s),
#      BASE_URL, APP_CONTAINER, PG_CONTAINER, PROM_URL, OUTDIR.
set -uo pipefail
export LC_ALL=C   # force '.' as the decimal separator so printf never emits commas (breaks CSV)

ROOT="$(git rev-parse --show-toplevel)"; cd "$ROOT"

DURATION="${DURATION:-56m}"
RAMP_UP="${RAMP_UP:-2m}"
RAMP_DOWN="${RAMP_DOWN:-2m}"
VUS="${VUS:-10}"
SAMPLE_INTERVAL="${SAMPLE_INTERVAL:-60}"
BASE_URL="${BASE_URL:-http://localhost:8080}"
APP_CONTAINER="${APP_CONTAINER:-payment-gateway-service-app}"
PG_CONTAINER="${PG_CONTAINER:-payment-gateway-service-postgres}"
PROM_URL="${PROM_URL:-$BASE_URL/actuator/prometheus}"

TS="$(date +%Y%m%d-%H%M%S)"
OUTDIR="${OUTDIR:-$ROOT/load-test/soak/out}"; mkdir -p "$OUTDIR"
CSV="$OUTDIR/soak-metrics-$TS.csv"
REPORT="$OUTDIR/soak-report-$TS.md"
K6LOG="$OUTDIR/soak-k6-$TS.log"
RUNFLAG="$OUTDIR/.running-$TS"

command -v k6 >/dev/null 2>&1 || { echo "k6 not installed (brew install k6)"; exit 1; }
code=$(curl -s -o /dev/null -w '%{http_code}' --max-time 5 "$BASE_URL/actuator/health" || echo 000)
[ "$code" = "200" ] || { echo "App not healthy at $BASE_URL (got $code). Start it: make docker-up"; exit 1; }
START_RESTARTS=$(docker inspect -f '{{.RestartCount}}' "$APP_CONTAINER" 2>/dev/null || echo 0)

# Sum the values of all prometheus samples whose line matches a regex (ignores # comments).
metric() { curl -s --max-time 5 "$PROM_URL" | awk -v re="$1" '$0!~/^#/ && $0~re {s+=$NF} END{printf "%.0f", s+0}'; }

echo "ts,heap_used,heap_max,threads,gc_pause_sum,hikari_active,hikari_pending,kafka_lag,outbox_pending,cmem_pct,ccpu_pct" > "$CSV"

sampler() {
  while [ -f "$RUNFLAG" ]; do
    local snap; snap="$(curl -s --max-time 5 "$PROM_URL")"
    local heap_used heap_max threads gc hik_a hik_p lag outbox stats cmem ccpu
    heap_used=$(awk '$0!~/^#/ && /jvm_memory_used_bytes/ && /area="heap"/{s+=$NF} END{printf "%.0f",s+0}' <<<"$snap")
    heap_max=$(awk '$0!~/^#/ && /jvm_memory_max_bytes/ && /area="heap"/{if($NF>0)s+=$NF} END{printf "%.0f",s+0}' <<<"$snap")
    threads=$(awk '$0!~/^#/ && /jvm_threads_live_threads/{s+=$NF} END{printf "%.0f",s+0}' <<<"$snap")
    gc=$(awk '$0!~/^#/ && /jvm_gc_pause_seconds_sum/{s+=$NF} END{printf "%.3f",s+0}' <<<"$snap")
    hik_a=$(awk '$0!~/^#/ && /hikaricp_connections_active/{s+=$NF} END{printf "%.0f",s+0}' <<<"$snap")
    hik_p=$(awk '$0!~/^#/ && /hikaricp_connections_pending/{s+=$NF} END{printf "%.0f",s+0}' <<<"$snap")
    lag=$(awk '$0!~/^#/ && /kafka/ && /lag/{s+=$NF} END{printf "%.0f",s+0}' <<<"$snap")
    outbox=$(docker exec "$PG_CONTAINER" psql -U admin -d payment_gateway -tAc \
             "select count(*) from outbox_events where status='PENDING'" 2>/dev/null | tr -d ' ')
    stats=$(docker stats --no-stream --format '{{.MemPerc}} {{.CPUPerc}}' "$APP_CONTAINER" 2>/dev/null | tr -d '%')
    cmem=$(awk '{print $1}' <<<"$stats"); ccpu=$(awk '{print $2}' <<<"$stats")
    echo "$(date +%s),${heap_used},${heap_max},${threads},${gc},${hik_a},${hik_p},${lag},${outbox:-},${cmem:-},${ccpu:-}" >> "$CSV"
    sleep "$SAMPLE_INTERVAL"
  done
}

echo "=== Soak: VUS=$VUS hold=$DURATION sample=${SAMPLE_INTERVAL}s ==="
echo "  metrics CSV : $CSV"
echo "  k6 log      : $K6LOG"
touch "$RUNFLAG"; sampler & SAMPLER_PID=$!

BASE_URL="$BASE_URL" VUS="$VUS" RAMP_UP="$RAMP_UP" HOLD="$DURATION" RAMP_DOWN="$RAMP_DOWN" SLEEP=0.5 \
  k6 run "$ROOT/load-test/k6/payment-load.js" > "$K6LOG" 2>&1
K6_EXIT=$?

rm -f "$RUNFLAG"; wait "$SAMPLER_PID" 2>/dev/null
END_RESTARTS=$(docker inspect -f '{{.RestartCount}}' "$APP_CONTAINER" 2>/dev/null || echo 0)
# Count real problems only: OOM and ERROR-level log lines (the logback level token,
# not the substring "error" which appears in field names like errorMessage).
ERR_LOGS=$(docker logs "$APP_CONTAINER" 2>&1 | grep -cE "OutOfMemoryError| ERROR " || true)
OOM_HITS=$(docker logs "$APP_CONTAINER" 2>&1 | grep -cE "OutOfMemoryError" || true)

CSV="$CSV" K6LOG="$K6LOG" REPORT="$REPORT" K6_EXIT="$K6_EXIT" \
START_RESTARTS="$START_RESTARTS" END_RESTARTS="$END_RESTARTS" ERR_LOGS="$ERR_LOGS" OOM_HITS="$OOM_HITS" \
DURATION="$DURATION" VUS="$VUS" python3 "$ROOT/load-test/soak/analyze.py"
RC=$?
echo
cat "$REPORT"
exit "$RC"
