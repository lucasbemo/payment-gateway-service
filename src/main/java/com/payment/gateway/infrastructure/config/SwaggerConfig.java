package com.payment.gateway.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
public class SwaggerConfig {

    @Value("${spring.application.name}")
    private String applicationName;

    @Bean
    public OpenAPI paymentGatewayOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(servers())
                .components(securityComponents())
                .tags(tags());
    }

    private Info apiInfo() {
        return new Info()
                .title("Payment Gateway API")
                .description("""
                    Production-ready Payment Gateway Service REST API.
                    
                    ## Features
                    - Payment authorization, capture, and cancellation
                    - Customer and payment method management
                    - Refund processing (full and partial)
                    - Transaction reconciliation and settlement reports
                    
                    ## Authentication
                    - **Local/Dev**: No authentication required
                    - **Production**: API Key (X-API-Key header) or JWT Bearer token
                    
                    ## Idempotency
                    Payment and refund endpoints support idempotency via `X-Idempotency-Key` header.
                    """)
                .version("1.0.0")
                .contact(new Contact()
                        .name("Payment Gateway Team")
                        .email("team@paymentgateway.com")
                        .url("https://paymentgateway.com"))
                .license(new License()
                        .name("Apache 2.0")
                        .url("https://www.apache.org/licenses/LICENSE-2.0"));
    }

    private List<Server> servers() {
        return Arrays.asList(
                new Server()
                        .url("http://localhost:8080")
                        .description("Local Development Server"),
                new Server()
                        .url("https://dev.payment-gateway.com")
                        .description("Development Server"),
                new Server()
                        .url("https://api.payment-gateway.com")
                        .description("Production Server")
        );
    }

    private Components securityComponents() {
        return new Components()
                .addSecuritySchemes("api-key", new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.HEADER)
                        .name("X-API-Key")
                        .description("Production API key for merchant authentication"))
                .addSecuritySchemes("bearer-jwt", new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("JWT token for authenticated users"));
    }

    private List<Tag> tags() {
        return Arrays.asList(
                new Tag().name("Health & Monitoring").description("Health checks and system monitoring endpoints"),
                new Tag().name("Merchant Management").description("Merchant registration, updates, and suspension"),
                new Tag().name("Customer Management").description("Customer registration and payment method management"),
                new Tag().name("Payment Processing").description("Payment authorization, capture, and cancellation"),
                new Tag().name("Transaction Management").description("Transaction operations and voiding"),
                new Tag().name("Refund Processing").description("Full and partial refund processing"),
                new Tag().name("Reconciliation").description("Transaction reconciliation and settlement reports")
        );
    }
}