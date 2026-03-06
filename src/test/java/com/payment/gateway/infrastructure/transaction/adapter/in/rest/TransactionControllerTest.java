package com.payment.gateway.infrastructure.transaction.adapter.in.rest;

import com.payment.gateway.application.transaction.dto.TransactionResponse;
import com.payment.gateway.application.transaction.port.in.CaptureTransactionUseCase;
import com.payment.gateway.application.transaction.port.in.GetTransactionUseCase;
import com.payment.gateway.application.transaction.port.in.VoidTransactionUseCase;
import com.payment.gateway.infrastructure.config.SecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("TransactionController Tests")
@WebMvcTest(TransactionController.class)
@Import(SecurityConfig.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GetTransactionUseCase getTransactionUseCase;

    @MockBean
    private CaptureTransactionUseCase captureTransactionUseCase;

    @MockBean
    private VoidTransactionUseCase voidTransactionUseCase;

    @Test
    @DisplayName("GET /api/v1/transactions/{id} - should get transaction")
    void shouldGetTransaction() throws Exception {
        var response = TransactionResponse.builder()
                .id("txn-123")
                .paymentId("pay-123")
                .merchantId("m-1")
                .type("PAYMENT")
                .amount(10000L)
                .currency("USD")
                .status("AUTHORIZED")
                .createdAt(Instant.now())
                .build();

        given(getTransactionUseCase.getTransactionById("txn-123", "m-1")).willReturn(response);

        mockMvc.perform(get("/api/v1/transactions/txn-123").param("merchantId", "m-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("txn-123"))
                .andExpect(jsonPath("$.data.status").value("AUTHORIZED"));
    }

    @Test
    @DisplayName("POST /api/v1/transactions/{id}/capture - should capture transaction")
    void shouldCaptureTransaction() throws Exception {
        var response = TransactionResponse.builder()
                .id("txn-123")
                .paymentId("pay-123")
                .merchantId("m-1")
                .type("CAPTURE")
                .amount(10000L)
                .currency("USD")
                .status("CAPTURED")
                .createdAt(Instant.now())
                .build();

        given(captureTransactionUseCase.captureTransaction("txn-123", "m-1")).willReturn(response);

        mockMvc.perform(post("/api/v1/transactions/txn-123/capture").param("merchantId", "m-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CAPTURED"));
    }

    @Test
    @DisplayName("POST /api/v1/transactions/{id}/void - should void transaction")
    void shouldVoidTransaction() throws Exception {
        var response = TransactionResponse.builder()
                .id("txn-123")
                .paymentId("pay-123")
                .merchantId("m-1")
                .type("REVERSAL")
                .amount(10000L)
                .currency("USD")
                .status("REVERSED")
                .createdAt(Instant.now())
                .build();

        given(voidTransactionUseCase.voidTransaction("txn-123", "m-1")).willReturn(response);

        mockMvc.perform(post("/api/v1/transactions/txn-123/void").param("merchantId", "m-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("REVERSED"));
    }
}
