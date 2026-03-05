package com.payment.gateway.commons.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility class for cryptographic operations.
 */
public final class CryptoUtils {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String HASH_ALGORITHM = "SHA-256";
    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private CryptoUtils() {
        // Private constructor to prevent instantiation
    }

    /**
     * Generate a SHA-256 hash of a string.
     */
    public static String hash(String value) {
        if (value == null) {
            return null;
        }
        try {
            MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
            byte[] hashBytes = md.digest(value.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing value", e);
        }
    }

    /**
     * Generate a random API key.
     */
    public static String generateApiKey(String prefix) {
        byte[] randomBytes = new byte[24];
        RANDOM.nextBytes(randomBytes);
        String randomPart = Base64.getUrlEncoder().withoutPadding()
            .encodeToString(randomBytes)
            .replace("-", "");
        return prefix + "_" + randomPart.substring(0, Math.min(32, randomPart.length()));
    }

    /**
     * Generate a random webhook secret.
     */
    public static String generateWebhookSecret() {
        byte[] randomBytes = new byte[32];
        RANDOM.nextBytes(randomBytes);
        return "whsec_" + Base64.getUrlEncoder().withoutPadding()
            .encodeToString(randomBytes)
            .replace("-", "");
    }

    /**
     * Generate a random idempotency key.
     */
    public static String generateIdempotencyKey() {
        return "idem_" + IdGenerator.generateShortUuid();
    }

    /**
     * Verify a hash matches a value.
     */
    public static boolean verifyHash(String value, String hash) {
        if (value == null || hash == null) {
            return false;
        }
        return hash(value).equals(hash);
    }

    /**
     * Convert bytes to hexadecimal string.
     */
    public static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            hexString.append(String.format("%02x", b));
        }
        return hexString.toString();
    }

    /**
     * Generate a random string of specified length.
     */
    public static String generateRandomString(int length) {
        byte[] randomBytes = new byte[length];
        RANDOM.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding()
            .encodeToString(randomBytes)
            .substring(0, length);
    }

    /**
     * Generate a CSRF token.
     */
    public static String generateCsrfToken() {
        return generateRandomString(32);
    }
}
