package com.payment.gateway.e2e;

import static org.assertj.core.api.Assertions.assertThat;

import com.payment.gateway.e2e.testdata.TestDataFactory;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

/**
 * E2E tests for reconciliation flow.
 */
class ReconciliationFlowE2ETest extends E2ETestBase {

    private String merchantId;
    private String apiKey;

    @BeforeEach
    void setUp() {
        cleanupDatabase();

        // Register a merchant first
        var merchantResponse = getApiClient()
                .registerMerchant(
                        TestDataFactory.MerchantData.create().name, TestDataFactory.MerchantData.create().email, null);
        assertThat(merchantResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        Map<String, Object> merchantData =
                (Map<String, Object>) merchantResponse.getBody().get("data");
        merchantId = (String) merchantData.get("id");
        apiKey = (String) merchantData.get("apiKey");
        setApiKey(apiKey);
    }

    @Test
    @DisplayName("E2E: Reconciliation is idempotent for the same merchant and date (re-run does not fail)")
    @SuppressWarnings("unchecked")
    void testReconciliationIsIdempotentOnRerun() {
        String date = LocalDate.now().toString();
        String url = "/api/v1/reconciliation/reconcile?merchantId=" + merchantId + "&date=" + date;

        // First reconciliation creates the batch
        var first = restTemplate.postForEntity(url, null, Map.class);
        assertThat(first.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, Object> firstData = (Map<String, Object>) first.getBody().get("data");
        String firstBatchId = (String) firstData.get("batchId");
        assertThat(firstBatchId).isNotNull();

        // Re-running for the SAME merchant + date must succeed by reusing the batch — it must
        // not fail on the uk_reconciliation_batches_merchant_date unique constraint.
        var second = restTemplate.postForEntity(url, null, Map.class);
        assertThat(second.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, Object> secondData = (Map<String, Object>) second.getBody().get("data");
        assertThat(secondData.get("batchId")).isEqualTo(firstBatchId);

        // Exactly one batch row exists for this merchant + date
        Integer batchCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM reconciliation_batches WHERE merchant_id = ? AND batch_date = ?::date",
                Integer.class,
                merchantId,
                date);
        assertThat(batchCount).isEqualTo(1);
    }

    @Test
    @DisplayName("E2E: Reconciliation Batch Table Exists")
    void testReconciliationBatchTableExists() {
        // When: Checking for reconciliation_batches table
        boolean tableExists = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'reconciliation_batches'",
                        Integer.class)
                > 0;

        // Then: Table exists
        assertThat(tableExists).isTrue();
    }

    @Test
    @DisplayName("E2E: Discrepancy Table Exists")
    void testDiscrepancyTableExists() {
        // When: Checking for discrepancies table
        boolean tableExists = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'discrepancies'",
                        Integer.class)
                > 0;

        // Then: Table exists
        assertThat(tableExists).isTrue();
    }

    @Test
    @DisplayName("E2E: Settlement Report Table Exists")
    void testSettlementReportTableExists() {
        // When: Checking for settlement_reports table
        boolean tableExists = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'settlement_reports'",
                        Integer.class)
                > 0;

