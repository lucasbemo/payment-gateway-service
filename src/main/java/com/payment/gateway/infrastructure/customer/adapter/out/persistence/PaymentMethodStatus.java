package com.payment.gateway.infrastructure.customer.adapter.out.persistence;

/**
 * Enum for Payment Method status.
 */
public enum PaymentMethodStatus {
    ACTIVE,
    INACTIVE,
    EXPIRED,
    SUSPENDED,
    PENDING_VERIFICATION,
    VERIFIED,
    FAILED_VERIFICATION,
    REVOKED
}
