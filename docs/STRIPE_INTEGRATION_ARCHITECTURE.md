# Comprehensive Stripe Integration Plan - Hexagonal Architecture

## Overview

This plan implements real Stripe integration while maintaining strict adherence to the project's hexagonal architecture, allowing easy swapping of payment providers without affecting the core domain.

---

## Current Architecture Analysis

### Existing Structure

```
┌─────────────────────────────────────────────────────────────────────────┐
│                           DOMAIN LAYER                                   │
│  Payment, Transaction, Refund aggregates - NO Stripe dependencies      │
├─────────────────────────────────────────────────────────────────────────┤
│                        APPLICATION LAYER                                 │
│  ┌─────────────────────────────────────────────────────────────────┐    │
│  │  Ports (Interfaces)                                              │    │
│  │  - ExternalPaymentProviderPort.java                              │    │
│  │  - TokenizationServicePort.java                                  │    │
│  │  - ExternalRefundProviderPort.java (NEW - to be created)        │    │
│  │  - WebhookProcessingPort.java (NEW - to be created)             │    │
│  └─────────────────────────────────────────────────────────────────┘    │
├─────────────────────────────────────────────────────────────────────────┤
│                       INFRASTRUCTURE LAYER                               │
│  ┌─────────────────────────────────────────────────────────────────┐    │
│  │  Provider Adapters ( interchangeable)                            │    │
│  │  - StubPaymentProvider.java (default, for testing)              │    │
│  │  - StripePaymentProvider.java (real implementation)             │    │
│  │  - PayPalPaymentProvider.java (alternative)                     │    │
│  │                                                                   │    │
│  │  Configuration                                                   │    │
│  │  - PaymentProviderConfig.java (NEW - conditional bean config)   │    │
│  └─────────────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────────────┘
```

### Key Principle: Dependency Inversion

```
Application Layer defines PORT (interface)
         ↑
         │ implements
         │
Infrastructure Layer provides ADAPTER (implementation)
```

---

## Part 1: Port Extensions (Application Layer)

### 1.1 ExternalPaymentProviderPort - Enhance

**Current Methods:**
- `authorize()` - Create payment authorization
- `capture()` - Capture authorized payment
- `cancel()` - Cancel/void payment
- `tokenizeCard()` - Tokenize card data

**Additions Needed:**
```java
public interface ExternalPaymentProviderPort {
    
    // Existing
    CompletableFuture<PaymentProviderResult> authorize(PaymentProviderRequest request);
    CompletableFuture<PaymentProviderResult> capture(PaymentProviderRequest request);
    CompletableFuture<PaymentProviderResult> cancel(PaymentProviderRequest request);
    CompletableFuture<String> tokenizeCard(CardTokenizationRequest request);
    
    // NEW: Get provider info
    String getProviderName();
    boolean isHealthy();
}
```

### 1.2 ExternalRefundProviderPort - NEW

**File:** `application/refund/port/out/ExternalRefundProviderPort.java`

```java
public interface ExternalRefundProviderPort {
    
    /**
     * Process a refund with the external provider.
     */
    CompletableFuture<RefundProviderResult> processRefund(RefundProviderRequest request);
    
    /**
     * Get refund status from provider.
     */
    CompletableFuture<RefundStatusResult> getRefundStatus(String providerRefundId);
    
    record RefundProviderRequest(
        String refundId,
        String originalPaymentId,
        String providerTransactionId,
        Long amount,
        String currency,
        String reason
    ) {}
    
    record RefundProviderResult(
        boolean success,
        String providerRefundId,
        String errorCode,
        String errorMessage
    ) {}
    
    record RefundStatusResult(
        String status,
        Instant processedAt
    ) {}
}
```

### 1.3 WebhookProcessingPort - NEW

**File:** `application/webhook/port/in/WebhookProcessingPort.java`

