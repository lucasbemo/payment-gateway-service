package com.payment.gateway.domain.outbox.model;

/**
 * Event type enumeration for outbox events.
 */
public enum EventType {
    PAYMENT_CREATED,
    PAYMENT_COMPLETED,
    PAYMENT_FAILED,
    PAYMENT_CANCELLED,
    REFUND_PROCESSED,
    TRANSACTION_CREATED,
    TRANSACTION_COMPLETED,
    TRANSACTION_FAILED,
    CUSTOMER_CREATED,
    CUSTOMER_UPDATED,
    MERCHANT_ACTIVATED,
    MERCHANT_SUSPENDED
}
