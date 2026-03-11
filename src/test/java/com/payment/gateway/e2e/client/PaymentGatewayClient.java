package com.payment.gateway.e2e.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.Map;

/**
 * REST client helper for E2E tests.
 * Provides convenient methods for calling the Payment Gateway API.
 */
public class PaymentGatewayClient {

    private final TestRestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private String apiKey;

    public PaymentGatewayClient(TestRestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
    }

    private static final ParameterizedTypeReference<Map<String, Object>> MAP_TYPE_REF =
        new ParameterizedTypeReference<Map<String, Object>>() {};

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    // ==================== MERCHANT ENDPOINTS ====================

    /**
     * Register a new merchant.
     * POST /api/v1/merchants
     */
    public ResponseEntity<Map<String, Object>> registerMerchant(String name, String email, String webhookUrl) {
        Map<String, Object> body = new java.util.HashMap<>();
        body.put("name", name);
        body.put("email", email);
        body.put("webhookUrl", webhookUrl != null ? webhookUrl : "");
        return post("/api/v1/merchants", body, new LinkedMultiValueMap<>());
    }

    /**
     * Get merchant by ID.
     * GET /api/v1/merchants/{id}
     */
    public ResponseEntity<Map<String, Object>> getMerchant(String merchantId) {
        return get("/api/v1/merchants/" + merchantId);
    }

    /**
     * Update merchant.
     * PUT /api/v1/merchants/{id}
     */
    public ResponseEntity<Map<String, Object>> updateMerchant(String merchantId, String name, String email, String webhookUrl) {
        Map<String, Object> body = Map.of(
            "name", name,
            "email", email,
            "webhookUrl", webhookUrl != null ? webhookUrl : ""
        );
        return put("/api/v1/merchants/" + merchantId, body);
    }

    /**
     * Suspend merchant.
     * POST /api/v1/merchants/{id}/suspend
     */
    public ResponseEntity<Map<String, Object>> suspendMerchant(String merchantId) {
        return post("/api/v1/merchants/" + merchantId + "/suspend", Map.of());
    }

    // ==================== CUSTOMER ENDPOINTS ====================

    /**
     * Register a new customer.
     * POST /api/v1/customers
     */
    public ResponseEntity<Map<String, Object>> registerCustomer(String merchantId, String email, String name,
                                                                String phone, String externalId) {
        Map<String, Object> body = Map.of(
            "merchantId", merchantId,
            "email", email,
            "name", name,
            "phone", phone != null ? phone : "",
            "externalId", externalId != null ? externalId : ""
        );
        return post("/api/v1/customers", body);
    }

