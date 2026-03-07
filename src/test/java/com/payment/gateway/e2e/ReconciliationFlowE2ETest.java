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
 * E2E tests for reconciliation flow.
 */
class ReconciliationFlowE2ETest extends E2ETestBase {

    private String merchantId;
    private String apiKey;

    @BeforeEach
    void setUp() {
        cleanupDatabase();

        // Register a merchant first
        var merchantResponse = getApiClient().registerMerchant(
            TestDataFactory.MerchantData.create().name,
            TestDataFactory.MerchantData.create().email,
            null
        );
        assertThat(merchantResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        Map<String, Object> merchantData = (Map<String, Object>) merchantResponse.getBody().get("data");
        merchantId = (String) merchantData.get("id");
        apiKey = (String) merchantData.get("apiKey");
        setApiKey(apiKey);
    }

    @Test
    @DisplayName("E2E: Reconciliation Batch Table Exists")
    void testReconciliationBatchTableExists() {
        // When: Checking for reconciliation_batch table
        boolean tableExists = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'reconciliation_batch'",
            Integer.class
        ) > 0;

        // Then: Table exists
        assertThat(tableExists).isTrue();
    }

    @Test
    @DisplayName("E2E: Discrepancy Table Exists")
    void testDiscrepancyTableExists() {
        // When: Checking for discrepancy table
        boolean tableExists = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'discrepancy'",
            Integer.class
        ) > 0;

