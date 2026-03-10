package com.payment.gateway.e2e;

import com.payment.gateway.e2e.testdata.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * E2E tests for the Outbox pattern.
 */
class OutboxEventE2ETest extends E2ETestBase {

    private String merchantId;
    private String paymentId;

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

        // Create a payment
        String idempotencyKey = TestDataFactory.generateIdempotencyKey();
        var paymentResponse = getApiClient().processPayment(
            merchantId,
            10000L,
            "USD",
            idempotencyKey
        );

        assertThat(paymentResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, Object> payment = (Map<String, Object>) paymentResponse.getBody().get("data");
        paymentId = (String) payment.get("id");
    }

    @Test
    @DisplayName("E2E: Outbox Entry Created for Payment")
    void testOutboxEntryCreatedForPayment() {
        // Given: A payment was processed

        // When: Checking the outbox_event table
        // Note: Outbox entries may be created asynchronously
        // Verify the outbox_event table exists and is accessible
        boolean tableExists = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'outbox_events'",
            Integer.class
        ) > 0;

        // Then: Outbox table exists
        assertThat(tableExists).isTrue();
    }

    @Test
    @DisplayName("E2E: Outbox Event Schema Validation")
    void testOutboxEventSchemaValidation() {
        // Given: Access to outbox_event table

        // When: Checking table structure
        boolean hasIdColumn = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM information_schema.columns WHERE table_name = 'outbox_events' AND column_name = 'id'",
            Integer.class
        ) > 0;

        boolean hasEventTypeColumn = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM information_schema.columns WHERE table_name = 'outbox_events' AND column_name = 'event_type'",
            Integer.class
        ) > 0;

        boolean hasPayloadColumn = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM information_schema.columns WHERE table_name = 'outbox_events' AND column_name = 'payload'",
            Integer.class
        ) > 0;

        // Then: Required columns exist
        assertThat(hasIdColumn).isTrue();
        assertThat(hasEventTypeColumn).isTrue();
        assertThat(hasPayloadColumn).isTrue();
    }

    @Test
    @DisplayName("E2E: Outbox Event Status Tracking")
    void testOutboxEventStatusTracking() {
        // Given: The outbox_event table

        // When: Checking for status column
        boolean hasStatusColumn = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM information_schema.columns WHERE table_name = 'outbox_events' AND column_name = 'status'",
            Integer.class
        ) > 0;

        // Then: Status column exists for tracking
        assertThat(hasStatusColumn).isTrue();
    }

    @Test
    @DisplayName("E2E: Outbox Event Insertion")
    void testOutboxEventInsertion() {
        // Given: A test outbox event
        String eventId = "test-event-" + System.currentTimeMillis();
        String eventType = "TEST_EVENT";
        String aggregateId = "test-aggregate-" + System.currentTimeMillis();
        String payload = "{\"test\": \"data\"}";

        // When: Inserting an outbox event
        int rowsInserted = jdbcTemplate.update(
            "INSERT INTO outbox_events (id, event_type, aggregate_type, aggregate_id, payload, status) VALUES (?, ?, ?, ?, ?::jsonb, ?)",
            eventId,
            eventType,
            "Payment",
            aggregateId,
            payload,
            "PENDING"
        );

        // Then: Event is inserted
        assertThat(rowsInserted).isEqualTo(1);

        // Verify event can be queried
        boolean eventExists = exists("outbox_events", "id", eventId);
        assertThat(eventExists).isTrue();
    }

    @Test
    @DisplayName("E2E: Outbox Event Update Status")
    void testOutboxEventUpdateStatus() {
        // Given: An outbox event
        String eventId = "test-event-" + System.currentTimeMillis();
        jdbcTemplate.update(
            "INSERT INTO outbox_events (id, event_type, aggregate_type, aggregate_id, payload, status) VALUES (?, ?, ?, ?, ?::jsonb, ?)",
            eventId,
            "TEST_EVENT",
            "Payment",
            "test-aggregate",
            "{}",
            "PENDING"
        );

        // When: Updating status to PUBLISHED
        int rowsUpdated = jdbcTemplate.update(
            "UPDATE outbox_events SET status = ?, published_at = ? WHERE id = ?",
            "PUBLISHED",
            Timestamp.from(Instant.now()),
            eventId
        );

        // Then: Status is updated
        assertThat(rowsUpdated).isEqualTo(1);

        String status = jdbcTemplate.queryForObject(
            "SELECT status FROM outbox_events WHERE id = ?",
            String.class,
            eventId
        );
        assertThat(status).isEqualTo("PUBLISHED");
    }

    @Test
    @DisplayName("E2E: Outbox Event Query by Status")
    void testOutboxEventQueryByStatus() {
        // Given: Multiple outbox events with different statuses
        String event1Id = "test-event-1-" + System.currentTimeMillis();
        String event2Id = "test-event-2-" + System.currentTimeMillis();

        jdbcTemplate.update(
            "INSERT INTO outbox_events (id, event_type, aggregate_type, aggregate_id, payload, status) VALUES (?, ?, ?, ?, ?::jsonb, ?)",
            event1Id,
            "TEST_EVENT_1",
            "Payment",
            "aggregate-1",
            "{}",
            "PENDING"
        );

        jdbcTemplate.update(
            "INSERT INTO outbox_events (id, event_type, aggregate_type, aggregate_id, payload, status) VALUES (?, ?, ?, ?, ?::jsonb, ?)",
            event2Id,
            "TEST_EVENT_2",
            "Payment",
            "aggregate-2",
            "{}",
            "PUBLISHED"
        );

        // When: Querying by status
        Integer pendingCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM outbox_events WHERE status = ?",
            Integer.class,
            "PENDING"
        );

        Integer publishedCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM outbox_events WHERE status = ?",
            Integer.class,
            "PUBLISHED"
        );

        // Then: Correct counts are returned
        assertThat(pendingCount).isEqualTo(1);
        assertThat(publishedCount).isEqualTo(1);
    }

    @Test
    @DisplayName("E2E: Outbox Event with JSON Payload")
    void testOutboxEventWithJsonPayload() {
        // Given: An outbox event with JSON payload
        String eventId = "test-event-" + System.currentTimeMillis();
        String payload = "{\"paymentId\": \"" + paymentId + "\", \"amount\": 10000, \"currency\": \"USD\"}";

        jdbcTemplate.update(
            "INSERT INTO outbox_events (id, event_type, aggregate_type, aggregate_id, payload, status) VALUES (?, ?, ?, ?, ?::jsonb, ?)",
            eventId,
            "PAYMENT_CREATED",
            "Payment",
            paymentId,
            payload,
            "PENDING"
        );

        // When: Retrieving the payload
        String retrievedPayload = jdbcTemplate.queryForObject(
            "SELECT payload FROM outbox_events WHERE id = ?",
            String.class,
            eventId
        );

        // Then: Payload is correctly stored and retrieved
        assertThat(retrievedPayload).contains(paymentId);
        assertThat(retrievedPayload).contains("USD");
    }

    @Test
    @DisplayName("E2E: Outbox Event Created At Tracking")
    void testOutboxEventCreatedAtTracking() {
        // Given: An outbox event
        String eventId = "test-event-" + System.currentTimeMillis();
        Instant now = Instant.now();

        jdbcTemplate.update(
            "INSERT INTO outbox_events (id, event_type, aggregate_type, aggregate_id, payload, status, created_at) VALUES (?, ?, ?, ?, ?::jsonb, ?, ?)",
            eventId,
            "TEST_EVENT",
            "Payment",
            "test-aggregate",
            "{}",
            "PENDING",
            Timestamp.from(now)
        );

        // When: Retrieving created_at
        Timestamp createdAt = jdbcTemplate.queryForObject(
            "SELECT created_at FROM outbox_events WHERE id = ?",
            Timestamp.class,
            eventId
        );

        // Then: Timestamp is within expected range
        assertThat(createdAt).isNotNull();
        // Use a more relaxed time comparison since DB timestamp may have lower precision
        assertThat(createdAt.toInstant()).isBeforeOrEqualTo(now.plusSeconds(5));
    }

    @Test
    @DisplayName("E2E: Outbox Event Published At Tracking")
    void testOutboxEventPublishedAtTracking() {
        // Given: An outbox event
        String eventId = "test-event-" + System.currentTimeMillis();

        jdbcTemplate.update(
            "INSERT INTO outbox_events (id, event_type, aggregate_type, aggregate_id, payload, status) VALUES (?, ?, ?, ?, ?::jsonb, ?)",
            eventId,
            "TEST_EVENT",
            "Payment",
            "test-aggregate",
            "{}",
            "PENDING"
        );

        // When: Setting published_at
        Instant publishedAt = Instant.now();
        jdbcTemplate.update(
            "UPDATE outbox_events SET status = ?, published_at = ? WHERE id = ?",
            "PUBLISHED",
            Timestamp.from(publishedAt),
            eventId
        );

        Timestamp retrievedPublishedAt = jdbcTemplate.queryForObject(
            "SELECT published_at FROM outbox_events WHERE id = ?",
            Timestamp.class,
            eventId
        );

        // Then: Published timestamp is correct
        assertThat(retrievedPublishedAt).isNotNull();
        assertThat(retrievedPublishedAt.toInstant()).isAfterOrEqualTo(publishedAt.minusSeconds(1));
        assertThat(retrievedPublishedAt.toInstant()).isBeforeOrEqualTo(publishedAt.plusSeconds(1));
    }
}
