# Payment Gateway Service Documentation

Welcome to the Payment Gateway Service documentation.

## Documentation Index

### Getting Started

| Document | Description |
|----------|-------------|
| [Postman Collection Guide](./POSTMAN_COLLECTION_GUIDE.md) | Complete guide for using the Postman collection |
| [API Reference](./API_REFERENCE.md) | Full API endpoint reference |
| [Testing Guide](./TESTING_GUIDE.md) | Testing procedures and test scenarios |

### Quick Links

- **Postman Collection**: `../postman/Payment Gateway.postman_collection.json`
- **Environments**: `../postman/*.postman_environment.json`
- **Main README**: `../README.md`

---

## Postman Collection Overview

The Payment Gateway Postman collection provides:

- **25+ API endpoints** across 7 functional areas
- **3 environments** (Local, Dev, Production)
- **Auto-populated variables** for workflow chaining
- **Test scripts** for response validation
- **Pre-request scripts** for dynamic data generation

### Collection Structure

```
Payment Gateway API/
├── 0. Health & Info         # Health checks and monitoring
├── 1. Merchant Management   # Merchant CRUD operations
├── 2. Customer Management   # Customer and payment methods
├── 3. Payment Processing    # Payment authorization/capture
├── 4. Transaction Management # Transaction operations
├── 5. Refund Processing     # Refund processing
├── 6. Reconciliation        # Reconciliation and settlement
└── 7. Complete Flow         # End-to-end test scenarios
```

---

## Quick Start

### 1. Import Collection

1. Open Postman
2. Click **Import**
3. Select all files from `../postman/` directory

### 2. Start Application

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

### 3. Select Environment

- Choose "Payment Gateway Local" from environment dropdown

### 4. Run First Request

- Navigate to `1. Merchant Management` → `Register Merchant`
- Click **Send**
- Verify `merchantId` is auto-saved

### 5. Continue Flow

Run requests in order through the collection to test the complete payment flow.

---

## API Endpoints Summary

### Merchant Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/merchants` | Register merchant |
| GET | `/api/v1/merchants/{id}` | Get merchant |
| PUT | `/api/v1/merchants/{id}` | Update merchant |
| POST | `/api/v1/merchants/{id}/suspend` | Suspend merchant |

### Customer Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/customers` | Register customer |
| GET | `/api/v1/customers/{id}?merchantId={id}` | Get customer |
| POST | `/api/v1/customers/{id}/payment-methods` | Add payment method |
| DELETE | `/api/v1/customers/{id}/payment-methods/{pmId}` | Remove payment method |

### Payment Processing

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/payments` | Process payment (idempotent) |
| GET | `/api/v1/payments/{id}?merchantId={id}` | Get payment |
| GET | `/api/v1/payments?merchantId={id}` | Get all payments |
| POST | `/api/v1/payments/{id}/capture` | Capture payment |
| POST | `/api/v1/payments/{id}/cancel` | Cancel payment |

### Transaction Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/transactions/{id}?merchantId={id}` | Get transaction |
| POST | `/api/v1/transactions/{id}/capture` | Capture transaction |
| POST | `/api/v1/transactions/{id}/void` | Void transaction |

### Refund Processing

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/refunds` | Process refund (idempotent) |
| GET | `/api/v1/refunds/{id}?merchantId={id}` | Get refund |
| POST | `/api/v1/refunds/{id}/cancel` | Cancel refund |

### Reconciliation

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/reconciliation/reconcile` | Reconcile transactions |
| POST | `/api/v1/reconciliation/settlement-report` | Generate settlement report |

---

## Response Format

All responses follow the `ApiResponse<T>` structure:

```json
{
  "success": true,
  "message": "Success",
  "data": { ... },
  "timestamp": "2026-03-14T10:30:00Z"
}
```

---

## Environment Variables

| Variable | Description | Auto-populated |
|----------|-------------|----------------|
| `baseUrl` | API base URL | No |
| `merchantId` | Current merchant | Yes |
| `customerId` | Current customer | Yes |
| `paymentId` | Last payment | Yes |
| `transactionId` | Last transaction | Yes |
| `refundId` | Last refund | Yes |
| `paymentMethodId` | Last payment method | Yes |
| `idempotencyKey` | Current idempotency key | Yes |
| `apiKey` | Production API key | No |

---

## Testing Checklist

Before deployment, ensure:

- [ ] All health endpoints respond
- [ ] Merchant registration works
- [ ] Customer registration works
- [ ] Payment processing works
- [ ] Idempotency is enforced
- [ ] Refunds process correctly
- [ ] Reconciliation generates reports

---

## Support

For questions or issues:

1. Check the relevant documentation guide
2. Review Postman Console output
3. Check application logs
4. Contact the development team

---

## Document Changelog

| Date | Version | Changes |
|------|---------|---------|
| 2026-03-14 | 1.0.0 | Initial documentation release |
