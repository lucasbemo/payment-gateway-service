# Lessons

## 2026-06-09 — Fix findings, don't just report them
- Correction: user asked "why did you not fix the kafka issue?" after I reported the
  unwired event publishing as an open finding needing a decision.
- Rule: in this project, when validation uncovers a broken capability and the codebase
  already embodies the intended design (here: full outbox infra existed unused), implement
  the fix along that design instead of deferring for an architecture decision. Reserve
  "report only" for cases where the design intent is genuinely ambiguous.

## 2026-06-09 — Verify build exit codes directly, not through pipes
- `./mvnw compile | tail` made `$?` report tail's exit, hiding a compile failure.
- `docker-compose up -d --build` kept the old image running when the build failed and
  printed nothing alarming; the stale image masqueraded as "change had no effect".
- Rule: capture build exit codes explicitly (`cmd > log; echo $?`) and after any container
  rebuild, confirm the image ID actually changed before testing behavior.

## 2026-06-09 — Latent bugs hide behind dead code paths
- The outbox `payload jsonb` column mapping was broken (Hibernate bound varchar), but it
  never surfaced because nothing wrote outbox rows. Wiring the first writer exposed it.
- Rule: when activating a previously-unused code path, expect and test for latent bugs in
  that path (schema mismatches, serialization), not just the new code.

## 2026-06-10 — "Response includes X" isn't done until the DTO actually has X
- A prior session's todo marked "add-payment-method response includes paymentMethodId" and
  "payment responses include transactionId" as done; an exploration summary agreed. Both
  were false — the field existed only in the Postman collection's variable, not in
  `CustomerResponse`/`PaymentResponse`. The collection silently captured `pm.id` /
  `json.data.id` and carried an empty/wrong value, so downstream requests 500'd.
- Rule: when a task says a response exposes a field, grep the response DTO for that field
  before trusting it. A collection referencing `data.X` proves nothing about the server.

## 2026-06-10 — Full end-to-end (Newman) surfaces latent bugs unit+e2e miss
- All unit + e2e were green, yet a full Postman/Newman pass against the live stack found 16
  failures: a `UNIQUE(batch_date)` reconciliation constraint (only one batch per date
  system-wide), refunds that complete synchronously (so "cancel refund" is always a 400),
  captured transactions that can't be voided, and a docker Kafka-wiring boot failure.
- Rule: for "make the collection pass" tasks, run the real collection against the real
  stack early — per-endpoint unit/e2e coverage hides cross-request ordering, idempotency,
  unique-constraint, and infra-wiring defects. Classify each failure (env vs collection vs
  real bug) before fixing.
