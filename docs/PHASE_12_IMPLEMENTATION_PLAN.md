# Phase 12: Real Stripe Integration & Webhook System - Implementation Plan

**Created:** 2026-03-21  
**Status:** Ready for Implementation  
**Estimated Time:** 8-10 hours

---

## Executive Summary

Phase 12 focuses on two major features:

| Feature | Description | Est. Time |
|---------|-------------|-----------|
| **Real Stripe Integration** | Replace stub with real Stripe SDK | 4-5 hours |
| **Webhook System** | Implement webhook delivery service | 4-5 hours |

---

## Part 1: Real Stripe Integration

### 1.1 Current State

The project currently has a **StubPaymentGatewayService** that simulates payment processing:

```
infrastructure/payment/adapter/out/provider/StubPaymentGatewayService.java
```

### 1.2 Implementation Tasks

#### 1.2.1 Add Stripe Dependencies

**File:** `pom.xml`

```xml
<dependency>
    <groupId>com.stripe</groupId>
    <artifactId>stripe-java</artifactId>
    <version>24.0.0</version>
</dependency>
```

#### 1.2.2 Create Stripe Configuration

**File:** `infrastructure/payment/config/StripeConfig.java`

```java
@Configuration
@ConfigurationProperties(prefix = "stripe")
public class StripeConfig {
    private String apiKey;
    private String webhookSecret;
    private String apiVersion;
}
```

#### 1.2.3 Implement Real Stripe Gateway Service

**File:** `infrastructure/payment/adapter/out/provider/StripePaymentGatewayService.java`

**Methods to implement:**

| Method | Stripe API | Purpose |
|--------|------------|---------|
| `authorizePayment()` | `PaymentIntent.create()` | Create payment intent |
| `capturePayment()` | `PaymentIntent.capture()` | Capture authorized payment |
| `cancelPayment()` | `PaymentIntent.cancel()` | Cancel payment |
| `createRefund()` | `Refund.create()` | Process refund |
| `createCustomer()` | `Customer.create()` | Create Stripe customer |
| `attachPaymentMethod()` | `PaymentMethod.attach()` | Attach card to customer |

#### 1.2.4 Create Payment Method Tokenization

**File:** `infrastructure/payment/adapter/out/provider/StripeTokenizationService.java`

- Create payment methods using Stripe tokens
- Store only tokens, not card numbers
- Support multiple card brands

#### 1.2.5 Add Feature Toggle

**File:** `application.yml`

```yaml
payment:
  gateway:
    provider: ${PAYMENT_PROVIDER:stub}  # stub or stripe
```

#### 1.2.6 Update Application Properties

```yaml
stripe:
  api-key: ${STRIPE_API_KEY}
  webhook-secret: ${STRIPE_WEBHOOK_SECRET}
  api-version: 2023-10-16
```

### 1.3 Files to Create

| File | Purpose |
|------|---------|
| `StripeConfig.java` | Stripe SDK configuration |
| `StripePaymentGatewayService.java` | Real Stripe integration |
| `StripeTokenizationService.java` | Card tokenization |
| `StripeWebhookHandler.java` | Handle Stripe webhooks |
| `StripePaymentMapper.java` | Map Stripe objects to domain |
| `StripeExceptionTranslator.java` | Convert Stripe exceptions |

### 1.4 Files to Modify

| File | Changes |
|------|---------|
| `pom.xml` | Add Stripe dependency |
| `application.yml` | Add Stripe configuration |
| `PaymentGatewayPort.java` | May need interface updates |
| `.env.example` | Add Stripe variables |

---

## Part 2: Webhook System

### 2.1 Overview

The webhook system allows merchants to receive real-time notifications about payment events.

### 2.2 Architecture

```
┌──────────────────┐     ┌──────────────────┐     ┌──────────────────┐
│   Domain Event   │────▶│  Webhook Queue   │────▶│   HTTP POST to   │
│   (Payment)      │     │   (Outbox)       │     │   Merchant URL   │
└──────────────────┘     └──────────────────┘     └──────────────────┘
                                │
                                ▼
                         ┌──────────────────┐
                         │   Retry Logic    │
                         │   (5 attempts)   │
                         └──────────────────┘
```

