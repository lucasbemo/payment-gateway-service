// k6 load test: payment processing under concurrency.
// Run: docker run --rm --network host -v $PWD/scripts/load:/scripts grafana/k6 run /scripts/payments-load.js
// Observes: p95 latency, error rate, and (via Grafana/Prometheus) rate-limiter and bulkhead behavior.
import http from 'k6/http';
import { check, sleep } from 'k6';
import { uuidv4 } from 'https://jslib.k6.io/k6-utils/1.4.0/index.js';

const BASE = __ENV.BASE_URL || 'http://localhost:8080';

export const options = {
  stages: [
    { duration: '20s', target: 10 },
    { duration: '90s', target: 50 },
    { duration: '10s', target: 0 },
  ],
  thresholds: {
    http_req_duration: ['p(95)<2000'], // matches the HighPaymentLatency alert threshold
    http_req_failed: ['rate<0.05'],    // matches the HighPaymentErrorRate alert threshold
  },
};

export function setup() {
  const headers = { 'Content-Type': 'application/json' };
  const m = http.post(`${BASE}/api/v1/merchants`, JSON.stringify({
    name: 'k6 Load Merchant',
    email: `k6-${uuidv4()}@test.com`,
  }), { headers });
  const merchantId = m.json('data.id');
  http.post(`${BASE}/api/v1/merchants/${merchantId}/activate`, null, { headers });
  const c = http.post(`${BASE}/api/v1/customers`, JSON.stringify({
    merchantId, email: `k6c-${uuidv4()}@test.com`, name: 'k6 Customer',
  }), { headers });
  return { merchantId, customerId: c.json('data.id') };
}

export default function (data) {
  const res = http.post(`${BASE}/api/v1/payments`, JSON.stringify({
    merchantId: data.merchantId,
    // avoid amounts ending in 99: the stub provider declines those by design
    amountInCents: 1000 + (Math.floor(Math.random() * 80) * 10),
    currency: 'USD',
    customerId: data.customerId,
    description: 'k6 load payment',
  }), {
    headers: {
      'Content-Type': 'application/json',
      'X-Idempotency-Key': uuidv4(),
    },
  });
  check(res, {
    'status 200': (r) => r.status === 200,
    'authorized': (r) => r.json('data.status') === 'AUTHORIZED',
  });
  sleep(0.1);
}
