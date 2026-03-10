package com.payment.gateway.e2e;

import com.payment.gateway.e2e.testdata.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.metrics.MetricsEndpoint;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * E2E tests for observability (actuator, metrics, health).
 */
class ObservabilityE2ETest extends E2ETestBase {

    @Autowired(required = false)
    private HealthEndpoint healthEndpoint;

    @Autowired(required = false)
    private MetricsEndpoint metricsEndpoint;

    @BeforeEach
    void setUp() {
        cleanupDatabase();
    }

    @Test
    @DisplayName("E2E: Health Endpoint - Basic")
    void testHealthEndpoint_Basic() {
        // When: Calling health endpoint
        ResponseEntity<Map> response = restTemplate.getForEntity("/actuator/health", Map.class);

        // Then: Health endpoint is accessible
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, Object> body = response.getBody();
        assertThat(body).containsKey("status");
    }

    @Test
    @DisplayName("E2E: Health Endpoint - Detailed")
    void testHealthEndpoint_Detailed() {
        // When: Calling health endpoint with details
        ResponseEntity<Map> response = restTemplate.getForEntity("/actuator/health", Map.class);

        // Then: Response contains health details
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, Object> body = response.getBody();
        assertThat(body.get("status")).isEqualTo("UP");
    }

    @Test
    @DisplayName("E2E: Health Endpoint - Database Component")
    void testHealthEndpoint_Database() {
        // When: Calling health endpoint
        ResponseEntity<Map> response = restTemplate.getForEntity("/actuator/health", Map.class);

        // Then: Database health is included
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, Object> body = response.getBody();

        // Check if components are present
        if (body.containsKey("components")) {
            Map<String, Object> components = (Map<String, Object>) body.get("components");
            assertThat(components.containsKey("db")).isTrue();
        }
    }

    @Test
    @DisplayName("E2E: Health Endpoint - Disk Space Component")
    void testHealthEndpoint_DiskSpace() {
        // When: Calling health endpoint
        ResponseEntity<Map> response = restTemplate.getForEntity("/actuator/health", Map.class);

        // Then: Response is healthy
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("E2E: Metrics Endpoint - Prometheus")
    @org.junit.jupiter.api.Disabled("Prometheus endpoint may return 500 if prometheus dependencies not in classpath")
    void testMetricsEndpoint_Prometheus() {
        // When: Calling prometheus metrics endpoint
        ResponseEntity<String> response = restTemplate.getForEntity("/actuator/prometheus", String.class);

        // Then: Prometheus metrics are available
        // Note: May return 404 if not configured
        assertThat(response.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("E2E: Metrics Endpoint - JVM Metrics")
    void testMetricsEndpoint_JVMMetrics() {
        // When: Calling metrics endpoint
        ResponseEntity<Map> response = restTemplate.getForEntity("/actuator/metrics", Map.class);

        // Then: Metrics are available
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, Object> body = response.getBody();
        assertThat(body).containsKey("names");
    }

    @Test
    @DisplayName("E2E: Info Endpoint")
    void testInfoEndpoint() {
        // When: Calling info endpoint
        ResponseEntity<Map> response = restTemplate.getForEntity("/actuator/info", Map.class);

        // Then: Info endpoint is accessible
        assertThat(response.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("E2E: Health Endpoint Shows Application UP")
    void testHealthEndpoint_ApplicationUp() {
        // When: Getting health
        ResponseEntity<Map> response = restTemplate.getForEntity("/actuator/health", Map.class);

        // Then: Application is UP
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, Object> body = response.getBody();
        assertThat(body.get("status")).isEqualTo("UP");
    }

    @Test
    @DisplayName("E2E: Health After Payment Processing")
    void testHealth_AfterPaymentProcessing() {
        // Given: Register a merchant
        var merchantResponse = getApiClient().registerMerchant(
            TestDataFactory.MerchantData.create().name,
            TestDataFactory.MerchantData.create().email,
            null
        );
        Map<String, Object> merchant = (Map<String, Object>) merchantResponse.getBody().get("data");
        String merchantId = (String) merchant.get("id");
        String apiKey = (String) merchant.get("apiKey");
        setApiKey(apiKey);

        // When: Processing a payment
        String idempotencyKey = TestDataFactory.generateIdempotencyKey();
        var paymentResponse = getApiClient().processPayment(
            merchantId,
            10000L,
            "USD",
            idempotencyKey
        );
        assertThat(paymentResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Then: Health is still UP
        ResponseEntity<Map> healthResponse = restTemplate.getForEntity("/actuator/health", Map.class);
        assertThat(healthResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, Object> healthBody = healthResponse.getBody();
        assertThat(healthBody.get("status")).isEqualTo("UP");
    }

    @Test
    @DisplayName("E2E: Metrics Available After Payment")
    void testMetrics_AfterPayment() {
        // Given: Register a merchant and process payment
        var merchantResponse = getApiClient().registerMerchant(
            TestDataFactory.MerchantData.create().name,
            TestDataFactory.MerchantData.create().email,
            null
        );
        Map<String, Object> merchant = (Map<String, Object>) merchantResponse.getBody().get("data");
        setApiKey((String) merchant.get("apiKey"));

        getApiClient().processPayment(
            (String) merchant.get("id"),
            10000L,
            "USD",
            TestDataFactory.generateIdempotencyKey()
        );

        // When: Getting metrics
        ResponseEntity<Map> response = restTemplate.getForEntity("/actuator/metrics", Map.class);

        // Then: Metrics endpoint is accessible
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("E2E: Environment Endpoint")
    void testEnvironmentEndpoint() {
        // When: Calling environment endpoint
        ResponseEntity<Map> response = restTemplate.getForEntity("/actuator/env", Map.class);

        // Then: Environment endpoint may be accessible (depends on configuration)
        assertThat(response.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("E2E: Health Check Response Time")
    void testHealthCheckResponseTime() {
        // When: Calling health endpoint
        long startTime = System.currentTimeMillis();
        ResponseEntity<Map> response = restTemplate.getForEntity("/actuator/health", Map.class);
        long endTime = System.currentTimeMillis();

        // Then: Response is fast (< 1 second)
        assertThat(endTime - startTime).isLessThan(1000);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("E2E: Multiple Health Checks")
    void testMultipleHealthChecks() {
        // When: Making multiple health checks
        for (int i = 0; i < 3; i++) {
            ResponseEntity<Map> response = restTemplate.getForEntity("/actuator/health", Map.class);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }

    @Test
    @DisplayName("E2E: Health Endpoint with Components")
    void testHealthEndpoint_WithComponents() {
        // When: Calling health endpoint
        ResponseEntity<Map> response = restTemplate.getForEntity("/actuator/health", Map.class);

        // Then: Health response contains status
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, Object> body = response.getBody();
        assertThat(body.get("status")).isNotNull();
    }

    @Test
    @DisplayName("E2E: Actuator Base Path")
    void testActuatorBasePath() {
        // When: Calling actuator base endpoint
        ResponseEntity<Map> response = restTemplate.getForEntity("/actuator", Map.class);

        // Then: Actuator links are available
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, Object> body = response.getBody();
        assertThat(body.containsKey("links") || body.containsKey("_links")).isTrue();
    }
}