### 2.3 Implementation Tasks

#### 2.3.1 Create Webhook Entity

**File:** `domain/webhook/model/WebhookDelivery.java`

```java
@Entity
public class WebhookDelivery {
    private String id;
    private String merchantId;
    private String eventType;
    private String payload;
    private WebhookStatus status;
    private int attemptCount;
    private Instant nextAttemptAt;
    private Instant deliveredAt;
}
```

#### 2.3.2 Create Webhook Service

**File:** `application/webhook/service/WebhookDeliveryService.java`

**Features:**
- Queue webhook for delivery
- Retry failed deliveries (exponential backoff)
- Track delivery status
- Generate HMAC signatures

#### 2.3.3 Create Webhook Publisher

**File:** `infrastructure/webhook/adapter/out/WebhookPublisher.java`

**Features:**
- HTTP POST to merchant webhook URL
- Timeout handling
- Signature header generation
- Response validation

#### 2.3.4 Create Webhook Retry Scheduler

**File:** `infrastructure/webhook/scheduler/WebhookRetryScheduler.java`

```java
@Scheduled(fixedDelay = 60000)  // Every minute
public void processPendingWebhooks() {
    // Find pending webhooks
    // Attempt delivery
    // Update status
}
```

#### 2.3.5 Create Webhook Endpoint (for testing)

**File:** `infrastructure/webhook/adapter/in/rest/WebhookTestController.java`

- Endpoint to test webhook delivery
- Endpoint to view delivery logs

#### 2.3.6 Create Event to Webhook Mapper

**File:** `application/webhook/mapper/EventToWebhookMapper.java`

Map domain events to webhook payloads:

| Domain Event | Webhook Event Type |
|--------------|-------------------|
| PaymentCreatedEvent | `payment.created` |
| PaymentCapturedEvent | `payment.completed` |
| PaymentFailedEvent | `payment.failed` |
| PaymentCancelledEvent | `payment.cancelled` |
| RefundProcessedEvent | `refund.processed` |
| RefundFailedEvent | `refund.failed` |

### 2.4 Files to Create

| File | Purpose |
|------|---------|
| `WebhookDelivery.java` | Domain entity |
| `WebhookStatus.java` | Status enum |
| `WebhookDeliveryRepository.java` | Repository port |
| `WebhookDeliveryService.java` | Application service |
| `WebhookPublisher.java` | HTTP publisher |
| `WebhookRetryScheduler.java` | Retry scheduler |
| `WebhookSignatureGenerator.java` | HMAC signature |
| `WebhookPayloadBuilder.java` | Build JSON payloads |
| `EventToWebhookMapper.java` | Map events |
| `WebhookController.java` | Test endpoints |
| `V013__create_webhook_deliveries.sql` | Database migration |

### 2.5 Webhook Payload Format

```json
{
  "id": "evt_abc123",
  "type": "payment.completed",
  "created": "2026-03-21T10:00:00Z",
  "data": {
    "payment": {
      "id": "pay_xyz789",
      "merchantId": "merch_abc123",
      "amount": 10000,
      "currency": "USD",
      "status": "CAPTURED",
      "createdAt": "2026-03-21T09:55:00Z",
      "capturedAt": "2026-03-21T10:00:00Z"
    }
  },
  "signature": "sha256=abc123..."
}
```

### 2.6 Signature Verification

```java
public String generateSignature(String payload, String secret) {
    Mac mac = Mac.getInstance("HmacSHA256");
    SecretKeySpec key = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
    mac.init(key);
    byte[] hash = mac.doFinal(payload.getBytes());
    return "sha256=" + Hex.encodeHexString(hash);
}
```

---

## Part 3: Documentation

### 3.1 Stripe Integration Documentation

**File:** `docs/STRIPE_INTEGRATION.md`

