# Error Codes Reference

## Overview

All API errors return a consistent JSON response structure with a success flag, message, and timestamp.

## Error Response Format

```json
{
  "success": false,
  "message": "Human-readable error message",
  "data": null,
  "timestamp": "2026-03-20T10:00:00Z"
}
```

## Payment Errors

| Code | HTTP Status | Description | Solution |
|------|-------------|-------------|----------|
| `PAYMENT_NOT_FOUND` | 404 | Payment does not exist | Verify the payment ID is correct |
| `PAYMENT_PROCESSING_ERROR` | 500 | Error during payment processing | Check logs and retry or contact support |
| `DUPLICATE_PAYMENT` | 400 | Idempotency key already used for a different request | Use a new idempotency key or retrieve the original payment |

### Payment Error Examples

**Payment Not Found (404)**
```json
{
  "success": false,
  "message": "Payment not found with id: pay_invalid123",
  "data": null,
  "timestamp": "2026-03-20T10:00:00Z"
}
```

**Payment Processing Error (500)**
```json
{
  "success": false,
  "message": "Payment processing failed: Gateway timeout",
  "data": null,
  "timestamp": "2026-03-20T10:00:00Z"
}
```

**Duplicate Payment (400)**
```json
{
  "success": false,
  "message": "A payment with idempotency key already exists: 550e8400-e29b-41d4-a716-446655440000",
  "data": null,
  "timestamp": "2026-03-20T10:00:00Z"
}
```

---

## Merchant Errors

| Code | HTTP Status | Description | Solution |
|------|-------------|-------------|----------|
| `MERCHANT_NOT_FOUND` | 404 | Merchant does not exist | Verify the merchant ID is correct |
| `INVALID_MERCHANT` | 400 | Invalid merchant data provided | Check request parameters |
| `MERCHANT_CONFIGURATION_ERROR` | 400 | Merchant configuration is invalid | Review merchant settings |

### Merchant Error Examples

**Merchant Not Found (404)**
```json
{
  "success": false,
  "message": "Merchant not found with id: merch_invalid",
  "data": null,
  "timestamp": "2026-03-20T10:00:00Z"
}
```

**Invalid Merchant (400)**
```json
{
  "success": false,
  "message": "Invalid merchant: Email format is invalid",
  "data": null,
  "timestamp": "2026-03-20T10:00:00Z"
}
```

---

## Customer Errors

| Code | HTTP Status | Description | Solution |
|------|-------------|-------------|----------|
| `CUSTOMER_NOT_FOUND` | 404 | Customer does not exist | Verify the customer ID is correct |
| `DUPLICATE_CUSTOMER` | 400 | Customer email already registered | Use a different email address |
| `INVALID_PAYMENT_METHOD` | 400 | Invalid or missing payment method | Verify card details are correct |

### Customer Error Examples

**Customer Not Found (404)**
```json
{
  "success": false,
  "message": "Customer not found with id: cust_invalid",
  "data": null,
  "timestamp": "2026-03-20T10:00:00Z"
}
```

**Duplicate Customer (400)**
```json
{
  "success": false,
  "message": "Customer already exists with email: john@example.com",
  "data": null,
  "timestamp": "2026-03-20T10:00:00Z"
}
```

**Invalid Payment Method (400)**
```json
{
  "success": false,
  "message": "Payment method pm_invalid not found for customer cust_123",
  "data": null,
  "timestamp": "2026-03-20T10:00:00Z"
}
```

---

## Transaction Errors

| Code | HTTP Status | Description | Solution |
|------|-------------|-------------|----------|
| `TRANSACTION_NOT_FOUND` | 404 | Transaction does not exist | Verify the transaction ID is correct |
| `TRANSACTION_PROCESSING_ERROR` | 500 | Error during transaction processing | Check logs and retry |
| `INVALID_TRANSACTION_STATE` | 400 | Invalid state transition attempted | Check current transaction status |

### Transaction Error Examples

