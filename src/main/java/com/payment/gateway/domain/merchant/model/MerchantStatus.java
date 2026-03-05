package com.payment.gateway.domain.merchant.model;

import java.util.Arrays;

/**
 * Enum representing the status of a merchant.
 */
public enum MerchantStatus {

    /**
     * Merchant is pending approval.
     */
    PENDING,

    /**
     * Merchant is active and can process payments.
     */
    ACTIVE,

    /**
     * Merchant has been suspended and cannot process payments.
     */
    SUSPENDED,

    /**
     * Merchant account has been closed.
     */
    CLOSED;

    /**
     * Check if this status allows transition to another status.
     */
    public boolean canTransitionTo(MerchantStatus newStatus) {
        return switch (this) {
            case PENDING -> newStatus == ACTIVE || newStatus == SUSPENDED;
            case ACTIVE -> newStatus == SUSPENDED || newStatus == CLOSED;
            case SUSPENDED -> newStatus == ACTIVE || newStatus == CLOSED;
            case CLOSED -> false;
        };
    }

    /**
     * Check if merchant can process payments.
     */
    public boolean canProcessPayments() {
        return this == ACTIVE;
    }

    /**
     * Parse status from string, case-insensitive.
     */
    public static MerchantStatus fromString(String value) {
        return Arrays.stream(values())
                .filter(s -> s.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                    "Invalid merchant status: " + value));
    }
}
