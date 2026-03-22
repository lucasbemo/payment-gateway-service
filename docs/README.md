# Payment Gateway Service Documentation

Welcome to the Payment Gateway Service documentation.

## Documentation Index

### Getting Started

| Document | Description |
|----------|-------------|
| [Getting Started](./GETTING_STARTED.md) | Quick start guide for new developers |
| [Development Guide](./DEVELOPMENT_GUIDE.md) | Development workflow and best practices |
| [Deployment Guide](./DEPLOYMENT_GUIDE.md) | Deployment procedures and configurations |
| [Security Guide](./SECURITY_GUIDE.md) | Security best practices and compliance |

### API Documentation

| Document | Description |
|----------|-------------|
| [API Reference](./API_REFERENCE.md) | Full API endpoint reference |
| [API Documentation](./API_DOCUMENTATION.md) | Detailed API documentation with examples |
| [Error Codes](./ERROR_CODES.md) | Error code reference |
| [Webhooks](./WEBHOOKS.md) | Webhook configuration and events |

### Testing & Integration

| Document | Description |
|----------|-------------|
| [Testing Guide](./TESTING_GUIDE.md) | Testing procedures and test scenarios |
| [Postman Collection Guide](./POSTMAN_COLLECTION_GUIDE.md) | Complete guide for using the Postman collection |
| [Troubleshooting](./TROUBLESHOOTING.md) | Common issues and solutions |

### Architecture & Design

| Document | Description |
|----------|-------------|
| [Stripe Integration Architecture](./STRIPE_INTEGRATION_ARCHITECTURE.md) | Stripe integration design |
| [Observability Test Report](./OBSERVABILITY_TEST_REPORT.md) | Observability validation results |
| [Implementation Checkpoint](./IMPLEMENTATION_CHECKPOINT.md) | Implementation progress tracker |
| [ADR Decisions](./decisions/) | Architecture Decision Records |

### Archive

| Document | Description |
|----------|-------------|
| [archive/](./archive/) | Completed implementation plans and historical docs |

---

## Quick Links

- **Postman Collection**: `../postman/Payment Gateway.postman_collection.json`
- **Environments**: `../postman/*.postman_environment.json`
- **Main README**: `../README.md`

---

## Documentation Structure

```
docs/
├── README.md                           # This file
├── GETTING_STARTED.md                  # Quick start guide
├── DEVELOPMENT_GUIDE.md                # Development workflow
├── DEPLOYMENT_GUIDE.md                 # Deployment procedures
├── SECURITY_GUIDE.md                   # Security practices
├── API_REFERENCE.md                    # API endpoint reference
├── API_DOCUMENTATION.md                # Detailed API docs
├── ERROR_CODES.md                      # Error codes
├── WEBHOOKS.md                         # Webhook documentation
├── TESTING_GUIDE.md                    # Testing guide
├── POSTMAN_COLLECTION_GUIDE.md         # Postman guide
├── TROUBLESHOOTING.md                  # Troubleshooting
├── STRIPE_INTEGRATION_ARCHITECTURE.md  # Stripe integration
├── OBSERVABILITY_TEST_REPORT.md        # Observability report
├── IMPLEMENTATION_CHECKPOINT.md        # Implementation tracker
├── archive/                            # Archived documents
│   ├── PROJECT_PLAN.md                 # Original project plan
│   ├── PHASE_10_API_DOCUMENTATION_PLAN.md
│   ├── PHASE_10_REMAINING_PLAN.md
│   ├── PHASE_12_IMPLEMENTATION_PLAN.md
│   └── OBSERVABILITY_FIX_PLAN.md
└── decisions/                          # Architecture Decision Records
    ├── ADR-001-hexagonal-architecture.md
    ├── ADR-002-outbox-pattern.md
    ├── ADR-003-kafka-event-streaming.md
    └── ADR-004-resilience-patterns.md
```

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

## Document Changelog

| Date | Version | Changes |
|------|---------|---------|
| 2026-03-22 | 1.1.0 | Reorganized documentation structure, archived completed plans |
| 2026-03-14 | 1.0.0 | Initial documentation release |