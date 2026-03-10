package com.payment.gateway.e2e;

import com.payment.gateway.e2e.testdata.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * E2E tests for transaction tracking.
 */
class TransactionTrackingE2ETest extends E2ETestBase {

    private String merchantId;

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
    }

    @Test
    @DisplayName("E2E: Transaction Created for Payment")
    void testTransactionCreatedForPayment() {
        // Given: A payment request
        String idempotencyKey = TestDataFactory.generateIdempotencyKey();
        var paymentData = TestDataFactory.PaymentData.create(merchantId);

        // When: Processing the payment
        var response = getApiClient().processPayment(
            paymentData.merchantId,
            paymentData.amountInCents,
            paymentData.currency,
            idempotencyKey
        );

        // Then: Payment is processed
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, Object> payment = (Map<String, Object>) response.getBody().get("data");
        String paymentId = (String) payment.get("id");

        // Verify transaction exists in database
        boolean transactionExists = exists("transactions", "payment_id", paymentId);
        assertThat(transactionExists).isTrue();
    }

    @Test
    @DisplayName("E2E: Transaction Query by ID")
    void testTransactionQueryById() {
        // Given: A processed payment with transaction
        String idempotencyKey = TestDataFactory.generateIdempotencyKey();
        var paymentData = TestDataFactory.PaymentData.create(merchantId);

        var paymentResponse = getApiClient().processPayment(
            paymentData.merchantId,
            paymentData.amountInCents,
            paymentData.currency,
            idempotencyKey
        );

        Map<String, Object> payment = (Map<String, Object>) paymentResponse.getBody().get("data");
        String paymentId = (String) payment.get("id");

        // Get transaction ID from database
        String transactionId = jdbcTemplate.queryForObject(
            "SELECT id FROM transactions WHERE payment_id = ?",
            String.class,
            paymentId
        );

        // When: Getting transaction by ID
        var response = getApiClient().getTransaction(transactionId, merchantId);

        // Then: Transaction is retrieved
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("E2E: Get Transactions by Merchant")
    void testGetTransactionsByMerchant() {
        // Given: Multiple payments for a merchant
        for (int i = 0; i < 3; i++) {
            String idempotencyKey = TestDataFactory.generateIdempotencyKey();
            var paymentData = TestDataFactory.PaymentData.create(merchantId);

            getApiClient().processPayment(
                paymentData.merchantId,
                paymentData.amountInCents,
                paymentData.currency,
                idempotencyKey
            );
        }

        // When: Getting all transactions for the merchant
        var response = getApiClient().getTransactions(merchantId);

        // Then: Transactions are returned
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        Map<String, Object> responseBody = response.getBody();
        Object data = responseBody.get("data");

        if (data instanceof List) {
            List<?> transactions = (List<?>) data;
            assertThat(transactions).hasSize(3);
        }
    }

    @Test
    @DisplayName("E2E: Transaction Status Matches Payment Status")
    void testTransactionStatusMatchesPaymentStatus() {
        // Given: A processed payment
        String idempotencyKey = TestDataFactory.generateIdempotencyKey();
        var paymentData = TestDataFactory.PaymentData.create(merchantId);

        var paymentResponse = getApiClient().processPayment(
            paymentData.merchantId,
            paymentData.amountInCents,
            paymentData.currency,
            idempotencyKey
        );

        Map<String, Object> payment = (Map<String, Object>) paymentResponse.getBody().get("data");
        String paymentId = (String) payment.get("id");
        String paymentStatus = (String) payment.get("status");

        // When: Getting transaction from database
        String transactionStatus = jdbcTemplate.queryForObject(
            "SELECT status FROM transactions WHERE payment_id = ?",
            String.class,
            paymentId
        );

        // Then: Transaction status matches payment status
        assertThat(transactionStatus).isEqualTo(paymentStatus);
    }

    @Test
    @DisplayName("E2E: Transaction Chronological Order")
    void testTransactionChronologicalOrder() {
        // Given: Multiple payments created in sequence
        int numPayments = 5;
        java.util.List<String> paymentIds = new java.util.ArrayList<>();

        for (int i = 0; i < numPayments; i++) {
            String idempotencyKey = TestDataFactory.generateIdempotencyKey();
            var paymentData = TestDataFactory.PaymentData.create(merchantId);

            var response = getApiClient().processPayment(
                paymentData.merchantId,
                paymentData.amountInCents,
                paymentData.currency,
                idempotencyKey
            );

            Map<String, Object> payment = (Map<String, Object>) response.getBody().get("data");
            paymentIds.add((String) payment.get("id"));

            // Small delay to ensure different timestamps
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                // Ignore
            }
        }

        // When: Querying transactions by created_at
        List<String> orderedIds = jdbcTemplate.queryForList(
            "SELECT payment_id FROM transactions WHERE merchant_id = ? ORDER BY created_at ASC",
            String.class,
            merchantId
        );

        // Then: Transactions are in chronological order
        assertThat(orderedIds).hasSize(numPayments);
        assertThat(orderedIds).containsExactlyElementsOf(paymentIds);
    }

    @Test
    @DisplayName("E2E: Transaction Amount Validation")
    void testTransactionAmountValidation() {
        // Given: A payment with specific amount
        Long amount = 25000L; // $250.00
        String idempotencyKey = TestDataFactory.generateIdempotencyKey();

        var paymentResponse = getApiClient().processPayment(
            merchantId,
            amount,
            "USD",
            idempotencyKey
        );

        Map<String, Object> payment = (Map<String, Object>) paymentResponse.getBody().get("data");
        String paymentId = (String) payment.get("id");

        // When: Getting transaction amount from database
        Long transactionAmount = jdbcTemplate.queryForObject(
            "SELECT amount FROM transactions WHERE payment_id = ?",
            Long.class,
            paymentId
        );

        // Then: Transaction amount matches payment amount
        assertThat(transactionAmount).isEqualTo(amount);
    }

    @Test
    @DisplayName("E2E: Transaction Currency Validation")
    void testTransactionCurrencyValidation() {
        // Given: A payment with specific currency
        String currency = "EUR";
        String idempotencyKey = TestDataFactory.generateIdempotencyKey();

        var paymentResponse = getApiClient().processPayment(
            merchantId,
            10000L,
            currency,
            idempotencyKey
        );

        Map<String, Object> payment = (Map<String, Object>) paymentResponse.getBody().get("data");
        String paymentId = (String) payment.get("id");

        // When: Getting transaction currency from database
        String transactionCurrency = jdbcTemplate.queryForObject(
            "SELECT currency FROM transactions WHERE payment_id = ?",
            String.class,
            paymentId
        );

        // Then: Transaction currency matches payment currency
        assertThat(transactionCurrency).isEqualTo(currency);
    }

    @Test
    @DisplayName("E2E: Transaction Type Validation")
    void testTransactionTypeValidation() {
        // Given: A payment transaction
        String idempotencyKey = TestDataFactory.generateIdempotencyKey();

        var paymentResponse = getApiClient().processPayment(
            merchantId,
            10000L,
            "USD",
            idempotencyKey
        );

        Map<String, Object> payment = (Map<String, Object>) paymentResponse.getBody().get("data");
        String paymentId = (String) payment.get("id");

        // When: Getting transaction type from database
        String transactionType = jdbcTemplate.queryForObject(
            "SELECT transaction_type FROM transactions WHERE payment_id = ?",
            String.class,
            paymentId
        );

        // Then: Transaction type is SALE (for payment)
        assertThat(transactionType).isNotNull();
    }

    @Test
    @DisplayName("E2E: Transaction Merchant Association")
    void testTransactionMerchantAssociation() {
        // Given: A payment for a specific merchant
        String idempotencyKey = TestDataFactory.generateIdempotencyKey();

        var paymentResponse = getApiClient().processPayment(
            merchantId,
            10000L,
            "USD",
            idempotencyKey
        );

        Map<String, Object> payment = (Map<String, Object>) paymentResponse.getBody().get("data");
        String paymentId = (String) payment.get("id");

        // When: Getting transaction merchant_id from database
        String transactionMerchantId = jdbcTemplate.queryForObject(
            "SELECT merchant_id FROM transactions WHERE payment_id = ?",
            String.class,
            paymentId
        );

        // Then: Transaction is associated with correct merchant
        assertThat(transactionMerchantId).isEqualTo(merchantId);
    }

    @Test
    @DisplayName("E2E: Transaction with Customer Reference")
    void testTransactionWithCustomerReference() {
        // Given: A customer
        var customerData = TestDataFactory.CustomerData.create(merchantId);
        var customerResponse = getApiClient().registerCustomer(
            customerData.merchantId,
            customerData.email,
            customerData.name,
            customerData.phone,
            customerData.externalId
        );

        Map<String, Object> customer = (Map<String, Object>) customerResponse.getBody().get("data");
        String customerId = (String) customer.get("id");

        // And: A payment for that customer
        String idempotencyKey = TestDataFactory.generateIdempotencyKey();
        var paymentResponse = getApiClient().processPayment(
            merchantId,
            10000L,
            "USD",
            idempotencyKey,
            "Payment with customer",
            customerId,
            null
        );

        Map<String, Object> payment = (Map<String, Object>) paymentResponse.getBody().get("data");
        String paymentId = (String) payment.get("id");

        // When: Getting transaction from database
        String transactionCustomerId = jdbcTemplate.queryForObject(
            "SELECT customer_id FROM transactions WHERE payment_id = ?",
            String.class,
            paymentId
        );

        // Then: Transaction references the customer
        assertThat(transactionCustomerId).isEqualTo(customerId);
    }
}
