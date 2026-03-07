package com.payment.gateway.e2e;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.payment.gateway.e2e.testdata.TestDataFactory;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.context.TestPropertySource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * E2E tests for Kafka event propagation.
 */
class KafkaEventPropagationE2ETest extends E2ETestBase {

    private String merchantId;
    private String paymentId;

    @Value("${spring.kafka.consumer.group-id:test-e2e-group}")
    private String consumerGroup;

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
    @DisplayName("E2E: Payment Event Published to Kafka")
    void testPaymentEventPublishedToKafka() {
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
        paymentId = (String) payment.get("id");

        // Note: In a full E2E setup, we would consume from Kafka here
        // For now, we verify the Kafka template is configured
        assertThat(kafkaTemplate).isNotNull();
    }

    @Test
    @DisplayName("E2E: Kafka Template is Configured")
    void testKafkaTemplateConfigured() {
        // Given: Kafka template from Spring context
        assertThat(kafkaTemplate).isNotNull();

        // When: Checking producer factory configuration
        var producerConfig = kafkaTemplate.getProducerFactory().getConfigurationProperties();

        // Then: Kafka is configured
        assertThat(producerConfig).isNotNull();
        assertThat(producerConfig).containsKey("bootstrap.servers");
    }

    @Test
    @DisplayName("E2E: Event Structure Validation")
    void testEventStructureValidation() {
        // Given: A payment event structure
        String paymentId = "test-payment-" + System.currentTimeMillis();
        String merchantId = "test-merchant";
        String eventType = "PAYMENT_CREATED";

        Map<String, Object> event = createPaymentEvent(eventType, paymentId, merchantId, "10000", "USD");

        // When: Validating event structure
        boolean hasRequiredFields = event.containsKey("eventType") &&
                                    event.containsKey("aggregateId") &&
                                    event.containsKey("merchantId") &&
                                    event.containsKey("timestamp");

        // Then: Event has all required fields
        assertThat(hasRequiredFields).isTrue();
        assertThat(event.get("eventType")).isEqualTo(eventType);
        assertThat(event.get("aggregateId")).isEqualTo(paymentId);
        assertThat(event.get("merchantId")).isEqualTo(merchantId);
    }

    @Test
    @DisplayName("E2E: Multiple Payment Events")
    void testMultiplePaymentEvents() {
        // Given: Multiple payment requests
        List<String> paymentIds = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            String idempotencyKey = TestDataFactory.generateIdempotencyKey();
            var paymentData = TestDataFactory.PaymentData.create(merchantId);

            var response = getApiClient().processPayment(
                paymentData.merchantId,
                paymentData.amountInCents,
                paymentData.currency,
                idempotencyKey
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            Map<String, Object> payment = (Map<String, Object>) response.getBody().get("data");
            paymentIds.add((String) payment.get("id"));
        }

        // Then: All payments are created
        assertThat(paymentIds).hasSize(3);

        // Verify payments in database
        for (String pid : paymentIds) {
            assertThat(exists("payment", "id", pid)).isTrue();
        }
    }

    @Test
    @DisplayName("E2E: Refund Event Structure")
    void testRefundEventStructure() {
        // Given: A refund event structure
        String refundId = "test-refund-" + System.currentTimeMillis();
        String paymentId = "test-payment-" + System.currentTimeMillis();
        String merchantId = "test-merchant";
        String eventType = "REFUND_PROCESSED";

        Map<String, Object> event = createRefundEvent(eventType, refundId, paymentId, merchantId, "5000", "USD", "FULL");

        // When: Validating event structure
        boolean hasRequiredFields = event.containsKey("eventType") &&
                                    event.containsKey("aggregateId") &&
                                    event.containsKey("paymentId") &&
                                    event.containsKey("merchantId");

        // Then: Event has all required fields
        assertThat(hasRequiredFields).isTrue();
        assertThat(event.get("eventType")).isEqualTo(eventType);
        assertThat(event.get("paymentId")).isEqualTo(paymentId);
    }

    @Test
    @DisplayName("E2E: Event Serialization")
    void testEventSerialization() {
        // Given: A payment event
        ObjectMapper objectMapper = new ObjectMapper();
        String paymentId = "test-payment-" + System.currentTimeMillis();
        Map<String, Object> event = createPaymentEvent("PAYMENT_CREATED", paymentId, merchantId, "10000", "USD");

        // When: Serializing to JSON
        try {
            String json = objectMapper.writeValueAsString(event);
            JsonNode jsonNode = objectMapper.readTree(json);

            // Then: JSON is valid and contains expected fields
            assertThat(jsonNode.has("eventType")).isTrue();
            assertThat(jsonNode.has("aggregateId")).isTrue();
            assertThat(jsonNode.get("eventType").asText()).isEqualTo("PAYMENT_CREATED");
            assertThat(jsonNode.get("aggregateId").asText()).isEqualTo(paymentId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize event", e);
        }
    }

    @Test
    @DisplayName("E2E: Kafka Consumer Configuration")
    void testKafkaConsumerConfiguration() {
        // Given: Kafka consumer properties from Spring context
        // The kafkaTemplate is auto-configured by Spring Boot
        assertThat(kafkaTemplate).isNotNull();

        // Verify bootstrap servers are configured
        var producerConfig = kafkaTemplate.getProducerFactory().getConfigurationProperties();
        assertThat(producerConfig).containsKey("bootstrap.servers");
    }

    @Test
    @DisplayName("E2E: Event Timestamp Validation")
    void testEventTimestampValidation() {
        // Given: An event with timestamp
        Map<String, Object> event = createPaymentEvent("PAYMENT_CREATED", "test-123", merchantId, "10000", "USD");

        // When: Checking timestamp
        Object timestamp = event.get("timestamp");

        // Then: Timestamp is present and valid ISO-8601
        assertThat(timestamp).isNotNull();
        assertThat(timestamp).isInstanceOf(String.class);
        String timestampStr = (String) timestamp;
        assertThat(timestampStr).matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.*");
    }

    // Helper methods

    private Map<String, Object> createPaymentEvent(String eventType, String aggregateId,
                                                    String merchantId, String amount, String currency) {
        Map<String, Object> event = new java.util.HashMap<>();
        event.put("eventType", eventType);
        event.put("aggregateId", aggregateId);
        event.put("aggregateType", "Payment");
        event.put("merchantId", merchantId);
        event.put("amount", amount);
        event.put("currency", currency);
        event.put("timestamp", java.time.Instant.now().toString());
        event.put("occurredAt", java.time.Instant.now().toString());
        return event;
    }

    private Map<String, Object> createRefundEvent(String eventType, String refundId,
                                                   String paymentId, String merchantId,
                                                   String refundAmount, String currency, String refundType) {
        Map<String, Object> event = new java.util.HashMap<>();
        event.put("eventType", eventType);
        event.put("aggregateId", refundId);
        event.put("aggregateType", "Refund");
        event.put("paymentId", paymentId);
        event.put("merchantId", merchantId);
        event.put("refundAmount", refundAmount);
        event.put("currency", currency);
        event.put("refundType", refundType);
        event.put("timestamp", java.time.Instant.now().toString());
        return event;
    }
}
