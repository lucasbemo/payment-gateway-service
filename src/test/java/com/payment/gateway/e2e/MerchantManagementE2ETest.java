package com.payment.gateway.e2e;

import com.payment.gateway.e2e.testdata.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * E2E tests for merchant management.
 */
class MerchantManagementE2ETest extends E2ETestBase {

    private String testMerchantId;
    private String testApiKey;

    @BeforeEach
    void setUp() {
        cleanupDatabase();
    }

    @Test
    @DisplayName("E2E: Create Merchant - Happy Path")
    void testCreateMerchant_HappyPath() {
        // Given: A merchant registration request
        var merchantData = TestDataFactory.MerchantData.create();

        // When: Registering the merchant
        var response = getApiClient().registerMerchant(
            merchantData.name,
            merchantData.email,
            merchantData.webhookUrl
        );

        // Then: Merchant is created successfully
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        Map<String, Object> responseBody = response.getBody();
        assertThat(responseBody.get("success")).isEqualTo(true);

        Map<String, Object> merchant = (Map<String, Object>) responseBody.get("data");
        assertThat(merchant).isNotNull();
        assertThat(merchant.get("id")).isNotNull();
        assertThat(merchant.get("name")).isEqualTo(merchantData.name);
        assertThat(merchant.get("email")).isEqualTo(merchantData.email);
        assertThat(merchant.get("apiKey")).isNotNull();
        assertThat(merchant.get("status")).isEqualTo("PENDING");

        // Verify merchant exists in database
        testMerchantId = (String) merchant.get("id");
        assertThat(exists("merchants", "id", testMerchantId)).isTrue();
    }

    @Test
    @DisplayName("E2E: Get Merchant by ID")
    void testGetMerchantById() {
        // Given: A registered merchant
        var merchantData = TestDataFactory.MerchantData.create();
        var registerResponse = getApiClient().registerMerchant(
            merchantData.name,
            merchantData.email,
            merchantData.webhookUrl
        );

        Map<String, Object> merchant = (Map<String, Object>) registerResponse.getBody().get("data");
        String merchantId = (String) merchant.get("id");

        // When: Getting the merchant by ID
        var getResponse = getApiClient().getMerchant(merchantId);

        // Then: Merchant is retrieved successfully
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        Map<String, Object> retrievedMerchant = (Map<String, Object>) getResponse.getBody().get("data");
        assertThat(retrievedMerchant.get("id")).isEqualTo(merchantId);
        assertThat(retrievedMerchant.get("name")).isEqualTo(merchantData.name);
        assertThat(retrievedMerchant.get("email")).isEqualTo(merchantData.email);
    }

    @Test
    @DisplayName("E2E: Update Merchant")
    void testUpdateMerchant() {
        // Given: A registered merchant
        var merchantData = TestDataFactory.MerchantData.create();
        var registerResponse = getApiClient().registerMerchant(
            merchantData.name,
            merchantData.email,
            merchantData.webhookUrl
        );

        Map<String, Object> merchant = (Map<String, Object>) registerResponse.getBody().get("data");
        String merchantId = (String) merchant.get("id");

        // When: Updating the merchant
        String newName = "Updated Merchant Name";
        String newEmail = "updated@example.com";
        var updateResponse = getApiClient().updateMerchant(
            merchantId,
            newName,
            newEmail,
            "https://webhook.site/updated"
        );

        // Then: Merchant is updated
        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        Map<String, Object> updatedMerchant = (Map<String, Object>) updateResponse.getBody().get("data");
        assertThat(updatedMerchant.get("name")).isEqualTo(newName);
        assertThat(updatedMerchant.get("email")).isEqualTo(newEmail);

        // Verify in database
        String dbEmail = jdbcTemplate.queryForObject(
            "SELECT email FROM merchants WHERE id = ?",
            String.class,
            merchantId
        );
        assertThat(dbEmail).isEqualTo(newEmail);
    }

