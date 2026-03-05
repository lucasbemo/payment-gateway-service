package com.payment.gateway.domain.transaction.model;

/**
 * Transaction type enumeration.
 */
public enum TransactionType {
    PAYMENT,        // Standard payment transaction
    CAPTURE,        // Capture an authorized payment
    AUTHORIZATION,  // Authorize a payment
    REFUND,         // Full refund
    PARTIAL_REFUND, // Partial refund
    REVERSAL,       // Reverse an authorization
    CHARGEBACK,     // Chargeback from card issuer
    ADJUSTMENT      // Manual adjustment
}
