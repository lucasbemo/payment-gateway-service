package com.payment.gateway.infrastructure.refund.adapter.in.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payment.gateway.application.refund.dto.RefundResponse;
import com.payment.gateway.application.refund.port.in.CancelRefundUseCase;
import com.payment.gateway.application.refund.port.in.GetRefundUseCase;
import com.payment.gateway.application.refund.port.in.ProcessRefundUseCase;
import com.payment.gateway.infrastructure.config.SecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("RefundController Tests")
@WebMvcTest(RefundController.class)
@Import(SecurityConfig.class)
class RefundControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProcessRefundUseCase processRefundUseCase;

    @MockBean
    private GetRefundUseCase getRefundUseCase;

    @MockBean
    private CancelRefundUseCase cancelRefundUseCase;

    @Test
    @DisplayName("POST /api/v1/refunds - should process refund")
    void shouldProcessRefund() throws Exception {
        var response = RefundResponse.builder()
                .id("ref-123")
                .paymentId("pay-123")
                .merchantId("m-1")
                .amount(5000L)
                .currency("USD")
                .status("PENDING")
                .type("PARTIAL")
                .reason("Customer request")
                .createdAt(Instant.now())
                .build();

        given(processRefundUseCase.processRefund(any(), any(), any(), any(), any())).willReturn(response);

        mockMvc.perform(post("/api/v1/refunds")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"paymentId\":\"pay-123\",\"merchantId\":\"m-1\",\"amount\":5000,\"reason\":\"Customer request\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("ref-123"));
    }

    @Test
    @DisplayName("GET /api/v1/refunds/{id} - should get refund")
    void shouldGetRefund() throws Exception {
        var response = RefundResponse.builder()
                .id("ref-123")
                .paymentId("pay-123")
                .merchantId("m-1")
                .amount(5000L)
                .currency("USD")
                .status("COMPLETED")
                .type("PARTIAL")
                .createdAt(Instant.now())
                .build();

        given(getRefundUseCase.getRefundById("ref-123", "m-1")).willReturn(response);

        mockMvc.perform(get("/api/v1/refunds/ref-123").param("merchantId", "m-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("ref-123"))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));
    }

    @Test
    @DisplayName("POST /api/v1/refunds/{id}/cancel - should cancel refund")
    void shouldCancelRefund() throws Exception {
        var response = RefundResponse.builder()
                .id("ref-123")
                .paymentId("pay-123")
                .merchantId("m-1")
                .amount(5000L)
                .currency("USD")
                .status("CANCELLED")
                .type("PARTIAL")
                .createdAt(Instant.now())
                .build();

        given(cancelRefundUseCase.cancelRefund(eq("ref-123"), eq("m-1"), any())).willReturn(response);

        mockMvc.perform(post("/api/v1/refunds/ref-123/cancel")
                        .param("merchantId", "m-1")
                        .param("reason", "Changed mind"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));
    }

    @Test
    @DisplayName("POST /api/v1/refunds - should fail validation with missing fields")
    void shouldFailValidation() throws Exception {
        mockMvc.perform(post("/api/v1/refunds")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
