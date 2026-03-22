# Observability Issues Fix Plan

**Created:** 2026-03-22  
**Status:** Ready for Implementation  
**Based on:** OBSERVABILITY_TEST_REPORT.md  

---

## Issues Summary

| Priority | Issue | Impact |
|----------|-------|--------|
| **HIGH** | CustomMetricsBinder not integrated | Business metrics not tracked |
| **MEDIUM** | Traces not reaching Zipkin | No distributed tracing |
| **LOW** | audit.log file empty | Audit entries in wrong file |
| **LOW** | Kafka health shows "unknown" | Health check incomplete |

---

## Issue 1: CustomMetricsBinder Not Integrated (HIGH)

### Problem
`CustomMetricsBinder` is defined in `infrastructure/commons/monitoring/` with methods to record:
- `recordPaymentProcessed()`
- `recordPaymentApproved()`
- `recordPaymentRejected()`
- `recordPaymentFailed()`
- `recordPaymentAmount(long amountCents)`

However, it's **NOT injected** into any service layer class, so the counters remain at 0.

### Solution

#### Step 1: Inject CustomMetricsBinder into ProcessPaymentService

**File:** `src/main/java/com/payment/gateway/application/payment/service/ProcessPaymentService.java`

```java
// Add import
import com.payment.gateway.infrastructure.commons.monitoring.CustomMetricsBinder;

// Add field
private final CustomMetricsBinder metricsBinder;

// Update constructor
public ProcessPaymentService(
        PaymentRepositoryPort paymentRepository,
        TransactionRepositoryPort transactionRepository,
        MerchantRepositoryPort merchantRepository,
        ExternalPaymentProviderPort paymentProvider,
        IdempotencyDomainService idempotencyService,
        OutboxEventDomainService outboxService,
        CustomMetricsBinder metricsBinder) {  // Add this
    // ... existing assignments
    this.metricsBinder = metricsBinder;
}
```

#### Step 2: Call metrics methods in ProcessPaymentService

```java
// In processPayment() method after successful payment:
metricsBinder.recordPaymentApproved();
metricsBinder.recordPaymentAmount(request.amountInCents());

// In processPayment() method on failure:
metricsBinder.recordPaymentFailed();

// In processPayment() method on rejection:
metricsBinder.recordPaymentRejected();
```

#### Step 3: Inject CustomMetricsBinder into ProcessRefundService

**File:** `src/main/java/com/payment/gateway/application/refund/service/ProcessRefundService.java`

Similar injection pattern:
```java
private final CustomMetricsBinder metricsBinder;

// After successful refund:
metricsBinder.recordRefundApproved();
metricsBinder.recordRefundAmount(request.amountInCents());
```

#### Step 4: Update Unit Tests

- Update `ProcessPaymentServiceTest.java` to mock `CustomMetricsBinder`
- Add tests verifying metrics are called

### Files to Modify

| File | Change |
|------|--------|
| `ProcessPaymentService.java` | Inject and use CustomMetricsBinder |
| `ProcessRefundService.java` | Inject and use CustomMetricsBinder |
| `ProcessPaymentServiceTest.java` | Add mock and verify calls |
| `ProcessRefundServiceTest.java` | Add mock and verify calls |

---

## Issue 2: Traces Not Reaching Zipkin (MEDIUM)

### Problem
Zipkin is running and healthy, but no traces are being captured. The application has:
- `micrometer-tracing-bridge-brave` dependency
- `zipkin-reporter-brave` dependency
- TracingConfig.java with aspects

But traces are not reaching Zipkin.

### Root Cause Analysis

1. Check `application.yml` - Zipkin endpoint configured as `http://zipkin:9411/api/v2/spans`
2. In Docker, the service may need to resolve `zipkin` hostname
3. The tracing aspects may not be creating spans correctly

### Solution

#### Step 1: Verify Zipkin Hostname Resolution

**File:** `src/main/resources/application.yml`

