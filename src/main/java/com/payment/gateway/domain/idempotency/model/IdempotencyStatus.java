package com.payment.gateway.domain.idempotency.model;

/**
 * Idempotency key status enumeration.
 */
public enum IdempotencyStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED,
    EXPIRED
}
