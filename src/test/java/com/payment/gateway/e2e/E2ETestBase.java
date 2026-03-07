package com.payment.gateway.e2e;

import com.payment.gateway.e2e.client.PaymentGatewayClient;
import com.payment.gateway.test.ContainerConfig;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;

/**
 * Base class for E2E tests with full Spring context.
 * Provides shared test infrastructure including REST client, Kafka, and database access.
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = {
        com.payment.gateway.PaymentGatewayApplication.class,
        com.payment.gateway.test.TestSecurityConfig.class
    },
    properties = {
        "spring.profiles.active=e2e",
        "spring.main.allow-bean-definition-override=true",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=false",
        "spring.jpa.properties.hibernate.format_sql=false",
        "logging.level.org.hibernate.SQL=warn",
        "logging.level.org.hibernate.type.descriptor.sql.BasicBinder=warn"
    }
)
@TestPropertySource(properties = {
    "spring.kafka.consumer.auto-offset-reset=earliest",
    "spring.kafka.consumer.group-id=test-e2e-group",
    "spring.kafka.listener.ack-mode=manual",
    "payment.gateway.idempotency.enabled=true"
})
@ActiveProfiles("e2e")
@Transactional
public abstract class E2ETestBase extends ContainerConfig {

    @Autowired
    protected TestRestTemplate restTemplate;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Autowired
    protected DataSource dataSource;

    @Autowired(required = false)
    protected KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    protected com.payment.gateway.infrastructure.merchant.adapter.in.rest.MerchantController merchantController;

    @Autowired
    protected com.payment.gateway.infrastructure.customer.adapter.in.rest.CustomerController customerController;

    @Autowired
    protected com.payment.gateway.infrastructure.payment.adapter.in.rest.PaymentController paymentController;

    @Autowired
    protected com.payment.gateway.infrastructure.refund.adapter.in.rest.RefundController refundController;

    @Autowired
    protected com.payment.gateway.infrastructure.transaction.adapter.in.rest.TransactionController transactionController;

    protected PaymentGatewayClient apiClient;

    protected String testApiKey;

    @BeforeEach
    void setupE2EBase() {
        // Reset test API key
        testApiKey = null;
    }

    /**
     * Create or get the API client for REST calls.
     */
    protected PaymentGatewayClient getApiClient() {
        if (apiClient == null) {
            apiClient = new PaymentGatewayClient(restTemplate);
        }
        return apiClient;
    }

    /**
     * Set the API key for subsequent requests.
     */
    protected void setApiKey(String apiKey) {
        this.testApiKey = apiKey;
        if (apiClient != null) {
            apiClient.setApiKey(apiKey);
        }
    }

    /**
     * Get API key header for requests.
     */
    protected HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        if (testApiKey != null) {
            headers.set("X-API-Key", testApiKey);
        }
        return headers;
    }

    /**
     * Clean up database after test.
     */
    protected void cleanupDatabase() {
        try {
            jdbcTemplate.execute("TRUNCATE TABLE reconciliation_batch CASCADE");
            jdbcTemplate.execute("TRUNCATE TABLE discrepancy CASCADE");
            jdbcTemplate.execute("TRUNCATE TABLE settlement_report CASCADE");
            jdbcTemplate.execute("TRUNCATE TABLE outbox_event CASCADE");
            jdbcTemplate.execute("TRUNCATE TABLE transaction CASCADE");
            jdbcTemplate.execute("TRUNCATE TABLE refund CASCADE");
            jdbcTemplate.execute("TRUNCATE TABLE payment CASCADE");
            jdbcTemplate.execute("TRUNCATE TABLE payment_method CASCADE");
            jdbcTemplate.execute("TRUNCATE TABLE customer CASCADE");
            jdbcTemplate.execute("TRUNCATE TABLE merchant CASCADE");
            jdbcTemplate.execute("TRUNCATE TABLE idempotency_key CASCADE");
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }

    /**
     * Force transaction flush to database.
     */
    protected void flushTransaction() {
        TestTransaction.flagForCommit();
        TestTransaction.end();
    }

    /**
     * Get count of records in a table.
     */
    protected int getCount(String tableName) {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + tableName, Integer.class);
    }

    /**
     * Check if a record exists.
     */
    protected boolean exists(String tableName, String idColumn, String id) {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM " + tableName + " WHERE " + idColumn + " = ?",
            Integer.class,
            id
        );
        return count != null && count > 0;
    }
}
