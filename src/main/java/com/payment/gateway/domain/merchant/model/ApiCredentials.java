package com.payment.gateway.domain.merchant.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

/**
 * Value Object representing API credentials.
 */
@Getter
@EqualsAndHashCode
public class ApiCredentials {

    private final String apiKey;
    private final String apiSecret;
    private final String apiKeyHash;
    private final String apiSecretHash;
    private final Instant expiresAt;

    @Builder
    public ApiCredentials(String apiKey, String apiSecret, String apiKeyHash,
                          String apiSecretHash, Instant expiresAt) {
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.apiKeyHash = apiKeyHash;
        this.apiSecretHash = apiSecretHash;
        this.expiresAt = expiresAt;
    }

    /**
     * Generate new API credentials.
     */
    public static ApiCredentials generate(String apiKeyHash, String apiSecretHash) {
        String apiKey = "pk_" + UUID.randomUUID().toString().replace("-", "");
        String apiSecret = "sk_" + UUID.randomUUID().toString().replace("-", "");

        return ApiCredentials.builder()
                .apiKey(apiKey)
                .apiSecret(apiSecret)
                .apiKeyHash(apiKeyHash)
                .apiSecretHash(apiSecretHash)
                .build();
    }

    /**
     * Check if credentials are expired.
     */
    public boolean isExpired() {
        return expiresAt != null && Instant.now().isAfter(expiresAt);
    }
}