**Contents:**
- Setup Stripe account
- Get API keys (test vs production)
- Configure environment variables
- Supported payment flows
- Error handling
- Testing with Stripe CLI
- Webhook setup in Stripe Dashboard

### 3.2 Webhook Delivery Documentation

**File:** `docs/WEBHOOK_DELIVERY.md`

**Contents:**
- Event types and payloads
- Signature verification
- Retry policy
- Best practices
- Testing webhooks
- Troubleshooting

### 3.3 Update Existing Documentation

| File | Changes |
|------|---------|
| `docs/GETTING_STARTED.md` | Add Stripe setup |
| `docs/DEPLOYMENT_GUIDE.md` | Add production Stripe config |
| `docs/SECURITY_GUIDE.md` | Add webhook security |
| `.env.example` | Add Stripe variables |

---

## Part 4: Database Migrations

### 4.1 Webhook Deliveries Table

**File:** `V013__create_webhook_deliveries.sql`

```sql
CREATE TABLE webhook_deliveries (
    id VARCHAR(36) PRIMARY KEY,
    merchant_id VARCHAR(36) NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    payload JSONB NOT NULL,
    status VARCHAR(20) NOT NULL,
    attempt_count INTEGER DEFAULT 0,
    max_attempts INTEGER DEFAULT 5,
    next_attempt_at TIMESTAMP,
    last_attempt_at TIMESTAMP,
    delivered_at TIMESTAMP,
    error_message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_webhook_status ON webhook_deliveries(status);
CREATE INDEX idx_webhook_next_attempt ON webhook_deliveries(next_attempt_at);
CREATE INDEX idx_webhook_merchant ON webhook_deliveries(merchant_id);
```

---

## Part 5: Tests

### 5.1 Stripe Integration Tests

| Test Class | Purpose |
|------------|---------|
| `StripePaymentGatewayServiceTest.java` | Unit tests with mocked Stripe |
| `StripePaymentIntegrationTest.java` | Integration with Stripe test mode |
| `StripeWebhookHandlerTest.java` | Webhook handling tests |

### 5.2 Webhook System Tests

| Test Class | Purpose |
|------------|---------|
| `WebhookDeliveryServiceTest.java` | Service unit tests |
| `WebhookPublisherTest.java` | HTTP publisher tests |
| `WebhookSignatureTest.java` | Signature generation |
| `WebhookRetryTest.java` | Retry logic tests |
| `WebhookE2ETest.java` | End-to-end webhook flow |

---

## Execution Order

| Order | Task | Files | Est. Time |
|-------|------|-------|-----------|
| 1 | Add Stripe dependency | pom.xml | 10 min |
| 2 | Create Stripe config | 1 file | 20 min |
| 3 | Implement Stripe gateway | 4 files | 2 hours |
| 4 | Create webhook domain | 3 files | 45 min |
| 5 | Create webhook service | 3 files | 1 hour |
| 6 | Create webhook publisher | 2 files | 45 min |
| 7 | Create retry scheduler | 1 file | 30 min |
| 8 | Create database migration | 1 file | 15 min |
| 9 | Create documentation | 2 files | 1 hour |
| 10 | Write tests | 8 files | 2 hours |
| 11 | Update CHECKPOINT.md | 1 file | 10 min |
| **Total** | | **26 files** | **~9 hours** |

---

## Environment Variables Required

```bash
# Stripe Configuration
STRIPE_API_KEY=sk_test_xxx           # Test key for development
STRIPE_API_KEY=sk_live_xxx           # Live key for production
STRIPE_WEBHOOK_SECRET=whsec_xxx      # From Stripe Dashboard

# Payment Provider Toggle
PAYMENT_PROVIDER=stripe              # or "stub" for testing
```

---

## Verification Checklist

After implementation:

- [ ] Stripe payments work in test mode
- [ ] Authorize → Capture flow works
- [ ] Refunds work through Stripe
- [ ] Webhooks are queued after events
- [ ] Webhooks are delivered to merchant URLs
- [ ] Retry logic works for failed deliveries
- [ ] Signatures can be verified
- [ ] All tests pass
- [ ] Documentation is complete