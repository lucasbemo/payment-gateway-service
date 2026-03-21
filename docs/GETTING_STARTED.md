# Getting Started with Payment Gateway API

This guide will help you make your first API calls and integrate the Payment Gateway into your application.

---

## Prerequisites

- A merchant account (register via API or contact support)
- API credentials (`apiKey` and `apiSecret`)
- An HTTP client (curl, Postman, or any programming language)

---

## Quick Start

### Step 1: Register a Merchant

First, create a merchant account to receive your API credentials.

```bash
curl -X POST https://api.payment-gateway.com/api/v1/merchants \
  -H "Content-Type: application/json" \
  -d '{
    "name": "My Store",
    "email": "store@example.com",
    "webhookUrl": "https://mystore.com/webhooks"
  }'
```

**Response:**
```json
{
  "success": true,
  "message": "Merchant registered successfully",
  "data": {
    "id": "merch_abc123",
    "name": "My Store",
    "email": "store@example.com",
    "status": "ACTIVE",
    "apiKey": "pk_live_abc123...",
    "apiSecret": "sk_live_xyz789...",
    "createdAt": "2026-03-20T10:00:00Z"
  }
}
```

> ⚠️ **Important:** Save your `apiKey` and `apiSecret` securely. The `apiSecret` is only shown once!

---

### Step 2: Register a Customer

Create a customer to associate with payments.

```bash
curl -X POST https://api.payment-gateway.com/api/v1/customers \
  -H "Content-Type: application/json" \
  -H "X-API-Key: YOUR_API_KEY" \
  -d '{
    "merchantId": "merch_abc123",
    "email": "customer@example.com",
    "name": "John Doe",
    "phone": "+1234567890"
  }'
```

**Response:**
```json
{
  "success": true,
  "message": "Customer registered successfully",
  "data": {
    "id": "cust_abc123",
    "merchantId": "merch_abc123",
    "email": "customer@example.com",
    "name": "John Doe",
    "status": "ACTIVE"
  }
}
```

---

### Step 3: Add a Payment Method

Add a card to the customer's profile.

```bash
curl -X POST https://api.payment-gateway.com/api/v1/customers/cust_abc123/payment-methods \
  -H "Content-Type: application/json" \
  -H "X-API-Key: YOUR_API_KEY" \
  -d '{
    "merchantId": "merch_abc123",
    "cardNumber": "4111111111111111",
    "cardExpiryMonth": "12",
    "cardExpiryYear": "2028",
    "cardCvv": "123",
    "cardholderName": "John Doe",
    "isDefault": true
  }'
```

> 🧪 **Test Cards:** Use `4111111111111111` (Visa) or `5500000000000004` (Mastercard) for testing.

---

### Step 4: Process a Payment

Now you can charge the customer.

```bash
curl -X POST https://api.payment-gateway.com/api/v1/payments \
  -H "Content-Type: application/json" \
  -H "X-API-Key: YOUR_API_KEY" \
  -H "X-Idempotency-Key: 550e8400-e29b-41d4-a716-446655440000" \
  -d '{
    "merchantId": "merch_abc123",
    "amountInCents": 10000,
    "currency": "USD",
    "customerId": "cust_abc123",
    "description": "Order #12345"
  }'
```

**Response:**
```json
{
  "success": true,
  "message": "Payment processed successfully",
  "data": {
    "id": "pay_xyz789",
    "merchantId": "merch_abc123",
    "customerId": "cust_abc123",
    "amountInCents": 10000,
    "currency": "USD",
    "status": "AUTHORIZED",
    "description": "Order #12345",
    "createdAt": "2026-03-20T10:30:00Z"
  }
}
```

---

### Step 5: Capture the Payment

Once you're ready to collect the funds, capture the authorized payment.

```bash
curl -X POST "https://api.payment-gateway.com/api/v1/payments/pay_xyz789/capture?merchantId=merch_abc123" \
  -H "X-API-Key: YOUR_API_KEY"
```

**Response:**
```json
{
  "success": true,
  "message": "Payment captured successfully",
  "data": {
    "id": "pay_xyz789",
    "status": "CAPTURED",
    "capturedAt": "2026-03-20T12:00:00Z"
  }
}
```

---

## Complete Payment Flow

```
┌──────────────────┐     ┌──────────────────┐     ┌──────────────────┐
│  Register        │────▶│  Add Payment     │────▶│  Process         │
│  Customer        │     │  Method          │     │  Payment         │
└──────────────────┘     └──────────────────┘     └──────────────────┘
                                                          │
                                                          ▼
                                                 ┌──────────────────┐
                                                 │  Capture or      │
                                                 │  Cancel          │
                                                 └──────────────────┘
```