```java
public interface WebhookProcessingPort {
    
    /**
     * Process an incoming webhook from payment provider.
     */
    WebhookProcessingResult processWebhook(
        String providerName,
        String payload,
        Map<String, String> headers
    );
    
    /**
     * Verify webhook signature.
     */
    boolean verifySignature(
        String providerName,
        String payload,
        String signature,
        String secret
    );
    
    record WebhookProcessingResult(
        boolean processed,
        String eventType,
        String entityId,
        String errorMessage
    ) {}
}
```

### 1.4 PaymentMethodPort - NEW

**File:** `application/customer/port/out/PaymentMethodPort.java`

```java
public interface PaymentMethodPort {
    
    /**
     * Create a payment method with the provider.
     */
    CompletableFuture<PaymentMethodResult> createPaymentMethod(PaymentMethodRequest request);
    
    /**
     * Attach payment method to a customer.
     */
    CompletableFuture<Void> attachToCustomer(String paymentMethodToken, String providerCustomerId);
    
    /**
     * Detach/remove a payment method.
     */
    CompletableFuture<Void> detachPaymentMethod(String paymentMethodToken);
    
    record PaymentMethodRequest(
        String cardNumber,
        String expiryMonth,
        String expiryYear,
        String cvv,
        String cardholderName
    ) {}
    
    record PaymentMethodResult(
        String paymentMethodToken,
        String brand,
        String last4,
        int expiryMonth,
        int expiryYear
    ) {}
}
```

---

## Part 2: Stripe Implementation (Infrastructure Layer)

### 2.1 Directory Structure

```
infrastructure/
├── payment/
│   ├── adapter/
│   │   └── out/
│   │       └── provider/
│   │           ├── stripe/
│   │           │   ├── StripePaymentProvider.java       (enhance existing)
│   │           │   ├── StripeRefundProvider.java        (NEW)
│   │           │   ├── StripePaymentMethodAdapter.java  (NEW)
│   │           │   ├── StripeWebhookHandler.java        (NEW)
│   │           │   ├── StripeClientFactory.java         (NEW)
│   │           │   ├── StripeExceptionMapper.java       (NEW)
│   │           │   └── dto/
│   │           │       ├── StripePaymentIntentResponse.java
│   │           │       ├── StripeRefundResponse.java
│   │           │       └── StripeWebhookEvent.java
│   │           ├── StubPaymentProvider.java             (existing)
│   │           └── PayPalPaymentProvider.java           (existing)
│   └── config/
│       └── PaymentProviderConfig.java                   (NEW)
├── refund/
│   └── adapter/
│       └── out/
│           └── provider/
│               └── stripe/
│                   └── StripeRefundProviderAdapter.java (NEW)
└── webhook/
    └── adapter/
        └── in/
            └── rest/
                └── StripeWebhookController.java         (NEW)
```

### 2.2 PaymentProviderConfig.java - NEW

**Purpose:** Conditional bean registration based on configuration

```java
package com.payment.gateway.infrastructure.payment.config;

@Configuration
public class PaymentProviderConfig {
    
    @Bean
    @ConditionalOnProperty(name = "payment.provider", havingValue = "stripe")
    @Primary
    public ExternalPaymentProviderPort stripePaymentProvider(
            @Value("${stripe.api-key}") String apiKey,
            @Value("${stripe.api-base-url:https://api.stripe.com}") String apiBaseUrl) {
        return new StripePaymentProvider(apiKey, apiBaseUrl);
    }
    
    @Bean
    @ConditionalOnProperty(name = "payment.provider", havingValue = "paypal")
    @Primary
    public ExternalPaymentProviderPort paypalPaymentProvider(
            @Value("${paypal.client-id}") String clientId,
            @Value("${paypal.client-secret}") String clientSecret,
            @Value("${paypal.api-base-url}") String apiBaseUrl) {
        return new PayPalPaymentProvider(clientId, clientSecret, apiBaseUrl);
    }
    
    @Bean
    @ConditionalOnMissingBean(ExternalPaymentProviderPort.class)
    public ExternalPaymentProviderPort stubPaymentProvider() {
        return new StubPaymentProvider();
    }
    
    // Refund provider
    @Bean
    @ConditionalOnProperty(name = "payment.provider", havingValue = "stripe")
    public ExternalRefundProviderPort stripeRefundProvider(
            @Value("${stripe.api-key}") String apiKey) {
        return new StripeRefundProviderAdapter(apiKey);
    }
    
    @Bean
    @ConditionalOnMissingBean(ExternalRefundProviderPort.class)
    public ExternalRefundProviderPort stubRefundProvider() {
        return new StubRefundProvider();
    }
    
    // Webhook handler
    @Bean
    @ConditionalOnProperty(name = "payment.provider", havingValue = "stripe")
    public WebhookProcessingPort stripeWebhookHandler(
            @Value("${stripe.webhook-secret}") String webhookSecret) {
        return new StripeWebhookHandler(webhookSecret);
    }
}
```

