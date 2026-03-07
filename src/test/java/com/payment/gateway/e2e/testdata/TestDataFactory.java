package com.payment.gateway.e2e.testdata;

import com.payment.gateway.domain.customer.model.Customer;
import com.payment.gateway.domain.merchant.model.Merchant;
import com.payment.gateway.domain.merchant.model.MerchantConfiguration;
import com.payment.gateway.domain.merchant.model.MerchantStatus;
import com.payment.gateway.domain.payment.model.PaymentMethod;
import com.payment.gateway.domain.payment.model.PaymentMetadata;
import com.payment.gateway.domain.payment.model.PaymentStatus;
import com.payment.gateway.infrastructure.customer.adapter.in.rest.CustomerController;
import com.payment.gateway.infrastructure.merchant.adapter.in.rest.MerchantController;
import com.payment.gateway.infrastructure.payment.adapter.in.rest.CreatePaymentRequest;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Factory for creating test data for E2E tests.
 */
public class TestDataFactory {

    private static final String DEFAULT_TEST_EMAIL = "test-{uuid}@example.com";
    private static final String DEFAULT_TEST_NAME = "Test Merchant {uuid}";
    private static final String DEFAULT_WEBHOOK_URL = "https://webhook.site/test-{uuid}";

    // Merchant test data
    public static class MerchantData {
        public final String name;
        public final String email;
        public final String webhookUrl;

        public MerchantData(String name, String email, String webhookUrl) {
            this.name = name;
            this.email = email;
            this.webhookUrl = webhookUrl;
        }

        public static MerchantData create() {
            String uuid = UUID.randomUUID().toString().substring(0, 8);
            return new MerchantData(
                "Test Merchant " + uuid,
                "test-" + uuid + "@example.com",
                "https://webhook.site/" + uuid
            );
        }

        public static MerchantData create(String name, String email) {
            return new MerchantData(name, email, "https://webhook.site/test");
        }
    }

    // Customer test data
    public static class CustomerData {
        public final String merchantId;
        public final String email;
        public final String name;
        public final String phone;
        public final String externalId;

        public CustomerData(String merchantId, String email, String name, String phone, String externalId) {
            this.merchantId = merchantId;
            this.email = email;
            this.name = name;
            this.phone = phone;
            this.externalId = externalId;
        }

        public static CustomerData create(String merchantId) {
            String uuid = UUID.randomUUID().toString().substring(0, 8);
            return new CustomerData(
                merchantId,
                "customer-" + uuid + "@example.com",
                "Test Customer " + uuid,
                "+1-555-0100",
                "ext-" + uuid
            );
        }
    }

    // Payment test data
    public static class PaymentData {
        public final String merchantId;
        public final Long amountInCents;
        public final String currency;
        public final String customerId;
        public final String description;
        public final String idempotencyKey;
        public final List<CreatePaymentRequest.PaymentItemRequest> items;

        public PaymentData(String merchantId, Long amountInCents, String currency,
                          String customerId, String description, String idempotencyKey,
                          List<CreatePaymentRequest.PaymentItemRequest> items) {
            this.merchantId = merchantId;
            this.amountInCents = amountInCents;
            this.currency = currency;
            this.customerId = customerId;
            this.description = description;
            this.idempotencyKey = idempotencyKey;
            this.items = items;
        }

        public static PaymentData create(String merchantId) {
            String uuid = UUID.randomUUID().toString().substring(0, 8);
            return new PaymentData(
                merchantId,
                10000L, // $100.00
                "USD",
                null,
                "Test payment " + uuid,
                uuid,
                null
            );
        }

        public static PaymentData createWithCustomer(String merchantId, String customerId) {
            String uuid = UUID.randomUUID().toString().substring(0, 8);
            return new PaymentData(
                merchantId,
                15000L, // $150.00
                "USD",
                customerId,
                "Test payment with customer " + uuid,
                uuid,
                Arrays.asList(
                    new CreatePaymentRequest.PaymentItemRequest("Item 1", 2, 5000L),
                    new CreatePaymentRequest.PaymentItemRequest("Item 2", 1, 5000L)
                )
            );
        }

        public static PaymentData createWithAmount(String merchantId, Long amountInCents) {
            String uuid = UUID.randomUUID().toString().substring(0, 8);
            return new PaymentData(
                merchantId,
                amountInCents,
                "USD",
                null,
                "Test payment " + uuid,
                uuid,
                null
            );
        }
    }

    // Refund test data
    public static class RefundData {
        public final String paymentId;
        public final String merchantId;
        public final Long amountInCents;
        public final String idempotencyKey;
        public final String reason;

        public RefundData(String paymentId, String merchantId, Long amountInCents,
                         String idempotencyKey, String reason) {
            this.paymentId = paymentId;
            this.merchantId = merchantId;
            this.amountInCents = amountInCents;
            this.idempotencyKey = idempotencyKey;
            this.reason = reason;
        }

        public static RefundData create(String paymentId, String merchantId) {
            String uuid = UUID.randomUUID().toString().substring(0, 8);
            return new RefundData(
                paymentId,
                merchantId,
                10000L, // $100.00
                "refund-" + uuid,
                "Customer requested refund"
            );
        }

        public static RefundData createWithAmount(String paymentId, String merchantId, Long amountInCents) {
            String uuid = UUID.randomUUID().toString().substring(0, 8);
            return new RefundData(
                paymentId,
                merchantId,
                amountInCents,
                "refund-" + uuid,
                "Partial refund"
            );
        }
    }

    /**
     * Generate a unique idempotency key.
     */
    public static String generateIdempotencyKey() {
        return "idem-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    /**
     * Generate a test API key format.
     */
    public static String generateApiKey() {
        return "pk_test_" + UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * Create a test card number (valid Luhn).
     */
    public static String getTestCardNumber() {
        return "4111111111111111"; // Visa test card
    }

    /**
     * Create test card expiry month.
     */
    public static String getTestExpiryMonth() {
        return "12";
    }

    /**
     * Create test card expiry year.
     */
    public static String getTestExpiryYear() {
        return "2030";
    }

    /**
     * Create test CVV.
     */
    public static String getTestCvv() {
        return "123";
    }

    /**
     * Create test cardholder name.
     */
    public static String getTestCardholderName() {
        return "TEST CARDHOLDER";
    }
}
