# Phase 12: Stripe Integration, Webhook System & Dashboard Analytics - Implementation Plan

**Created:** 2026-03-21  
**Status:** Ready for Implementation  
**Estimated Time:** 12-14 hours

---

## Executive Summary

Phase 12 focuses on three major features:

| Feature | Description | Est. Time |
|---------|-------------|-----------|
| **Real Stripe Integration** | Replace stub with real Stripe SDK | 4-5 hours |
| **Webhook System** | Implement webhook delivery service | 3-4 hours |
| **Dashboard Analytics** | Admin analytics endpoints | 4-5 hours |

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

### 1.3 Files to Create

| File | Purpose |
|------|---------|
| `StripeConfig.java` | Stripe SDK configuration |
| `StripePaymentGatewayService.java` | Real Stripe integration |
| `StripeTokenizationService.java` | Card tokenization |
| `StripeWebhookHandler.java` | Handle Stripe webhooks |
| `StripePaymentMapper.java` | Map Stripe objects to domain |
| `StripeExceptionTranslator.java` | Convert Stripe exceptions |

### 1.4 Documentation

**File:** `docs/STRIPE_INTEGRATION.md`

**Contents:**
- Setup Stripe account
- Get API keys (test vs production)
- Configure environment variables
- Supported payment flows
- Error handling
- Testing with Stripe CLI
- Webhook setup in Stripe Dashboard

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

### 2.3 Files to Create

| File | Purpose |
|------|---------|
| `domain/webhook/model/WebhookDelivery.java` | Domain entity |
| `domain/webhook/model/WebhookStatus.java` | Status enum |
| `domain/webhook/port/WebhookDeliveryRepository.java` | Repository port |
| `application/webhook/service/WebhookDeliveryService.java` | Application service |
| `infrastructure/webhook/adapter/out/WebhookPublisher.java` | HTTP publisher |
| `infrastructure/webhook/scheduler/WebhookRetryScheduler.java` | Retry scheduler |
| `infrastructure/webhook/util/WebhookSignatureGenerator.java` | HMAC signature |
| `infrastructure/webhook/mapper/EventToWebhookMapper.java` | Map events |
| `V013__create_webhook_deliveries.sql` | Database migration |

### 2.4 Event Types

| Domain Event | Webhook Event Type |
|--------------|-------------------|
| PaymentCreatedEvent | `payment.created` |
| PaymentCapturedEvent | `payment.completed` |
| PaymentFailedEvent | `payment.failed` |
| PaymentCancelledEvent | `payment.cancelled` |
| RefundProcessedEvent | `refund.processed` |
| RefundFailedEvent | `refund.failed` |

### 2.5 Documentation

**File:** `docs/WEBHOOK_DELIVERY.md`

**Contents:**
- Event types and payloads
- Signature verification
- Retry policy
- Best practices
- Testing webhooks
- Troubleshooting

---

## Part 3: Dashboard Analytics Endpoints

### 3.1 Overview

Admin dashboard analytics endpoints provide real-time insights into payment operations, merchant performance, and system health.

