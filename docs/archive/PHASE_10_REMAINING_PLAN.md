# Phase 10 Remaining Work: Comprehensive Implementation Plan

**Created:** 2026-03-20  
**Status:** Ready for Implementation  
**Estimated Time:** 6-7 hours

---

## Executive Summary

| Category | Current Status | Remaining Work |
|----------|---------------|----------------|
| OpenAPI/Swagger Configuration | ✅ Complete | None |
| API Documentation Interfaces | ✅ Complete | None |
| Controller Implementation | ✅ Complete | None |
| DTO @Schema Annotations | 🟡 3/16 files | **13 files remaining** |
| API Documentation Pages | ❌ Not Started | **4 files to create** |
| Postman Collection | ✅ Complete | No changes needed |
| Error Codes Documentation | ❌ Not Started | **1 file to create** |

---

## Part 1: DTO @Schema Annotations (Est. 2 hours)

### 1.1 Files Requiring Annotations

#### Application Layer Response DTOs (6 files)

| File | Location | Fields | Priority |
|------|----------|--------|----------|
| `MerchantResponse.java` | `application/merchant/dto/` | 8 fields | HIGH |
| `CustomerResponse.java` | `application/customer/dto/` | 9 fields | HIGH |
| `TransactionResponse.java` | `application/transaction/dto/` | 13 fields | HIGH |
| `RefundResponse.java` | `application/refund/dto/` | 11 fields | HIGH |
| `ReconciliationResponse.java` | `application/reconciliation/dto/` | 10 fields | MEDIUM |
| `SettlementReportDTO.java` | `application/reconciliation/dto/` | 15 fields | MEDIUM |

#### Infrastructure Commons DTOs (2 files)

| File | Location | Fields | Priority |
|------|----------|--------|----------|
| `PagedResponse.java` | `infrastructure/commons/rest/` | 2 fields | MEDIUM |
| `PageInfo.java` | `infrastructure/commons/rest/` | 6 fields | MEDIUM |

#### Controller Inner Request Classes (5 files)

| File | Location | Fields | Priority |
|------|----------|--------|----------|
| `MerchantController.CreateMerchantRequest` | Inner class | 3 fields | HIGH |
| `MerchantController.UpdateMerchantRequest` | Inner class | 3 fields | HIGH |
| `CustomerController.CreateCustomerRequest` | Inner class | 6 fields | HIGH |
| `CustomerController.AddPaymentMethodRequest` | Inner class | 8 fields | HIGH |
| `RefundController.CreateRefundRequest` | Inner class | 5 fields | MEDIUM |

### 1.2 Annotation Pattern

Use consistent pattern for all DTOs:

```java
@Schema(description = "Human-readable description of the DTO")
public class ExampleDTO {

    @Schema(
        description = "Field description",
        example = "example_value",
        requiredMode = Schema.RequiredMode.REQUIRED  // only for required fields
    )
    private String field;

    @Schema(
        description = "Status field with allowed values",
        example = "ACTIVE",
        allowableValues = {"ACTIVE", "SUSPENDED", "PENDING", "CLOSED"}
    )
    private String status;
}
```

### 1.3 Detailed Annotations for Each File

#### MerchantResponse.java
```java
@Schema(description = "Merchant account details")
public class MerchantResponse {

    @Schema(description = "Unique merchant identifier", example = "merch_abc123")
    private final String id;

    @Schema(description = "Merchant display name", example = "Acme Corporation")
    private final String name;

    @Schema(description = "Merchant email address", example = "contact@acme.com")
    private final String email;

    @Schema(
        description = "Merchant account status",
        example = "ACTIVE",
        allowableValues = {"ACTIVE", "SUSPENDED", "PENDING", "CLOSED"}
    )
    private final String status;

    @Schema(description = "API key for authentication (shown only on registration)", example = "pk_live_abc123...")
    private final String apiKey;

    @Schema(description = "API secret for authentication (shown only on registration)", example = "sk_live_xyz789...")
    private final String apiSecret;

    @Schema(description = "Webhook URL for event notifications", example = "https://acme.com/webhooks")
    private final String webhookUrl;

    @Schema(description = "Account creation timestamp", example = "2026-03-20T10:00:00Z")
    private final Instant createdAt;
}
```

