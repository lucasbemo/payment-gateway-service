# Webhooks Documentation

Webhooks allow you to receive real-time notifications about payment events in your system.

---

## Overview

When events occur in the Payment Gateway (e.g., payment completed, refund processed), we send HTTP POST requests to your configured webhook URL.

### Configuration

Set your webhook URL when registering a merchant or update it later:

```bash
curl -X PUT https://api.payment-gateway.com/api/v1/merchants/merch_abc123 \
  -H "Content-Type: application/json" \
  -H "X-API-Key: YOUR_API_KEY" \
  -d '{
    "webhookUrl": "https://your-domain.com/webhooks/payment-gateway"
  }'
```

---

## Event Types

| Event | Description | Triggered When |
|-------|-------------|----------------|
| `payment.created` | Payment authorized | New payment is authorized |
| `payment.completed` | Payment captured | Authorized payment is captured |
| `payment.failed` | Payment failed | Payment processing fails |
| `payment.cancelled` | Payment cancelled | Authorized payment is cancelled |
| `refund.processed` | Refund completed | Refund is successfully processed |
| `refund.failed` | Refund failed | Refund processing fails |

---

## Webhook Payload

### Payment Events

#### payment.created

```json
{
  "eventId": "evt_abc123def456",
  "eventType": "payment.created",
  "eventVersion": "1.0",
  "createdAt": "2026-03-20T10:30:00Z",
  "data": {
    "paymentId": "pay_xyz789",
    "merchantId": "merch_abc123",
    "customerId": "cust_123",
    "amountInCents": 10000,
    "currency": "USD",
    "status": "AUTHORIZED",
    "description": "Order #12345"
  }
}
```

#### payment.completed

```json
{
  "eventId": "evt_def456ghi789",
  "eventType": "payment.completed",
  "eventVersion": "1.0",
  "createdAt": "2026-03-20T12:00:00Z",
  "data": {
    "paymentId": "pay_xyz789",
    "merchantId": "merch_abc123",
    "customerId": "cust_123",
    "amountInCents": 10000,
    "currency": "USD",
    "status": "CAPTURED",
    "capturedAt": "2026-03-20T12:00:00Z"
  }
}
```

#### payment.failed

```json
{
  "eventId": "evt_ghi789jkl012",
  "eventType": "payment.failed",
  "eventVersion": "1.0",
  "createdAt": "2026-03-20T10:30:00Z",
  "data": {
    "paymentId": "pay_xyz789",
    "merchantId": "merch_abc123",
    "customerId": "cust_123",
    "amountInCents": 10000,
    "currency": "USD",
    "status": "FAILED",
    "errorCode": "INSUFFICIENT_FUNDS",
    "errorMessage": "Card has insufficient funds"
  }
}
```

#### payment.cancelled

```json
{
  "eventId": "evt_jkl012mno345",
  "eventType": "payment.cancelled",
  "eventVersion": "1.0",
  "createdAt": "2026-03-20T11:00:00Z",
  "data": {
    "paymentId": "pay_xyz789",
    "merchantId": "merch_abc123",
    "customerId": "cust_123",
    "amountInCents": 10000,
    "currency": "USD",
    "status": "CANCELLED",
    "cancelledAt": "2026-03-20T11:00:00Z"
  }
}
```

### Refund Events

#### refund.processed

```json
{
  "eventId": "evt_mno345pqr678",
  "eventType": "refund.processed",
  "eventVersion": "1.0",
  "createdAt": "2026-03-20T14:00:00Z",
  "data": {
    "refundId": "ref_abc123",
    "paymentId": "pay_xyz789",
    "merchantId": "merch_abc123",
    "amount": 5000,
    "currency": "USD",
    "status": "COMPLETED",
    "type": "PARTIAL",
    "reason": "Customer requested"
  }
}
```

#### refund.failed

```json
{
  "eventId": "evt_pqr678stu901",
  "eventType": "refund.failed",
  "eventVersion": "1.0",
  "createdAt": "2026-03-20T14:00:00Z",
  "data": {
    "refundId": "ref_abc123",
    "paymentId": "pay_xyz789",
    "merchantId": "merch_abc123",
    "amount": 5000,
    "currency": "USD",
    "status": "FAILED",
    "errorCode": "REFUND_LIMIT_EXCEEDED",
    "errorMessage": "Refund amount exceeds remaining balance"
  }
}
```

---

## Signature Verification

Verify that webhooks are genuinely from Payment Gateway using HMAC-SHA256 signatures.

### Headers

```http
X-Webhook-Signature: sha256=abc123def456...
X-Webhook-Timestamp: 1710903600
X-Webhook-Event: payment.created
```

### Verification (Node.js)

