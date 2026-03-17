# Payment Gateway API Reference

Complete API reference for the Payment Gateway Service.

## Base URL

| Environment | URL |
|-------------|-----|
| Local | `http://localhost:8080` |
| Development | `https://dev.payment-gateway.com` |
| Production | `https://api.payment-gateway.com` |

## Authentication

### Local/Development Profiles
No authentication required. All endpoints are open access.

### Production Profile
- **API Key:** Required in `X-API-Key` header
- **JWT:** Required for certain endpoints

---

## Common Response Format

All responses use the `ApiResponse<T>` wrapper:

```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": { ... },
  "timestamp": "2026-03-14T10:30:00Z"
}
```

### Error Response

```json
{
  "success": false,
  "message": "Error description",
  "data": null,
  "timestamp": "2026-03-14T10:30:00Z"
}
```

### Validation Error Response

```json
{
  "success": false,
  "message": "Validation failed",
  "errors": [
    {
      "field": "email",
      "message": "Email is required"
    }
  ]
}
```

---

## Headers

### Required Headers

| Header | Description |
|--------|-------------|
| `Content-Type` | `application/json` for POST/PUT requests |
| `X-Idempotency-Key` | Required for payment/refund creation |

### Optional Headers

| Header | Description |
|--------|-------------|
| `X-API-Key` | Production API key |
| `Authorization` | JWT token (production only) |

---

## Endpoints

### Health & Monitoring

#### Health Check
```http
GET /actuator/health
```

#### Prometheus Metrics
```http
GET /actuator/prometheus
```

---

## Merchant Management

### Register Merchant
```http
POST /api/v1/merchants
```

**Request Body:**
```json
{
  "name": "Merchant Name",
  "email": "merchant@example.com",
  "webhookUrl": "https://webhook.site/merchant"
}
```

**Response (201 Created):**
```json
{
  "success": true,
  "message": "Merchant registered successfully",
  "data": {
    "id": "merch_xxx",
    "name": "Merchant Name",
    "email": "merchant@example.com",
    "status": "ACTIVE",
    "createdAt": "2026-03-14T10:00:00Z"
  }
}
```

### Get Merchant
```http
GET /api/v1/merchants/{merchantId}
```

### Update Merchant
```http
PUT /api/v1/merchants/{merchantId}
```

**Request Body:**
```json
{
  "name": "Updated Name",
  "email": "updated@example.com",
  "webhookUrl": "https://webhook.site/updated"
}
```

### Suspend Merchant
```http
POST /api/v1/merchants/{merchantId}/suspend
```

---

## Customer Management

### Register Customer
```http
POST /api/v1/customers
```

**Request Body:**
```json
{
  "merchantId": "merch_xxx",
  "email": "customer@example.com",
  "name": "Customer Name",
  "phone": "+1234567890",
  "externalId": "EXT-001"
}
```

**Response (201 Created):**
```json
{
  "success": true,
  "message": "Customer registered successfully",
  "data": {
    "id": "cust_xxx",
    "merchantId": "merch_xxx",
    "email": "customer@example.com",
    "name": "Customer Name",
    "externalId": "EXT-001"
  }
}
```

### Get Customer
```http
GET /api/v1/customers/{customerId}?merchantId={merchantId}
```

### Add Payment Method
```http
POST /api/v1/customers/{customerId}/payment-methods
```

**Request Body:**
```json
{
  "merchantId": "merch_xxx",
  "cardNumber": "4111111111111111",
  "cardExpiryMonth": "12",
  "cardExpiryYear": "2028",
  "cardCvv": "123",
  "cardholderName": "Customer Name",
  "isDefault": true
}
```

### Remove Payment Method
```http
DELETE /api/v1/customers/{customerId}/payment-methods/{paymentMethodId}
```

---

## Payment Processing

### Process Payment
```http
POST /api/v1/payments
```

**Headers:**
```
Content-Type: application/json
X-Idempotency-Key: uuid-here
```

**Request Body:**
```json
{
  "merchantId": "merch_xxx",
  "amountInCents": 10000,
  "currency": "USD",
  "customerId": "cust_xxx",
  "description": "Payment description",
  "items": [
    {
      "description": "Item name",
      "quantity": 2,
      "unitPriceInCents": 5000
    }
  ]
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Payment processed successfully",
  "data": {
    "id": "pay_xxx",
    "merchantId": "merch_xxx",
    "customerId": "cust_xxx",
    "amountInCents": 10000,
    "currency": "USD",
    "status": "AUTHORIZED",
    "idempotencyKey": "uuid-here",
    "createdAt": "2026-03-14T10:00:00Z"
  }
}
```

### Get Payment
```http
GET /api/v1/payments/{paymentId}?merchantId={merchantId}
```

### Get All Payments
```http
GET /api/v1/payments?merchantId={merchantId}
```

### Capture Payment
```http
POST /api/v1/payments/{paymentId}/capture?merchantId={merchantId}
```

### Cancel Payment
```http
POST /api/v1/payments/{paymentId}/cancel?merchantId={merchantId}
```