#### CustomerResponse.java
```java
@Schema(description = "Customer account details")
public class CustomerResponse {

    @Schema(description = "Unique customer identifier", example = "cust_abc123")
    private final String id;

    @Schema(description = "Merchant ID that owns this customer", example = "merch_xyz789")
    private final String merchantId;

    @Schema(description = "Customer email address", example = "john@example.com")
    private final String email;

    @Schema(description = "Customer full name", example = "John Doe")
    private final String name;

    @Schema(description = "Customer phone number", example = "+1234567890")
    private final String phone;

    @Schema(description = "External ID from merchant's system", example = "EXT-12345")
    private final String externalId;

    @Schema(
        description = "Customer account status",
        example = "ACTIVE",
        allowableValues = {"ACTIVE", "SUSPENDED", "INACTIVE"}
    )
    private final String status;

    @Schema(description = "Account creation timestamp", example = "2026-03-20T10:00:00Z")
    private final Instant createdAt;
}
```

#### TransactionResponse.java
```java
@Schema(description = "Transaction details")
public class TransactionResponse {

    @Schema(description = "Unique transaction identifier", example = "txn_abc123")
    private final String id;

    @Schema(description = "Associated payment ID", example = "pay_xyz789")
    private final String paymentId;

    @Schema(description = "Merchant ID", example = "merch_abc123")
    private final String merchantId;

    @Schema(
        description = "Transaction type",
        example = "PAYMENT",
        allowableValues = {"PAYMENT", "CAPTURE", "AUTHORIZATION", "REFUND", "PARTIAL_REFUND", "REVERSAL", "CHARGEBACK", "ADJUSTMENT"}
    )
    private final String type;

    @Schema(description = "Transaction amount in cents", example = "10000")
    private final Long amount;

    @Schema(description = "ISO 4217 currency code", example = "USD")
    private final String currency;

    @Schema(
        description = "Transaction status",
        example = "AUTHORIZED",
        allowableValues = {"PENDING", "PROCESSING", "AUTHORIZED", "CAPTURED", "SETTLED", "REVERSED", "FAILED", "REFUNDED", "PARTIALLY_REFUNDED"}
    )
    private final String status;

    @Schema(description = "Payment gateway transaction ID", example = "ch_stripe_abc123")
    private final String gatewayTransactionId;

    @Schema(description = "Error code if transaction failed", example = "CARD_DECLINED")
    private final String errorCode;

    @Schema(description = "Error message if transaction failed", example = "Card was declined")
    private final String errorMessage;

    @Schema(description = "Transaction creation timestamp", example = "2026-03-20T10:00:00Z")
    private final Instant createdAt;

    @Schema(description = "Transaction processing timestamp", example = "2026-03-20T10:00:05Z")
    private final Instant processedAt;
}
```

#### RefundResponse.java
```java
@Schema(description = "Refund details")
public class RefundResponse {

    @Schema(description = "Unique refund identifier", example = "ref_abc123")
    private final String id;

    @Schema(description = "Associated payment ID", example = "pay_xyz789")
    private final String paymentId;

    @Schema(description = "Associated transaction ID", example = "txn_abc123")
    private final String transactionId;

    @Schema(description = "Merchant ID", example = "merch_abc123")
    private final String merchantId;

    @Schema(description = "Refund amount in cents", example = "5000")
    private final Long amount;

    @Schema(description = "ISO 4217 currency code", example = "USD")
    private final String currency;

    @Schema(
        description = "Refund status",
        example = "COMPLETED",
        allowableValues = {"PENDING", "PROCESSING", "APPROVED", "REJECTED", "COMPLETED", "FAILED", "CANCELLED"}
    )
    private final String status;

    @Schema(
        description = "Refund type",
        example = "PARTIAL",
        allowableValues = {"FULL", "PARTIAL", "MULTIPLE", "CHARGEBACK", "CANCELLATION"}
    )
    private final String type;

    @Schema(description = "Reason for the refund", example = "Customer requested")
    private final String reason;

    @Schema(description = "Refund creation timestamp", example = "2026-03-20T10:00:00Z")
    private final Instant createdAt;

    @Schema(description = "Refund processing timestamp", example = "2026-03-20T10:00:05Z")
    private final Instant processedAt;
}
```