```javascript
const crypto = require('crypto');

function verifyWebhook(payload, signature, secret) {
  const expectedSignature = 'sha256=' + 
    crypto
      .createHmac('sha256', secret)
      .update(JSON.stringify(payload))
      .digest('hex');
  
  return crypto.timingSafeEqual(
    Buffer.from(signature),
    Buffer.from(expectedSignature)
  );
}

// Usage
app.post('/webhooks/payment-gateway', (req, res) => {
  const signature = req.headers['x-webhook-signature'];
  const secret = 'YOUR_WEBHOOK_SECRET';
  
  if (!verifyWebhook(req.body, signature, secret)) {
    return res.status(401).send('Invalid signature');
  }
  
  // Process webhook
  console.log('Event:', req.body.eventType);
  console.log('Data:', req.body.data);
  
  res.status(200).send('OK');
});
```

### Verification (Python)

```python
import hmac
import hashlib
import json

def verify_webhook(payload: dict, signature: str, secret: str) -> bool:
    expected = 'sha256=' + hmac.new(
        secret.encode(),
        json.dumps(payload).encode(),
        hashlib.sha256
    ).hexdigest()
    
    return hmac.compare_digest(signature, expected)

# Usage (Flask)
@app.route('/webhooks/payment-gateway', methods=['POST'])
def handle_webhook():
    signature = request.headers.get('X-Webhook-Signature')
    secret = 'YOUR_WEBHOOK_SECRET'
    
    if not verify_webhook(request.json, signature, secret):
        return 'Invalid signature', 401
    
    event = request.json
    print(f"Event: {event['eventType']}")
    
    return 'OK', 200
```

---

## Retry Policy

If your endpoint doesn't respond with a 2xx status code, we'll retry:

| Attempt | Delay After Previous |
|---------|---------------------|
| 1 | Immediate |
| 2 | 1 minute |
| 3 | 5 minutes |
| 4 | 15 minutes |
| 5 | 1 hour |
| 6 | 6 hours |

After 6 failed attempts (over ~24 hours), the webhook is marked as failed and logged.

---

## Best Practices

### 1. Respond Quickly

Return a 200 response immediately, then process the event asynchronously.

```javascript
app.post('/webhooks/payment-gateway', async (req, res) => {
  // Respond immediately
  res.status(200).send('OK');
  
  // Process in background
  await processWebhook(req.body);
});
```

### 2. Make Processing Idempotent

Webhooks may be delivered more than once. Use `eventId` to deduplicate.

```javascript
const processedEvents = new Set();

async function processWebhook(event) {
  if (processedEvents.has(event.eventId)) {
    console.log('Duplicate event, skipping');
    return;
  }
  
  // Process event
  await updateOrderStatus(event.data);
  
  // Mark as processed
  processedEvents.add(event.eventId);
}
```

### 3. Verify Signatures

Always verify the webhook signature to prevent fraudulent requests.

### 4. Handle Event Versions

Events include `eventVersion`. Handle version changes gracefully.

```javascript
function handleEvent(event) {
  switch (event.eventVersion) {
    case '1.0':
      return handleV1(event);
    default:
      console.warn('Unknown version:', event.eventVersion);
  }
}
```

---

## Testing Webhooks

### Using Webhook.site

1. Go to https://webhook.site
2. Copy your unique URL
3. Configure it as your merchant's `webhookUrl`
4. Trigger events and see them arrive in real-time

### Local Testing with ngrok

```bash
# Start your local server
npm start

# Expose it via ngrok
ngrok http 3000

# Use the ngrok URL as your webhookUrl
# e.g., https://abc123.ngrok.io/webhooks/payment-gateway
```

### Test Endpoint

```javascript
// Express.js test endpoint
app.post('/webhooks/payment-gateway', (req, res) => {
  console.log('=== Webhook Received ===');
  console.log('Event:', req.body.eventType);
  console.log('ID:', req.body.eventId);
  console.log('Data:', JSON.stringify(req.body.data, null, 2));
  
  res.status(200).send('OK');
});
```

---

## Event Ordering

Events are delivered in order for the same entity (payment, refund), but events for different entities may arrive out of order.

Example sequence for a payment:
1. `payment.created` (status: AUTHORIZED)
2. `payment.completed` (status: CAPTURED)

---

## Security

- **HTTPS required:** Webhook URLs must use HTTPS
- **Signature verification:** Always verify `X-Webhook-Signature`
- **IP allowlisting:** Contact support for webhook source IPs

---

## Troubleshooting

### Webhooks not arriving

1. Verify your webhook URL is accessible from the internet
2. Check your server returns a 200 status code
3. Ensure HTTPS certificate is valid
4. Check firewall rules

### Duplicate events

This is expected behavior. Make your processing idempotent using `eventId`.

### Signature verification failing

1. Ensure you're using the raw request body (not parsed JSON)
2. Verify your webhook secret is correct
3. Check the signature algorithm is HMAC-SHA256

---

## Support

For webhook issues, contact support with:
- Merchant ID
- Event ID (if available)
- Timestamp of expected webhook
- Your endpoint URL