package com.payment.gateway.domain.reconciliation.model;

/**
 * Reconciliation batch status enumeration.
 */
public enum ReconciliationStatus {
    PENDING,
    PROCESSING,
    RECONCILING,
    COMPLETED,
    FAILED,
    PARTIALLY_RECONCILED
}
