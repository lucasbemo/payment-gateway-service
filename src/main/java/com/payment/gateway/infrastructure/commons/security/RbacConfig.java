package com.payment.gateway.infrastructure.commons.security;

/**
 * Role-Based Access Control (RBAC) configuration.
 * Defines roles used throughout the application for authorization.
 *
 * Defined roles:
 * - ADMIN: Full access to all operations
 * - MERCHANT: Access to merchant-scoped operations (payment processing, reports)
 * - DEVELOPER: Read-only access plus API key management
 */
public class RbacConfig {

    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_MERCHANT = "MERCHANT";
    public static final String ROLE_DEVELOPER = "DEVELOPER";
}
