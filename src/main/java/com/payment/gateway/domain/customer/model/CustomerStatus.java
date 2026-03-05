package com.payment.gateway.domain.customer.model;

import java.util.Set;

/**
 * Customer status enumeration.
 */
public enum CustomerStatus {
    ACTIVE,
    INACTIVE,
    SUSPENDED,
    BLOCKED,
    PENDING_VERIFICATION,
    VERIFIED;

    public boolean canTransitionTo(CustomerStatus newStatus) {
        return switch (this) {
            case ACTIVE -> Set.of(INACTIVE, SUSPENDED, BLOCKED).contains(newStatus);
            case INACTIVE -> Set.of(ACTIVE, SUSPENDED).contains(newStatus);
            case SUSPENDED -> Set.of(ACTIVE, INACTIVE, BLOCKED).contains(newStatus);
            case BLOCKED -> Set.of(SUSPENDED).contains(newStatus);
            case PENDING_VERIFICATION -> Set.of(VERIFIED, ACTIVE, INACTIVE).contains(newStatus);
            case VERIFIED -> Set.of(ACTIVE, INACTIVE, SUSPENDED).contains(newStatus);
        };
    }

    public boolean isActive() {
        return this == ACTIVE || this == VERIFIED;
    }
}
