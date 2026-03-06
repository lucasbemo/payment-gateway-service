package com.payment.gateway.domain.merchant.service;

import com.payment.gateway.domain.merchant.exception.MerchantNotFoundException;
import com.payment.gateway.domain.merchant.model.ApiCredentials;
import com.payment.gateway.domain.merchant.model.Merchant;
import com.payment.gateway.domain.merchant.model.MerchantConfiguration;
import com.payment.gateway.domain.merchant.port.MerchantRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * Domain service for Merchant operations.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class MerchantDomainService {

    private final MerchantRepositoryPort merchantRepository;

    /**
     * Register a new merchant.
     */
    public Merchant registerMerchant(String name,
                                      String email,
                                      String apiKey,
                                      String apiSecret,
                                      String webhookUrl,
                                      MerchantConfiguration configuration) {
        // Check for duplicate email
        if (merchantRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Merchant with this email already exists: " + email);
        }

        // Hash API credentials
        String apiKeyHash = hash(apiKey);
        String apiSecretHash = hash(apiSecret);

        // Create and save merchant
        Merchant merchant = Merchant.register(
            name,
            email,
            apiKey,
            apiKeyHash,
            apiSecretHash,
            webhookUrl,
            configuration
        );

        return merchantRepository.save(merchant);
    }

    /**
     * Get merchant by ID.
     */
    public Merchant getMerchant(String merchantId) {
        return merchantRepository.findById(merchantId)
            .orElseThrow(() -> new MerchantNotFoundException(merchantId));
    }

    /**
     * Get merchant by API key.
     */
    public Merchant getMerchantByApiKey(String apiKey) {
        String apiKeyHash = hash(apiKey);
        return merchantRepository.findByApiKeyHash(apiKeyHash)
            .orElseThrow(() -> new MerchantNotFoundException("Invalid API key"));
    }

    /**
     * Activate a merchant.
     */
    public Merchant activateMerchant(String merchantId) {
        Merchant merchant = getMerchant(merchantId);
        merchant.activate();
        return merchantRepository.save(merchant);
    }

    /**
     * Suspend a merchant.
     */
    public Merchant suspendMerchant(String merchantId) {
        Merchant merchant = getMerchant(merchantId);
        merchant.suspend();
        return merchantRepository.save(merchant);
    }

    /**
     * Reactivate a suspended merchant.
     */
    public Merchant reactivateMerchant(String merchantId) {
        Merchant merchant = getMerchant(merchantId);
        merchant.reactivate();
        return merchantRepository.save(merchant);
    }

    /**
     * Close a merchant account.
     */
    public Merchant closeMerchant(String merchantId) {
        Merchant merchant = getMerchant(merchantId);
        merchant.close();
        return merchantRepository.save(merchant);
    }

    /**
     * Update webhook URL.
     */
    public Merchant updateWebhookUrl(String merchantId, String webhookUrl) {
        Merchant merchant = getMerchant(merchantId);
        merchant.updateWebhookUrl(webhookUrl);
        return merchantRepository.save(merchant);
    }

    /**
     * Regenerate API credentials.
     */
    public ApiCredentials regenerateCredentials(String merchantId, String newApiKey, String newApiSecret) {
        Merchant merchant = getMerchant(merchantId);
        String apiKeyHash = hash(newApiKey);
        String apiSecretHash = hash(newApiSecret);
        merchant.regenerateCredentials(apiKeyHash, apiSecretHash);
        merchantRepository.save(merchant);

        return ApiCredentials.generate(apiKeyHash, apiSecretHash);
    }

    /**
     * List all merchants.
     */
    public List<Merchant> listAllMerchants() {
        return merchantRepository.findAll();
    }

    /**
     * Validate merchant status for processing payments.
     */
    public void validateMerchantCanProcess(String merchantId) {
        Merchant merchant = getMerchant(merchantId);
        if (!merchant.canProcessPayments()) {
            throw new IllegalStateException(
                "Merchant " + merchantId + " cannot process payments (status: " + merchant.getStatus() + ")");
        }
    }

    private String hash(String value) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing value", e);
        }
    }
}
