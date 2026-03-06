package com.payment.gateway.infrastructure.commons.resilience;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for Resilience4j configuration including Circuit Breaker, Retry, Rate Limiter,
 * Bulkhead, and Time Limiter patterns.
 */
@DisplayName("Resilience4j Configuration Tests")
@SpringBootTest(classes = {
        CircuitBreakerConfig.class,
        RetryConfig.class,
        RateLimiterConfig.class,
        BulkheadConfig.class,
        TimeLimiterConfig.class
})
@ActiveProfiles("test")
class Resilience4jConfigTest {

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @Autowired
    private RetryRegistry retryRegistry;

    @Autowired
    private RateLimiterRegistry rateLimiterRegistry;

    @Autowired
    private BulkheadRegistry bulkheadRegistry;

    @Autowired
    private TimeLimiterRegistry timeLimiterRegistry;

    @Nested
    @DisplayName("Circuit Breaker Tests")
    class CircuitBreakerTests {

        @Test
        @DisplayName("Should have paymentProvider circuit breaker configured")
        void shouldHavePaymentProviderCircuitBreaker() {
            CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("paymentProvider");

            assertThat(circuitBreaker).isNotNull();
            assertThat(circuitBreaker.getCircuitBreakerConfig().getFailureRateThreshold()).isEqualTo(50f);
            assertThat(circuitBreaker.getCircuitBreakerConfig().getSlidingWindowSize()).isEqualTo(10);
            assertThat(circuitBreaker.getCircuitBreakerConfig().getMinimumNumberOfCalls()).isEqualTo(5);
        }

        @Test
        @DisplayName("Should have externalService circuit breaker configured")
        void shouldHaveExternalServiceCircuitBreaker() {
            CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("externalService");

            assertThat(circuitBreaker).isNotNull();
        }
    }

    @Nested
    @DisplayName("Retry Tests")
    class RetryTests {

        @Test
        @DisplayName("Should have paymentProvider retry configured")
        void shouldHavePaymentProviderRetry() {
            Retry retry = retryRegistry.retry("paymentProvider");

            assertThat(retry).isNotNull();
            assertThat(retry.getRetryConfig().getMaxAttempts()).isEqualTo(3);
        }

        @Test
        @DisplayName("Should have externalService retry configured")
        void shouldHaveExternalServiceRetry() {
            Retry retry = retryRegistry.retry("externalService");

            assertThat(retry).isNotNull();
        }
    }

    @Nested
    @DisplayName("Rate Limiter Tests")
    class RateLimiterTests {

        @Test
        @DisplayName("Should have api rate limiter configured")
        void shouldHaveApiRateLimiter() {
            RateLimiter rateLimiter = rateLimiterRegistry.rateLimiter("api");

            assertThat(rateLimiter).isNotNull();
            assertThat(rateLimiter.getRateLimiterConfig().getLimitForPeriod()).isEqualTo(100);
        }

        @Test
        @DisplayName("Should have payment rate limiter configured")
        void shouldHavePaymentRateLimiter() {
            RateLimiter rateLimiter = rateLimiterRegistry.rateLimiter("payment");

            assertThat(rateLimiter).isNotNull();
            assertThat(rateLimiter.getRateLimiterConfig().getLimitForPeriod()).isEqualTo(50);
        }
    }

    @Nested
    @DisplayName("Bulkhead Tests")
    class BulkheadTests {

        @Test
        @DisplayName("Should have payment bulkhead configured")
        void shouldHavePaymentBulkhead() {
            Bulkhead bulkhead = bulkheadRegistry.bulkhead("payment");

            assertThat(bulkhead).isNotNull();
            assertThat(bulkhead.getBulkheadConfig().getMaxConcurrentCalls()).isEqualTo(25);
        }

        @Test
        @DisplayName("Should have externalService bulkhead configured")
        void shouldHaveExternalServiceBulkhead() {
            Bulkhead bulkhead = bulkheadRegistry.bulkhead("externalService");

            assertThat(bulkhead).isNotNull();
            assertThat(bulkhead.getBulkheadConfig().getMaxConcurrentCalls()).isEqualTo(10);
        }
    }

    @Nested
    @DisplayName("Time Limiter Tests")
    class TimeLimiterTests {

        @Test
        @DisplayName("Should have payment time limiter configured")
        void shouldHavePaymentTimeLimiter() {
            TimeLimiter timeLimiter = timeLimiterRegistry.timeLimiter("payment");

            assertThat(timeLimiter).isNotNull();
            assertThat(timeLimiter.getTimeLimiterConfig().getTimeoutDuration())
                    .isEqualTo(java.time.Duration.ofSeconds(5));
        }

        @Test
        @DisplayName("Should have externalService time limiter configured")
        void shouldHaveExternalServiceTimeLimiter() {
            TimeLimiter timeLimiter = timeLimiterRegistry.timeLimiter("externalService");

            assertThat(timeLimiter).isNotNull();
            assertThat(timeLimiter.getTimeLimiterConfig().getTimeoutDuration())
                    .isEqualTo(java.time.Duration.ofSeconds(10));
        }
    }

    @Nested
    @DisplayName("Combined Resilience Tests")
    class CombinedResilienceTests {

        @Test
        @DisplayName("Should have all resilience patterns configured")
        void shouldHaveAllResiliencePatternsConfigured() {
            assertThat(circuitBreakerRegistry).isNotNull();
            assertThat(retryRegistry).isNotNull();
            assertThat(rateLimiterRegistry).isNotNull();
            assertThat(bulkheadRegistry).isNotNull();
            assertThat(timeLimiterRegistry).isNotNull();
        }

        @Test
        @DisplayName("Should create circuit breakers for all configured names")
        void shouldCreateCircuitBreakersForAllNames() {
            CircuitBreaker paymentProvider = circuitBreakerRegistry.circuitBreaker("paymentProvider");
            CircuitBreaker externalService = circuitBreakerRegistry.circuitBreaker("externalService");

            assertThat(paymentProvider).isNotNull();
            assertThat(externalService).isNotNull();
        }

        @Test
        @DisplayName("Should create retries for all configured names")
        void shouldCreateRetriesForAllNames() {
            Retry paymentProvider = retryRegistry.retry("paymentProvider");
            Retry externalService = retryRegistry.retry("externalService");

            assertThat(paymentProvider).isNotNull();
            assertThat(externalService).isNotNull();
        }

        @Test
        @DisplayName("Should create rate limiters for all configured names")
        void shouldCreateRateLimitersForAllNames() {
            RateLimiter api = rateLimiterRegistry.rateLimiter("api");
            RateLimiter payment = rateLimiterRegistry.rateLimiter("payment");

            assertThat(api).isNotNull();
            assertThat(payment).isNotNull();
        }

        @Test
        @DisplayName("Should create bulkheads for all configured names")
        void shouldCreateBulkheadsForAllNames() {
            Bulkhead payment = bulkheadRegistry.bulkhead("payment");
            Bulkhead externalService = bulkheadRegistry.bulkhead("externalService");

            assertThat(payment).isNotNull();
            assertThat(externalService).isNotNull();
        }

        @Test
        @DisplayName("Should create time limiters for all configured names")
        void shouldCreateTimeLimitersForAllNames() {
            TimeLimiter payment = timeLimiterRegistry.timeLimiter("payment");
            TimeLimiter externalService = timeLimiterRegistry.timeLimiter("externalService");

            assertThat(payment).isNotNull();
            assertThat(externalService).isNotNull();
        }
    }
}
