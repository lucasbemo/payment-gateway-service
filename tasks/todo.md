# Decision-resolution implementation — 2026-06-10

Decisions captured via interview; implementing on branch feat/product-decisions (based on PR #5).

## Group 1 — Business rules
- [ ] 1. Merchant gate: POST /merchants/{id}/activate (PENDING→ACTIVE, SUSPENDED→ACTIVE);
      ProcessPaymentService + ProcessRefundService reject non-ACTIVE merchants (clear 400).
      Update e2e tests + Postman flow to activate after registration.
- [ ] 2. Payment-method soft delete: V17 migration adds status to payment_methods;
      removal sets INACTIVE; reads filter ACTIVE.
- [ ] 3. StubPaymentProvider magic declines: amountInCents ending in 99 → DECLINED
      (insufficient_funds); deterministic, documented in the stub.
- [ ] 4. Failed payments persist + publish PAYMENT_FAILED: decline path saves status=FAILED
      with errorCode/Message and writes outbox event in the same committed transaction
      (PaymentDeclinedException with noRollbackFor), API returns 400 with error details.
- [ ] 5. Refund cents migration: V18 multiplies refunds.amount ×100; RefundMapper switches
      to cents convention (matches payments/transactions).

## Group 2 — Integrations
- [ ] 6. Reconciliation internal matching: load merchant+date transactions, matched =
      CAPTURED/SETTLED, discrepancies = FAILED/stuck PENDING; persist real counts/amounts.
- [ ] 7. Settlement report to MinIO: real report content (JSON), S3 upload to
      settlement-reports bucket (auto-create), return s3 path; path-style access for MinIO.
      SettlementReportDTO gains totalAmount/transactionCount.
- [ ] 8. Webhook delivery: payment.completed/failed/cancelled consumers POST event to
      merchant webhookUrl (3 retries, audit log result).

## Group 3 — API drift + collection
- [ ] 9. Add-payment-method response includes paymentMethodId; payment responses include
      transactionId; collection: activate step, separate cancel scenario, env vars
      (reconciliationDate via prerequest), align assertions. Newman target: 0 failures.

## Group 4 — Quality + ops (separate PRs/runs)
- [ ] 10. Spotless plugin + one-time apply + CI check; drop sonar Makefile target (own PR).
- [ ] 11. Prod-profile auth smoke: boot with production profile + secrets, verify 401
      unauthenticated / API-key accepted.
- [ ] 12. k6 load test: 50 VUs × 2min on POST /payments; report p95, rate-limiter behavior.

### Item 8 sub-plan (webhook delivery agent)
- [x] WebhookDeliveryPort (application/webhook/port/out) + WebhookDeliveryService adapter
      (infrastructure/webhook/adapter/out/delivery): merchant lookup via MerchantQueryPort,
      POST payload with X-Webhook-Event/X-Webhook-Id headers, 3 attempts, 1s/2s backoff.
- [x] Dedicated webhookRestTemplate bean (2s connect/read timeouts; e2e: 0.5s, 1 attempt).
- [x] AuditPort.logWebhookDelivery + AuditAdapter/AuditLogger implementations.
- [x] Wire PaymentEventListeners (completed/failed/cancelled) + RefundEventListeners
      (refund.processed); delivery failures never break consumption.
- [x] WebhookDeliveryServiceTest (11 tests) + updated PaymentEventListenersTest (10 tests);
      compile + WebhookDeliveryServiceTest/KafkaEventPropagationE2ETest/HexagonalArchitectureTest
      all green (JDK 21).

## Review

### Status (verified)
- **Items 1–9 + 12: DONE and verified.** Items 10–11 (Group 4: Spotless/CI, prod-auth
  smoke) intentionally deferred to separate PRs.

### Implemented this pass (resume + Newman-driven hardening)
- **Item 9 finished.** Postman collection: added Activate Merchant (ends section 1 so the
  merchant is ACTIVE before payments), a standalone Cancel Payment scenario, and a Void
  Transaction scenario backed by its own freshly-authorized payment. Fixed stale assertions:
  Add Payment Method now extracts `data.paymentMethodId`; Capture Payment extracts
  `data.transactionId`; Update Merchant uses a unique `{{$guid}}` email; Cancel Refund
  asserts the real terminal-state 400; Reconcile asserts `discrepancyCount`.
- **Surfaced fields that item 9 assumed but were never implemented:**
  - `transactionId` on payment responses — added via a read-side lookup
    (`TransactionQueryPort.findLatestByPaymentId`) wired into Process/Capture/Get payment
    responses + REST DTO/mapper. No schema/domain mutation.
  - `paymentMethodId` on the add-payment-method response (`CustomerResponse.paymentMethodId`,
    set in `AddPaymentMethodService`).
- **Real defect fixed (item 6):** reconciliation unique constraint was `UNIQUE(batch_date)`
  — only one batch per calendar date system-wide, so a second merchant/date re-run 500'd.
  Migration `V19__reconciliation_unique_per_merchant_date.sql` changes it to
  `UNIQUE(merchant_id, batch_date)`.
- **Infra (Kafka wiring):** the app container crashed on boot (`dev` profile + Kafka not
  yet resolvable). Added a Kafka healthcheck and made the app `depends_on` Kafka
  `service_healthy`; the full stack now boots with health `UP` (kafka connected).

### Verification
- `./mvnw clean test-compile` clean (JDK 21).
- Non-e2e suite: **1079 tests, 0 failures**. Key e2e (Payment/Transaction/Merchant/Refund),
  one class per JVM: **43 tests, 0 failures** (2–3 pre-existing `@Disabled` skips).
- Full stack via docker-compose: app health **200** (`kafka: UP`).
- Newman (full collection vs live stack): **30 requests, 81 assertions, 0 failures.**
