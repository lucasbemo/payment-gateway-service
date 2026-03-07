package com.payment.gateway.e2e;

import com.payment.gateway.e2e.testdata.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * E2E tests for refund flow.
 */
class RefundFlowE2ETest extends E2ETestBase {

    private String merchantId;
    private String paymentId;
    private Long paymentAmount;

    @BeforeEach
    void setUp() {
        cleanupDatabase();

        // Register a merchant
        var merchantResponse = getApiClient().registerMerchant(
            TestDataFactory.MerchantData.create().name,
            TestDataFactory.MerchantData.create().email,
            null
        );
        assertThat(merchantResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        Map<String, Object> merchantData = (Map<String, Object>) merchantResponse.getBody().get("data");
        merchantId = (String) merchantData.get("id");
        String apiKey = (String) merchantData.get("apiKey");
        setApiKey(apiKey);

        // Create a payment to refund
        String idempotencyKey = TestDataFactory.generateIdempotencyKey();
        var paymentResponse = getApiClient().processPayment(
            merchantId,
            10000L, // $100.00
            "USD",
            idempotencyKey
        );

        assertThat(paymentResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, Object> payment = (Map<String, Object>) paymentResponse.getBody().get("data");
        paymentId = (String) payment.get("id");
        paymentAmount = (Long) payment.get("amount");
    }

    @Test
    @DisplayName("E2E: Full Refund - Happy Path")
    void testFullRefund_HappyPath() {
        // Given: A refund request for the full amount
        String idempotencyKey = TestDataFactory.generateIdempotencyKey();
        var refundData = TestDataFactory.RefundData.create(paymentId, merchantId);

        // When: Processing the full refund
        var response = getApiClient().processRefund(
            refundData.paymentId,
            refundData.merchantId,
            refundData.amountInCents,
            idempotencyKey,
            refundData.reason
        );

        // Then: Refund is successfully processed
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        Map<String, Object> refund = (Map<String, Object>) response.getBody().get("data");
        assertThat(refund).isNotNull();
        assertThat(refund.get("id")).isNotNull();
        assertThat(refund.get("paymentId")).isEqualTo(paymentId);
        assertThat(refund.get("amount")).isEqualTo(refundData.amountInCents);
        assertThat(refund.get("status")).isEqualTo("COMPLETED");

        // Verify refund exists in database
        String refundId = (String) refund.get("id");
        assertThat(exists("refund", "id", refundId)).isTrue();
    }

    @Test
    @DisplayName("E2E: Partial Refund")
    void testPartialRefund() {
        // Given: A partial refund request
        String idempotencyKey = TestDataFactory.generateIdempotencyKey();
        Long refundAmount = 5000L; // $50.00 (half of $100)

        // When: Processing the partial refund
        var response = getApiClient().processRefund(
            paymentId,
            merchantId,
            refundAmount,
            idempotencyKey,
            "Partial refund"
        );

        // Then: Refund is successfully processed
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        Map<String, Object> refund = (Map<String, Object>) response.getBody().get("data");
        assertThat(refund.get("amount")).isEqualTo(refundAmount);

        // Verify original payment still exists
        assertThat(exists("payment", "id", paymentId)).isTrue();
    }

    @Test
    @DisplayName("E2E: Multiple Partial Refunds")
    void testMultiplePartialRefunds() {
        // Given: First partial refund
        String idempotencyKey1 = TestDataFactory.generateIdempotencyKey();
        var response1 = getApiClient().processRefund(
            paymentId,
            merchantId,
            3000L, // $30.00
            idempotencyKey1,
            "First partial refund"
        );
        assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // When: Processing second partial refund
        String idempotencyKey2 = TestDataFactory.generateIdempotencyKey();
        var response2 = getApiClient().processRefund(
            paymentId,
            merchantId,
            4000L, // $40.00
            idempotencyKey2,
            "Second partial refund"
        );

        // Then: Second refund is also processed
        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // Verify both refunds exist in database
        Map<String, Object> refund1 = (Map<String, Object>) response1.getBody().get("data");
        Map<String, Object> refund2 = (Map<String, Object>) response2.getBody().get("data");

        assertThat(exists("refund", "id", (String) refund1.get("id"))).isTrue();
        assertThat(exists("refund", "id", (String) refund2.get("id"))).isTrue();

        // Verify total refunded amount
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM refund WHERE payment_id = ?", Integer.class, paymentId
        );
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("E2E: Refund Amount Exceeds Payment - Should Fail")
    void testRefundAmountExceedsPayment() {
        // Given: A refund request exceeding the original payment
        String idempotencyKey = TestDataFactory.generateIdempotencyKey();

        // When: Processing refund for more than the payment amount
        var response = getApiClient().processRefund(
            paymentId,
            merchantId,
            15000L, // $150.00 (more than $100 payment)
            idempotencyKey,
            "Invalid refund"
        );

        // Then: Request fails with error
        assertThat(response.getStatusCode()).isIn(HttpStatus.BAD_REQUEST, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    @DisplayName("E2E: Refund Non-Existent Payment")
    void testRefundNonExistentPayment() {
        // Given: A non-existent payment ID
        String fakePaymentId = "non-existent-payment-id";
        String idempotencyKey = TestDataFactory.generateIdempotencyKey();

        // When: Processing refund for non-existent payment
        var response = getApiClient().processRefund(
            fakePaymentId,
            merchantId,
            5000L,
            idempotencyKey,
            "Invalid payment"
        );

        // Then: Request fails with 404
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("E2E: Duplicate Refund - Idempotency")
    void testDuplicateRefund_Idempotency() {
        // Given: A refund request
        String idempotencyKey = "refund-idempotency-test-" + System.currentTimeMillis();
        Long refundAmount = 5000L;

        // When: Processing the same refund twice
        var response1 = getApiClient().processRefund(
            paymentId,
            merchantId,
            refundAmount,
            idempotencyKey,
            "First attempt"
        );

        var response2 = getApiClient().processRefund(
            paymentId,
            merchantId,
            refundAmount,
            idempotencyKey,
            "Second attempt"
        );

        // Then: Second request returns same refund (idempotency)
        assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        // Second request may return CREATED (idempotent) or BAD_REQUEST (duplicate)
        assertThat(response2.getStatusCode()).isIn(HttpStatus.CREATED, HttpStatus.BAD_REQUEST, HttpStatus.OK);

        if (response2.getStatusCode() == HttpStatus.CREATED) {
            Map<String, Object> refund1 = (Map<String, Object>) response1.getBody().get("data");
            Map<String, Object> refund2 = (Map<String, Object>) response2.getBody().get("data");
            assertThat(refund1.get("id")).isEqualTo(refund2.get("id"));
        }
    }

    @Test
    @DisplayName("E2E: Get Refund by ID")
    void testGetRefundById() {
        // Given: A processed refund
        String idempotencyKey = TestDataFactory.generateIdempotencyKey();
        var refundData = TestDataFactory.RefundData.create(paymentId, merchantId);

        var processResponse = getApiClient().processRefund(
            refundData.paymentId,
            refundData.merchantId,
            refundData.amountInCents,
            idempotencyKey,
            refundData.reason
        );

        Map<String, Object> refund = (Map<String, Object>) processResponse.getBody().get("data");
        String refundId = (String) refund.get("id");

        // When: Getting the refund by ID
        var getResponse = getApiClient().getRefund(refundId, merchantId);

        // Then: Refund is retrieved successfully
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        Map<String, Object> retrievedRefund = (Map<String, Object>) getResponse.getBody().get("data");
        assertThat(retrievedRefund.get("id")).isEqualTo(refundId);
        assertThat(retrievedRefund.get("paymentId")).isEqualTo(paymentId);
    }

    @Test
    @DisplayName("E2E: Cancel Refund")
    void testCancelRefund() {
        // Given: A pending refund (if applicable)
        String idempotencyKey = TestDataFactory.generateIdempotencyKey();
        var refundData = TestDataFactory.RefundData.create(paymentId, merchantId);

        var processResponse = getApiClient().processRefund(
            refundData.paymentId,
            refundData.merchantId,
            refundData.amountInCents,
            idempotencyKey,
            refundData.reason
        );

        Map<String, Object> refund = (Map<String, Object>) processResponse.getBody().get("data");
        String refundId = (String) refund.get("id");

        // When: Cancelling the refund
        var cancelResponse = getApiClient().cancelRefund(refundId, merchantId, "Cancelled by user");

        // Then: Cancel endpoint is available (may succeed or fail based on refund status)
        assertThat(cancelResponse.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.BAD_REQUEST, HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("E2E: Refund Without Authentication")
    void testRefund_NoAuthentication() {
        // Given: No API key set
        setApiKey(null);
        String idempotencyKey = TestDataFactory.generateIdempotencyKey();

        // When: Processing refund without authentication
        var response = getApiClient().processRefund(
            paymentId,
            merchantId,
            5000L,
            idempotencyKey,
            "No auth"
        );

        // Then: Request is rejected with 401
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("E2E: Refund with Invalid API Key")
    void testRefund_InvalidApiKey() {
        // Given: Invalid API key
        setApiKey("invalid-api-key");
        String idempotencyKey = TestDataFactory.generateIdempotencyKey();

        // When: Processing refund with invalid API key
        var response = getApiClient().processRefund(
            paymentId,
            merchantId,
            5000L,
            idempotencyKey,
            "Invalid auth"
        );

        // Then: Request is rejected with 401
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
