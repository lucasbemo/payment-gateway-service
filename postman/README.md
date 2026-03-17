# Payment Gateway Postman Collection

Complete Postman collection for testing the Payment Gateway API locally and in development/production environments.

## Quick Start

### 1. Import into Postman

1. Open Postman
2. Click **Import**
3. Select all files from this directory:
   - `Payment Gateway.postman_collection.json`
   - `Payment Gateway Local.postman_environment.json`
   - `Payment Gateway Dev.postman_environment.json`
   - `Payment Gateway Production.postman_environment.json`

### 2. Select Environment

- **Local**: For `localhost:8080` (default for development)
- **Dev**: For development server
- **Production**: For production API (requires API key)

### 3. Run the Complete Flow

Execute requests in order:
1. `1. Merchant Management` → Register Merchant (auto-saves `merchantId`)
2. `2. Customer Management` → Register Customer (auto-saves `customerId`)
3. `2. Customer Management` → Add Payment Method (auto-saves `paymentMethodId`)
4. `3. Payment Processing` → Process Payment (auto-saves `paymentId`)
5. `3. Payment Processing` → Capture Payment (auto-saves `transactionId`)
6. `5. Refund Processing` → Process Refund (auto-saves `refundId`)

## Collection Structure

```
Payment Gateway API/
├── 0. Health & Info/
│   ├── Health Check (GET /actuator/health)
│   ├── Prometheus Metrics (GET /actuator/prometheus)
│   └── Swagger UI (GET /swagger-ui.html)
├── 1. Merchant Management/
│   ├── Register Merchant (POST /api/v1/merchants)
│   ├── Get Merchant (GET /api/v1/merchants/{id})
│   ├── Update Merchant (PUT /api/v1/merchants/{id})
│   └── Suspend Merchant (POST /api/v1/merchants/{id}/suspend)
├── 2. Customer Management/
│   ├── Register Customer (POST /api/v1/customers)
│   ├── Get Customer (GET /api/v1/customers/{id}?merchantId={merchantId})
│   ├── Add Payment Method (POST /api/v1/customers/{id}/payment-methods)
│   └── Remove Payment Method (DELETE /api/v1/customers/{id}/payment-methods/{pmId})
├── 3. Payment Processing/
│   ├── Process Payment (POST /api/v1/payments) [Idempotent]
│   ├── Get Payment (GET /api/v1/payments/{id}?merchantId={merchantId})
│   ├── Get All Payments (GET /api/v1/payments?merchantId={merchantId})
│   ├── Capture Payment (POST /api/v1/payments/{id}/capture)
│   └── Cancel Payment (POST /api/v1/payments/{id}/cancel)
├── 4. Transaction Management/
│   ├── Get Transaction (GET /api/v1/transactions/{id}?merchantId={merchantId})
│   ├── Capture Transaction (POST /api/v1/transactions/{id}/capture)
│   └── Void Transaction (POST /api/v1/transactions/{id}/void)
├── 5. Refund Processing/
│   ├── Process Refund (POST /api/v1/refunds) [Idempotent]
│   ├── Get Refund (GET /api/v1/refunds/{id}?merchantId={merchantId})
│   └── Cancel Refund (POST /api/v1/refunds/{id}/cancel)
├── 6. Reconciliation/
│   ├── Reconcile Transactions (POST /api/v1/reconciliation/reconcile)
│   └── Generate Settlement Report (POST /api/v1/reconciliation/settlement-report)
└── 7. Complete Flow/
    ├── Full Payment Flow (Health check placeholder)
    ├── IDEMPOTENCY TEST - First Request
    └── IDEMPOTENCY TEST - Second Request (Same Key)
```

## Features

### Automatic ID Chaining

The collection automatically extracts and stores IDs from responses:
- `merchantId` - From Register Merchant response
- `customerId` - From Register Customer response
- `paymentId` - From Process Payment response
- `transactionId` - From Capture Payment response
- `refundId` - From Process Refund response
- `paymentMethodId` - From Add Payment Method response

### Pre-request Scripts

- **UUID Generation**: Automatically generates UUIDs for idempotency keys
- **Date Generation**: Generates current date for reconciliation requests
- **Unique Emails**: Generates unique emails to avoid conflicts

### Test Scripts

Each request includes validation:
- Status code verification (200/201)
- ApiResponse structure validation (`success`, `message`, `data`, `timestamp`)
- Required field assertions
- ID extraction and environment variable updates

## Testing Idempotency

The "Complete Flow" folder includes idempotency tests:

1. Run `IDEMPOTENCY TEST - First Request` - Creates a payment
2. Run `IDEMPOTENCY TEST - Second Request (Same Key)` - Same idempotency key
3. Verify both requests return the same payment ID

## Environment Variables

| Variable | Description | Local | Production |
|----------|-------------|-------|------------|
| `baseUrl` | API base URL | `http://localhost:8080` | `https://api.payment-gateway.com` |
| `merchantId` | Current merchant | Auto-populated | Auto-populated |
| `customerId` | Current customer | Auto-populated | Auto-populated |
| `paymentId` | Last payment | Auto-populated | Auto-populated |
| `transactionId` | Last transaction | Auto-populated | Auto-populated |
| `refundId` | Last refund | Auto-populated | Auto-populated |
| `idempotencyKey` | Current idempotency key | Auto-generated | Auto-generated |
| `apiKey` | Production API key | Empty | Required |

## Running Locally

1. Start the application:
   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=local
   ```

2. Import the collection and "Payment Gateway Local" environment

3. Select the "Payment Gateway Local" environment in Postman

4. Run requests in sequence or use the Collection Runner

## Test Cards

Use these test card numbers for payment method testing:

| Card Type | Number | Description |
|-----------|--------|-------------|
| Visa | 4111111111111111 | Standard test card |
| Mastercard | 5500000000000004 | Standard test card |
| Amex | 340000000000009 | Standard test card |

## Response Format

All responses follow the `ApiResponse<T>` structure:

```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": {
    // Response-specific data
  },
  "timestamp": "2026-03-14T10:30:00Z"
}
```

## Troubleshooting

### 404 Errors
- Verify the application is running on the correct port
- Check that `baseUrl` environment variable is correct

### 400 Validation Errors
- Ensure all required fields are in the request body
- Check that `merchantId` and `customerId` are populated

### 401/403 Unauthorized
- For production: Add API key to the `apiKey` environment variable
- Check security configuration for the profile you're using

### Tests Failing
- Run requests in order (IDs must be populated first)
- Clear environment variables and re-run from Register Merchant
