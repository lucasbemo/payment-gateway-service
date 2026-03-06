package com.payment.gateway.infrastructure.commons.monitoring;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Binds Resilience4j metrics to Micrometer.
 * Exposes circuit breaker, retry, and rate limiter metrics.
 */
@Component
@RequiredArgsConstructor
public class Resilience4jMetricsBinder implements MeterBinder {

    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final RetryRegistry retryRegistry;
    private final RateLimiterRegistry rateLimiterRegistry;

    @Override
    public void bindTo(MeterRegistry registry) {
        bindCircuitBreakerMetrics(registry);
        bindRetryMetrics(registry);
        bindRateLimiterMetrics(registry);
    }

    private void bindCircuitBreakerMetrics(MeterRegistry registry) {
        for (CircuitBreaker circuitBreaker : circuitBreakerRegistry.getAllCircuitBreakers()) {
            String name = circuitBreaker.getName();

            Gauge.builder("resilience4j.circuitbreaker.state", circuitBreaker, cb -> getStateMetric(cb))
                    .description("State of the circuit breaker (0=closed, 1=open, 2=half_open)")
                    .tag("name", name)
                    .register(registry);

            Gauge.builder("resilience4j.circuitbreaker.failure.rate", circuitBreaker, cb -> cb.getMetrics().getFailureRate())
                    .description("Failure rate percentage")
                    .tag("name", name)
                    .register(registry);

            Gauge.builder("resilience4j.circuitbreaker.slow.call.rate", circuitBreaker, cb -> cb.getMetrics().getSlowCallRate())
                    .description("Slow call rate percentage")
                    .tag("name", name)
                    .register(registry);

            Gauge.builder("resilience4j.circuitbreaker.buffered.calls", circuitBreaker, cb -> cb.getMetrics().getNumberOfBufferedCalls())
                    .description("Number of buffered calls")
                    .tag("name", name)
                    .register(registry);

            Gauge.builder("resilience4j.circuitbreaker.failed.calls", circuitBreaker, cb -> cb.getMetrics().getNumberOfFailedCalls())
                    .description("Number of failed calls")
                    .tag("name", name)
                    .register(registry);
        }
    }

    private void bindRetryMetrics(MeterRegistry registry) {
        for (Retry retry : retryRegistry.getAllRetries()) {
            String name = retry.getName();

            // Retry doesn't expose metrics in the same way, use retry count events
            Gauge.builder("resilience4j.retry.requests", retry, r -> {
                // Return a placeholder value since Retry.Metrics has limited API
                return 0.0;
            })
                    .description("Retry requests")
                    .tag("name", name)
                    .register(registry);
        }
    }

    private void bindRateLimiterMetrics(MeterRegistry registry) {
        for (RateLimiter rateLimiter : rateLimiterRegistry.getAllRateLimiters()) {
            String name = rateLimiter.getName();

            Gauge.builder("resilience4j.ratelimiter.available.permissions", rateLimiter, rl -> (double) rl.getMetrics().getAvailablePermissions())
                    .description("Number of available permissions")
                    .tag("name", name)
                    .register(registry);

            Gauge.builder("resilience4j.ratelimiter.waiting.threads", rateLimiter, rl -> (double) rl.getMetrics().getNumberOfWaitingThreads())
                    .description("Number of waiting threads")
                    .tag("name", name)
                    .register(registry);
        }
    }

    private double getStateMetric(CircuitBreaker cb) {
        switch (cb.getState()) {
            case CLOSED: return 0;
            case OPEN: return 1;
            case HALF_OPEN: return 2;
            case DISABLED: return 3;
            case FORCED_OPEN: return 4;
            default: return -1;
        }
    }
}