#### ReconciliationResponse.java
```java
@Schema(description = "Reconciliation batch results")
public class ReconciliationResponse {

    @Schema(description = "Reconciliation batch ID", example = "batch_abc123")
    private final String batchId;

    @Schema(
        description = "Reconciliation status",
        example = "COMPLETED",
        allowableValues = {"PENDING", "IN_PROGRESS", "COMPLETED", "FAILED"}
    )
    private final String status;

    @Schema(description = "Total number of transactions reconciled", example = "150")
    private final Integer totalTransactions;

    @Schema(description = "Number of matched transactions", example = "145")
    private final Integer matchedCount;

    @Schema(description = "Number of discrepancies found", example = "5")
    private final Integer discrepancyCount;

    @Schema(description = "Total transaction amount in cents", example = "1500000")
    private final Long totalAmount;

    @Schema(description = "Reconciled amount in cents", example = "1495000")
    private final Long reconciledAmount;

    @Schema(description = "Discrepancy amount in cents", example = "5000")
    private final Long discrepancyAmount;

    @Schema(description = "Batch creation timestamp", example = "2026-03-20T10:00:00Z")
    private final Instant createdAt;

    @Schema(description = "Batch completion timestamp", example = "2026-03-20T10:05:00Z")
    private final Instant completedAt;
}
```

#### SettlementReportDTO.java
```java
@Schema(description = "Settlement report details")
public class SettlementReportDTO {

    @Schema(description = "Unique report identifier", example = "report_abc123")
    private final String id;

    @Schema(description = "Merchant ID", example = "merch_abc123")
    private final String merchantId;

    @Schema(description = "Payment gateway name", example = "STRIPE")
    private final String gatewayName;

    @Schema(description = "Settlement date", example = "2026-03-20")
    private final String settlementDate;

    @Schema(description = "Gateway report ID", example = "settle_xyz789")
    private final String gatewayReportId;

    @Schema(description = "Gross amount in cents", example = "100000")
    private final Long grossAmount;

    @Schema(description = "Fee amount in cents", example = "2900")
    private final Long feeAmount;

    @Schema(description = "Net amount in cents", example = "97100")
    private final Long netAmount;

    @Schema(description = "ISO 4217 currency code", example = "USD")
    private final String currency;

    @Schema(description = "Number of transactions", example = "50")
    private final Integer transactionCount;

    @Schema(description = "Number of refunds", example = "3")
    private final Integer refundCount;

    @Schema(description = "Number of chargebacks", example = "0")
    private final Integer chargebackCount;

    @Schema(description = "File path for downloaded report", example = "/reports/2026-03-20/settlement_merch_abc123.json")
    private final String filePath;

    @Schema(description = "Report format", example = "JSON", allowableValues = {"JSON", "CSV", "PDF"})
    private final String format;

    @Schema(description = "Report generation timestamp", example = "2026-03-20T10:00:00Z")
    private final Instant createdAt;
}
```

#### PagedResponse.java
```java
@Schema(description = "Paginated response wrapper")
public class PagedResponse<T> {

    @Schema(description = "Page content items")
    private List<T> content;

    @Schema(description = "Pagination metadata")
    private PageInfo pageInfo;
}
```

#### PageInfo.java
```java
@Schema(description = "Pagination metadata")
public class PageInfo {

    @Schema(description = "Current page number (0-indexed)", example = "0")
    private int page;

    @Schema(description = "Page size", example = "20")
    private int size;

    @Schema(description = "Total number of elements", example = "150")
    private long totalElements;

    @Schema(description = "Total number of pages", example = "8")
    private int totalPages;

    @Schema(description = "Whether this is the first page", example = "true")
    private boolean first;

    @Schema(description = "Whether this is the last page", example = "false")
    private boolean last;
}
```

