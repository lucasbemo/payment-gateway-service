package com.payment.gateway.domain.reconciliation.model;

/**
 * Discrepancy type enumeration.
 */
public enum DiscrepancyType {
    MISSING_IN_GATEWAY,
    MISSING_IN_SYSTEM,
    AMOUNT_MISMATCH,
    STATUS_MISMATCH,
    CURRENCY_MISMATCH,
    FEE_MISMATCH,
    TIMING_DIFFERENCE,
    DUPLICATE_TRANSACTION
}
