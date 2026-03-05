package com.payment.gateway.domain.refund.model;

/**
 * Refund type enumeration.
 */
public enum RefundType {
    FULL,           // Full refund of the transaction
    PARTIAL,        // Partial refund
    MULTIPLE,       // Multiple partial refunds
    CHARGEBACK,     // Chargeback refund
    CANCELLATION    // Cancellation refund
}
