package com.payment.gateway.commons.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for IdGenerator.
 */
class IdGeneratorTest {

    @Test
    void shouldGenerateUuid() {
        String uuid = IdGenerator.generateUuid();

        assertNotNull(uuid);
        assertEquals(36, uuid.length());
        assertTrue(uuid.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$"));
    }

    @Test
    void shouldGenerateUniqueUuids() {
        String uuid1 = IdGenerator.generateUuid();
        String uuid2 = IdGenerator.generateUuid();

        assertNotEquals(uuid1, uuid2);
    }

    @Test
    void shouldGenerateShortUuid() {
        String shortUuid = IdGenerator.generateShortUuid();

        assertNotNull(shortUuid);
        assertEquals(16, shortUuid.length());
    }

    @Test
    void shouldGeneratePaymentId() {
        String paymentId = IdGenerator.generatePaymentId();

        assertNotNull(paymentId);
        assertTrue(paymentId.startsWith("pay_"));
    }

    @Test
    void shouldGenerateTransactionId() {
        String transactionId = IdGenerator.generateTransactionId();

        assertNotNull(transactionId);
        assertTrue(transactionId.startsWith("txn_"));
    }

    @Test
    void shouldGenerateRefundId() {
        String refundId = IdGenerator.generateRefundId();

        assertNotNull(refundId);
        assertTrue(refundId.startsWith("ref_"));
    }

    @Test
    void shouldGenerateMerchantId() {
        String merchantId = IdGenerator.generateMerchantId();

        assertNotNull(merchantId);
        assertTrue(merchantId.startsWith("mer_"));
    }

    @Test
    void shouldGenerateCustomerId() {
        String customerId = IdGenerator.generateCustomerId();

        assertNotNull(customerId);
        assertTrue(customerId.startsWith("cus_"));
    }

    @Test
    void shouldGenerateBatchId() {
        String batchId = IdGenerator.generateBatchId();

        assertNotNull(batchId);
        assertTrue(batchId.startsWith("batch_"));
    }

    @Test
    void shouldGenerateUniqueTimeBasedIds() {
        String id1 = IdGenerator.generatePaymentId();
        String id2 = IdGenerator.generatePaymentId();

        assertNotEquals(id1, id2);
    }

    @Test
    void shouldReturnCurrentInstant() {
        var now = IdGenerator.now();

        assertNotNull(now);
        assertTrue(now.isBefore(java.time.Instant.now().plusSeconds(1)));
    }

    @Test
    void shouldFormatInstant() {
        var instant = java.time.Instant.now();

        String formatted = IdGenerator.formatInstant(instant);

        assertNotNull(formatted);
        assertTrue(formatted.matches("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.*$"));
    }

    @Test
    void shouldReturnNullForNullInstant() {
        String formatted = IdGenerator.formatInstant(null);

        assertNull(formatted);
    }
}