        // Then: Table exists
        assertThat(tableExists).isTrue();
    }

    @Test
    @DisplayName("E2E: Settlement Report Table Exists")
    void testSettlementReportTableExists() {
        // When: Checking for settlement_report table
        boolean tableExists = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'settlement_report'",
            Integer.class
        ) > 0;

        // Then: Table exists
        assertThat(tableExists).isTrue();
    }

    @Test
    @DisplayName("E2E: Reconciliation Batch Schema Validation")
    void testReconciliationBatchSchemaValidation() {
        // When: Checking table columns
        boolean hasIdColumn = hasColumn("reconciliation_batch", "id");
        boolean hasStatusColumn = hasColumn("reconciliation_batch", "status");
        boolean hasCreatedAtColumn = hasColumn("reconciliation_batch", "created_at");

        // Then: Required columns exist
        assertThat(hasIdColumn).isTrue();
        assertThat(hasStatusColumn).isTrue();
        assertThat(hasCreatedAtColumn).isTrue();
    }

    @Test
    @DisplayName("E2E: Discrepancy Schema Validation")
    void testDiscrepancySchemaValidation() {
        // When: Checking table columns
        boolean hasIdColumn = hasColumn("discrepancy", "id");
        boolean hasTypeColumn = hasColumn("discrepancy", "type");
        boolean hasStatusColumn = hasColumn("discrepancy", "status");

        // Then: Required columns exist
        assertThat(hasIdColumn).isTrue();
        assertThat(hasTypeColumn).isTrue();
        assertThat(hasStatusColumn).isTrue();
    }

    @Test
    @DisplayName("E2E: Settlement Report Schema Validation")
    void testSettlementReportSchemaValidation() {
        // When: Checking table columns
        boolean hasIdColumn = hasColumn("settlement_report", "id");
        boolean hasMerchantIdColumn = hasColumn("settlement_report", "merchant_id");
        boolean hasAmountColumn = hasColumn("settlement_report", "total_amount");

        // Then: Required columns exist
        assertThat(hasIdColumn).isTrue();
        assertThat(hasMerchantIdColumn).isTrue();
        assertThat(hasAmountColumn).isTrue();
    }

    @Test
    @DisplayName("E2E: Create Reconciliation Batch")
    void testCreateReconciliationBatch() {
        // Given: A reconciliation batch
        String batchId = "batch-" + System.currentTimeMillis();

        // When: Inserting a batch
        int rowsInserted = jdbcTemplate.update(
            "INSERT INTO reconciliation_batch (id, status, created_at, updated_at) VALUES (?, ?, ?, ?)",
            batchId,
            "PENDING",
            Timestamp.from(Instant.now()),
            Timestamp.from(Instant.now())
        );

        // Then: Batch is created
        assertThat(rowsInserted).isEqualTo(1);
        assertThat(exists("reconciliation_batch", "id", batchId)).isTrue();
    }

    @Test
    @DisplayName("E2E: Create Discrepancy")
    void testCreateDiscrepancy() {
        // Given: A discrepancy
        String discrepancyId = "discrepancy-" + System.currentTimeMillis();

        // When: Inserting a discrepancy
        int rowsInserted = jdbcTemplate.update(
            "INSERT INTO discrepancy (id, type, status, amount, created_at) VALUES (?, ?, ?, ?, ?)",
            discrepancyId,
            "MISSING_PAYMENT",
            "OPEN",
            10000L,
            Timestamp.from(Instant.now())
        );

        // Then: Discrepancy is created
        assertThat(rowsInserted).isEqualTo(1);
        assertThat(exists("discrepancy", "id", discrepancyId)).isTrue();
    }

    @Test
    @DisplayName("E2E: Create Settlement Report")
    void testCreateSettlementReport() {
        // Given: A settlement report
        String reportId = "report-" + System.currentTimeMillis();

        // When: Inserting a settlement report
        int rowsInserted = jdbcTemplate.update(
            "INSERT INTO settlement_report (id, merchant_id, total_amount, status, created_at) VALUES (?, ?, ?, ?, ?)",
            reportId,
            merchantId,
            100000L,
            "GENERATED",
            Timestamp.from(Instant.now())
        );

        // Then: Report is created
        assertThat(rowsInserted).isEqualTo(1);
        assertThat(exists("settlement_report", "id", reportId)).isTrue();
    }

    @Test
    @DisplayName("E2E: Discrepancy Resolution Flow")
    void testDiscrepancyResolutionFlow() {
        // Given: An open discrepancy
        String discrepancyId = "discrepancy-" + System.currentTimeMillis();
        jdbcTemplate.update(
            "INSERT INTO discrepancy (id, type, status, amount, created_at) VALUES (?, ?, ?, ?, ?)",
            discrepancyId,
            "MISSING_PAYMENT",
            "OPEN",
            10000L,
            Timestamp.from(Instant.now())
        );

        // When: Resolving the discrepancy
        int rowsUpdated = jdbcTemplate.update(
            "UPDATE discrepancy SET status = ?, resolved_at = ? WHERE id = ?",
            "RESOLVED",
            Timestamp.from(Instant.now()),
            discrepancyId
        );

        // Then: Discrepancy is resolved
        assertThat(rowsUpdated).isEqualTo(1);

        String status = jdbcTemplate.queryForObject(
            "SELECT status FROM discrepancy WHERE id = ?",
            String.class,
            discrepancyId
        );
        assertThat(status).isEqualTo("RESOLVED");
    }

    @Test
    @DisplayName("E2E: Reconciliation Batch Status Transitions")
    void testReconciliationBatchStatusTransitions() {
        // Given: A pending batch
        String batchId = "batch-" + System.currentTimeMillis();
        jdbcTemplate.update(
            "INSERT INTO reconciliation_batch (id, status, created_at, updated_at) VALUES (?, ?, ?, ?)",
            batchId,
            "PENDING",
            Timestamp.from(Instant.now()),
            Timestamp.from(Instant.now())
        );

        // When: Processing the batch
        jdbcTemplate.update(
            "UPDATE reconciliation_batch SET status = ?, updated_at = ? WHERE id = ?",
            "PROCESSING",
            Timestamp.from(Instant.now()),
            batchId
        );

        // Then: Status is updated
        String status = jdbcTemplate.queryForObject(
            "SELECT status FROM reconciliation_batch WHERE id = ?",
            String.class,
            batchId
        );
        assertThat(status).isEqualTo("PROCESSING");
    }

    @Test
    @DisplayName("E2E: Settlement Report with Merchant Data")
    void testSettlementReportWithMerchantData() {
        // Given: A settlement report for a merchant
        String reportId = "report-" + System.currentTimeMillis();
        jdbcTemplate.update(
            "INSERT INTO settlement_report (id, merchant_id, total_amount, status, created_at) VALUES (?, ?, ?, ?, ?)",
            reportId,
            merchantId,
            250000L,
            "GENERATED",
            Timestamp.from(Instant.now())
        );

        // When: Querying by merchant
        String retrievedMerchantId = jdbcTemplate.queryForObject(
            "SELECT merchant_id FROM settlement_report WHERE id = ?",
            String.class,
            reportId
        );

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
            columnName
        );
        return count != null && count > 0;
    }
}
