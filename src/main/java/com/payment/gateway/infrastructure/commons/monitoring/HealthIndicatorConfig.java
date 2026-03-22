package com.payment.gateway.infrastructure.commons.monitoring;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Optional;

/**
 * Custom health indicators for monitoring subsystem health.
 */
@Configuration
public class HealthIndicatorConfig {

    private final RedisConnectionFactory redisConnectionFactory;
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final RateLimiterRegistry rateLimiterRegistry;
    private final Optional<KafkaTemplate<String, Object>> kafkaTemplate;

    public HealthIndicatorConfig(RedisConnectionFactory redisConnectionFactory,
                                  CircuitBreakerRegistry circuitBreakerRegistry,
                                  RateLimiterRegistry rateLimiterRegistry,
                                  Optional<KafkaTemplate<String, Object>> kafkaTemplate) {
        this.redisConnectionFactory = redisConnectionFactory;
        this.circuitBreakerRegistry = circuitBreakerRegistry;
        this.rateLimiterRegistry = rateLimiterRegistry;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Bean
    public HealthIndicator redisHealthIndicator() {
        return () -> {
            try {
                redisConnectionFactory.getConnection().ping();
                return Health.up()
                        .withDetail("component", "redis")
                        .withDetail("status", "connected")
                        .build();
            } catch (Exception e) {
                return Health.down()
                        .withDetail("component", "redis")
                        .withException(e)
                        .build();
            }
        };
    }

    @Bean
    public HealthIndicator kafkaHealthIndicator() {
        return () -> {
            if (kafkaTemplate.isEmpty()) {
                return Health.unknown()
                        .withDetail("component", "kafka")
                        .withDetail("status", "not configured")
                        .build();
            }
            try {
                kafkaTemplate.get().send("health-check-topic", "health-check").get();
                return Health.up()
                        .withDetail("component", "kafka")
                        .withDetail("status", "connected")
                        .build();
            } catch (Exception e) {
                return Health.down()
                        .withDetail("component", "kafka")
                        .withDetail("error", e.getMessage())
                        .build();
            }
        };
    }

    @Bean
    public HealthIndicator paymentProviderHealthIndicator() {
        return () -> Health.up()
                .withDetail("component", "payment-provider")
                .withDetail("provider", "stub")
                .build();
    }

    @Bean
    public HealthIndicator circuitBreakerHealthIndicator() {
        return () -> {
            try {
                var circuitBreakers = circuitBreakerRegistry.getAllCircuitBreakers();
                var details = new java.util.HashMap<String, Object>();
                int healthyCount = 0;

                for (var cb : circuitBreakers) {
                    var metrics = cb.getMetrics();
                    var cbDetails = new java.util.HashMap<String, Object>();
                    cbDetails.put("state", cb.getState().name());
                    cbDetails.put("failureRate", metrics.getFailureRate());
                    cbDetails.put("slowCallRate", metrics.getSlowCallRate());
                    if (cb.getState().name().equals("CLOSED") || cb.getState().name().equals("HALF_OPEN")) {
                        healthyCount++;
                    }
                    details.put(cb.getName(), cbDetails);
                }

                details.put("healthyCount", healthyCount);
                details.put("totalCount", circuitBreakers.size());

                if (healthyCount == circuitBreakers.size()) {
                    return Health.up()
                            .withDetail("component", "circuit-breaker")
                            .withDetails(details)
                            .build();
                } else {
                    return Health.status("DEGRADED")
                            .withDetail("component", "circuit-breaker")
                            .withDetails(details)
                            .build();
                }
            } catch (Exception e) {
                return Health.down()
                        .withDetail("component", "circuit-breaker")
                        .withException(e)
                        .build();
            }
        };
    }

    @Bean
    public HealthIndicator rateLimiterHealthIndicator() {
        return () -> {
            try {
                var rateLimiters = rateLimiterRegistry.getAllRateLimiters();
                var details = new java.util.HashMap<String, Object>();

                for (var rl : rateLimiters) {
                    var metrics = rl.getMetrics();
                    var rlDetails = new java.util.HashMap<String, Object>();
                    rlDetails.put("availablePermissions", metrics.getAvailablePermissions());
                    rlDetails.put("waitingThreads", metrics.getNumberOfWaitingThreads());
                    details.put(rl.getName(), rlDetails);
                }

                return Health.up()
                        .withDetail("component", "rate-limiter")
                        .withDetails(details)
                        .build();
            } catch (Exception e) {
                return Health.down()
                        .withDetail("component", "rate-limiter")
                        .withException(e)
                        .build();
            }
        };
    }
}