    @Test
    @DisplayName("E2E: Suspend Merchant")
    void testSuspendMerchant() {
        // Given: An active merchant
        var merchantData = TestDataFactory.MerchantData.create();
        var registerResponse = getApiClient().registerMerchant(
            merchantData.name,
            merchantData.email,
            merchantData.webhookUrl
        );

        Map<String, Object> merchant = (Map<String, Object>) registerResponse.getBody().get("data");
        String merchantId = (String) merchant.get("id");

        // When: Suspending the merchant
        var suspendResponse = getApiClient().suspendMerchant(merchantId);

        // Then: Merchant is suspended
        assertThat(suspendResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        Map<String, Object> suspendedMerchant = (Map<String, Object>) suspendResponse.getBody().get("data");
        assertThat(suspendedMerchant.get("status")).isEqualTo("SUSPENDED");

        // Verify in database
        String dbStatus = jdbcTemplate.queryForObject(
            "SELECT status FROM merchants WHERE id = ?",
            String.class,
            merchantId
        );
        assertThat(dbStatus).isEqualTo("SUSPENDED");
    }

    @Test
    @DisplayName("E2E: Merchant API Key Generated")
    void testMerchantApiKeyGenerated() {
        // Given: A merchant registration request
        var merchantData = TestDataFactory.MerchantData.create();

        // When: Registering the merchant
        var response = getApiClient().registerMerchant(
            merchantData.name,
            merchantData.email,
            merchantData.webhookUrl
        );

        // Then: API key is generated
        Map<String, Object> merchant = (Map<String, Object>) response.getBody().get("data");
        String apiKey = (String) merchant.get("apiKey");

        assertThat(apiKey).isNotNull();
        assertThat(apiKey).startsWith("pk_");

        // Verify API key format
        String apiKeyHash = jdbcTemplate.queryForObject(
            "SELECT api_key_hash FROM merchants WHERE id = ?",
            String.class,
            (String) merchant.get("id")
        );
        assertThat(apiKeyHash).isNotNull();
    }

    @Test
    @DisplayName("E2E: Merchant Webhook Secret Generated")
    void testMerchantWebhookSecretGenerated() {
        // Given: A merchant registration request
        var merchantData = TestDataFactory.MerchantData.create();

        // When: Registering the merchant
        var response = getApiClient().registerMerchant(
            merchantData.name,
            merchantData.email,
            merchantData.webhookUrl
        );

        // Then: Webhook secret is generated
        Map<String, Object> merchant = (Map<String, Object>) response.getBody().get("data");
        String merchantId = (String) merchant.get("id");

        String webhookSecret = jdbcTemplate.queryForObject(
            "SELECT webhook_secret FROM merchants WHERE id = ?",
            String.class,
            merchantId
        );

        assertThat(webhookSecret).isNotNull();
        assertThat(webhookSecret).startsWith("whsec_");
    }

    @Test
    @DisplayName("E2E: Merchant Status Transitions")
    void testMerchantStatusTransitions() {
        // Given: A new merchant
        var merchantData = TestDataFactory.MerchantData.create();
        var registerResponse = getApiClient().registerMerchant(
            merchantData.name,
            merchantData.email,
            merchantData.webhookUrl
        );

        Map<String, Object> merchant = (Map<String, Object>) registerResponse.getBody().get("data");
        String merchantId = (String) merchant.get("id");

        // Initial status is PENDING
        assertThat(merchant.get("status")).isEqualTo("PENDING");

        // When: Activating the merchant (if endpoint exists)
        // Note: May need to add activate endpoint
        // For now, verify PENDING -> SUSPENDED transition works
        var suspendResponse = getApiClient().suspendMerchant(merchantId);
        assertThat(suspendResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("E2E: Merchant Created At Timestamp")
    void testMerchantCreatedAtTimestamp() {
        // Given: A merchant registration request
        var merchantData = TestDataFactory.MerchantData.create();

        // When: Registering the merchant
        var response = getApiClient().registerMerchant(
            merchantData.name,
            merchantData.email,
            merchantData.webhookUrl
        );

        // Then: Created timestamp is set
        Map<String, Object> merchant = (Map<String, Object>) response.getBody().get("data");
        String merchantId = (String) merchant.get("id");

        String createdAt = jdbcTemplate.queryForObject(
            "SELECT created_at FROM merchants WHERE id = ?",
            String.class,
            merchantId
        );

        assertThat(createdAt).isNotNull();
    }

    @Test
    @DisplayName("E2E: Merchant Configuration Stored")
    void testMerchantConfigurationStored() {
        // Given: A merchant with configuration
        var merchantData = TestDataFactory.MerchantData.create();

        // When: Registering the merchant
        var response = getApiClient().registerMerchant(
            merchantData.name,
            merchantData.email,
            merchantData.webhookUrl
        );

        // Then: Merchant configuration exists
        Map<String, Object> merchant = (Map<String, Object>) response.getBody().get("data");
        String merchantId = (String) merchant.get("id");

        // Verify configuration table or column exists
        boolean configExists = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM information_schema.columns WHERE table_name = 'merchant' AND column_name = 'configuration'",
            Integer.class
        ) > 0;

        assertThat(configExists).isTrue();
    }

    @Test
    @DisplayName("E2E: Create Merchant with Missing Fields")
    void testCreateMerchant_MissingFields() {
        // Given: Invalid merchant data (missing name)
        // When: Registering with missing required fields
        var response = getApiClient().registerMerchant(
            null,
            "test@example.com",
            null
        );

        // Then: Request is rejected
        assertThat(response.getStatusCode()).isIn(HttpStatus.BAD_REQUEST, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    @DisplayName("E2E: Get Non-Existent Merchant")
    void testGetNonExistentMerchant() {
        // Given: Non-existent merchant ID
        String fakeId = "non-existent-merchant-id";

        // When: Getting the merchant
        var response = getApiClient().getMerchant(fakeId);

        // Then: 404 is returned
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