---

## Test Card Numbers

| Card Type | Number | Result |
|-----------|--------|--------|
| Visa | `4111111111111111` | Success |
| Mastercard | `5500000000000004` | Success |
| Amex | `340000000000009` | Success |
| Visa (Decline) | `4000000000000002` | Decline |
| Visa (Insufficient) | `4000000000009995` | Insufficient funds |

---

## Using Postman

1. Import the collection: `postman/Payment Gateway.postman_collection.json`
2. Import an environment: `postman/Payment Gateway Local.postman_environment.json`
3. Set your `apiKey` in the environment variables
4. Run requests in order from the "Complete Flow" folder

---

## SDK Examples

### JavaScript/TypeScript

```javascript
const axios = require('axios');

const client = axios.create({
  baseURL: 'https://api.payment-gateway.com/api/v1',
  headers: {
    'X-API-Key': 'YOUR_API_KEY',
    'Content-Type': 'application/json'
  }
});

async function checkout(customerId, amount, description) {
  const idempotencyKey = crypto.randomUUID();
  
  // Process payment
  const payment = await client.post('/payments', {
    merchantId: 'merch_abc123',
    amountInCents: amount,
    currency: 'USD',
    customerId,
    description
  }, {
    headers: { 'X-Idempotency-Key': idempotencyKey }
  });

  // Capture immediately for two-step flow
  if (payment.data.data.status === 'AUTHORIZED') {
    await client.post(`/payments/${payment.data.data.id}/capture`, null, {
      params: { merchantId: 'merch_abc123' }
    });
  }

  return payment.data;
}
```

### Python

```python
import requests
import uuid

BASE_URL = 'https://api.payment-gateway.com/api/v1'
API_KEY = 'YOUR_API_KEY'

headers = {
    'X-API-Key': API_KEY,
    'Content-Type': 'application/json'
}

def process_payment(merchant_id, customer_id, amount, description):
    idempotency_key = str(uuid.uuid4())
    headers['X-Idempotency-Key'] = idempotency_key
    
    response = requests.post(
        f'{BASE_URL}/payments',
        json={
            'merchantId': merchant_id,
            'amountInCents': amount,
            'currency': 'USD',
            'customerId': customer_id,
            'description': description
        },
        headers=headers
    )
    
    return response.json()
```

### Java (Spring Boot)

```java
@RestController
public class CheckoutController {
    
    private final RestTemplate restTemplate;
    
    @Value("${payment-gateway.api-key}")
    private String apiKey;
    
    public PaymentResponse processPayment(String customerId, Long amount, String description) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-API-Key", apiKey);
        headers.set("X-Idempotency-Key", UUID.randomUUID().toString());
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        Map<String, Object> body = Map.of(
            "merchantId", "merch_abc123",
            "amountInCents", amount,
            "currency", "USD",
            "customerId", customerId,
            "description", description
        );
        
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        
        ResponseEntity<ApiResponse> response = restTemplate.postForEntity(
            "https://api.payment-gateway.com/api/v1/payments",
            request,
            ApiResponse.class
        );
        
        return response.getBody();
    }
}
```

---

## Common Patterns

### Two-Step Authorization

Best for e-commerce where you authorize at checkout and capture on shipment.

```javascript
// 1. Authorize at checkout
const auth = await client.post('/payments', { ...paymentData });

// 2. Later, when order ships
await client.post(`/payments/${auth.data.id}/capture`);
```

### One-Step Payment

For immediate payment capture.

```javascript
// Set capture=true in metadata or capture immediately after authorization
const payment = await client.post('/payments', paymentData);
await client.post(`/payments/${payment.data.id}/capture`);
```

### Refund Processing

```javascript
const refund = await client.post('/refunds', {
  paymentId: 'pay_xyz789',
  merchantId: 'merch_abc123',
  amount: 5000,  // Partial refund
  reason: 'Customer requested'
});
```

---

## Next Steps

- Read the full [API Documentation](./API_DOCUMENTATION.md)
- Learn about [Webhooks](./WEBHOOKS.md) for real-time notifications
- Check [Error Codes](./ERROR_CODES.md) for error handling

---

## Support

- **Issues:** https://github.com/anomalyco/payment-gateway-service/issues
- **Swagger UI:** https://api.payment-gateway.com/swagger-ui.html