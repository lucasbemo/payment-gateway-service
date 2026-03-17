# Payment Gateway API - Postman Collection Guide

Complete documentation for using the Payment Gateway Postman collection for API testing and development.

## Table of Contents

- [Overview](#overview)
- [Installation](#installation)
- [Quick Start](#quick-start)
- [Collection Structure](#collection-structure)
- [Environment Variables](#environment-variables)
- [API Endpoints](#api-endpoints)
- [Testing Workflows](#testing-workflows)
- [Troubleshooting](#troubleshooting)

---

## Overview

This Postman collection provides complete API coverage for the Payment Gateway Service, enabling:

- **Rapid API Testing** - Test all endpoints without writing code
- **Workflow Validation** - Execute complete payment flows end-to-end
- **Idempotency Testing** - Verify duplicate request handling
- **Onboarding** - Quick setup for new developers
- **Regression Testing** - Validate changes before deployment

### Collection Stats

| Metric | Value |
|--------|-------|
| Total Endpoints | ~25 |
| Folders | 7 |
| Environments | 3 (Local, Dev, Production) |
| Auto-populated Variables | 8 |

---

## Installation

### Step 1: Import Files

1. Open Postman (v10+ recommended)
2. Click **Import** button (top left)
3. Drag and drop or select these files from the `postman/` directory:
   - `Payment Gateway.postman_collection.json`
   - `Payment Gateway Local.postman_environment.json`
   - `Payment Gateway Dev.postman_environment.json`
   - `Payment Gateway Production.postman_environment.json`

### Step 2: Verify Import

After import, you should see:
- **Collections** panel: "Payment Gateway API"
- **Environments** panel: Three environments (Local, Dev, Production)

---

## Quick Start

### For Local Development

1. **Start the application:**
   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=local
   ```

2. **Select environment in Postman:**
   - Click environment dropdown (top right)
   - Select "Payment Gateway Local"

3. **Execute the flow:**
   - Navigate to `1. Merchant Management` → `Register Merchant`
   - Click **Send**
   - Continue with `2. Customer Management` → `Register Customer`
   - Continue with remaining requests in order

4. **Verify IDs are auto-populated:**
   - Open environment viewer (eye icon)
   - Confirm `merchantId`, `customerId`, etc. are populated

### For Production

1. **Configure API Key:**
   - Select "Payment Gateway Production" environment
   - Click environment eye icon → Edit
   - Add your API key to `apiKey` variable
   - Save changes

2. **Update requests:**
   - Production requests require `Authorization` header
   - See [Production Setup](#production-setup) for details

---

## Collection Structure

```
Payment Gateway API/
│
├── 0. Health & Info/
│   ├── Health Check                    # GET /actuator/health
│   ├── Prometheus Metrics              # GET /actuator/prometheus
│   └── Swagger UI                      # GET /swagger-ui.html
│
├── 1. Merchant Management/
│   ├── Register Merchant               # POST /api/v1/merchants
│   ├── Get Merchant                    # GET /api/v1/merchants/{id}
│   ├── Update Merchant                 # PUT /api/v1/merchants/{id}
│   └── Suspend Merchant                # POST /api/v1/merchants/{id}/suspend
│
├── 2. Customer Management/
│   ├── Register Customer               # POST /api/v1/customers
│   ├── Get Customer                    # GET /api/v1/customers/{id}?merchantId={id}
│   ├── Add Payment Method              # POST /api/v1/customers/{id}/payment-methods
│   └── Remove Payment Method           # DELETE /api/v1/customers/{id}/payment-methods/{pmId}
│
├── 3. Payment Processing/
│   ├── Process Payment                 # POST /api/v1/payments [Idempotent]
│   ├── Get Payment                     # GET /api/v1/payments/{id}?merchantId={id}
│   ├── Get All Payments                # GET /api/v1/payments?merchantId={id}
│   ├── Capture Payment                 # POST /api/v1/payments/{id}/capture
│   └── Cancel Payment                 # POST /api/v1/payments/{id}/cancel
│
├── 4. Transaction Management/
│   ├── Get Transaction                 # GET /api/v1/transactions/{id}?merchantId={id}
│   ├── Capture Transaction             # POST /api/v1/transactions/{id}/capture
│   └── Void Transaction                # POST /api/v1/transactions/{id}/void
│
├── 5. Refund Processing/
│   ├── Process Refund                  # POST /api/v1/refunds [Idempotent]
│   ├── Get Refund                      # GET /api/v1/refunds/{id}?merchantId={id}
│   └── Cancel Refund                   # POST /api/v1/refunds/{id}/cancel
│
├── 6. Reconciliation/
│   ├── Reconcile Transactions          # POST /api/v1/reconciliation/reconcile
│   └── Generate Settlement Report      # POST /api/v1/reconciliation/settlement-report
│
└── 7. Complete Flow/
    ├── Full Payment Flow               # Health check placeholder
    ├── IDEMPOTENCY TEST - First Request
    └── IDEMPOTENCY TEST - Second Request (Same Key)
```

---

## Environment Variables

### Configuration Variables

| Variable | Type | Local Value | Production Value | Description |
|----------|------|-------------|------------------|-------------|
| `baseUrl` | default | `http://localhost:8080` | `https://api.payment-gateway.com` | API base URL |
| `apiKey` | secret | *(empty)* | *your-api-key* | Production API key |

### Auto-populated Variables

These are automatically set when you run requests:

| Variable | Set By | Description |
|----------|--------|-------------|
| `merchantId` | Register Merchant | Current merchant ID |
| `customerId` | Register Customer | Current customer ID |
| `paymentId` | Process Payment | Last payment ID |
| `transactionId` | Capture Payment | Last transaction ID |
| `refundId` | Process Refund | Last refund ID |
| `paymentMethodId` | Add Payment Method | Last payment method ID |
| `idempotencyKey` | Process Payment (pre-request) | Current idempotency key |
| `reconciliationDate` | Reconcile (pre-request) | Current date (YYYY-MM-DD) |

### Additional Variables

| Variable | Set By | Description |
|----------|--------|-------------|
| `refundIdempotencyKey` | Process Refund (pre-request) | Refund-specific idempotency key |
| `settlementStartDate` | Settlement Report (pre-request) | Start date (7 days ago) |
| `settlementEndDate` | Settlement Report (pre-request) | End date (today) |

---

## API Endpoints

### Health & Info

#### Health Check

```http
GET {{baseUrl}}/actuator/health
```

**Expected Response:**
```json
{
  "status": "UP"
}
```

#### Prometheus Metrics

```http
GET {{baseUrl}}/actuator/prometheus
```

Returns metrics in Prometheus format.

---

### Merchant Management

#### Register Merchant

```http
POST {{baseUrl}}/api/v1/merchants
Content-Type: application/json

{
  "name": "Test Merchant",
  "email": "merchant@test.com",
  "webhookUrl": "https://webhook.site/test"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Merchant registered successfully",
  "data": {
    "id": "merch_xxx",
    "name": "Test Merchant",
    "email": "merchant@test.com",
    "status": "ACTIVE"
  },
  "timestamp": "2026-03-14T10:00:00Z"
}
```

#### Get Merchant

```http
GET {{baseUrl}}/api/v1/merchants/{{merchantId}}
```

#### Update Merchant

```http
PUT {{baseUrl}}/api/v1/merchants/{{merchantId}}
Content-Type: application/json

{
  "name": "Updated Merchant",
  "email": "updated@test.com",
  "webhookUrl": "https://webhook.site/updated"
}
```

#### Suspend Merchant

```http
POST {{baseUrl}}/api/v1/merchants/{{merchantId}}/suspend
```

---

### Customer Management

#### Register Customer

```http
POST {{baseUrl}}/api/v1/customers
Content-Type: application/json

{
  "merchantId": "{{merchantId}}",
  "email": "customer@test.com",
  "name": "Test Customer",
  "phone": "+1234567890",
  "externalId": "EXT-001"
}
```

#### Get Customer

```http
GET {{baseUrl}}/api/v1/customers/{{customerId}}?merchantId={{merchantId}}
```

#### Add Payment Method

```http
POST {{baseUrl}}/api/v1/customers/{{customerId}}/payment-methods
Content-Type: application/json

{
  "merchantId": "{{merchantId}}",
  "cardNumber": "4111111111111111",
  "cardExpiryMonth": "12",
  "cardExpiryYear": "2028",
  "cardCvv": "123",
  "cardholderName": "Test Customer",
  "isDefault": true
}
```

#### Remove Payment Method

```http
DELETE {{baseUrl}}/api/v1/customers/{{customerId}}/payment-methods/{{paymentMethodId}}
```

---

### Payment Processing

#### Process Payment

```http
POST {{baseUrl}}/api/v1/payments
Content-Type: application/json
X-Idempotency-Key: {{idempotencyKey}}

{
  "merchantId": "{{merchantId}}",
  "amountInCents": 10000,
  "currency": "USD",
  "customerId": "{{customerId}}",
  "description": "Test payment",
  "items": [
    {
      "description": "Test Item",
      "quantity": 2,
      "unitPriceInCents": 5000
    }
  ]
}
```

**Important:** The `X-Idempotency-Key` header is required. The pre-request script auto-generates this.

#### Get Payment

```http
GET {{baseUrl}}/api/v1/payments/{{paymentId}}?merchantId={{merchantId}}
```

#### Get All Payments

```http
GET {{baseUrl}}/api/v1/payments?merchantId={{merchantId}}
```

#### Capture Payment

```http
POST {{baseUrl}}/api/v1/payments/{{paymentId}}/capture?merchantId={{merchantId}}
```

#### Cancel Payment

```http
POST {{baseUrl}}/api/v1/payments/{{paymentId}}/cancel?merchantId={{merchantId}}
```

---

### Transaction Management

#### Get Transaction

```http
GET {{baseUrl}}/api/v1/transactions/{{transactionId}}?merchantId={{merchantId}}
```

#### Capture Transaction

```http
POST {{baseUrl}}/api/v1/transactions/{{transactionId}}/capture?merchantId={{merchantId}}
```

#### Void Transaction

```http
POST {{baseUrl}}/api/v1/transactions/{{transactionId}}/void?merchantId={{merchantId}}
```

---

### Refund Processing

#### Process Refund

```http
POST {{baseUrl}}/api/v1/refunds
Content-Type: application/json

{
  "paymentId": "{{paymentId}}",
  "merchantId": "{{merchantId}}",
  "amount": 5000,
  "idempotencyKey": "{{refundIdempotencyKey}}",
  "reason": "Customer request"
}
```

#### Get Refund

```http
GET {{baseUrl}}/api/v1/refunds/{{refundId}}?merchantId={{merchantId}}
```

#### Cancel Refund

```http
POST {{baseUrl}}/api/v1/refunds/{{refundId}}/cancel?merchantId={{merchantId}}&reason=Customer changed mind
```

---

### Reconciliation

#### Reconcile Transactions

```http
POST {{baseUrl}}/api/v1/reconciliation/reconcile?merchantId={{merchantId}}&date={{reconciliationDate}}
```

#### Generate Settlement Report

```http
POST {{baseUrl}}/api/v1/reconciliation/settlement-report?merchantId={{merchantId}}&startDate={{settlementStartDate}}&endDate={{settlementEndDate}}&format=JSON
```

---

## Testing Workflows

### Complete Payment Flow

Run requests in this order for a complete end-to-end test:

```
1. Health Check (verify server is running)
2. Register Merchant → merchantId saved
3. Register Customer → customerId saved
4. Add Payment Method → paymentMethodId saved
5. Process Payment → paymentId saved
6. Get Payment (verify payment was created)
7. Capture Payment → transactionId saved
8. Process Refund → refundId saved
9. Get Refund (verify refund was created)
```

### Idempotency Testing

The collection includes built-in idempotency tests:

1. **Run First Request:**
   - `7. Complete Flow` → `IDEMPOTENCY TEST - First Request`
   - Note the payment ID in the response

2. **Run Second Request:**
   - `7. Complete Flow` → `IDEMPOTENCY TEST - Second Request (Same Key)`
   - Verify the same payment ID is returned

3. **Expected Behavior:**
   - Both requests return identical payment ID
   - No duplicate payment is created
   - Response includes original payment details

### Using Collection Runner

1. Click **Runner** in Postman
2. Select "Payment Gateway API" collection
3. Select environment (e.g., "Payment Gateway Local")
4. Configure iterations (default: 1)
5. Click **Run**

**Note:** For the full flow, run requests in order. Some requests depend on IDs from previous requests.

---

## Test Scripts

Each request includes test scripts that validate:

### Response Structure

```javascript
pm.test("Response has valid ApiResponse structure", function () {
    const json = pm.response.json();
    pm.expect(json).to.have.property('success', true);
    pm.expect(json).to.have.property('message');
    pm.expect(json).to.have.property('data');
    pm.expect(json).to.have.property('timestamp');
});
```

### ID Extraction

```javascript
pm.test("Extract payment ID from response", function () {
    const json = pm.response.json();
    if (json.data && json.data.id) {
        pm.environment.set('paymentId', json.data.id);
        console.log('Payment ID saved:', json.data.id);
    }
});
```

### Status Code Validation

```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});
```

---

## Troubleshooting

### Common Issues

#### 404 Not Found

**Cause:** Application not running or wrong base URL

**Solution:**
1. Verify application is running: `curl http://localhost:8080/actuator/health`
2. Check `baseUrl` environment variable is correct
3. Ensure correct environment is selected in Postman

#### 400 Bad Request

**Cause:** Missing required fields or invalid data

**Solution:**
1. Check request body has all required fields
2. Verify `merchantId` is populated (run Register Merchant first)
3. Check field formats (email, phone, etc.)

#### 401 Unauthorized

**Cause:** Missing or invalid API key (production only)

**Solution:**
1. Add API key to `apiKey` environment variable
2. Add `Authorization` header: `Bearer {{apiKey}}`
3. Contact admin for valid API key

#### Tests Failing

**Cause:** IDs not populated or out of order execution

**Solution:**
1. Clear environment variables (eye icon → Reset)
2. Run requests in order starting with Register Merchant
3. Check Console for debug output

#### Console Debug Output

View console for debug information:
1. Click **View** → **Show Postman Console** (or Ctrl+Alt+C)
2. Run request
3. Check output for:
   - Generated UUIDs
   - Saved IDs
   - Error messages

### Reset Environment

To clear all auto-populated variables:

1. Click environment eye icon
2. Click **Reset** button
3. Confirm reset
4. Re-run from Register Merchant

---

## Production Setup

For production testing:

1. **Configure API Key:**
   ```
   Environment: Payment Gateway Production
   Variable: apiKey
   Value: <your-production-api-key>
   ```

2. **Add Authorization Header:**
   - Production requests may require `Authorization: Bearer {{apiKey}}`
   - Add to requests as needed

3. **Verify Base URL:**
   ```
   baseUrl: https://api.payment-gateway.com
   ```

4. **Test with Health Check:**
   - Run `Health Check` first to verify connectivity

---

## Additional Resources

- [API Documentation](../README.md) - Main API documentation
- [Swagger UI](http://localhost:8080/swagger-ui.html) - Interactive API docs
- [Postman Learning Center](https://learning.postman.com/) - Postman tutorials

---

## Support

For issues or questions:
1. Check [Troubleshooting](#troubleshooting) section
2. Review Postman Console output
3. Contact the development team