#### Controller Inner Classes

**MerchantController.CreateMerchantRequest**
```java
@Schema(description = "Request to register a new merchant")
public static class CreateMerchantRequest {

    @Schema(description = "Merchant display name", example = "Acme Corporation", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Name is required")
    private String name;

    @Schema(description = "Merchant email address", example = "contact@acme.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Email is required")
    private String email;

    @Schema(description = "Webhook URL for event notifications", example = "https://acme.com/webhooks")
    private String webhookUrl;
}
```

**MerchantController.UpdateMerchantRequest**
```java
@Schema(description = "Request to update merchant information")
public static class UpdateMerchantRequest {

    @Schema(description = "New merchant display name", example = "Acme Corp Updated")
    private String name;

    @Schema(description = "New merchant email address", example = "new-contact@acme.com")
    private String email;

    @Schema(description = "New webhook URL", example = "https://acme.com/new-webhooks")
    private String webhookUrl;
}
```

**CustomerController.CreateCustomerRequest**
```java
@Schema(description = "Request to register a new customer")
public static class CreateCustomerRequest {

    @Schema(description = "Merchant ID that will own this customer", example = "merch_abc123", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Merchant ID is required")
    private String merchantId;

    @Schema(description = "Customer email address", example = "john@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Email is required")
    private String email;

    @Schema(description = "Customer full name", example = "John Doe", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Name is required")
    private String name;

    @Schema(description = "Customer phone number", example = "+1234567890")
    private String phone;

    @Schema(description = "External ID from merchant's system", example = "EXT-12345")
    private String externalId;
}
```

**CustomerController.AddPaymentMethodRequest**
```java
@Schema(description = "Request to add a payment method to a customer")
public static class AddPaymentMethodRequest {

    @Schema(description = "Merchant ID", example = "merch_abc123", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Merchant ID is required")
    private String merchantId;

    @Schema(description = "Card number (test: 4111111111111111)", example = "4111111111111111", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Card number is required")
    private String cardNumber;

    @Schema(description = "Card expiry month (MM)", example = "12", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Expiry month is required")
    private String cardExpiryMonth;

    @Schema(description = "Card expiry year (YYYY)", example = "2028", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Expiry year is required")
    private String cardExpiryYear;

    @Schema(description = "Card CVV", example = "123", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "CVV is required")
    private String cardCvv;

    @Schema(description = "Cardholder name", example = "John Doe")
    private String cardholderName;

    @Schema(description = "Set as default payment method", example = "true")
    private Boolean isDefault;
}
```

**RefundController.CreateRefundRequest**
```java
@Schema(description = "Request to process a refund")
public static class CreateRefundRequest {

    @Schema(description = "Payment ID to refund", example = "pay_abc123", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Payment ID is required")
    private String paymentId;

    @Schema(description = "Merchant ID", example = "merch_abc123", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Merchant ID is required")
    private String merchantId;

    @Schema(description = "Refund amount in cents", example = "5000", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private Long amount;

    @Schema(description = "Idempotency key for duplicate protection", example = "550e8400-e29b-41d4-a716-446655440000")
    private String idempotencyKey;

    @Schema(description = "Reason for the refund", example = "Customer requested")
    private String reason;
}
```

---

## Part 2: Error Codes Documentation (Est. 30 min)

### 2.1 Extracted Error Codes from Codebase

Based on exception classes analysis:

