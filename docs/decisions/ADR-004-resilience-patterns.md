# ADR-004: Resilience Patterns with Resilience4j

## Status

Accepted

## Context

Payment systems require high availability and resilience. Common failures include:
- External payment gateway timeouts
- Database connection failures
- Rate limiting from external APIs
- Cascading failures from downstream services

We needed a comprehensive resilience strategy to:
- Prevent cascading failures
- Handle transient failures gracefully
- Protect system resources
- Maintain service availability

## Decision

We implemented **Resilience4j** resilience patterns:

### Patterns Implemented

| Pattern | Purpose | Configuration |
|---------|---------|---------------|
| **Circuit Breaker** | Prevent cascading failures | Failure threshold: 50% |
| **Retry** | Handle transient failures | Max 3 attempts, exponential backoff |
| **Rate Limiter** | Protect resources | 100 requests/minute |
| **Bulkhead** | Isolate failures | Max 10 concurrent calls |
| **Time Limiter** | Prevent hanging calls | 5 second timeout |

### Circuit Breaker States

```
     ┌──────────────┐
     │    CLOSED    │ ◀──── Normal operation
     └──────┬───────┘
            │ Failure rate > 50%
            ▼
     ┌──────────────┐
     │     OPEN     │ ◀──── Failing fast
     └──────┬───────┘
            │ After 30 seconds
            ▼
     ┌──────────────┐
     │  HALF-OPEN   │ ◀──── Testing recovery
     └──────────────┘
```

### Implementation Example

```java
@Service
public class PaymentGatewayAdapter {
    
    @CircuitBreaker(name = "paymentGateway", fallbackMethod = "fallback")
    @Retry(name = "paymentGateway")
    @RateLimiter(name = "paymentGateway")
    @Bulkhead(name = "paymentGateway")
    public PaymentResponse processPayment(PaymentRequest request) {
        // External gateway call
    }
    
    public PaymentResponse fallback(PaymentRequest request, Exception e) {
        // Fallback logic
    }
}
```

## Consequences

### Positive
- System remains available during partial failures
- Graceful degradation with fallbacks
- Protected from overload
- Self-healing capabilities
- Configurable per-service

### Negative
- Configuration complexity
- Need to tune thresholds per service
- Fallback logic adds code complexity
- Testing resilience is harder

### Mitigation
- Document configuration for each service
- Use resilience tests in CI/CD
- Monitor circuit breaker states
- Adjust thresholds based on production metrics

## Configuration

```yaml
resilience4j:
  circuitbreaker:
    instances:
      paymentGateway:
        sliding-window-size: 10
        failure-rate-threshold: 50
        wait-duration-in-open-state: 30s
  retry:
    instances:
      paymentGateway:
        max-attempts: 3
        wait-duration: 500ms
  ratelimiter:
    instances:
      paymentGateway:
        limit-for-period: 100
        limit-refresh-period: 60s
```

## References

- [Resilience4j Documentation](https://resilience4j.readme.io/)
- [Circuit Breaker Pattern - Martin Fowler](https://martinfowler.com/bliki/CircuitBreaker.html)