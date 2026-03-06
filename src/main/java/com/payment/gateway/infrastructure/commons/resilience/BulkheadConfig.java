package com.payment.gateway.infrastructure.commons.resilience;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Bulkhead configuration for concurrent call isolation.
 */
@Configuration
public class BulkheadConfig {

    @Bean
    public BulkheadRegistry bulkheadRegistry() {
        io.github.resilience4j.bulkhead.BulkheadConfig defaultConfig =
                io.github.resilience4j.bulkhead.BulkheadConfig.custom()
                        .maxConcurrentCalls(25)
                        .maxWaitDuration(Duration.ofMillis(500))
                        .build();

        return BulkheadRegistry.of(defaultConfig);
    }

    @Bean
    public Bulkhead paymentBulkhead(BulkheadRegistry registry) {
        return registry.bulkhead("payment");
    }

    @Bean
    public Bulkhead externalServiceBulkhead(BulkheadRegistry registry) {
        return registry.bulkhead("externalService",
                io.github.resilience4j.bulkhead.BulkheadConfig.custom()
                        .maxConcurrentCalls(10)
                        .maxWaitDuration(Duration.ofMillis(1000))
                        .build());
    }
}