---

## Transaction Management

### Get Transaction
```http
GET /api/v1/transactions/{transactionId}?merchantId={merchantId}
```

### Capture Transaction
```http
POST /api/v1/transactions/{transactionId}/capture?merchantId={merchantId}
```

### Void Transaction
```http
POST /api/v1/transactions/{transactionId}/void?merchantId={merchantId}
```

---

## Refund Processing

### Process Refund
```http
POST /api/v1/refunds
```

**Request Body:**
```json
{
  "paymentId": "pay_xxx",
  "merchantId": "merch_xxx",
  "amount": 5000,
  "idempotencyKey": "uuid-here",
  "reason": "Customer request"
}
```

**Response (201 Created):**
```json
{
  "success": true,
  "message": "Refund processed successfully",
  "data": {
    "id": "ref_xxx",
    "paymentId": "pay_xxx",
    "amount": 5000,
    "status": "COMPLETED",
    "reason": "Customer request"
  }
}
```

### Get Refund
```http
GET /api/v1/refunds/{refundId}?merchantId={merchantId}
```

### Cancel Refund
```http
POST /api/v1/refunds/{refundId}/cancel?merchantId={merchantId}&reason={reason}
```

---

## Reconciliation

### Reconcile Transactions
```http
POST /api/v1/reconciliation/reconcile?merchantId={merchantId}&date={date}
```

**Query Parameters:**
- `merchantId` - Required
- `date` - Required (YYYY-MM-DD format)

**Response:**
```json
{
  "success": true,
  "message": "Reconciliation completed",
  "data": {
    "matchedCount": 10,
    "unmatchedCount": 0,
    "discrepancies": []
  }
}
```

### Generate Settlement Report
```http
POST /api/v1/reconciliation/settlement-report?merchantId={merchantId}&startDate={start}&endDate={end}&format={format}
```

**Query Parameters:**
- `merchantId` - Required
- `startDate` - Required (YYYY-MM-DD format)
- `endDate` - Required (YYYY-MM-DD format)
- `format` - Optional (default: JSON)

---

## Status Codes

| Code | Description |
|------|-------------|
| 200 OK | Request successful |
| 201 Created | Resource created successfully |
| 400 Bad Request | Invalid request body or missing fields |
| 404 Not Found | Resource not found |
| 409 Conflict | Resource conflict (e.g., duplicate idempotency key) |
| 500 Internal Server Error | Server error |

---

## Idempotency

The following endpoints support idempotency:

- `POST /api/v1/payments`
- `POST /api/v1/refunds`

**Usage:**
1. Generate a UUID for the `X-Idempotency-Key` header
2. Include the same key for retry requests
3. Server returns the same response for duplicate keys

**Key Requirements:**
- Must be unique per operation
- Minimum 32 characters recommended
- Stored for 24 hours

---

## Test Card Numbers

| Card Type | Number | Description |
|-----------|--------|-------------|
| Visa | 4111111111111111 | Standard test |
| Mastercard | 5500000000000004 | Standard test |
| American Express | 340000000000009 | Standard test |

**Note:** These are test card numbers only. Do not use real card numbers in development.

---

## Rate Limits

| Environment | Rate Limit |
|-------------|------------|
| Local | None |
| Development | 100 requests/minute |
| Production | 1000 requests/minute |

---

## Pagination

For list endpoints (e.g., Get All Payments):

**Query Parameters:**
- `page` - Page number (default: 0)
- `size` - Items per page (default: 20, max: 100)
- `sort` - Sort field (default: createdAt)
- `direction` - Sort direction (ASC/DESC)

**Example:**
```http
GET /api/v1/payments?merchantId=merch_xxx&page=0&size=50&sort=createdAt&direction=DESC
```

---

## Error Handling

### Common Errors

| Error | Cause | Solution |
|-------|-------|----------|
| `MERCHANT_NOT_FOUND` | Invalid merchantId | Verify merchant exists |
| `CUSTOMER_NOT_FOUND` | Invalid customerId | Verify customer exists |
| `PAYMENT_NOT_FOUND` | Invalid paymentId | Verify payment exists |
| `INSUFFICIENT_AMOUNT` | Refund amount exceeds payment | Reduce refund amount |
| `DUPLICATE_KEY` | Idempotency key already used | Use new key or accept cached response |

---

## Webhooks

Configure webhook URL when registering a merchant:

```json
{
  "name": "Merchant",
  "email": "merchant@example.com",
  "webhookUrl": "https://your-server.com/webhooks/payment"
}
```

### Webhook Events

- `payment.completed`
- `payment.failed`
- `refund.completed`
- `refund.failed`

### Webhook Payload

```json
{
  "eventId": "evt_xxx",
  "type": "payment.completed",
  "merchantId": "merch_xxx",
  "data": {
    "paymentId": "pay_xxx",
    "amount": 10000,
    "currency": "USD"
  },
  "timestamp": "2026-03-14T10:00:00Z"
}
```
