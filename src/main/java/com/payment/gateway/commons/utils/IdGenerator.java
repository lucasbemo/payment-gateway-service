package com.payment.gateway.commons.utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Utility class for generating unique identifiers and timestamps.
 */
public final class IdGenerator {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
    private static final ZoneId UTC_ZONE = ZoneId.of("UTC");

    private IdGenerator() {
        // Private constructor to prevent instantiation
    }

    /**
     * Generate a random UUID (version 4).
     */
    public static String generateUuid() {
        return UUID.randomUUID().toString();
    }

    /**
     * Generate a short UUID (first 16 characters).
     */
    public static String generateShortUuid() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    /**
     * Generate a time-based ID with prefix.
     */
    public static String generateTimeBasedId(String prefix) {
        String timestamp = LocalDateTime.now(UTC_ZONE).format(FORMATTER);
        String uniquePart = generateShortUuid();
        return String.format("%s_%s_%s", prefix, timestamp, uniquePart);
    }

    /**
     * Generate a payment ID.
     */
    public static String generatePaymentId() {
        return generateTimeBasedId("pay");
    }

    /**
     * Generate a transaction ID.
     */
    public static String generateTransactionId() {
        return generateTimeBasedId("txn");
    }

    /**
     * Generate a refund ID.
     */
    public static String generateRefundId() {
        return generateTimeBasedId("ref");
    }

    /**
     * Generate a merchant ID.
     */
    public static String generateMerchantId() {
        return generateTimeBasedId("mer");
    }

    /**
     * Generate a customer ID.
     */
    public static String generateCustomerId() {
        return generateTimeBasedId("cus");
    }

    /**
     * Generate a reconciliation batch ID.
     */
    public static String generateBatchId() {
        return generateTimeBasedId("batch");
    }

    /**
     * Get current timestamp as Instant.
     */
    public static Instant now() {
        return Instant.now();
    }

    /**
     * Get current timestamp in UTC as LocalDateTime.
     */
    public static LocalDateTime nowLocal() {
        return LocalDateTime.now(UTC_ZONE);
    }

    /**
     * Format an Instant to ISO string.
     */
    public static String formatInstant(Instant instant) {
        if (instant == null) {
            return null;
        }
        return instant.toString();
    }
}
