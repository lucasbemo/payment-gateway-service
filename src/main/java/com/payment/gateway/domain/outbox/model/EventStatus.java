package com.payment.gateway.domain.outbox.model;

/**
 * Event status enumeration for outbox events.
 */
public enum EventStatus {
    PENDING,
    PROCESSING,
    PUBLISHED,
    FAILED,
    RETRYING
}
