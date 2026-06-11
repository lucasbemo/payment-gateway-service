package com.payment.gateway.infrastructure.merchant.adapter.in.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payment.gateway.application.merchant.dto.MerchantResponse;
import com.payment.gateway.application.merchant.port.in.ActivateMerchantUseCase;
import com.payment.gateway.application.merchant.port.in.GetMerchantUseCase;
import com.payment.gateway.application.merchant.port.in.RegisterMerchantUseCase;
import com.payment.gateway.application.merchant.port.in.SuspendMerchantUseCase;
import com.payment.gateway.application.merchant.port.in.UpdateMerchantUseCase;
import com.payment.gateway.infrastructure.config.SecurityConfig;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@DisplayName("MerchantController Tests")
@WebMvcTest(MerchantController.class)
@Import(SecurityConfig.class)
class MerchantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RegisterMerchantUseCase registerMerchantUseCase;

    @MockitoBean
    private GetMerchantUseCase getMerchantUseCase;

    @MockitoBean
    private UpdateMerchantUseCase updateMerchantUseCase;

    @MockitoBean
    private SuspendMerchantUseCase suspendMerchantUseCase;

    @MockitoBean
    private ActivateMerchantUseCase activateMerchantUseCase;

    @Test
    @DisplayName("POST /api/v1/merchants - should register merchant")
    @WithMockUser
    void shouldRegisterMerchant() throws Exception {
        var response = MerchantResponse.builder()
                .id("m-123")
                .name("Test Merchant")
                .email("test@merchant.com")
                .status("ACTIVE")
                .createdAt(Instant.now())
                .build();

        given(registerMerchantUseCase.registerMerchant(any())).willReturn(response);

        mockMvc.perform(post("/api/v1/merchants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Test Merchant\",\"email\":\"test@merchant.com\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("m-123"));
    }

    @Test
    @DisplayName("GET /api/v1/merchants/{id} - should get merchant")
    @WithMockUser
    void shouldGetMerchant() throws Exception {
        var response = MerchantResponse.builder()
                .id("m-123")
                .name("Test Merchant")
                .email("test@merchant.com")
                .status("ACTIVE")
                .createdAt(Instant.now())
                .build();

        given(getMerchantUseCase.getMerchantById("m-123")).willReturn(response);

        mockMvc.perform(get("/api/v1/merchants/m-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("m-123"))
                .andExpect(jsonPath("$.data.name").value("Test Merchant"));
    }

    @Test
    @DisplayName("PUT /api/v1/merchants/{id} - should update merchant")
    @WithMockUser
    void shouldUpdateMerchant() throws Exception {
        var response = MerchantResponse.builder()
                .id("m-123")
                .name("Updated Merchant")
                .email("updated@merchant.com")
                .status("ACTIVE")
                .createdAt(Instant.now())
                .build();

        given(updateMerchantUseCase.updateMerchant(any(), any(), any(), any())).willReturn(response);

        mockMvc.perform(put("/api/v1/merchants/m-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Updated Merchant\",\"email\":\"updated@merchant.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Updated Merchant"));
    }

    @Test
    @DisplayName("POST /api/v1/merchants/{id}/suspend - should suspend merchant")
    @WithMockUser
    void shouldSuspendMerchant() throws Exception {
        var response = MerchantResponse.builder()
                .id("m-123")
                .name("Test Merchant")
                .email("test@merchant.com")
                .status("SUSPENDED")
                .createdAt(Instant.now())
                .build();

        given(suspendMerchantUseCase.suspendMerchant("m-123")).willReturn(response);

        mockMvc.perform(post("/api/v1/merchants/m-123/suspend"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SUSPENDED"));
    }

    @Test
    @DisplayName("POST /api/v1/merchants/{id}/activate - should activate merchant")
    @WithMockUser
    void shouldActivateMerchant() throws Exception {
        var response = MerchantResponse.builder()
                .id("m-123")
                .name("Test Merchant")
                .email("test@merchant.com")
                .status("ACTIVE")
                .createdAt(Instant.now())
                .build();

        given(activateMerchantUseCase.activateMerchant("m-123")).willReturn(response);

        mockMvc.perform(post("/api/v1/merchants/m-123/activate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("POST /api/v1/merchants - should fail validation with missing fields")
    @WithMockUser
    void shouldFailValidation() throws Exception {
        mockMvc.perform(post("/api/v1/merchants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
