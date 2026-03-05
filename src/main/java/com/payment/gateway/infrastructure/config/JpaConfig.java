package com.payment.gateway.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Configuration for JPA and Spring Data repositories.
 */
@Configuration
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "com.payment.gateway.infrastructure")
public class JpaConfig {

    // JPA configuration is handled via Spring Boot auto-configuration
    // and application.yml properties
}
