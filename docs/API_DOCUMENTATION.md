# Payment Gateway API Documentation

## Overview

- **API Version:** 1.0.0
- **Base URL:** `https://api.payment-gateway.com/api/v1`
- **Content-Type:** `application/json`
- **Swagger UI:** `/swagger-ui.html`
- **OpenAPI Spec:** `/v3/api-docs`

---

## Authentication

### API Key Authentication

Use the API key obtained during merchant registration.

```http
X-API-Key: pk_live_abc123def456...
```

### JWT Bearer Authentication

For admin and developer roles, use JWT tokens.

```http
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

---

## Rate Limits

| Endpoint Category | Rate Limit | Window |
|-------------------|------------|--------|
| Health & Monitoring | 60 requests | 1 minute |
| Merchant Management | 100 requests | 1 minute |
| Customer Management | 100 requests | 1 minute |
| Payment Processing | 50 requests | 1 minute |
| Transaction Management | 100 requests | 1 minute |
| Refund Processing | 50 requests | 1 minute |
| Reconciliation | 30 requests | 1 minute |

### Rate Limit Headers

```http
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 95
X-RateLimit-Reset: 1710903600
```

---

## Idempotency

Payment and refund endpoints support idempotency to prevent duplicate operations.

### Usage

Include a unique `X-Idempotency-Key` header with each request:

```http
X-Idempotency-Key: 550e8400-e29b-41d4-a716-446655440000
```

### Behavior

- **First request:** Processes normally and stores the response
- **Duplicate request:** Returns the original response without reprocessing
- **Retention:** Keys are stored for 24 hours
- **Format:** UUID recommended (any unique string accepted)

---

## Endpoints

### 1. Health & Monitoring

#### Health Check

```http
GET /actuator/health
```

**Response:**
```json
{
  "status": "UP",
  "components": {
    "db": { "status": "UP" },
    "kafka": { "status": "UP" },
    "redis": { "status": "UP" }
  }
}
```

---

### 2. Merchant Management

#### Register Merchant

```http
POST /api/v1/merchants
Content-Type: application/json

{
  "name": "Acme Corporation",
  "email": "contact@acme.com",
  "webhookUrl": "https://acme.com/webhooks"
}
```

**Response (201):**
```json
{
  "success": true,
  "message": "Merchant registered successfully",
  "data": {
    "id": "merch_abc123",
    "name": "Acme Corporation",
    "email": "contact@acme.com",
    "status": "ACTIVE",
    "apiKey": "pk_live_abc123...",
    "apiSecret": "sk_live_xyz789...",
    "webhookUrl": "https://acme.com/webhooks",
    "createdAt": "2026-03-20T10:00:00Z"
  },
  "timestamp": "2026-03-20T10:00:00Z"
}
```

#### Get Merchant

```http
GET /api/v1/merchants/{id}
```

#### Update Merchant

```http
PUT /api/v1/merchants/{id}
Content-Type: application/json

{
  "name": "Acme Corp Updated",
  "email": "new-contact@acme.com",
  "webhookUrl": "https://acme.com/new-webhooks"
}
```

#### Suspend Merchant

```http
POST /api/v1/merchants/{id}/suspend
```

---

### 3. Customer Management

#### Register Customer

```http
POST /api/v1/customers
Content-Type: application/json

{
  "merchantId": "merch_abc123",
  "email": "john@example.com",
  "name": "John Doe",
  "phone": "+1234567890",
  "externalId": "EXT-12345"
}
```

**Response (201):**
```json
{
  "success": true,
  "message": "Customer registered successfully",
  "data": {
    "id": "cust_abc123",
    "merchantId": "merch_xyz789",
    "email": "john@example.com",
    "name": "John Doe",
    "phone": "+1234567890",
    "externalId": "EXT-12345",
    "status": "ACTIVE",
    "createdAt": "2026-03-20T10:00:00Z"
  },
  "timestamp": "2026-03-20T10:00:00Z"
}
```

#### Get Customer

```http
GET /api/v1/customers/{id}?merchantId={merchantId}
```

#### Add Payment Method

```http
POST /api/v1/customers/{id}/payment-methods
Content-Type: application/json

