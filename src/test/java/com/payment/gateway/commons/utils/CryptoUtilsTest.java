package com.payment.gateway.commons.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CryptoUtils.
 */
class CryptoUtilsTest {

    @Test
    void shouldGenerateHash() {
        String hash = CryptoUtils.hash("test");

        assertNotNull(hash);
        assertEquals(64, hash.length());
        assertTrue(hash.matches("^[0-9a-f]+$"));
    }

    @Test
    void shouldGenerateConsistentHashes() {
        String hash1 = CryptoUtils.hash("test");
        String hash2 = CryptoUtils.hash("test");

        assertEquals(hash1, hash2);
    }

    @Test
    void shouldGenerateDifferentHashes() {
        String hash1 = CryptoUtils.hash("test1");
        String hash2 = CryptoUtils.hash("test2");

        assertNotEquals(hash1, hash2);
    }

    @Test
    void shouldHandleNullHash() {
        assertNull(CryptoUtils.hash(null));
    }

    @Test
    void shouldGenerateApiKey() {
        String apiKey = CryptoUtils.generateApiKey("pk");

        assertNotNull(apiKey);
        assertTrue(apiKey.startsWith("pk_"));
        assertTrue(apiKey.length() > 10);
    }

    @Test
    void shouldGenerateUniqueApiKeys() {
        String key1 = CryptoUtils.generateApiKey("pk");
        String key2 = CryptoUtils.generateApiKey("pk");

        assertNotEquals(key1, key2);
    }

    @Test
    void shouldGenerateWebhookSecret() {
        String secret = CryptoUtils.generateWebhookSecret();

        assertNotNull(secret);
        assertTrue(secret.startsWith("whsec_"));
    }

    @Test
    void shouldGenerateUniqueWebhookSecrets() {
        String secret1 = CryptoUtils.generateWebhookSecret();
        String secret2 = CryptoUtils.generateWebhookSecret();

        assertNotEquals(secret1, secret2);
    }

    @Test
    void shouldGenerateIdempotencyKey() {
        String key = CryptoUtils.generateIdempotencyKey();

        assertNotNull(key);
        assertTrue(key.startsWith("idem_"));
    }

    @Test
    void shouldVerifyHash() {
        String value = "test";
        String hash = CryptoUtils.hash(value);

        assertTrue(CryptoUtils.verifyHash(value, hash));
    }

    @Test
    void shouldRejectInvalidHashVerification() {
        String value = "test";
        String wrongHash = CryptoUtils.hash("wrong");

        assertFalse(CryptoUtils.verifyHash(value, wrongHash));
    }

    @Test
    void shouldRejectNullHashVerification() {
        assertFalse(CryptoUtils.verifyHash(null, "hash"));
        assertFalse(CryptoUtils.verifyHash("value", null));
    }

    @Test
    void shouldGenerateRandomString() {
        String random = CryptoUtils.generateRandomString(32);

        assertNotNull(random);
        assertEquals(32, random.length());
    }

    @Test
    void shouldGenerateUniqueRandomStrings() {
        String random1 = CryptoUtils.generateRandomString(32);
        String random2 = CryptoUtils.generateRandomString(32);

        assertNotEquals(random1, random2);
    }

    @Test
    void shouldGenerateCsrfToken() {
        String token = CryptoUtils.generateCsrfToken();

        assertNotNull(token);
        assertEquals(32, token.length());
    }
}