### 2.3 StripePaymentProvider.java - Enhanced

**Key Implementation Details:**

```java
package com.payment.gateway.infrastructure.payment.adapter.out.provider.stripe;

@Slf4j
public class StripePaymentProvider implements ExternalPaymentProviderPort {

    private final StripeClient stripeClient;
    private final StripeExceptionMapper exceptionMapper;

    public StripePaymentProvider(String apiKey, String apiBaseUrl) {
        Stripe.apiKey = apiKey;
        this.stripeClient = StripeClient.builder()
            .apiKey(apiKey)
            .baseUrl(apiBaseUrl)
            .build();
        this.exceptionMapper = new StripeExceptionMapper();
    }

    @Override
    @CircuitBreaker(name = "paymentProvider", fallbackMethod = "authorizeFallback")
    @Retry(name = "paymentProvider")
    @TimeLimiter(name = "payment")
    @Bulkhead(name = "payment")
    @RateLimiter(name = "payment")
    @Async
    public CompletableFuture<PaymentProviderResult> authorize(PaymentProviderRequest request) {
        try {
            // 1. Create PaymentIntent
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(request.amount())
                .setCurrency(request.currency().toLowerCase())
                .setPaymentMethod(request.paymentMethodToken())
                .setCaptureMethod(PaymentIntentCreateParams.CaptureMethod.MANUAL)
                .setDescription("Payment for merchant: " + request.merchantId())
                .putMetadata("payment_id", request.paymentId())
                .putMetadata("merchant_id", request.merchantId())
                .build();
            
            PaymentIntent intent = PaymentIntent.create(params);
            
            return CompletableFuture.completedFuture(new PaymentProviderResult(
                true,
                intent.getId(),
                null,
                null
            ));
            
        } catch (StripeException e) {
            return CompletableFuture.completedFuture(
                exceptionMapper.mapException(e, request.paymentId())
            );
        }
    }

    @Override
    @CircuitBreaker(name = "paymentProvider", fallbackMethod = "captureFallback")
    @Retry(name = "paymentProvider")
    @TimeLimiter(name = "payment")
    @Bulkhead(name = "payment")
    @RateLimiter(name = "payment")
    @Async
    public CompletableFuture<PaymentProviderResult> capture(PaymentProviderRequest request) {
        try {
            PaymentIntent intent = PaymentIntent.retrieve(request.paymentId());
            
            PaymentIntentCaptureParams params = PaymentIntentCaptureParams.builder()
                .setAmountToCapture(request.amount())
                .build();
            
            intent.capture(params);
            
            return CompletableFuture.completedFuture(new PaymentProviderResult(
                true,
                intent.getId(),
                null,
                null
            ));
            
        } catch (StripeException e) {
            return CompletableFuture.completedFuture(
                exceptionMapper.mapException(e, request.paymentId())
            );
        }
    }

    @Override
    @CircuitBreaker(name = "paymentProvider", fallbackMethod = "cancelFallback")
    @Retry(name = "paymentProvider")
    @TimeLimiter(name = "payment")
    @Bulkhead(name = "payment")
    @RateLimiter(name = "payment")
    @Async
    public CompletableFuture<PaymentProviderResult> cancel(PaymentProviderRequest request) {
        try {
            PaymentIntent intent = PaymentIntent.retrieve(request.paymentId());
            
            PaymentIntentCancelParams params = PaymentIntentCancelParams.builder()
                .build();
            
            intent.cancel(params);
            
            return CompletableFuture.completedFuture(new PaymentProviderResult(
                true,
                intent.getId(),
                null,
                null
            ));
            
        } catch (StripeException e) {
            return CompletableFuture.completedFuture(
                exceptionMapper.mapException(e, request.paymentId())
            );
        }
    }

    @Override
    public String getProviderName() {
        return "STRIPE";
    }

    @Override
    public boolean isHealthy() {
        try {
            // Simple health check - retrieve balance
            Balance.retrieve();
            return true;
        } catch (Exception e) {
            log.error("Stripe health check failed", e);
            return false;
        }
    }
}
```

