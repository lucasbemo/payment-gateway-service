# Load Testing

Two complementary load harnesses for the Payment Gateway. Use them to satisfy the
**performance / load** gate of the production-readiness checklist (e.g. after the
JDK 26 / Spring Boot 4 upgrade) and to catch latency or throughput regressions.

| Harness | Tool | Answers | Path |
|---|---|---|---|
| **Throughput / latency** | [k6](https://k6.io) | "How fast, at what concurrency, within budget?" | `k6/payment-load.js` |
| **Functional under load** | newman (Postman) | "Do all endpoints stay correct under sustained, concurrent load?" | `newman/newman-load.sh` |

Run both: k6 measures latency/throughput of the hot path (process + read payment),
while the newman loop replays the *whole* API collection concurrently and asserts
every response — together they cover speed **and** correctness under load.

---

## Prerequisites

1. **A running app + infra.** Start the stack first:
   ```bash
   make docker-up          # full stack, app on :8080
   # or run the app on the host (see CLAUDE.md) and point BASE_URL at it
   ```
2. **k6** — `brew install k6` (macOS) / `https://k6.io/docs/get-started/installation/`.
3. **newman** — used via `npx newman` (no install needed) or `npm i -g newman`.

> The default `BASE_URL` is `http://localhost:8080` (the docker-compose app). When
> testing a host-run app on another port, pass `BASE_URL=http://localhost:8082`.

---

## k6 — throughput & latency

```bash
# default: ramp to 20 VUs, hold 1m
make load-test-k6
# or directly, with tuning:
BASE_URL=http://localhost:8080 VUS=50 HOLD=2m P95_MS=600 \
  k6 run load-test/k6/payment-load.js
```

**What it does:** `setup()` registers + activates one merchant; then each virtual
user loops `POST /api/v1/payments` (unique `X-Idempotency-Key`) → `GET` the payment.

**Tunables (env):** `BASE_URL`, `VUS`, `RAMP_UP`, `HOLD`, `RAMP_DOWN`, `SLEEP`,
`P95_MS` (p95 latency budget, default 800), `ERROR_RATE` (max, default 0.01).

**Thresholds (the run fails if breached):**
- `http_req_failed   rate < 1%`
- `http_req_duration p95 < ${P95_MS}ms`
- `payment_errors    rate < 1%`
- `checks            rate > 99%`

**Reading the output:** check `http_req_duration` (avg/p95/max), `iterations` (≈
throughput), and that all `✓ thresholds` pass. `payment_processing_duration` is a
custom trend isolating the payment POST.

---

## newman — functional load loop

```bash
make load-test-newman
# or:
BASE_URL=http://localhost:8080 CONCURRENCY=8 ITERATIONS=25 \
  load-test/newman/newman-load.sh
```

**What it does:** launches `CONCURRENCY` parallel workers, each running the full
Postman collection `ITERATIONS` times (= `CONCURRENCY × ITERATIONS` total runs),
then aggregates assertion totals. Exits non-zero if any worker or assertion fails.

**Tunables (env):** `BASE_URL`, `CONCURRENCY`, `ITERATIONS`, `COLLECTION`, `ENVIRONMENT`.

---

## Suggested baseline (and regression gate)

Capture numbers on a known-good build, then compare after changes (e.g. before/after
a framework upgrade):

| Metric | Source | Example gate |
|---|---|---|
| p95 latency (process-payment) | k6 `http_req_duration{name:process-payment}` | within ±10% of baseline |
| Error rate | k6 `http_req_failed` | < 1% |
| Throughput (req/s) | k6 `http_reqs` / duration | ≥ baseline |
| Correctness under load | newman loop | 0 failed assertions |

> These are **local/dev** harnesses for smoke + regression load. A full
> production-readiness sign-off should also run an extended soak in a staging
> environment that mirrors prod sizing.
