package com.payment.gateway.infrastructure.config;

import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Spring Boot Actuator endpoints and health indicators.
 */
@Configuration
public class ActuatorConfig {

    @Bean
    public HealthIndicator paymentGatewayHealthIndicator() {
        return () -> Health.up()
                .withDetail("service", "payment-gateway")
                .withDetail("status", "operational")
                .build();
    }
}
