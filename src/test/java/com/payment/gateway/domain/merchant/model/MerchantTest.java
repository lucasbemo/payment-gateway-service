package com.payment.gateway.domain.merchant.model;

import com.payment.gateway.commons.exception.BusinessException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Merchant aggregate.
 */
class MerchantTest {

    private static final String MERCHANT_NAME = "Test Merchant";
    private static final String MERCHANT_EMAIL = "test@merchant.com";
    private static final String API_KEY = "test-api-key";
    private static final String API_SECRET = "test-api-secret";
    private static final String WEBHOOK_URL = "https://example.com/webhook";

    @Test
    void shouldRegisterMerchantWithValidData() {
        Merchant merchant = Merchant.register(
            MERCHANT_NAME,
            MERCHANT_EMAIL,
            hash(API_KEY),
            hash(API_SECRET),
            WEBHOOK_URL,
            MerchantConfiguration.empty()
        );

        assertNotNull(merchant);
        assertEquals(MERCHANT_NAME, merchant.getName());
        assertEquals(MERCHANT_EMAIL, merchant.getEmail());
        assertEquals(MerchantStatus.PENDING, merchant.getStatus());
        assertFalse(merchant.canProcessPayments());
    }

    @Test
    void shouldThrowExceptionWhenNameIsMissing() {
        assertThrows(
            BusinessException.class,
            () -> Merchant.register(
                null,
                MERCHANT_EMAIL,
                hash(API_KEY),
                hash(API_SECRET),
                WEBHOOK_URL,
                MerchantConfiguration.empty()
            )
        );
    }

    @Test
    void shouldThrowExceptionWhenEmailIsInvalid() {
        assertThrows(
            BusinessException.class,
            () -> Merchant.register(
                MERCHANT_NAME,
                "invalid-email",
                hash(API_KEY),
                hash(API_SECRET),
                WEBHOOK_URL,
                MerchantConfiguration.empty()
            )
        );
    }

    @Test
    void shouldThrowExceptionWhenApiKeyHashIsMissing() {
        assertThrows(
            BusinessException.class,
            () -> Merchant.register(
                MERCHANT_NAME,
                MERCHANT_EMAIL,
                null,
                hash(API_SECRET),
                WEBHOOK_URL,
                MerchantConfiguration.empty()
            )
        );
    }

    @Test
    void shouldActivatePendingMerchant() {
        Merchant merchant = createTestMerchant();

        merchant.activate();

        assertEquals(MerchantStatus.ACTIVE, merchant.getStatus());
        assertTrue(merchant.canProcessPayments());
    }

    @Test
    void shouldSuspendActiveMerchant() {
        Merchant merchant = createTestMerchant();
        merchant.activate();

        merchant.suspend();

        assertEquals(MerchantStatus.SUSPENDED, merchant.getStatus());
        assertFalse(merchant.canProcessPayments());
    }

    @Test
    void shouldReactivateSuspendedMerchant() {
        Merchant merchant = createTestMerchant();
        merchant.activate();
        merchant.suspend();

        merchant.reactivate();

        assertEquals(MerchantStatus.ACTIVE, merchant.getStatus());
        assertTrue(merchant.canProcessPayments());
    }

    @Test
    void shouldCloseMerchant() {
        Merchant merchant = createTestMerchant();
        merchant.activate();

        merchant.close();

        assertEquals(MerchantStatus.CLOSED, merchant.getStatus());
        assertFalse(merchant.canProcessPayments());
    }

    @Test
    void shouldNotTransitionFromClosed() {
        Merchant merchant = createTestMerchant();
        merchant.activate();
        merchant.close();

        BusinessException exception = assertThrows(
            BusinessException.class,
            merchant::reactivate
        );

        assertTrue(exception.getMessage().contains("Cannot transition from CLOSED to ACTIVE"));
    }

    @Test
    void shouldUpdateWebhookUrl() {
        Merchant merchant = createTestMerchant();
        String newWebhookUrl = "https://new-url.com/webhook";

        merchant.updateWebhookUrl(newWebhookUrl);

        assertEquals(newWebhookUrl, merchant.getWebhookUrl());
    }

    @Test
    void shouldRegenerateCredentials() {
        Merchant merchant = createTestMerchant();
        String oldApiKeyHash = merchant.getApiKeyHash();

        merchant.regenerateCredentials(hash("new-api-key"), hash("new-api-secret"));

        assertNotEquals(oldApiKeyHash, merchant.getApiKeyHash());
    }

    @Test
    void shouldUpdateConfiguration() {
        Merchant merchant = createTestMerchant();
        MerchantConfiguration newConfig = MerchantConfiguration.of(
            java.util.Map.of("key", "value")
        );

        merchant.updateConfiguration(newConfig);

        assertEquals("value", merchant.getConfiguration().get("key"));
    }

    @Test
    void shouldValidateApiKey() {
        Merchant merchant = createTestMerchant();

        assertDoesNotThrow(() -> merchant.validateApiKey(hash(API_KEY)));

        BusinessException exception = assertThrows(
            BusinessException.class,
            () -> merchant.validateApiKey(hash("wrong-key"))
        );

        assertEquals("Invalid API key", exception.getMessage());
    }

    private Merchant createTestMerchant() {
        return Merchant.register(
            MERCHANT_NAME,
            MERCHANT_EMAIL,
            hash(API_KEY),
            hash(API_SECRET),
            WEBHOOK_URL,
            MerchantConfiguration.empty()
        );
    }

    private String hash(String value) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(value.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing value", e);
        }
    }
}
