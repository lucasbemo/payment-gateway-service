// k6 load test for the Payment Gateway — throughput & latency under load.
//
// Exercises the core money path: register+activate a merchant once (setup),
// then each virtual user repeatedly processes a payment (unique idempotency key)
// and reads it back. Fails the run if latency/error thresholds are breached.
//
// Run:
//   k6 run load-test/k6/payment-load.js
//   BASE_URL=http://localhost:8080 VUS=50 HOLD=2m k6 run load-test/k6/payment-load.js
//
// Tunables (env): BASE_URL, VUS, RAMP_UP, HOLD, RAMP_DOWN, SLEEP,
//                 P95_MS (latency threshold), ERROR_RATE (max error rate).

import http from "k6/http";
import { check, sleep } from "k6";
import { Trend, Rate } from "k6/metrics";
import { uuidv4 } from "https://jslib.k6.io/k6-utils/1.4.0/index.js";

const BASE_URL = __ENV.BASE_URL || "http://localhost:8080";
const VUS = Number(__ENV.VUS || 20);

const paymentDuration = new Trend("payment_processing_duration", true);
const paymentErrors = new Rate("payment_errors");

export const options = {
  scenarios: {
    payments: {
      executor: "ramping-vus",
      startVUs: 0,
      stages: [
        { duration: __ENV.RAMP_UP || "30s", target: VUS },
        { duration: __ENV.HOLD || "1m", target: VUS },
        { duration: __ENV.RAMP_DOWN || "15s", target: 0 },
      ],
      gracefulRampDown: "10s",
    },
  },
  thresholds: {
    http_req_failed: [`rate<${__ENV.ERROR_RATE || 0.01}`], // <1% transport errors
    http_req_duration: [`p(95)<${__ENV.P95_MS || 800}`], // p95 latency budget
    payment_errors: [`rate<${__ENV.ERROR_RATE || 0.01}`],
    checks: ["rate>0.99"], // >99% of assertions pass
  },
};

const JSON_HEADERS = { "Content-Type": "application/json" };

// Create + activate a merchant once; shared by all VUs via the returned data.
export function setup() {
  const reg = http.post(
    `${BASE_URL}/api/v1/merchants`,
    JSON.stringify({
      name: "k6 Load Merchant",
      email: `k6-${uuidv4()}@example.com`,
      webhookUrl: "https://webhook.site/k6-load",
    }),
    { headers: JSON_HEADERS },
  );
  check(reg, { "setup: merchant registered": (r) => r.status === 200 || r.status === 201 });
  const merchantId = reg.json("data.id");

  const act = http.post(`${BASE_URL}/api/v1/merchants/${merchantId}/activate`, null, {
    headers: JSON_HEADERS,
  });
  check(act, { "setup: merchant activated": (r) => r.status === 200 });

  return { merchantId };
}

export default function (data) {
  const idem = uuidv4();
  const res = http.post(
    `${BASE_URL}/api/v1/payments`,
    JSON.stringify({
      merchantId: data.merchantId,
      amountInCents: 10000,
      currency: "USD",
      idempotencyKey: idem,
      description: "k6 load payment",
    }),
    { headers: { ...JSON_HEADERS, "X-Idempotency-Key": idem }, tags: { name: "process-payment" } },
  );
  paymentDuration.add(res.timings.duration);
  const ok = check(res, {
    "payment: status 200": (r) => r.status === 200,
    "payment: success=true": (r) => r.json("success") === true,
    "payment: AUTHORIZED": (r) => r.json("data.status") === "AUTHORIZED",
  });
  paymentErrors.add(!ok);

  if (ok) {
    const paymentId = res.json("data.id");
    const get = http.get(
      `${BASE_URL}/api/v1/payments/${paymentId}?merchantId=${data.merchantId}`,
      { tags: { name: "get-payment" } },
    );
    check(get, { "get-payment: status 200": (r) => r.status === 200 });
  }

  sleep(Number(__ENV.SLEEP || 0.5));
}
