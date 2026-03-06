package com.payment.gateway.infrastructure.commons.monitoring;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Custom health indicators for monitoring subsystem health.
 */
@Configuration
public class HealthIndicatorConfig {

    @Bean
    public HealthIndicator kafkaHealthIndicator() {
        return () -> {
            try {
                return Health.up()
                        .withDetail("component", "kafka")
                        .build();
            } catch (Exception e) {
                return Health.down()
                        .withDetail("component", "kafka")
                        .withException(e)
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
}
