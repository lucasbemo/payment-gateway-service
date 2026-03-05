package com.payment.gateway.domain.refund.model;

import java.util.Set;

/**
 * Refund status states.
 */
public enum RefundStatus {
    PENDING,
    PROCESSING,
    APPROVED,
    REJECTED,
    COMPLETED,
    FAILED,
    CANCELLED;

    public boolean canTransitionTo(RefundStatus newStatus) {
        return switch (this) {
            case PENDING -> Set.of(PROCESSING, APPROVED, REJECTED, FAILED, CANCELLED).contains(newStatus);
            case PROCESSING -> Set.of(APPROVED, COMPLETED, FAILED).contains(newStatus);
            case APPROVED -> Set.of(COMPLETED, FAILED, CANCELLED).contains(newStatus);
            case REJECTED -> false;
            case COMPLETED -> false;
            case FAILED -> Set.of(PENDING).contains(newStatus); // Can retry after failure
            case CANCELLED -> false;
        };
    }

    public boolean isTerminal() {
        return Set.of(REJECTED, COMPLETED, CANCELLED).contains(this);
    }

    public boolean isPending() {
        return Set.of(PENDING, PROCESSING).contains(this);
    }

    public boolean isSuccessful() {
        return Set.of(APPROVED, COMPLETED).contains(this);
    }
}
