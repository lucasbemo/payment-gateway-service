package com.payment.gateway.domain.reconciliation.model;

/**
 * Discrepancy status enumeration.
 */
public enum DiscrepancyStatus {
    OPEN,
    UNDER_REVIEW,
    RESOLVED,
    ESCALATED,
    CLOSED
}