{
  "merchantId": "merch_abc123",
  "cardNumber": "4111111111111111",
  "cardExpiryMonth": "12",
  "cardExpiryYear": "2028",
  "cardCvv": "123",
  "cardholderName": "John Doe",
  "isDefault": true
}
```

#### Remove Payment Method

```http
DELETE /api/v1/customers/{id}/payment-methods/{pmId}
```

---

### 4. Payment Processing

#### Process Payment

```http
POST /api/v1/payments
Content-Type: application/json
X-Idempotency-Key: 550e8400-e29b-41d4-a716-446655440000

{
  "merchantId": "merch_abc123",
  "amountInCents": 10000,
  "currency": "USD",
  "customerId": "cust_123abc",
  "description": "Order #12345",
  "items": [
    {
      "description": "Premium Widget",
      "quantity": 2,
      "unitPriceInCents": 5000
    }
  ]
}
```

**Response (200):**
```json
{
  "success": true,
  "message": "Payment processed successfully",
  "data": {
    "id": "pay_abc123def456",
    "merchantId": "merch_xyz789",
    "customerId": "cust_123abc",
    "amountInCents": 10000,
    "currency": "USD",
    "status": "AUTHORIZED",
    "idempotencyKey": "550e8400-e29b-41d4-a716-446655440000",
    "description": "Order #12345",
    "items": [
      {
        "description": "Premium Widget",
        "quantity": 2,
        "unitPriceInCents": 5000,
        "totalInCents": 10000
      }
    ],
    "createdAt": "2026-03-20T10:30:00Z",
    "authorizedAt": "2026-03-20T10:30:00Z"
  },
  "timestamp": "2026-03-20T10:30:00Z"
}
```

#### Get Payment

```http
GET /api/v1/payments/{id}?merchantId={merchantId}
```

#### Get All Payments

```http
GET /api/v1/payments?merchantId={merchantId}
```

#### Capture Payment

```http
POST /api/v1/payments/{id}/capture?merchantId={merchantId}
```

#### Cancel Payment

```http
POST /api/v1/payments/{id}/cancel?merchantId={merchantId}
```

---

### 5. Transaction Management

#### Get Transaction

```http
GET /api/v1/transactions/{id}?merchantId={merchantId}
```

**Response (200):**
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "id": "txn_abc123",
    "paymentId": "pay_xyz789",
    "merchantId": "merch_abc123",
    "type": "PAYMENT",
    "amount": 10000,
    "currency": "USD",
    "status": "AUTHORIZED",
    "gatewayTransactionId": "ch_stripe_abc123",
    "createdAt": "2026-03-20T10:00:00Z",
    "processedAt": "2026-03-20T10:00:05Z"
  },
  "timestamp": "2026-03-20T11:00:00Z"
}
```

#### Capture Transaction

```http
POST /api/v1/transactions/{id}/capture?merchantId={merchantId}
```

#### Void Transaction

```http
POST /api/v1/transactions/{id}/void?merchantId={merchantId}
```

---

### 6. Refund Processing

#### Process Refund

```http
POST /api/v1/refunds
Content-Type: application/json

{
  "paymentId": "pay_abc123",
  "merchantId": "merch_xyz789",
  "amount": 5000,
  "idempotencyKey": "550e8400-e29b-41d4-a716-446655440001",
  "reason": "Customer requested refund"
}
```

**Response (201):**
```json
{
  "success": true,
  "message": "Refund processed successfully",
  "data": {
    "id": "ref_abc123",
    "paymentId": "pay_xyz789",
    "transactionId": "txn_abc123",
    "merchantId": "merch_abc123",
    "amount": 5000,
    "currency": "USD",
    "status": "COMPLETED",
    "type": "PARTIAL",
    "reason": "Customer requested refund",
    "createdAt": "2026-03-20T12:00:00Z",
    "processedAt": "2026-03-20T12:00:05Z"
  },
  "timestamp": "2026-03-20T12:00:00Z"
}
```