| Error Code | HTTP Status | Exception Class | Description |
|------------|-------------|-----------------|-------------|
| `PAYMENT_NOT_FOUND` | 404 | PaymentNotFoundException | Payment does not exist |
| `PAYMENT_PROCESSING_ERROR` | 500 | PaymentProcessingException | Error during payment processing |
| `DUPLICATE_PAYMENT` | 400 | DuplicatePaymentException | Idempotency key already used |
| `MERCHANT_NOT_FOUND` | 404 | MerchantNotFoundException | Merchant does not exist |
| `INVALID_MERCHANT` | 400 | InvalidMerchantException | Invalid merchant data |
| `MERCHANT_CONFIGURATION_ERROR` | 400 | MerchantConfigurationException | Merchant config error |
| `TRANSACTION_NOT_FOUND` | 404 | TransactionNotFoundException | Transaction does not exist |
| `TRANSACTION_PROCESSING_ERROR` | 500 | TransactionProcessingException | Error during transaction processing |
| `INVALID_TRANSACTION_STATE` | 400 | InvalidTransactionStateException | Invalid state transition |
| `REFUND_NOT_FOUND` | 404 | RefundNotFoundException | Refund does not exist |
| `REFUND_PROCESSING_ERROR` | 500 | RefundProcessingException | Error during refund processing |
| `INVALID_REFUND_AMOUNT` | 400 | InvalidRefundAmountException | Refund exceeds payment amount |
| `CUSTOMER_NOT_FOUND` | 404 | CustomerNotFoundException | Customer does not exist |
| `DUPLICATE_CUSTOMER` | 400 | DuplicateCustomerException | Customer email already exists |
| `INVALID_PAYMENT_METHOD` | 400 | InvalidPaymentMethodException | Invalid or missing payment method |
| `VALIDATION_ERROR` | 400 | ValidationException | Request validation failed |
| `EXTERNAL_SERVICE_ERROR` | 503 | ExternalServiceException | External service unavailable |
| `NOT_FOUND` | 404 | NotFoundException | Generic resource not found |
| `BUSINESS_ERROR` | 400 | BusinessException | Business rule violation |
| `DOMAIN_ERROR` | 500 | DomainException | Generic domain error |

### 2.2 File: docs/ERROR_CODES.md

```markdown
# Error Codes Reference

## Overview

All API errors return a consistent JSON structure with an error code and message.

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

| Code | HTTP | Description | Solution |
|------|------|-------------|----------|
| `PAYMENT_NOT_FOUND` | 404 | Payment does not exist | Verify payment ID |
| `PAYMENT_PROCESSING_ERROR` | 500 | Error processing payment | Retry or contact support |
| `DUPLICATE_PAYMENT` | 400 | Idempotency key already used | Use new idempotency key or retrieve original payment |

## Merchant Errors

| Code | HTTP | Description | Solution |
|------|------|-------------|----------|
| `MERCHANT_NOT_FOUND` | 404 | Merchant does not exist | Verify merchant ID |
| `INVALID_MERCHANT` | 400 | Invalid merchant data | Check request parameters |
| `MERCHANT_CONFIGURATION_ERROR` | 400 | Configuration error | Check merchant settings |

## Customer Errors

| Code | HTTP | Description | Solution |
|------|------|-------------|----------|
| `CUSTOMER_NOT_FOUND` | 404 | Customer does not exist | Verify customer ID |
| `DUPLICATE_CUSTOMER` | 400 | Email already registered | Use different email |
| `INVALID_PAYMENT_METHOD` | 400 | Invalid payment method | Verify card details |

## Transaction Errors

| Code | HTTP | Description | Solution |
|------|------|-------------|----------|
| `TRANSACTION_NOT_FOUND` | 404 | Transaction does not exist | Verify transaction ID |
| `INVALID_TRANSACTION_STATE` | 400 | Invalid state transition | Check current status |

## Refund Errors

| Code | HTTP | Description | Solution |
|------|------|-------------|----------|
| `REFUND_NOT_FOUND` | 404 | Refund does not exist | Verify refund ID |
| `INVALID_REFUND_AMOUNT` | 400 | Amount exceeds payment | Check original payment amount |

## System Errors

| Code | HTTP | Description | Solution |
|------|------|-------------|----------|
| `VALIDATION_ERROR` | 400 | Request validation failed | Fix field errors |
| `EXTERNAL_SERVICE_ERROR` | 503 | External service unavailable | Retry later |
| `BUSINESS_ERROR` | 400 | Business rule violation | Check request constraints |
```

