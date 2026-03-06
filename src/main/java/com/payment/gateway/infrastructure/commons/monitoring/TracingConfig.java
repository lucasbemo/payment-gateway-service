package com.payment.gateway.infrastructure.commons.monitoring;

import org.springframework.context.annotation.Configuration;

/**
 * Distributed tracing configuration.
 * Configures Micrometer Tracing with Brave/Zipkin bridge.
 * Tracing properties are managed via application.yml.
 */
@Configuration
public class TracingConfig {
    // Tracing is auto-configured via micrometer-tracing-bridge-brave
    // and zipkin-reporter-brave dependencies.
    // Configuration is done through application.yml properties:
    //   management.tracing.sampling.probability
    //   management.zipkin.tracing.endpoint
}
