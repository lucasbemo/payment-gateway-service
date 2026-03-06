package com.payment.gateway.infrastructure.commons.resilience;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Retry configuration for transient failure handling.
 */
@Configuration
public class RetryConfig {

    @Bean
    public RetryRegistry retryRegistry() {
        io.github.resilience4j.retry.RetryConfig defaultConfig =
                io.github.resilience4j.retry.RetryConfig.custom()
                        .maxAttempts(3)
                        .waitDuration(Duration.ofMillis(500))
                        .retryExceptions(java.io.IOException.class, java.util.concurrent.TimeoutException.class)
                        .build();

        return RetryRegistry.of(defaultConfig);
    }

    @Bean
    public Retry paymentProviderRetry(RetryRegistry registry) {
        return registry.retry("paymentProvider");
    }

    @Bean
    public Retry externalServiceRetry(RetryRegistry registry) {
        return registry.retry("externalService");
    }
}