### 2.4 StripeRefundProviderAdapter.java - NEW

```java
package com.payment.gateway.infrastructure.refund.adapter.out.provider.stripe;

@Slf4j
public class StripeRefundProviderAdapter implements ExternalRefundProviderPort {

    private final String apiKey;

    public StripeRefundProviderAdapter(String apiKey) {
        Stripe.apiKey = apiKey;
        this.apiKey = apiKey;
    }

    @Override
    @CircuitBreaker(name = "refundProvider", fallbackMethod = "processRefundFallback")
    @Retry(name = "refundProvider")
    @TimeLimiter(name = "refund")
    @Async
    public CompletableFuture<RefundProviderResult> processRefund(RefundProviderRequest request) {
        try {
            RefundCreateParams params = RefundCreateParams.builder()
                .setPaymentIntent(request.providerTransactionId())
                .setAmount(request.amount())
                .setReason(mapReason(request.reason()))
                .putMetadata("refund_id", request.refundId())
                .build();
            
            Refund refund = Refund.create(params);
            
            return CompletableFuture.completedFuture(new RefundProviderResult(
                refund.getStatus().equals("succeeded"),
                refund.getId(),
                null,
                null
            ));
            
        } catch (StripeException e) {
            log.error("Stripe refund failed: {}", e.getMessage());
            return CompletableFuture.completedFuture(new RefundProviderResult(
                false,
                null,
                e.getCode(),
                e.getMessage()
            ));
        }
    }

    private String mapReason(String reason) {
        return switch (reason.toLowerCase()) {
            case "duplicate" -> "duplicate";
            case "fraudulent" -> "fraudulent";
            default -> "requested_by_customer";
        };
    }
}
```

### 2.5 StripeWebhookHandler.java - NEW

```java
package com.payment.gateway.infrastructure.webhook.adapter.in.handler;

@Slf4j
public class StripeWebhookHandler implements WebhookProcessingPort {

    private final String webhookSecret;
    private final ApplicationEventPublisher eventPublisher;

    public StripeWebhookHandler(String webhookSecret, ApplicationEventPublisher eventPublisher) {
        this.webhookSecret = webhookSecret;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public WebhookProcessingResult processWebhook(
            String providerName,
            String payload,
            Map<String, String> headers) {
        
        if (!"stripe".equalsIgnoreCase(providerName)) {
            return new WebhookProcessingResult(false, null, null, "Unknown provider");
        }

        try {
            String sigHeader = headers.get("Stripe-Signature");
            Event event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
            
            String eventType = event.getType();
            String entityId = extractEntityId(event);
            
            // Publish domain event
            eventPublisher.publishEvent(new StripeWebhookReceivedEvent(
                event.getId(),
                eventType,
                entityId,
                payload
            ));
            
            return new WebhookProcessingResult(true, eventType, entityId, null);
            
        } catch (SignatureVerificationException e) {
            log.error("Invalid webhook signature", e);
            return new WebhookProcessingResult(false, null, null, "Invalid signature");
        }
    }

    @Override
    public boolean verifySignature(String providerName, String payload, String signature, String secret) {
        try {
            Webhook.constructEvent(payload, signature, secret);
            return true;
        } catch (SignatureVerificationException e) {
            return false;
        }
    }
}
```

