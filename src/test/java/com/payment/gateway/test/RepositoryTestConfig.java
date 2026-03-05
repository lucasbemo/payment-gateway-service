package com.payment.gateway.test;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

/**
 * Test configuration for repository integration tests.
 * Excludes controllers and application services to focus on persistence layer.
 */
@TestConfiguration
@ComponentScan(
        basePackages = "com.payment.gateway",
        excludeFilters = {
                @ComponentScan.Filter(
                        pattern = "com.payment.gateway.infrastructure.*.adapter.in.rest.*",
                        type = FilterType.REGEX
                ),
                @ComponentScan.Filter(
                        pattern = "com.payment.gateway.application.*.service.*",
                        type = FilterType.REGEX
                )
        }
)
public class RepositoryTestConfig {
    // Minimal configuration for repository tests
}
