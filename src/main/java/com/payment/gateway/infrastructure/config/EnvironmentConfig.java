package com.payment.gateway.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.time.Clock;
import java.time.ZoneId;

/**
 * Configuration for environment and time-related beans.
 */
@Configuration
public class EnvironmentConfig {

    /**
     * Provides a UTC clock for consistent time handling across the application.
     */
    @Bean
    @Primary
    public Clock clock() {
        return Clock.systemUTC();
    }

    /**
     * Provides the system default zone ID.
     */
    @Bean
    public ZoneId zoneId() {
        return ZoneId.of("UTC");
    }
}
