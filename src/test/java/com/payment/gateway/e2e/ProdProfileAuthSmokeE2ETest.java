package com.payment.gateway.e2e;

import static org.assertj.core.api.Assertions.assertThat;

import com.payment.gateway.application.merchant.dto.MerchantResponse;
import com.payment.gateway.infrastructure.merchant.adapter.in.rest.MerchantController;
import com.payment.gateway.test.ContainerConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

/**
 * Smoke test that boots the application under the {@code prod} profile (the same profile
 * application-prod.yml and docker-compose.prod.yml use) with real security enabled, and
 * verifies the API-key authentication contract end-to-end:
 *
 * <ul>
 *   <li>unauthenticated calls to {@code /api/v1/**} are rejected with 401,</li>
 *   <li>a valid X-Api-Key / X-Api-Secret pair is accepted,</li>
 *   <li>actuator health stays public.</li>
 * </ul>
 *
 * Unlike {@link E2ETestBase}, this test does NOT load TestSecurityConfig and does NOT use
 * the {@code e2e} profile, so the production {@code SecurityFilterChain} (API key + JWT
 * filters) is exercised for real. Container infrastructure is provided by
 * {@link ContainerConfig}.
 */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = com.payment.gateway.PaymentGatewayApplication.class)
@ActiveProfiles("prod")
@DisplayName("Prod profile: API-key auth smoke test")
@AutoConfigureTestRestTemplate
class ProdProfileAuthSmokeE2ETest extends ContainerConfig {

    @Autowired
    private TestRestTemplate restTemplate;

    // Invoked directly (not over HTTP) to seed an authenticated principal, bypassing the
    // very security filter the HTTP assertions below exercise.
    @Autowired
    private MerchantController merchantController;

    private String merchantId;
    private String apiKey;
    private String apiSecret;

    @BeforeEach
    void seedActiveMerchant() {
        var request = MerchantController.CreateMerchantRequest.builder()
                .name("Prod Auth Smoke Merchant")
                .email("prod-auth-smoke-" + System.nanoTime() + "@test.com")
                .webhookUrl("https://webhook.test/prod-auth")
                .build();

        MerchantResponse created =
                merchantController.registerMerchant(request).getBody().getData();
        merchantId = created.getId();
        apiKey = created.getApiKey();
        apiSecret = created.getApiSecret();

        // API-key validation requires an ACTIVE merchant (PENDING/SUSPENDED are rejected).
        merchantController.activateMerchant(merchantId);
    }

    @Test
    @DisplayName("rejects unauthenticated /api/v1 requests with 401")
    void unauthenticatedRequestReturns401() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/v1/merchants/" + merchantId, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("accepts a valid API key + secret")
    void validApiKeyIsAccepted() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Api-Key", apiKey);
        headers.set("X-Api-Secret", apiSecret);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/merchants/" + merchantId, HttpMethod.GET, new HttpEntity<>(headers), String.class);

        assertThat(response.getStatusCode()).isNotEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    }

    @Test
    @DisplayName("leaves actuator health public")
    void actuatorHealthIsPublic() {
        ResponseEntity<String> response = restTemplate.getForEntity("/actuator/health", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
