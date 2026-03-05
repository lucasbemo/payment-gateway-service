package com.payment.gateway.domain.payment.model;

import java.util.Arrays;

/**
 * Enum representing the status of a payment.
 */
public enum PaymentStatus {

    /**
     * Payment has been initiated but not yet processed.
     */
    PENDING,

    /**
     * Payment has been authorized by the provider but not yet captured.
     */
    AUTHORIZED,

    /**
     * Payment has been captured (funds transferred).
     */
    CAPTURED,

    /**
     * Payment processing failed.
     */
    FAILED,

    /**
     * Payment was cancelled before completion.
     */
    CANCELLED,

    /**
     * Payment has been refunded (fully or partially).
     */
    REFUNDED;

    /**
     * Check if this status allows transition to another status.
     */
    public boolean canTransitionTo(PaymentStatus newStatus) {
        return switch (this) {
            case PENDING -> newStatus == AUTHORIZED || newStatus == CAPTURED ||
                           newStatus == FAILED || newStatus == CANCELLED;
            case AUTHORIZED -> newStatus == CAPTURED || newStatus == CANCELLED ||
                              newStatus == FAILED;
            case CAPTURED -> newStatus == REFUNDED;
            case FAILED, CANCELLED, REFUNDED -> false;
        };
    }

    /**
     * Check if this is a terminal status (no further transitions allowed).
     */
    public boolean isTerminal() {
        return this == CAPTURED || this == FAILED || this == CANCELLED || this == REFUNDED;
    }

    /**
     * Check if payment is successfully completed.
     */
    public boolean isSuccess() {
        return this == CAPTURED || this == REFUNDED;
    }

    /**
     * Parse status from string, case-insensitive.
     */
    public static PaymentStatus fromString(String value) {
        return Arrays.stream(values())
                .filter(s -> s.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                    "Invalid payment status: " + value));
    }
}
