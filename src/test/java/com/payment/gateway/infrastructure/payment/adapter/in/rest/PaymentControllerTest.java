package com.payment.gateway.infrastructure.payment.adapter.in.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payment.gateway.application.payment.dto.PaymentResponse;
import com.payment.gateway.application.payment.port.in.CancelPaymentUseCase;
import com.payment.gateway.application.payment.port.in.CapturePaymentUseCase;
import com.payment.gateway.application.payment.port.in.GetPaymentUseCase;
import com.payment.gateway.application.payment.port.in.ProcessPaymentUseCase;
import com.payment.gateway.infrastructure.config.SecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("PaymentController Tests")
@WebMvcTest(PaymentController.class)
@Import(SecurityConfig.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProcessPaymentUseCase processPaymentUseCase;

    @MockBean
    private CapturePaymentUseCase capturePaymentUseCase;

    @MockBean
    private CancelPaymentUseCase cancelPaymentUseCase;

    @MockBean
    private GetPaymentUseCase getPaymentUseCase;

    @MockBean
    private PaymentRestMapper paymentRestMapper;

    @Test
    @DisplayName("POST /api/v1/payments - should process payment")
    @WithMockUser
    void shouldProcessPayment() throws Exception {
        var appResponse = PaymentResponse.builder()
                .id("pay-123")
                .merchantId("merchant-1")
                .amount(10000L)
                .currency("USD")
                .status("PENDING")
                .idempotencyKey("idem-1")
                .description("Test")
                .createdAt(Instant.now())
                .build();

        given(processPaymentUseCase.processPayment(any())).willReturn(appResponse);
        given(paymentRestMapper.toResponse(any(com.payment.gateway.application.payment.dto.PaymentResponse.class)))
                .willReturn(com.payment.gateway.infrastructure.payment.adapter.in.rest.PaymentResponse.builder()
                        .id("pay-123")
                        .merchantId("merchant-1")
                        .amountInCents(10000L)
                        .currency("USD")
                        .status("PENDING")
                        .build());

        String requestBody = """
                {
                    "merchantId": "merchant-1",
                    "amountInCents": 10000,
                    "currency": "USD",
                    "idempotencyKey": "idem-1",
                    "description": "Test"
                }
                """;

        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Idempotency-Key", "idem-1")
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("pay-123"));
    }

    @Test
    @DisplayName("GET /api/v1/payments/{id} - should get payment")
    @WithMockUser
    void shouldGetPayment() throws Exception {
        var appResponse = PaymentResponse.builder()
                .id("pay-123")
                .merchantId("merchant-1")
                .amount(10000L)
                .currency("USD")
                .status("AUTHORIZED")
                .idempotencyKey("idem-1")
                .description("Test")
                .createdAt(Instant.now())
                .build();

        given(getPaymentUseCase.getPaymentById(eq("pay-123"), any())).willReturn(appResponse);
        given(paymentRestMapper.toResponse(any(com.payment.gateway.application.payment.dto.PaymentResponse.class)))
                .willReturn(com.payment.gateway.infrastructure.payment.adapter.in.rest.PaymentResponse.builder()
                        .id("pay-123")
                        .merchantId("merchant-1")
                        .amountInCents(10000L)
                        .currency("USD")
                        .status("AUTHORIZED")
                        .build());

        mockMvc.perform(get("/api/v1/payments/pay-123")
                        .param("merchantId", "merchant-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("pay-123"));
    }

    @Test
    @DisplayName("POST /api/v1/payments/{id}/capture - should capture payment")
    @WithMockUser
    void shouldCapturePayment() throws Exception {
        var appResponse = PaymentResponse.builder()
                .id("pay-123")
                .merchantId("merchant-1")
                .amount(10000L)
                .currency("USD")
                .status("CAPTURED")
                .createdAt(Instant.now())
                .build();

        given(capturePaymentUseCase.capturePayment("pay-123", "merchant-1")).willReturn(appResponse);

        mockMvc.perform(post("/api/v1/payments/pay-123/capture")
                        .param("merchantId", "merchant-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Payment captured successfully"));
    }

    @Test
    @DisplayName("POST /api/v1/payments/{id}/cancel - should cancel payment")
    @WithMockUser
    void shouldCancelPayment() throws Exception {
        var appResponse = PaymentResponse.builder()
                .id("pay-123")
                .merchantId("merchant-1")
                .amount(10000L)
                .currency("USD")
                .status("CANCELLED")
                .createdAt(Instant.now())
                .build();

        given(cancelPaymentUseCase.cancelPayment("pay-123", "merchant-1")).willReturn(appResponse);

        mockMvc.perform(post("/api/v1/payments/pay-123/cancel")
                        .param("merchantId", "merchant-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Payment cancelled successfully"));
    }

    @Test
    @DisplayName("POST /api/v1/payments - should fail validation with missing fields")
    @WithMockUser
    void shouldFailValidationWithMissingFields() throws Exception {
        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Idempotency-Key", "test-idem-key")
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