---

## Part 3: API Documentation Pages (Est. 3 hours)

### 3.1 File: docs/API_DOCUMENTATION.md

**Structure:**
- Overview (version, base URL, content type)
- Authentication (API Key, JWT)
- Rate Limits
- Idempotency
- Endpoints by Group (all 21 endpoints with full details)
- Request/Response Examples
- Pagination
- Error Handling
- SDK Examples

### 3.2 File: docs/GETTING_STARTED.md

**Structure:**
- Prerequisites
- Quick Start (3-step flow: Register → Create Customer → Process Payment)
- curl Examples
- SDK Examples (JavaScript, Python, Java)
- Testing with Postman
- Common Patterns

### 3.3 File: docs/WEBHOOKS.md

**Structure:**
- Overview
- Event Types (6 events)
- Webhook Payloads (full JSON examples)
- Signature Verification (HMAC-SHA256)
- Retry Policy
- Testing Webhooks

### 3.4 File: docs/ERROR_CODES.md

Already defined in Part 2.

---

## Part 4: Postman Collection Analysis

**Status: ✅ COMPLETE - No changes needed**

The existing Postman collection (`postman/Payment Gateway.postman_collection.json`) is comprehensive:

- **7 folders** organized by domain
- **21 endpoints** fully documented
- **Test scripts** with assertions
- **Environment variables** properly configured
- **3 environments**: Local, Dev, Production
- **Special test cases**: Idempotency testing, Full payment flow

No modifications required.

---

## Execution Order

| Step | Task | Files | Est. Time |
|------|------|-------|-----------|
| 1 | Add @Schema to MerchantResponse.java | 1 | 10 min |
| 2 | Add @Schema to CustomerResponse.java | 1 | 10 min |
| 3 | Add @Schema to TransactionResponse.java | 1 | 15 min |
| 4 | Add @Schema to RefundResponse.java | 1 | 15 min |
| 5 | Add @Schema to ReconciliationResponse.java | 1 | 10 min |
| 6 | Add @Schema to SettlementReportDTO.java | 1 | 10 min |
| 7 | Add @Schema to PagedResponse.java | 1 | 5 min |
| 8 | Add @Schema to PageInfo.java | 1 | 5 min |
| 9 | Add @Schema to MerchantController inner classes | 1 | 10 min |
| 10 | Add @Schema to CustomerController inner classes | 1 | 15 min |
| 11 | Add @Schema to RefundController inner class | 1 | 10 min |
| 12 | Create docs/ERROR_CODES.md | 1 | 30 min |
| 13 | Create docs/API_DOCUMENTATION.md | 1 | 90 min |
| 14 | Create docs/GETTING_STARTED.md | 1 | 60 min |
| 15 | Create docs/WEBHOOKS.md | 1 | 60 min |
| 16 | Run tests and verify | - | 15 min |
| 17 | Update CHECKPOINT.md | 1 | 10 min |
| **Total** | | **14 files** | **~7 hours** |

---

## Verification Checklist

### DTO Annotations
- [ ] All 13 DTO files have @Schema annotations
- [ ] All fields have descriptions and examples
- [ ] Enum fields have allowableValues defined
- [ ] Required fields have requiredMode set
- [ ] Swagger UI shows all field descriptions

### Documentation
- [ ] API_DOCUMENTATION.md covers all 21 endpoints
- [ ] GETTING_STARTED.md has working examples
- [ ] WEBHOOKS.md documents all 6 event types
- [ ] ERROR_CODES.md lists all 20 error codes

### Functional
- [ ] All tests pass (1,039+)
- [ ] Swagger UI accessible at /swagger-ui.html
- [ ] OpenAPI JSON valid at /v3/api-docs
- [ ] Postman collection imports successfully

---

## Notes

1. **Postman collection** is already complete with comprehensive test scripts
2. **Error codes** extracted directly from exception classes - single source of truth
3. **DTO annotations** follow consistent pattern for Swagger UI display
4. **Documentation** uses markdown with full JSON examples for clarity