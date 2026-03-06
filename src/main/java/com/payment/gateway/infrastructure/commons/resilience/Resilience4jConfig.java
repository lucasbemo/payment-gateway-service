package com.payment.gateway.infrastructure.commons.resilience;

import org.springframework.context.annotation.Configuration;

/**
 * Main Resilience4j configuration.
 * Imports all resilience sub-configurations.
 * Resilience4j settings are primarily defined in application.yml.
 */
@Configuration
public class Resilience4jConfig {
    // Resilience4j is auto-configured via spring-boot3 starter.
    // Sub-configurations (CircuitBreaker, Retry, RateLimiter, Bulkhead, TimeLimiter)
    // are defined as separate config classes for clarity.
}