```yaml
# Current
management:
  zipkin:
    tracing:
      endpoint: ${ZIPKIN_BASE_URL:http://zipkin:9411}/api/v2/spans

# Should work if 'zipkin' resolves in Docker network
# For local testing, use localhost:
management:
  zipkin:
    tracing:
      endpoint: ${ZIPKIN_BASE_URL:http://localhost:9411}/api/v2/spans
```

#### Step 2: Add Logging for Tracing

**File:** `src/main/resources/application.yml`

```yaml
logging:
  level:
    io.micrometer.tracing: DEBUG
    brave: DEBUG
```

#### Step 3: Verify Tracing Aspects Are Being Applied

The `TracingConfig.java` has aspects but they may not be Spring-managed beans properly.

**File:** `src/main/java/com/payment/gateway/infrastructure/commons/monitoring/TracingConfig.java`

Ensure aspects are registered as beans:

```java
@Configuration
public class TracingConfig {

    @Bean
    public PaymentTracingAspect paymentTracingAspect(Tracer tracer) {
        return new PaymentTracingAspect(tracer);
    }

    @Bean
    public RefundTracingAspect refundTracingAspect(Tracer tracer) {
        return new RefundTracingAspect(tracer);
    }

    @Bean
    public TransactionTracingAspect transactionTracingAspect(Tracer tracer) {
        return new TransactionTracingAspect(tracer);
    }
}
```

#### Step 4: Add RestTemplate/WebClient Instrumentation

If using RestTemplate for external calls, add instrumentation:

```java
@Bean
public RestTemplate restTemplate(TracingTracingContextInjector injector) {
    RestTemplate restTemplate = new RestTemplate();
    // Add tracing interceptor
    return restTemplate;
}
```

### Files to Modify

| File | Change |
|------|--------|
| `application.yml` | Add DEBUG logging for tracing |
| `TracingConfig.java` | Verify bean registration |
| `HttpClientConfig.java` | Add tracing to HTTP client |

---

## Issue 3: audit.log File Empty (LOW)

### Problem
The `audit.log` file exists but is 0 bytes. Audit entries are being logged to `payment-gateway.log` instead.

### Root Cause
The logback configuration has:
- AUDIT logger pointing to AUDIT_FILE appender
- But AuditLogger uses `log.info(AUDIT_LOG_NAME, ...)` which may not match

### Solution

#### Step 1: Fix AuditLogger Usage

**File:** `src/main/java/com/payment/gateway/infrastructure/commons/monitoring/AuditLogger.java`

Current:
```java
log.info(AUDIT_LOG_NAME, "PAYMENT_OPERATION | ...");
```

The issue is that `log.info(marker, message)` signature may not work as expected.

Fix:
```java
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

private static final Logger AUDIT_LOG = LoggerFactory.getLogger("AUDIT");

public void logPaymentOperation(...) {
    AUDIT_LOG.info("PAYMENT_OPERATION | paymentId={} | merchantId={} | ...", ...);
}
```

#### Step 2: Verify logback-spring.xml Configuration

**File:** `src/main/resources/logback-spring.xml`

```xml
<!-- The AUDIT logger should capture all INFO and above -->
<logger name="AUDIT" level="INFO" additivity="false">
    <appender-ref ref="AUDIT_FILE"/>
    <appender-ref ref="CONSOLE"/>
</logger>
```

Ensure the appender is correct:
```xml
<appender name="AUDIT_FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <file>${LOG_DIR}/audit.log</file>
    <!-- ... rolling policy ... -->
</appender>
```

### Files to Modify

| File | Change |
|------|--------|
| `AuditLogger.java` | Use dedicated Logger for "AUDIT" |
| `logback-spring.xml` | Verify logger/appender config |

---

## Issue 4: Kafka Health Shows "Unknown" (LOW)

### Problem
The Kafka health indicator shows `status: "UNKNOWN"` with `details: {"status": "not configured"}`.