### 2.6 StripeExceptionMapper.java - NEW

```java
package com.payment.gateway.infrastructure.payment.adapter.out.provider.stripe;

public class StripeExceptionMapper {

    public PaymentProviderResult mapException(StripeException e, String paymentId) {
        String errorCode = mapErrorCode(e);
        String errorMessage = mapErrorMessage(e);
        
        return new PaymentProviderResult(
            false,
            paymentId,
            errorCode,
            errorMessage
        );
    }

    private String mapErrorCode(StripeException e) {
        if (e instanceof CardException ce) {
            return ce.getCode(); // e.g., "card_declined", "insufficient_funds"
        }
        if (e instanceof InvalidRequestException) {
            return "INVALID_REQUEST";
        }
        if (e instanceof AuthenticationException) {
            return "AUTHENTICATION_FAILED";
        }
        if (e instanceof RateLimitException) {
            return "RATE_LIMIT_EXCEEDED";
        }
        return "STRIPE_ERROR";
    }

    private String mapErrorMessage(StripeException e) {
        if (e instanceof CardException ce) {
            return ce.getMessage();
        }
        return e.getMessage();
    }
}
```

### 2.7 StripeWebhookController.java - NEW

```java
package com.payment.gateway.infrastructure.webhook.adapter.in.rest;

@RestController
@RequestMapping("/webhooks")
@RequiredArgsConstructor
public class StripeWebhookController {

    private final WebhookProcessingPort webhookProcessor;

    @PostMapping("/stripe")
    public ResponseEntity<Void> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String signature,
            @Value("${stripe.webhook-secret}") String webhookSecret) {
        
        Map<String, String> headers = Map.of("Stripe-Signature", signature);
        
        WebhookProcessingResult result = webhookProcessor.processWebhook(
            "stripe",
            payload,
            headers
        );
        
        if (result.processed()) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }
}
```

---

## Part 3: Configuration

### 3.1 application.yml Additions

```yaml
# Payment Provider Configuration
payment:
  provider: ${PAYMENT_PROVIDER:stub}  # Options: stub, stripe, paypal

# Stripe Configuration (only used when payment.provider=stripe)
stripe:
  api-key: ${STRIPE_API_KEY:}
  api-base-url: ${STRIPE_API_BASE_URL:https://api.stripe.com}
  webhook-secret: ${STRIPE_WEBHOOK_SECRET:}
  api-version: 2023-10-16
```

### 3.2 Environment Variables

```bash
# Provider Selection
PAYMENT_PROVIDER=stripe

# Stripe Credentials
STRIPE_API_KEY=sk_test_xxx
STRIPE_WEBHOOK_SECRET=whsec_xxx
```

---

## Part 4: Tests

### 4.1 Unit Tests

| Test File | Purpose |
|-----------|---------|
| `StripePaymentProviderTest.java` | Test Stripe API calls with mocks |
| `StripeRefundProviderAdapterTest.java` | Test refund processing |
| `StripeWebhookHandlerTest.java` | Test webhook handling |
| `StripeExceptionMapperTest.java` | Test error mapping |

### 4.2 Integration Tests

| Test File | Purpose |
|-----------|---------|
| `StripePaymentIntegrationTest.java` | Test with Stripe test mode |
| `StripeWebhookIntegrationTest.java` | Test webhook flow end-to-end |

### 4.3 Example Test

