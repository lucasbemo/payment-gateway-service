package com.payment.gateway.infrastructure.commons.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * Role-Based Access Control (RBAC) configuration.
 * Enables method-level security with @PreAuthorize annotations.
 *
 * Defined roles:
 * - ADMIN: Full access to all operations
 * - MERCHANT: Access to merchant-scoped operations
 * - DEVELOPER: Read-only access plus API key management
 */
@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class RbacConfig {

    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_MERCHANT = "MERCHANT";
    public static final String ROLE_DEVELOPER = "DEVELOPER";
}