### Root Cause
In `HealthIndicatorConfig.java`:
```java
public HealthIndicatorConfig(..., Optional<KafkaTemplate<String, String>> kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
}

public HealthIndicator kafkaHealthIndicator() {
    if (kafkaTemplate.isEmpty()) {
        return Health.unknown().withDetail("status", "not configured").build();
    }
    // ...
}
```

The `KafkaTemplate` is optional and may not be injected.

### Solution

#### Step 1: Verify KafkaTemplate Bean Exists

Check if `KafkaTemplate` is properly configured as a bean in `KafkaProducerConfig.java` or auto-configured by Spring.

#### Step 2: Remove Optional Wrapper

**File:** `src/main/java/com/payment/gateway/infrastructure/commons/monitoring/HealthIndicatorConfig.java`

```java
// Change from Optional to direct injection
private final KafkaTemplate<String, String> kafkaTemplate;

public HealthIndicatorConfig(
        RedisConnectionFactory redisConnectionFactory,
        CircuitBreakerRegistry circuitBreakerRegistry,
        RateLimiterRegistry rateLimiterRegistry,
        KafkaTemplate<String, String> kafkaTemplate) {  // Remove Optional
    this.redisConnectionFactory = redisConnectionFactory;
    this.circuitBreakerRegistry = circuitBreakerRegistry;
    this.rateLimiterRegistry = rateLimiterRegistry;
    this.kafkaTemplate = kafkaTemplate;
}

@Bean
public HealthIndicator kafkaHealthIndicator() {
    return () -> {
        try {
            kafkaTemplate.send("health-check-topic", "health-check").get();
            return Health.up()
                    .withDetail("component", "kafka")
                    .withDetail("status", "connected")
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("component", "kafka")
                    .withException(e)
                    .build();
        }
    };
}
```

#### Step 3: Add @ConditionalOnBean

```java
@Bean
@ConditionalOnBean(KafkaTemplate.class)
public HealthIndicator kafkaHealthIndicator() {
    // ...
}
```

### Files to Modify

| File | Change |
|------|--------|
| `HealthIndicatorConfig.java` | Remove Optional, inject KafkaTemplate directly |

---

## Execution Order

| Order | Issue | Priority | Est. Time |
|-------|-------|----------|-----------|
| 1 | CustomMetricsBinder integration | HIGH | 30 min |
| 2 | Kafka health indicator fix | LOW | 15 min |
| 3 | AuditLogger fix | LOW | 15 min |
| 4 | Zipkin tracing fix | MEDIUM | 45 min |
| 5 | Run tests and verify | - | 15 min |
| **Total** | | | **~2 hours** |

---

## Verification Steps

After implementing fixes:

1. **Custom Metrics:**
   ```bash
   # Process a payment
   curl -X POST http://localhost:8085/api/v1/payments ...
   
   # Verify metrics incremented
   curl -s http://localhost:8085/actuator/prometheus | grep payment_gateway_payments_processed_total
   # Expected: counter > 0
   ```

2. **Distributed Tracing:**
   ```bash
   # Make a request
   curl -X POST http://localhost:8085/api/v1/payments ...
   
   # Check Zipkin
   curl -s "http://localhost:9411/api/v2/traces?serviceName=payment-gateway" | jq '.[0]'
   # Expected: trace with spans
   ```

3. **Audit Log:**
   ```bash
   # Check audit.log has entries
   tail -5 ./logs/audit.log
   # Expected: JSON audit entries
   ```

4. **Kafka Health:**
   ```bash
   curl -s http://localhost:8085/actuator/health | jq '.components.kafka'
   # Expected: {"status": "UP", "details": {"status": "connected"}}
   ```

---

## Risk Assessment

| Risk | Likelihood | Mitigation |
|------|------------|------------|
| Breaking existing tests | Medium | Run full test suite after changes |
| Metrics not incrementing | Low | Verify with integration test |
| Zipkin still not working | Medium | Check Docker network connectivity |
| Audit log still empty | Low | Verify Logger name matches logback config |

---

## Notes

- All fixes should be implemented in separate commits
- Run tests after each fix
- Update OBSERVABILITY_TEST_REPORT.md after fixes