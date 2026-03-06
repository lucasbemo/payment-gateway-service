package com.payment.gateway.infrastructure.commons.resilience;

import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Time limiter configuration for call timeout management.
 */
@Configuration
public class TimeLimiterConfig {

    @Bean
    public TimeLimiterRegistry timeLimiterRegistry() {
        io.github.resilience4j.timelimiter.TimeLimiterConfig defaultConfig =
                io.github.resilience4j.timelimiter.TimeLimiterConfig.custom()
                        .timeoutDuration(Duration.ofSeconds(5))
                        .cancelRunningFuture(true)
                        .build();

        return TimeLimiterRegistry.of(defaultConfig);
    }

    @Bean
    public TimeLimiter paymentTimeLimiter(TimeLimiterRegistry registry) {
        return registry.timeLimiter("payment");
    }

    @Bean
    public TimeLimiter externalServiceTimeLimiter(TimeLimiterRegistry registry) {
        return registry.timeLimiter("externalService",
                io.github.resilience4j.timelimiter.TimeLimiterConfig.custom()
                        .timeoutDuration(Duration.ofSeconds(10))
                        .cancelRunningFuture(true)
                        .build());
    }
}