    /**
     * Get customer by ID.
     * GET /api/v1/customers/{id}?merchantId={merchantId}
     */
    public ResponseEntity<Map<String, Object>> getCustomer(String customerId, String merchantId) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("merchantId", merchantId);
        return get("/api/v1/customers/" + customerId, params);
    }

    /**
     * Add payment method to customer.
     * POST /api/v1/customers/{id}/payment-methods
     */
    public ResponseEntity<Map<String, Object>> addPaymentMethod(String customerId, String merchantId,
                                                                String cardNumber, String expiryMonth,
                                                                String expiryYear, String cvv,
                                                                String cardholderName, Boolean isDefault) {
        Map<String, Object> body = Map.of(
            "merchantId", merchantId,
            "cardNumber", cardNumber,
            "cardExpiryMonth", expiryMonth,
            "cardExpiryYear", expiryYear,
            "cardCvv", cvv,
            "cardholderName", cardholderName != null ? cardholderName : "",
            "isDefault", isDefault != null ? isDefault : false
        );
        return post("/api/v1/customers/" + customerId + "/payment-methods", body);
    }

    /**
     * Remove payment method from customer.
     * DELETE /api/v1/customers/{id}/payment-methods/{pmId}
     */
    public ResponseEntity<Map<String, Object>> removePaymentMethod(String customerId, String paymentMethodId) {
        return delete("/api/v1/customers/" + customerId + "/payment-methods/" + paymentMethodId);
    }

    // ==================== PAYMENT ENDPOINTS ====================

    /**
     * Process a payment.
     * POST /api/v1/payments
     */
    public ResponseEntity<Map<String, Object>> processPayment(String merchantId, Long amountInCents, String currency,
                                                              String idempotencyKey, String description,
                                                              String customerId, List<Map<String, Object>> items) {
        Map<String, Object> body = createPaymentBody(merchantId, amountInCents, currency,
                                                      idempotencyKey, description, customerId, items);
        HttpHeaders headers = createAuthHeaders();
        headers.set("X-Idempotency-Key", idempotencyKey);
        return post("/api/v1/payments", body, headers);
    }

    /**
     * Process a payment with default headers.
     */
    public ResponseEntity<Map<String, Object>> processPayment(String merchantId, Long amountInCents, String currency,
                                                              String idempotencyKey) {
        return processPayment(merchantId, amountInCents, currency, idempotencyKey, "Test payment", null, null);
    }

    /**
     * Get payment by ID.
     * GET /api/v1/payments/{id}?merchantId={merchantId}
     */
    public ResponseEntity<Map<String, Object>> getPayment(String paymentId, String merchantId) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("merchantId", merchantId);
        return get("/api/v1/payments/" + paymentId, params);
    }

    /**
     * Get all payments for a merchant.
     * GET /api/v1/payments?merchantId={merchantId}
     */
    public ResponseEntity<Map<String, Object>> getPayments(String merchantId) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("merchantId", merchantId);
        return get("/api/v1/payments", params);
    }

    /**
     * Capture a payment.
     * POST /api/v1/payments/{id}/capture?merchantId={merchantId}
     */
    public ResponseEntity<Map<String, Object>> capturePayment(String paymentId, String merchantId) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("merchantId", merchantId);
        return post("/api/v1/payments/" + paymentId + "/capture", Map.of(), params);
    }

    /**
     * Cancel a payment.
     * POST /api/v1/payments/{id}/cancel?merchantId={merchantId}
     */
    public ResponseEntity<Map<String, Object>> cancelPayment(String paymentId, String merchantId) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("merchantId", merchantId);
        return post("/api/v1/payments/" + paymentId + "/cancel", Map.of(), params);
    }

    // ==================== REFUND ENDPOINTS ====================

    /**
     * Process a refund.
     * POST /api/v1/refunds
     */
    public ResponseEntity<Map<String, Object>> processRefund(String paymentId, String merchantId,
                                                             Long amountInCents, String idempotencyKey,
                                                             String reason) {
        Map<String, Object> body = Map.of(
            "paymentId", paymentId,
            "merchantId", merchantId,
            "amount", amountInCents,
            "idempotencyKey", idempotencyKey,
            "reason", reason != null ? reason : ""
        );
        return post("/api/v1/refunds", body);
    }

    /**
     * Get refund by ID.
     * GET /api/v1/refunds/{id}?merchantId={merchantId}
     */
    public ResponseEntity<Map<String, Object>> getRefund(String refundId, String merchantId) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("merchantId", merchantId);
        return get("/api/v1/refunds/" + refundId, params);
    }

    /**
     * Cancel a refund.
     * POST /api/v1/refunds/{id}/cancel?merchantId={merchantId}&reason={reason}
     */
    public ResponseEntity<Map<String, Object>> cancelRefund(String refundId, String merchantId, String reason) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("merchantId", merchantId);
        if (reason != null) {
            params.add("reason", reason);
        }
        return post("/api/v1/refunds/" + refundId + "/cancel", Map.of(), params);
    }

    // ==================== TRANSACTION ENDPOINTS ====================

    /**
     * Get transaction by ID.
     * GET /api/v1/transactions/{id}?merchantId={merchantId}
     */
    public ResponseEntity<Map<String, Object>> getTransaction(String transactionId, String merchantId) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("merchantId", merchantId);
        return get("/api/v1/transactions/" + transactionId, params);
    }

    /**
     * Get transactions for a merchant.
     * GET /api/v1/transactions?merchantId={merchantId}
     */
    public ResponseEntity<Map<String, Object>> getTransactions(String merchantId) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("merchantId", merchantId);
        return get("/api/v1/transactions", params);
    }

    // ==================== HELPER METHODS ====================

    private Map<String, Object> createPaymentBody(String merchantId, Long amountInCents, String currency,
                                                   String idempotencyKey, String description,
                                                   String customerId, List<Map<String, Object>> items) {
        Map<String, Object> body = new java.util.HashMap<>();
        body.put("merchantId", merchantId);
        body.put("amountInCents", amountInCents);
        body.put("currency", currency);
        body.put("idempotencyKey", idempotencyKey);
        body.put("description", description != null ? description : "");
        if (customerId != null) {
            body.put("customerId", customerId);
        }
        if (items != null) {
            body.put("items", items);
        }
        return body;
    }

    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (apiKey != null && !apiKey.isEmpty()) {
            headers.set("X-API-Key", apiKey);
        }
        return headers;
    }

    private ResponseEntity<Map<String, Object>> get(String path) {
        return get(path, new LinkedMultiValueMap<>());
    }

    private ResponseEntity<Map<String, Object>> get(String path, MultiValueMap<String, String> params) {
        HttpEntity<Void> entity = new HttpEntity<>(createAuthHeaders());
        return restTemplate.exchange(path + buildQueryString(params), HttpMethod.GET, entity, MAP_TYPE_REF);
    }

    private ResponseEntity<Map<String, Object>> post(String path, Map<String, Object> body) {
        return post(path, body, new LinkedMultiValueMap<>());
    }

    private ResponseEntity<Map<String, Object>> post(String path, Map<String, Object> body,
                                                      MultiValueMap<String, String> params) {
        return post(path, body, createAuthHeaders(), params);
    }

    private ResponseEntity<Map<String, Object>> post(String path, Map<String, Object> body,
                                                      HttpHeaders headers) {
        return post(path, body, headers, new LinkedMultiValueMap<>());
    }

    private ResponseEntity<Map<String, Object>> post(String path, Map<String, Object> body,
                                                      HttpHeaders headers, MultiValueMap<String, String> params) {
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        return restTemplate.exchange(path + buildQueryString(params), HttpMethod.POST, entity, MAP_TYPE_REF);
    }

    private ResponseEntity<Map<String, Object>> put(String path, Map<String, Object> body) {
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, createAuthHeaders());
        return restTemplate.exchange(path, HttpMethod.PUT, entity, MAP_TYPE_REF);
    }

    private ResponseEntity<Map<String, Object>> delete(String path) {
        HttpEntity<Void> entity = new HttpEntity<>(createAuthHeaders());
        return restTemplate.exchange(path, HttpMethod.DELETE, entity, MAP_TYPE_REF);
    }

    private String buildQueryString(MultiValueMap<String, String> params) {
        if (params == null || params.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder("?");
        boolean first = true;
        for (Map.Entry<String, List<String>> entry : params.entrySet()) {
            if (!first) {
                sb.append("&");
            }
            for (String value : entry.getValue()) {
                sb.append(entry.getKey()).append("=").append(value);
            }
            first = false;
        }
        return sb.toString();
    }

    /**
     * Extract data from API response.
     */
    @SuppressWarnings("unchecked")
    public <T> T extractData(ResponseEntity<Map<String, Object>> response, Class<T> clazz) {
        if (response.getBody() == null) {
            return null;
        }
        Object data = response.getBody().get("data");
        if (data == null) {
            return null;
        }
        return objectMapper.convertValue(data, clazz);
    }

    /**
     * Extract message from API response.
     */
    public String extractMessage(ResponseEntity<Map<String, Object>> response) {
        if (response.getBody() == null) {
            return null;
        }
        return (String) response.getBody().get("message");
    }

    /**
     * Extract success flag from API response.
     */
    public Boolean extractSuccess(ResponseEntity<Map<String, Object>> response) {
        if (response.getBody() == null) {
            return null;
        }
        Object success = response.getBody().get("success");
        return success instanceof Boolean ? (Boolean) success : null;
    }

    /**
     * Extract error message from API response.
     */
    public String extractErrorMessage(ResponseEntity<Map<String, Object>> response) {
        if (response.getBody() == null) {
            return null;
        }
        return (String) response.getBody().get("message");
    }
}
