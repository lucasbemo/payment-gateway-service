package com.payment.gateway.domain.merchant.model;

import com.payment.gateway.commons.exception.BusinessException;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

/**
 * Aggregate Root representing a Merchant.
 */
@Getter
public class Merchant {

    private String id;
    private String name;
    private String email;
    private String apiKey;
    private String apiKeyHash;
    private String apiSecretHash;
    private MerchantStatus status;
    private String webhookUrl;
    private String webhookSecret;
    private MerchantConfiguration configuration;
    private Instant createdAt;
    private Instant updatedAt;

    private Merchant() {}

    /**
     * Register a new merchant.
     */
    public static Merchant register(String name,
                                     String email,
                                     String apiKey,
                                     String apiKeyHash,
                                     String apiSecretHash,
                                     String webhookUrl,
                                     MerchantConfiguration configuration) {
        validateMerchantData(name, email, apiKey, apiKeyHash, apiSecretHash);

        Merchant merchant = new Merchant();
        merchant.id = UUID.randomUUID().toString();
        merchant.name = name;
        merchant.email = email;
        merchant.apiKey = apiKey;
        merchant.apiKeyHash = apiKeyHash;
        merchant.apiSecretHash = apiSecretHash;
        merchant.status = MerchantStatus.PENDING;
        merchant.webhookUrl = webhookUrl;
        merchant.webhookSecret = generateWebhookSecret();
        merchant.configuration = configuration != null ? configuration : MerchantConfiguration.empty();
        merchant.createdAt = Instant.now();
        merchant.updatedAt = Instant.now();

        return merchant;
    }

    private static void validateMerchantData(String name, String email,
                                              String apiKey, String apiKeyHash, String apiSecretHash) {
        if (name == null || name.isBlank()) {
            throw new BusinessException("Merchant name is required");
        }
        if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new BusinessException("Invalid email address: " + email);
        }
        if (apiKey == null || apiKey.isBlank()) {
            throw new BusinessException("API key is required");
        }
        if (apiKeyHash == null || apiKeyHash.isBlank()) {
            throw new BusinessException("API key hash is required");
        }
        if (apiSecretHash == null || apiSecretHash.isBlank()) {
            throw new BusinessException("API secret hash is required");
        }
    }

    /**
     * Activate the merchant.
     */
    public void activate() {
        validateTransition(MerchantStatus.ACTIVE);
        this.status = MerchantStatus.ACTIVE;
        this.updatedAt = Instant.now();
    }

    /**
     * Suspend the merchant.
     */
    public void suspend() {
        validateTransition(MerchantStatus.SUSPENDED);
        this.status = MerchantStatus.SUSPENDED;
        this.updatedAt = Instant.now();
    }

    /**
     * Reactivate a suspended merchant.
     */
    public void reactivate() {
        validateTransition(MerchantStatus.ACTIVE);
        this.status = MerchantStatus.ACTIVE;
        this.updatedAt = Instant.now();
    }

    /**
     * Close the merchant account.
     */
    public void close() {
        validateTransition(MerchantStatus.CLOSED);
        this.status = MerchantStatus.CLOSED;
        this.updatedAt = Instant.now();
    }

    /**
     * Update webhook URL.
     */
    public void updateWebhookUrl(String webhookUrl) {
        this.webhookUrl = webhookUrl;
        this.updatedAt = Instant.now();
    }

    /**
     * Regenerate API credentials.
     */
    public void regenerateCredentials(String newApiKeyHash, String newApiSecretHash) {
        this.apiKeyHash = newApiKeyHash;
        this.apiSecretHash = newApiSecretHash;
        this.updatedAt = Instant.now();
    }

    /**
     * Update configuration.
     */
    public void updateConfiguration(MerchantConfiguration newConfiguration) {
        this.configuration = newConfiguration;
        this.updatedAt = Instant.now();
    }

    private void validateTransition(MerchantStatus newStatus) {
        if (!this.status.canTransitionTo(newStatus)) {
            throw new BusinessException(
                "Cannot transition from " + this.status + " to " + newStatus);
        }
    }

    /**
     * Check if merchant can process payments.
     */
    public boolean canProcessPayments() {
        return status.canProcessPayments();
    }

    /**
     * Validate ownership of an API key.
     */
    public void validateApiKey(String apiKeyHash) {
        if (!this.apiKeyHash.equals(apiKeyHash)) {
            throw new BusinessException("Invalid API key");
        }
    }

    /**
     * Get the API key (public identifier).
     */
    public String getApiKey() {
        return apiKey;
    }

    private static String generateWebhookSecret() {
        return "whsec_" + UUID.randomUUID().toString().replace("-", "");
    }
}
