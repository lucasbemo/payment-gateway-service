package com.payment.gateway.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

/**
 * CORS configuration for the payment gateway API.
 * Defines allowed origins, methods, and headers per environment.
 * Note: CORS is also configured in SecurityConfig - this provides profile-specific CorsFilter beans.
 */
@Slf4j
@Configuration
@Profile("!test")
public class CorsConfig {

    @Value("${app.cors.allowed-origins:*}")
    private String allowedOrigins;

    @Value("${app.environment:development}")
    private String environment;

    /**
     * Development CORS configuration - more permissive.
     */
    @Bean
    @Profile({"development", "default"})
    public CorsFilter corsFilterDevelopment() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Allow all origins in development
        configuration.setAllowedOrigins(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of(
            "X-Idempotency-Key",
            "X-Correlation-Id",
            "X-Request-Id"
        ));
        configuration.setMaxAge(3600L);
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        log.info("CORS configured for development environment");

        return new CorsFilter(source);
    }

    /**
     * Production CORS configuration - restrictive.
     */
    @Bean
    @Profile("production")
    public CorsFilter corsFilterProduction() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Parse allowed origins from configuration
        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        configuration.setAllowedOrigins(origins.stream()
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .toList());

        configuration.setAllowedMethods(List.of(
            "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));

        configuration.setAllowedHeaders(List.of(
            "Content-Type",
            "Authorization",
            "X-Api-Key",
            "X-Api-Secret",
            "X-Idempotency-Key",
            "X-Correlation-Id",
            "X-Request-Id"
        ));

        configuration.setExposedHeaders(List.of(
            "X-Idempotency-Key",
            "X-Correlation-Id",
            "X-Request-Id",
            "X-Total-Count"
        ));

        configuration.setMaxAge(3600L);
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        log.info("CORS configured for production environment with origins: {}", origins);

        return new CorsFilter(source);
    }
}
