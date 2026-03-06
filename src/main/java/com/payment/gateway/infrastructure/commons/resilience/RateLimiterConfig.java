package com.payment.gateway.infrastructure.commons.resilience;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Rate limiter configuration for API throttling.
 */
@Configuration
public class RateLimiterConfig {

    @Bean
    public RateLimiterRegistry rateLimiterRegistry() {
        io.github.resilience4j.ratelimiter.RateLimiterConfig defaultConfig =
                io.github.resilience4j.ratelimiter.RateLimiterConfig.custom()
                        .limitForPeriod(100)
                        .limitRefreshPeriod(Duration.ofSeconds(1))
                        .timeoutDuration(Duration.ofMillis(500))
                        .build();

        return RateLimiterRegistry.of(defaultConfig);
    }

    @Bean
    public RateLimiter apiRateLimiter(RateLimiterRegistry registry) {
        return registry.rateLimiter("api");
    }

    @Bean
    public RateLimiter paymentRateLimiter(RateLimiterRegistry registry) {
        return registry.rateLimiter("payment",
                io.github.resilience4j.ratelimiter.RateLimiterConfig.custom()
                        .limitForPeriod(50)
                        .limitRefreshPeriod(Duration.ofSeconds(1))
                        .timeoutDuration(Duration.ofMillis(1000))
                        .build());
    }
}
