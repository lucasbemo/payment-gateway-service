package com.payment.gateway.domain.transaction.model;

import java.util.Set;

/**
 * Transaction status states.
 */
public enum TransactionStatus {
    PENDING,
    PROCESSING,
    AUTHORIZED,
    CAPTURED,
    SETTLED,
    REVERSED,
    FAILED,
    REFUNDED,
    PARTIALLY_REFUNDED;

    public boolean canTransitionTo(TransactionStatus newStatus) {
        return switch (this) {
            case PENDING -> Set.of(PROCESSING, AUTHORIZED, CAPTURED, SETTLED, FAILED).contains(newStatus);
            case PROCESSING -> Set.of(AUTHORIZED, CAPTURED, SETTLED, FAILED).contains(newStatus);
            case AUTHORIZED -> Set.of(CAPTURED, SETTLED, REVERSED, FAILED).contains(newStatus);
            case CAPTURED -> Set.of(SETTLED, REFUNDED, PARTIALLY_REFUNDED).contains(newStatus);
            case SETTLED -> Set.of(REFUNDED, PARTIALLY_REFUNDED).contains(newStatus);
            case REVERSED -> false;
            case FAILED -> false;
            case REFUNDED -> false;
            case PARTIALLY_REFUNDED -> Set.of(REFUNDED).contains(newStatus);
        };
    }

    public boolean isTerminal() {
        return Set.of(SETTLED, REVERSED, FAILED, REFUNDED).contains(this);
    }

    public boolean isPending() {
        return Set.of(PENDING, PROCESSING).contains(this);
    }

    public boolean isSuccessful() {
        return Set.of(AUTHORIZED, CAPTURED, SETTLED, REFUNDED, PARTIALLY_REFUNDED).contains(this);
    }
}
