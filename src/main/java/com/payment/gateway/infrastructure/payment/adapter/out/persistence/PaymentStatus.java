package com.payment.gateway.infrastructure.payment.adapter.out.persistence;

/**
 * Enum for Payment Status in the persistence layer.
 */
public enum PaymentStatus {
    PENDING,
    AUTHORIZED,
    CAPTURED,
    FAILED,
    CANCELLED,
    REFUNDED,
    PARTIALLY_REFUNDED
}
