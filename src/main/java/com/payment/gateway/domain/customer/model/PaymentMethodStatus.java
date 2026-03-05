package com.payment.gateway.domain.customer.model;

/**
 * Payment method status enumeration.
 */
public enum PaymentMethodStatus {
    ACTIVE,
    INACTIVE,
    EXPIRED,
    REVOKED,
    PENDING_VERIFICATION,
    VERIFIED,
    FAILED_VERIFICATION
}