#### Get Refund

```http
GET /api/v1/refunds/{id}?merchantId={merchantId}
```

#### Cancel Refund

```http
POST /api/v1/refunds/{id}/cancel?merchantId={merchantId}&reason=Customer changed mind
```

---

### 7. Reconciliation

#### Reconcile Transactions

```http
POST /api/v1/reconciliation/reconcile?merchantId={merchantId}&date=2026-03-20
```

**Response (200):**
```json
{
  "success": true,
  "message": "Reconciliation completed",
  "data": {
    "batchId": "batch_abc123",
    "status": "COMPLETED",
    "totalTransactions": 150,
    "matchedCount": 145,
    "discrepancyCount": 5,
    "totalAmount": 1500000,
    "reconciledAmount": 1495000,
    "discrepancyAmount": 5000,
    "createdAt": "2026-03-20T10:00:00Z",
    "completedAt": "2026-03-20T10:05:00Z"
  },
  "timestamp": "2026-03-20T10:05:00Z"
}
```

#### Generate Settlement Report

```http
POST /api/v1/reconciliation/settlement-report?merchantId={merchantId}&startDate=2026-03-13&endDate=2026-03-20&format=JSON
```

---

## Pagination

List endpoints support pagination:

**Request:**
```http
GET /api/v1/payments?merchantId={merchantId}&page=0&size=20
```

**Response:**
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "content": [...],
    "pageInfo": {
      "page": 0,
      "size": 20,
      "totalElements": 150,
      "totalPages": 8,
      "first": true,
      "last": false
    }
  },
  "timestamp": "2026-03-20T10:00:00Z"
}
```

---

## Error Handling

All errors follow a consistent format:

```json
{
  "success": false,
  "message": "Payment not found with id: pay_invalid",
  "data": null,
  "timestamp": "2026-03-20T10:00:00Z"
}
```

See [ERROR_CODES.md](./ERROR_CODES.md) for a complete list of error codes.

---

## SDK Examples

### JavaScript/Node.js

```javascript
const axios = require('axios');

const client = axios.create({
  baseURL: 'https://api.payment-gateway.com/api/v1',
  headers: {
    'X-API-Key': 'pk_live_abc123...',
    'Content-Type': 'application/json'
  }
});

// Process payment
async function processPayment(paymentData) {
  const idempotencyKey = crypto.randomUUID();
  const response = await client.post('/payments', paymentData, {
    headers: { 'X-Idempotency-Key': idempotencyKey }
  });
  return response.data;
}
```

### Python

```python
import requests
import uuid

BASE_URL = 'https://api.payment-gateway.com/api/v1'
API_KEY = 'pk_live_abc123...'

headers = {
    'X-API-Key': API_KEY,
    'Content-Type': 'application/json'
}

def process_payment(payment_data):
    idempotency_key = str(uuid.uuid4())
    headers['X-Idempotency-Key'] = idempotency_key
    response = requests.post(f'{BASE_URL}/payments', json=payment_data, headers=headers)
    return response.json()
```

### Java

```java
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;

public class PaymentGatewayClient {
    private static final String BASE_URL = "https://api.payment-gateway.com/api/v1";
    private static final String API_KEY = "pk_live_abc123...";
    
    private final HttpClient client = HttpClient.newHttpClient();
    
    public String processPayment(String paymentJson, String idempotencyKey) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/payments"))
            .header("X-API-Key", API_KEY)
            .header("X-Idempotency-Key", idempotencyKey)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(paymentJson))
            .build();
            
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }
}
```

---

## Support

- **Documentation:** [docs/](./)
- **Swagger UI:** `/swagger-ui.html`
- **Issues:** https://github.com/anomalyco/payment-gateway-service/issues