### 3.2 Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                    Admin Dashboard API                           │
├─────────────────────────────────────────────────────────────────┤
│  /api/v1/admin/analytics/*                                       │
│  ├── /overview          → Dashboard overview metrics             │
│  ├── /payments          → Payment analytics                      │
│  ├── /merchants         → Merchant analytics                     │
│  ├── /revenue           → Revenue analytics                      │
│  ├── /transactions      → Transaction analytics                  │
│  └── /health            → System health metrics                  │
└─────────────────────────────────────────────────────────────────┘
```

### 3.3 Endpoints

#### 3.3.1 Dashboard Overview

```
GET /api/v1/admin/analytics/overview
GET /api/v1/admin/analytics/overview?period=today|week|month|year
```

**Response:**
```json
{
  "success": true,
  "data": {
    "period": "month",
    "totalPayments": 15420,
    "totalAmount": 154200000,
    "totalRefunds": 342,
    "refundAmount": 3420000,
    "netRevenue": 150780000,
    "activeMerchants": 156,
    "newMerchants": 12,
    "successRate": 98.2,
    "averageProcessingTime": 1.2,
    "periodComparison": {
      "paymentsGrowth": 12.5,
      "revenueGrowth": 15.3,
      "merchantsGrowth": 8.2
    }
  }
}
```

#### 3.3.2 Payment Analytics

```
GET /api/v1/admin/analytics/payments
GET /api/v1/admin/analytics/payments/trends
GET /api/v1/admin/analytics/payments/by-status
GET /api/v1/admin/analytics/payments/by-currency
```

**Response:**
```json
{
  "success": true,
  "data": {
    "totalPayments": 15420,
    "byStatus": {
      "AUTHORIZED": 1200,
      "CAPTURED": 12500,
      "CANCELLED": 850,
      "FAILED": 370,
      "REFUNDED": 500
    },
    "byCurrency": {
      "USD": 12000,
      "EUR": 2500,
      "GBP": 920
    },
    "trends": [
      { "date": "2026-03-01", "count": 520, "amount": 5200000 },
      { "date": "2026-03-02", "count": 480, "amount": 4800000 }
    ],
    "averageAmount": 10000,
    "averageProcessingTimeMs": 1200
  }
}
```

#### 3.3.3 Merchant Analytics

```
GET /api/v1/admin/analytics/merchants
GET /api/v1/admin/analytics/merchants/{merchantId}
GET /api/v1/admin/analytics/merchants/top
GET /api/v1/admin/analytics/merchants/performance
```

**Response:**
```json
{
  "success": true,
  "data": {
    "totalMerchants": 156,
    "activeMerchants": 142,
    "suspendedMerchants": 14,
    "newMerchantsThisMonth": 12,
    "topMerchants": [
      {
        "id": "merch_abc123",
        "name": "Acme Corp",
        "totalPayments": 5000,
        "totalAmount": 50000000,
        "successRate": 99.5
      }
    ],
    "performanceDistribution": {
      "high": 45,
      "medium": 78,
      "low": 19
    }
  }
}
```

#### 3.3.4 Revenue Analytics

```
GET /api/v1/admin/analytics/revenue
GET /api/v1/admin/analytics/revenue/trends
GET /api/v1/admin/analytics/revenue/by-merchant
```

**Response:**
```json
{
  "success": true,
  "data": {
    "totalRevenue": 150780000,
    "grossRevenue": 154200000,
    "refunds": 3420000,
    "chargebacks": 0,
    "fees": 500000,
    "netRevenue": 150280000,
    "trends": [
      { "date": "2026-03-01", "gross": 5200000, "net": 5100000 },
      { "date": "2026-03-02", "gross": 4800000, "net": 4700000 }
    ],
    "byMerchant": [
      { "merchantId": "merch_abc123", "name": "Acme Corp", "revenue": 50000000 }
    ]
  }
}
```

#### 3.3.5 Transaction Analytics

```
GET /api/v1/admin/analytics/transactions
GET /api/v1/admin/analytics/transactions/by-type
GET /api/v1/admin/analytics/transactions/by-status
```

**Response:**
```json
{
  "success": true,
  "data": {
    "totalTransactions": 45000,
    "byType": {
      "PAYMENT": 15420,
      "CAPTURE": 12500,
      "REFUND": 500,
      "AUTHORIZATION": 15000,
      "REVERSAL": 580
    },
    "byStatus": {
      "AUTHORIZED": 1200,
      "CAPTURED": 12500,
      "SETTLED": 15000,
      "FAILED": 370,
      "REFUNDED": 500
    },
    "averageProcessingTimeMs": 850,
    "failureRate": 0.8
  }
}
```

#### 3.3.6 System Health Metrics

```
GET /api/v1/admin/analytics/system-health
```

**Response:**
```json
{
  "success": true,
  "data": {
    "uptime": "99.99%",
    "averageResponseTime": 120,
    "requestsPerMinute": 850,
    "errorRate": 0.02,
    "components": {
      "database": { "status": "healthy", "latency": 5 },
      "kafka": { "status": "healthy", "consumerLag": 12 },
      "redis": { "status": "healthy", "latency": 1 },
      "stripe": { "status": "healthy", "latency": 150 }
    },
    "alerts": []
  }
}
```

### 3.4 Implementation Tasks

#### 3.4.1 Create Analytics Service

**File:** `application/analytics/service/DashboardAnalyticsService.java`

```java
@Service
public class DashboardAnalyticsService {
    public DashboardOverview getOverview(Period period);
    public PaymentAnalytics getPaymentAnalytics(Period period);
    public MerchantAnalytics getMerchantAnalytics();
    public RevenueAnalytics getRevenueAnalytics(Period period);
    public TransactionAnalytics getTransactionAnalytics();
    public SystemHealth getSystemHealth();
}
```

#### 3.4.2 Create Analytics Controller

**File:** `infrastructure/analytics/adapter/in/rest/AdminAnalyticsController.java`

```java
@RestController
@RequestMapping("/api/v1/admin/analytics")
@RequiredArgsConstructor
public class AdminAnalyticsController implements AdminAnalyticsApi {
    
    private final DashboardAnalyticsService analyticsService;
    
    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<DashboardOverview>> getOverview(
        @RequestParam(defaultValue = "month") String period
    );
    
    @GetMapping("/payments")
    public ResponseEntity<ApiResponse<PaymentAnalytics>> getPaymentAnalytics(
        @RequestParam(defaultValue = "month") String period
    );
    
    // ... other endpoints
}
```

#### 3.4.3 Create DTOs

| DTO | Purpose |
|-----|---------|
| `DashboardOverview.java` | Overview metrics |
| `PaymentAnalytics.java` | Payment statistics |
| `MerchantAnalytics.java` | Merchant statistics |
| `RevenueAnalytics.java` | Revenue statistics |
| `TransactionAnalytics.java` | Transaction statistics |
| `SystemHealth.java` | System health metrics |
| `TrendData.java` | Time-series data point |

#### 3.4.4 Create Repository Methods

Add aggregation queries to repositories:

```java
public interface PaymentRepositoryCustom {
    Long countByStatusAndCreatedAtBetween(PaymentStatus status, Instant from, Instant to);
    Long sumAmountByCreatedAtBetween(Instant from, Instant to);
    List<Object[]> countByCurrencyGroupBy(Instant from, Instant to);
    List<TrendData> getDailyTrends(Instant from, Instant to);
}
```

#### 3.4.5 Add Caching

```java
@Cacheable(value = "analytics", key = "#period")
public DashboardOverview getOverview(Period period) {
    // Cached for 5 minutes
}
```

### 3.5 Files to Create

| File | Purpose |
|------|---------|
| `DashboardAnalyticsService.java` | Analytics business logic |
| `AdminAnalyticsController.java` | REST endpoints |
| `AdminAnalyticsApi.java` | OpenAPI interface |
| `DashboardOverview.java` | Overview DTO |
| `PaymentAnalytics.java` | Payment analytics DTO |
| `MerchantAnalytics.java` | Merchant analytics DTO |
| `RevenueAnalytics.java` | Revenue analytics DTO |
| `TransactionAnalytics.java` | Transaction analytics DTO |
| `SystemHealth.java` | System health DTO |
| `TrendData.java` | Time-series DTO |
| `Period.java` | Time period enum |
| `PaymentRepositoryCustom.java` | Custom repository |
| `PaymentRepositoryImpl.java` | Custom repository impl |

### 3.6 Security

All analytics endpoints require `ADMIN` role:

```java
@PreAuthorize("hasRole('ADMIN')")
@GetMapping("/api/v1/admin/analytics/**")
```

### 3.7 Documentation

**File:** `docs/ADMIN_ANALYTICS.md`

**Contents:**
- Available endpoints
- Request/response formats
- Authentication requirements
- Caching behavior
- Rate limits

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

### 4.2 Analytics Materialized Views (Optional)

**File:** `V014__create_analytics_views.sql`

```sql
-- Daily payment summaries for fast analytics
CREATE MATERIALIZED VIEW daily_payment_stats AS
SELECT 
    DATE(created_at) as stat_date,
    merchant_id,
    COUNT(*) as payment_count,
    SUM(amount) as total_amount,
    currency
FROM payments
GROUP BY DATE(created_at), merchant_id, currency;

-- Refresh every hour
CREATE INDEX idx_daily_stats_date ON daily_payment_stats(stat_date);
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

### 5.3 Analytics Tests

| Test Class | Purpose |
|------------|---------|
| `DashboardAnalyticsServiceTest.java` | Service unit tests |
| `AdminAnalyticsControllerTest.java` | Controller tests |
| `PaymentRepositoryCustomTest.java` | Custom queries tests |

---

## Execution Order

| Order | Task | Files | Est. Time |
|-------|------|-------|-----------|
| 1 | Add Stripe dependency | pom.xml | 10 min |
| 2 | Create Stripe config | 1 file | 20 min |
| 3 | Implement Stripe gateway | 6 files | 2.5 hours |
| 4 | Create webhook domain | 3 files | 45 min |
| 5 | Create webhook service | 4 files | 1 hour |
| 6 | Create webhook publisher | 2 files | 30 min |
| 7 | Create analytics DTOs | 8 files | 1 hour |
| 8 | Create analytics service | 2 files | 1.5 hours |
| 9 | Create analytics controller | 2 files | 45 min |
| 10 | Create database migrations | 2 files | 20 min |
| 11 | Create documentation | 3 files | 1 hour |
| 12 | Write tests | 12 files | 2.5 hours |
| 13 | Update CHECKPOINT.md | 1 file | 10 min |
| **Total** | | **45 files** | **~13 hours** |

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
- [ ] Analytics endpoints return correct data
- [ ] Analytics are cached properly
- [ ] Admin role required for analytics
- [ ] All tests pass
- [ ] Documentation is complete