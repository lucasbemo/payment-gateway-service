package com.payment.gateway.infrastructure.security.tokenization;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Entity representing a tokenized card in the token vault.
 * In production, this would be a JPA entity persisted to a secure database.
 */
@Getter
@Setter
class TokenVaultEntity {

    private final String token;
    private final String cardHash;
    private final String cardNumber;
    private final String lastFourDigits;
    private final String cardBrand;
    private final Instant createdAt;
    private boolean active;

    TokenVaultEntity(String token, String cardHash, String cardNumber,
                     String lastFourDigits, String cardBrand, Instant createdAt, boolean active) {
        this.token = token;
        this.cardHash = cardHash;
        this.cardNumber = cardNumber;
        this.lastFourDigits = lastFourDigits;
        this.cardBrand = cardBrand;
        this.createdAt = createdAt;
        this.active = active;
    }
}