**Transaction Not Found (404)**
```json
{
  "success": false,
  "message": "Transaction not found with id: txn_invalid",
  "data": null,
  "timestamp": "2026-03-20T10:00:00Z"
}
```

**Invalid Transaction State (400)**
```json
{
  "success": false,
  "message": "Invalid state transition from CAPTURED to AUTHORIZED",
  "data": null,
  "timestamp": "2026-03-20T10:00:00Z"
}
```

---

## Refund Errors

| Code | HTTP Status | Description | Solution |
|------|-------------|-------------|----------|
| `REFUND_NOT_FOUND` | 404 | Refund does not exist | Verify the refund ID is correct |
| `REFUND_PROCESSING_ERROR` | 500 | Error during refund processing | Check logs and retry |
| `INVALID_REFUND_AMOUNT` | 400 | Refund amount exceeds payment amount | Check original payment amount |

### Refund Error Examples

**Refund Not Found (404)**
```json
{
  "success": false,
  "message": "Refund not found with id: ref_invalid",
  "data": null,
  "timestamp": "2026-03-20T10:00:00Z"
}
```

**Invalid Refund Amount (400)**
```json
{
  "success": false,
  "message": "Refund amount 15000 exceeds payment amount 10000",
  "data": null,
  "timestamp": "2026-03-20T10:00:00Z"
}
```

---

## System Errors

| Code | HTTP Status | Description | Solution |
|------|-------------|-------------|----------|
| `VALIDATION_ERROR` | 400 | Request validation failed | Fix the field errors and retry |
| `EXTERNAL_SERVICE_ERROR` | 503 | External service unavailable | Retry after a short delay |
| `NOT_FOUND` | 404 | Generic resource not found | Verify resource ID |
| `BUSINESS_ERROR` | 400 | Business rule violation | Check request constraints |
| `DOMAIN_ERROR` | 500 | Generic domain error | Contact support |

### System Error Examples

**Validation Error (400)**
```json
{
  "success": false,
  "message": "Field 'amountInCents': Amount must be positive",
  "data": null,
  "timestamp": "2026-03-20T10:00:00Z"
}
```

**External Service Error (503)**
```json
{
  "success": false,
  "message": "External service 'STRIPE' failed: Connection timeout",
  "data": null,
  "timestamp": "2026-03-20T10:00:00Z"
}
```

---

## HTTP Status Codes

| Status Code | Meaning | When Used |
|-------------|---------|-----------|
| 200 | OK | Successful GET, PUT, or POST operation |
| 201 | Created | Successful resource creation |
| 400 | Bad Request | Invalid request data or validation failure |
| 401 | Unauthorized | Missing or invalid authentication |
| 403 | Forbidden | Insufficient permissions |
| 404 | Not Found | Resource does not exist |
| 409 | Conflict | Resource conflict (e.g., duplicate) |
| 500 | Internal Server Error | Unexpected server error |
| 503 | Service Unavailable | External service unavailable |

---

## Best Practices

1. **Always check the `success` field** - If `false`, the request failed
2. **Use the `message` field** for user-friendly error display
3. **Log the full error response** for debugging
4. **Implement retry logic** for 503 errors with exponential backoff
5. **Handle validation errors** by displaying field-specific messages to users

---

## Error Handling Example (JavaScript)

```javascript
async function processPayment(paymentData) {
  try {
    const response = await fetch('/api/v1/payments', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'X-Idempotency-Key': crypto.randomUUID()
      },
      body: JSON.stringify(paymentData)
    });

    const result = await response.json();

    if (!result.success) {
      // Handle business error
      console.error('Payment failed:', result.message);
      return { error: result.message };
    }

    return { success: true, data: result.data };
  } catch (error) {
    // Handle network/technical error
    console.error('Request failed:', error);
    return { error: 'Network error. Please try again.' };
  }
}
```

---

## Support

If you encounter errors not documented here, please contact support with:
- Error code and message
- Request ID (from response headers)
- Timestamp of the error
- Steps to reproduce