```java
@ExtendWith(MockitoExtension.class)
class StripePaymentProviderTest {

    @Mock
    private PaymentIntent mockPaymentIntent;
    
    private StripePaymentProvider provider;

    @BeforeEach
    void setUp() {
        provider = new StripePaymentProvider("sk_test_xxx", "https://api.stripe.com");
    }

    @Test
    void authorize_shouldReturnSuccess_whenStripeSucceeds() {
        // Given
        PaymentProviderRequest request = new PaymentProviderRequest(
            "pay_123", "merch_abc", 10000L, "USD", "pm_card_visa"
        );
        
        // When
        CompletableFuture<PaymentProviderResult> result = provider.authorize(request);
        
        // Then
        assertThat(result.join().success()).isTrue();
    }
}
```

---

## Part 5: Documentation

### 5.1 docs/STRIPE_INTEGRATION.md

**Contents:**
1. Prerequisites
   - Stripe account setup
   - API keys (test vs production)
2. Configuration
   - Environment variables
   - Provider switching
3. Supported Operations
   - Payment authorization
   - Capture
   - Cancel
   - Refund
   - Webhooks
4. Testing
   - Using Stripe CLI
   - Test card numbers
   - Webhook testing
5. Production Checklist
6. Error Handling
7. Rate Limits
8. Security Best Practices

---

## Part 6: Implementation Order

| Order | Task | Layer | Est. Time |
|-------|------|-------|-----------|
| 1 | Add Stripe SDK dependency | Infrastructure | 10 min |
| 2 | Create ExternalRefundProviderPort | Application | 20 min |
| 3 | Create WebhookProcessingPort | Application | 20 min |
| 4 | Create PaymentMethodPort | Application | 15 min |
| 5 | Create PaymentProviderConfig | Infrastructure | 30 min |
| 6 | Implement StripePaymentProvider | Infrastructure | 1.5 hours |
| 7 | Implement StripeRefundProviderAdapter | Infrastructure | 45 min |
| 8 | Implement StripeWebhookHandler | Infrastructure | 45 min |
| 9 | Implement StripeExceptionMapper | Infrastructure | 20 min |
| 10 | Create StripeWebhookController | Infrastructure | 30 min |
| 11 | Update application.yml | Configuration | 10 min |
| 12 | Write unit tests | Test | 1.5 hours |
| 13 | Write integration tests | Test | 1 hour |
| 14 | Create STRIPE_INTEGRATION.md | Documentation | 45 min |
| **Total** | | | **~8 hours** |

---

## Part 7: Switching Providers

### To Stripe:

```yaml
payment:
  provider: stripe
stripe:
  api-key: ${STRIPE_API_KEY}
  webhook-secret: ${STRIPE_WEBHOOK_SECRET}
```

### To PayPal:

```yaml
payment:
  provider: paypal
paypal:
  client-id: ${PAYPAL_CLIENT_ID}
  client-secret: ${PAYPAL_CLIENT_SECRET}
```

### To Stub (Development):

```yaml
payment:
  provider: stub
```

---

## Part 8: Key Design Decisions

### 1. Port Interface Location
- **Decision:** Ports in `application/*/port/out/` package
- **Rationale:** Application layer defines what it needs, infrastructure provides it

### 2. No Stripe Dependencies in Domain
- **Decision:** Domain layer has ZERO Stripe imports
- **Rationale:** Domain remains pure and testable

### 3. Conditional Bean Registration
- **Decision:** Use `@ConditionalOnProperty` for provider selection
- **Rationale:** Runtime provider switching without code changes

### 4. Exception Mapping
- **Decision:** Map Stripe exceptions to domain-agnostic error codes
- **Rationale:** Application layer doesn't need to know about Stripe errors

### 5. Webhook as Separate Concern
- **Decision:** Separate webhook handling from payment processing
- **Rationale:** Different lifecycle and error handling requirements

---

## Verification Checklist

- [ ] No Stripe imports in domain layer
- [ ] Ports defined in application layer
- [ ] Implementations in infrastructure layer
- [ ] Provider switchable via configuration
- [ ] All resilience patterns applied
- [ ] Exception mapping in place
- [ ] Webhook signature verification
- [ ] Unit tests with mocked Stripe
- [ ] Integration tests with test mode
- [ ] Documentation complete