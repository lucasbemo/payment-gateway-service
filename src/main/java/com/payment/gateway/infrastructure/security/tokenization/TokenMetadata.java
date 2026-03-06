package com.payment.gateway.infrastructure.security.tokenization;

import java.time.Instant;

/**
 * Metadata about a tokenized card without exposing the actual card number.
 */
public record TokenMetadata(
    String token,
    String lastFourDigits,
    String cardBrand,
    Instant createdAt,
    boolean active
) {
}