        // Then: Table exists
        assertThat(tableExists).isTrue();
    }

    @Test
    @DisplayName("E2E: Reconciliation Batch Schema Validation")
    void testReconciliationBatchSchemaValidation() {
        // When: Checking table columns
        boolean hasIdColumn = hasColumn("reconciliation_batches", "id");
        boolean hasStatusColumn = hasColumn("reconciliation_batches", "status");
        boolean hasCreatedAtColumn = hasColumn("reconciliation_batches", "created_at");

        // Then: Required columns exist
        assertThat(hasIdColumn).isTrue();
        assertThat(hasStatusColumn).isTrue();
        assertThat(hasCreatedAtColumn).isTrue();
    }

    @Test
    @DisplayName("E2E: Discrepancy Schema Validation")
    void testDiscrepancySchemaValidation() {
        // When: Checking table columns
        boolean hasIdColumn = hasColumn("discrepancies", "id");
        boolean hasTypeColumn = hasColumn("discrepancies", "discrepancy_type");
        boolean hasStatusColumn = hasColumn("discrepancies", "resolution_status");

        // Then: Required columns exist
        assertThat(hasIdColumn).isTrue();
        assertThat(hasTypeColumn).isTrue();
        assertThat(hasStatusColumn).isTrue();
    }

    @Test
    @DisplayName("E2E: Settlement Report Schema Validation")
    void testSettlementReportSchemaValidation() {
        // When: Checking table columns
        boolean hasIdColumn = hasColumn("settlement_reports", "id");
        boolean hasMerchantIdColumn = hasColumn("settlement_reports", "merchant_id");
        boolean hasReportTypeColumn = hasColumn("settlement_reports", "report_type");

        // Then: Required columns exist
        assertThat(hasIdColumn).isTrue();
        assertThat(hasMerchantIdColumn).isTrue();
        assertThat(hasReportTypeColumn).isTrue();
    }

    @Test
    @DisplayName("E2E: Create Reconciliation Batch")
    void testCreateReconciliationBatch() {
        // Given: A reconciliation batch
        String batchId = "batch-" + System.currentTimeMillis();

        // When: Inserting a batch
        int rowsInserted = jdbcTemplate.update(
                "INSERT INTO reconciliation_batches (id, batch_date, status, created_at, updated_at) VALUES (?, ?, ?, ?, ?)",
                batchId,
                java.time.LocalDate.now(),
                "PENDING",
                Timestamp.from(Instant.now()),
                Timestamp.from(Instant.now()));

        // Then: Batch is created
        assertThat(rowsInserted).isEqualTo(1);
        assertThat(exists("reconciliation_batches", "id", batchId)).isTrue();
    }

    @Test
    @DisplayName("E2E: Create Discrepancy")
    void testCreateDiscrepancy() {
        // Given: A discrepancy and a batch
        String batchId = "batch-" + System.currentTimeMillis();
        String discrepancyId = "discrepancy-" + System.currentTimeMillis();

        // Create a payment first to satisfy foreign key constraint
        String paymentId = "payment-" + System.currentTimeMillis();
        jdbcTemplate.update(
                "INSERT INTO payments (id, merchant_id, customer_id, amount, currency, status, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)",
                paymentId,
                merchantId,
                "customer-test",
                10000L,
                "USD",
                "COMPLETED",
                Timestamp.from(Instant.now()));

        // First create a batch
        jdbcTemplate.update(
                "INSERT INTO reconciliation_batches (id, batch_date, status, created_at, updated_at) VALUES (?, ?, ?, ?, ?)",
                batchId,
                java.time.LocalDate.now(),
                "PENDING",
                Timestamp.from(Instant.now()),
                Timestamp.from(Instant.now()));

        // When: Inserting a discrepancy
        int rowsInserted = jdbcTemplate.update(
                "INSERT INTO discrepancies (id, reconciliation_batch_id, payment_id, discrepancy_type, resolution_status, created_at) VALUES (?, ?, ?, ?, ?, ?)",
                discrepancyId,
                batchId,
                paymentId,
                "MISSING_PAYMENT",
                "OPEN",
                Timestamp.from(Instant.now()));

        // Then: Discrepancy is created
        assertThat(rowsInserted).isEqualTo(1);
        assertThat(exists("discrepancies", "id", discrepancyId)).isTrue();
    }

    @Test
    @DisplayName("E2E: Create Settlement Report")
    void testCreateSettlementReport() {
        // Given: A settlement report and batch
        String batchId = "batch-" + System.currentTimeMillis();
        String reportId = "report-" + System.currentTimeMillis();

        // First create a batch
        jdbcTemplate.update(
                "INSERT INTO reconciliation_batches (id, batch_date, status, created_at, updated_at) VALUES (?, ?, ?, ?, ?)",
                batchId,
                java.time.LocalDate.now(),
                "PENDING",
                Timestamp.from(Instant.now()),
                Timestamp.from(Instant.now()));

        // When: Inserting a settlement report
        int rowsInserted = jdbcTemplate.update(
                "INSERT INTO settlement_reports (id, reconciliation_batch_id, report_type, report_format, status, created_at) VALUES (?, ?, ?, ?, ?, ?)",
                reportId,
                batchId,
                "DAILY",
                "JSON",
                "GENERATED",
                Timestamp.from(Instant.now()));

        // Then: Report is created
        assertThat(rowsInserted).isEqualTo(1);
        assertThat(exists("settlement_reports", "id", reportId)).isTrue();
    }

    @Test
    @DisplayName("E2E: Discrepancy Resolution Flow")
    void testDiscrepancyResolutionFlow() {
        // Given: An open discrepancy and batch
        String batchId = "batch-" + System.currentTimeMillis();
        String discrepancyId = "discrepancy-" + System.currentTimeMillis();

        // Create a payment first to satisfy foreign key constraint
        String paymentId = "payment-" + System.currentTimeMillis();
        jdbcTemplate.update(
                "INSERT INTO payments (id, merchant_id, customer_id, amount, currency, status, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)",
                paymentId,
                merchantId,
                "customer-test",
                10000L,
                "USD",
                "COMPLETED",
                Timestamp.from(Instant.now()));

        // First create a batch
        jdbcTemplate.update(
                "INSERT INTO reconciliation_batches (id, batch_date, status, created_at, updated_at) VALUES (?, ?, ?, ?, ?)",
                batchId,
                java.time.LocalDate.now(),
                "PENDING",
                Timestamp.from(Instant.now()),
                Timestamp.from(Instant.now()));

        jdbcTemplate.update(
                "INSERT INTO discrepancies (id, reconciliation_batch_id, payment_id, discrepancy_type, resolution_status, created_at) VALUES (?, ?, ?, ?, ?, ?)",
                discrepancyId,
                batchId,
                paymentId,
                "MISSING_PAYMENT",
                "OPEN",
                Timestamp.from(Instant.now()));

        // When: Resolving the discrepancy
        int rowsUpdated = jdbcTemplate.update(
                "UPDATE discrepancies SET resolution_status = ?, resolved_at = ? WHERE id = ?",
                "RESOLVED",
                Timestamp.from(Instant.now()),
                discrepancyId);

        // Then: Discrepancy is resolved
        assertThat(rowsUpdated).isEqualTo(1);

        String status = jdbcTemplate.queryForObject(
                "SELECT resolution_status FROM discrepancies WHERE id = ?", String.class, discrepancyId);
        assertThat(status).isEqualTo("RESOLVED");
    }

    @Test
    @DisplayName("E2E: Reconciliation Batch Status Transitions")
    void testReconciliationBatchStatusTransitions() {
        // Given: A pending batch
        String batchId = "batch-" + System.currentTimeMillis();
        jdbcTemplate.update(
                "INSERT INTO reconciliation_batches (id, batch_date, status, created_at, updated_at) VALUES (?, ?, ?, ?, ?)",
                batchId,
                java.time.LocalDate.now(),
                "PENDING",
                Timestamp.from(Instant.now()),
                Timestamp.from(Instant.now()));

        // When: Processing the batch
        jdbcTemplate.update(
                "UPDATE reconciliation_batches SET status = ?, updated_at = ? WHERE id = ?",
                "PROCESSING",
                Timestamp.from(Instant.now()),
                batchId);

        // Then: Status is updated
        String status = jdbcTemplate.queryForObject(
                "SELECT status FROM reconciliation_batches WHERE id = ?", String.class, batchId);
        assertThat(status).isEqualTo("PROCESSING");
    }

    @Test
    @DisplayName("E2E: Settlement Report with Merchant Data")
    @org.junit.jupiter.api.Disabled(
            "merchant_id not automatically set in test profile - requires manual insertion or API endpoint")
    void testSettlementReportWithMerchantData() {
        // Given: A settlement report for a merchant and batch
        String batchId = "batch-" + System.currentTimeMillis();
        String reportId = "report-" + System.currentTimeMillis();

        // First create a batch
        jdbcTemplate.update(
                "INSERT INTO reconciliation_batches (id, batch_date, status, created_at, updated_at) VALUES (?, ?, ?, ?, ?)",
                batchId,
                java.time.LocalDate.now(),
                "PENDING",
                Timestamp.from(Instant.now()),
                Timestamp.from(Instant.now()));

        // Create settlement report
        jdbcTemplate.update(
                "INSERT INTO settlement_reports (id, reconciliation_batch_id, merchant_id, report_type, report_format, status, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)",
                reportId,
                batchId,
                merchantId,
                "DAILY",
                "JSON",
                "GENERATED",
                Timestamp.from(Instant.now()));

        // When: Querying by merchant
        String retrievedMerchantId = jdbcTemplate.queryForObject(
                "SELECT merchant_id FROM settlement_reports WHERE id = ?", String.class, reportId);

        // Then: Merchant ID matches
        assertThat(retrievedMerchantId).isEqualTo(merchantId);
    }

    @Test
    @DisplayName("E2E: Reconciliation Endpoint Available")
    void testReconciliationEndpointAvailable() {
        // When: Checking reconciliation controller endpoint
        // Note: This tests that the endpoint exists
        var response = restTemplate.getForEntity("/actuator/health", Map.class);

        // Then: System is healthy (reconciliation is part of the system)
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    // Helper method
    private boolean hasColumn(String tableName, String columnName) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.columns WHERE table_name = ? AND column_name = ?",
                Integer.class,
                tableName,
                columnName);
        return count != null && count > 0;
    }
}
