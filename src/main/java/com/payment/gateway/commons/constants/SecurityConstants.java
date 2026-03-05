package com.payment.gateway.commons.constants;

/**
 * Security-related constants.
 */
public final class SecurityConstants {

    private SecurityConstants() {
        // Private constructor to prevent instantiation
    }

    // Authentication
    public static final String AUTH_SCHEME_BEARER = "Bearer";
    public static final String AUTH_SCHEME_BASIC = "Basic";
    public static final String AUTH_HEADER_PREFIX_BEARER = "Bearer ";
    public static final String AUTH_HEADER_PREFIX_BASIC = "Basic ";

    // JWT Constants
    public static final String JWT_CLAIM_SUBJECT = "sub";
    public static final String JWT_CLAIM_ISSUED_AT = "iat";
    public static final String JWT_CLAIM_EXPIRATION = "exp";
    public static final String JWT_CLAIM_ROLES = "roles";
    public static final String JWT_CLAIM_MERCHANT_ID = "merchantId";
    public static final long JWT_EXPIRATION_MS = 86400000; // 24 hours
    public static final long JWT_REFRESH_EXPIRATION_MS = 604800000; // 7 days

    // API Key Constants
    public static final String API_KEY_PREFIX_MERCHANT = "pk_";
    public static final String API_KEY_PREFIX_SECRET = "sk_";
    public static final int API_KEY_LENGTH = 36;
    public static final String API_KEY_ALGORITHM = "SHA-256";

    // Roles
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_MERCHANT = "ROLE_MERCHANT";
    public static final String ROLE_DEVELOPER = "ROLE_DEVELOPER";

    // Permissions
    public static final String PERMISSION_PAYMENT_CREATE = "payment:create";
    public static final String PERMISSION_PAYMENT_READ = "payment:read";
    public static final String PERMISSION_PAYMENT_REFUND = "payment:refund";
    public static final String PERMISSION_MERCHANT_READ = "merchant:read";
    public static final String PERMISSION_MERCHANT_UPDATE = "merchant:update";

    // CORS
    public static final String CORS_ALLOWED_ORIGINS_DEV = "http://localhost:*,http://127.0.0.1:*";
    public static final long CORS_MAX_AGE_SECONDS = 3600;

    // CSRF
    public static final String CSRF_HEADER_NAME = "X-CSRF-TOKEN";
    public static final String CSRF_COOKIE_NAME = "XSRF-TOKEN";

    // Session
    public static final String SESSION_ATTR_AUTH = "authenticated";
    public static final String SESSION_ATTR_MERCHANT_ID = "merchantId";
}
