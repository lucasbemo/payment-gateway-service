#!/usr/bin/env python3
"""Analyze a soak run (CSV metric samples + k6 summary) and write a PASS/FAIL report.

Invoked by soak.sh with env: CSV, K6LOG, REPORT, K6_EXIT, START_RESTARTS,
END_RESTARTS, ERR_LOGS, DURATION, VUS. Exit 0 = PASS, 1 = FAIL.
"""
import csv
import os
import re
import statistics as st

CSV = os.environ["CSV"]
K6LOG = os.environ["K6LOG"]
REPORT = os.environ["REPORT"]


def num(x):
    try:
        return float(x)
    except (TypeError, ValueError):
        return None


def col(rows, name):
    return [num(r.get(name)) for r in rows if num(r.get(name)) is not None]


def thirds(values):
    if len(values) < 3:
        return (None, None)
    n = len(values) // 3
    return (st.mean(values[:n]), st.mean(values[-n:]))


rows = []
with open(CSV) as f:
    rows = list(csv.DictReader(f))

checks = []  # (signal, passed, detail)

# --- container restarts ---
sr, er = int(os.environ.get("START_RESTARTS", 0)), int(os.environ.get("END_RESTARTS", 0))
checks.append(("App container restarts", er == sr, f"start={sr} end={er}"))

# --- JVM heap: no monotonic growth post-warmup, stays under max ---
heap = col(rows, "heap_used")
hmax = max(col(rows, "heap_max") or [0]) or None
if heap:
    first, last = thirds(heap)
    peak = max(heap)
    grew = (first and last and last > first * 1.2)
    over = (hmax and peak > 0.95 * hmax)
    ok = not grew and not over
    fmt = lambda b: f"{b/1024/1024:.0f}MB" if b else "n/a"
    detail = f"first1/3={fmt(first)} last1/3={fmt(last)} peak={fmt(peak)} max={fmt(hmax)}"
    checks.append(("JVM heap stable (no creep)", ok, detail))
else:
    checks.append(("JVM heap stable (no creep)", False, "no heap samples"))

# --- Hikari pending connections ~0 (no pool exhaustion) ---
hp = col(rows, "hikari_pending")
if hp:
    checks.append(("Hikari pending ~0", max(hp) <= 5, f"max_pending={max(hp):.0f}"))

# --- Kafka consumer lag bounded ---
lag = col(rows, "kafka_lag")
if lag:
    f3, l3 = thirds(lag)
    bounded = (max(lag) < 10000) and not (f3 is not None and l3 is not None and l3 > f3 + 5000)
    checks.append(("Kafka lag bounded", bounded, f"max_lag={max(lag):.0f} first1/3={f3} last1/3={l3}"))

# --- Outbox PENDING backlog bounded (poller keeps up) ---
ob = col(rows, "outbox_pending")
if ob:
    f3, l3 = thirds(ob)
    bounded = (max(ob) < 5000) and not (f3 is not None and l3 is not None and l3 > f3 + 2000)
    checks.append(("Outbox backlog bounded", bounded, f"max_pending={max(ob):.0f} first1/3={f3} last1/3={l3}"))

# --- k6: error rate + thresholds ---
k6 = open(K6LOG).read() if os.path.exists(K6LOG) else ""
m_err = re.search(r"http_req_failed.*?([\d.]+)%", k6)
err_rate = float(m_err.group(1)) if m_err else None
m_p95 = re.search(r"http_req_duration.*?p\(95\)=([\d.]+)(ms|s)", k6)
p95 = (float(m_p95.group(1)) * (1000 if m_p95.group(2) == "s" else 1)) if m_p95 else None
m_chk = re.search(r"checks_succeeded.*?([\d.]+)%", k6) or re.search(r"checks.*?([\d.]+)%", k6)
checks_pct = float(m_chk.group(1)) if m_chk else None
k6_exit = int(os.environ.get("K6_EXIT", 1))
checks.append(("k6 error rate < 1%", err_rate is not None and err_rate < 1.0, f"http_req_failed={err_rate}%"))
checks.append(("k6 thresholds passed", k6_exit == 0, f"exit={k6_exit} p95={p95}ms checks={checks_pct}%"))

# --- logs: no OOM (hard); ERROR-level count is informational ---
err_logs = int(os.environ.get("ERR_LOGS", 0))
oom_hits = int(os.environ.get("OOM_HITS", 0))
checks.append(("No OutOfMemoryError", oom_hits == 0, f"OOM={oom_hits}, ERROR-level logs={err_logs}"))

overall = all(c[1] for c in checks)

lines = []
lines.append(f"# Soak Report — {'PASS ✅' if overall else 'FAIL ❌'}")
lines.append("")
lines.append(f"- Duration (k6 hold): `{os.environ.get('DURATION')}`  ·  VUs: `{os.environ.get('VUS')}`")
lines.append(f"- Samples: {len(rows)}  ·  Metrics CSV: `{CSV}`  ·  k6 log: `{K6LOG}`")
lines.append("")
lines.append("| Signal | Result | Detail |")
lines.append("|---|---|---|")
for sig, ok, detail in checks:
    lines.append(f"| {sig} | {'✅ PASS' if ok else '❌ FAIL'} | {detail} |")
lines.append("")
lines.append(f"**Overall: {'PASS — no instability observed over the soak window.' if overall else 'FAIL — see failing signal(s) above.'}**")
lines.append("")
lines.append("> A local ~1h soak is a smoke-soak: strong evidence of no gross regression, "
             "weaker for very slow leaks. For production sign-off run `DURATION=8h`.")
open(REPORT, "w").write("\n".join(lines) + "\n")
raise SystemExit(0 if overall else 1)
