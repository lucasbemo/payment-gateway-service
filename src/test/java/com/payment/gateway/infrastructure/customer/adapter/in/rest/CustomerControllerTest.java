package com.payment.gateway.infrastructure.customer.adapter.in.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payment.gateway.application.customer.dto.CustomerResponse;
import com.payment.gateway.application.customer.port.in.AddPaymentMethodUseCase;
import com.payment.gateway.application.customer.port.in.GetCustomerUseCase;
import com.payment.gateway.application.customer.port.in.RegisterCustomerUseCase;
import com.payment.gateway.application.customer.port.in.RemovePaymentMethodUseCase;
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
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("CustomerController Tests")
@WebMvcTest(CustomerController.class)
@Import(SecurityConfig.class)
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RegisterCustomerUseCase registerCustomerUseCase;

    @MockBean
    private GetCustomerUseCase getCustomerUseCase;

    @MockBean
    private AddPaymentMethodUseCase addPaymentMethodUseCase;

    @MockBean
    private RemovePaymentMethodUseCase removePaymentMethodUseCase;

    @Test
    @DisplayName("POST /api/v1/customers - should register customer")
    void shouldRegisterCustomer() throws Exception {
        var response = CustomerResponse.builder()
                .id("c-123")
                .merchantId("m-1")
                .email("customer@test.com")
                .name("John Doe")
                .status("ACTIVE")
                .createdAt(Instant.now())
                .build();

        given(registerCustomerUseCase.registerCustomer(any())).willReturn(response);

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"merchantId\":\"m-1\",\"email\":\"customer@test.com\",\"name\":\"John Doe\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value("c-123"))
                .andExpect(jsonPath("$.data.email").value("customer@test.com"));
    }

    @Test
    @DisplayName("GET /api/v1/customers/{id} - should get customer")
    void shouldGetCustomer() throws Exception {
        var response = CustomerResponse.builder()
                .id("c-123")
                .merchantId("m-1")
                .email("customer@test.com")
                .name("John Doe")
                .status("ACTIVE")
                .createdAt(Instant.now())
                .build();

        given(getCustomerUseCase.getCustomerById("c-123", "m-1")).willReturn(response);

        mockMvc.perform(get("/api/v1/customers/c-123").param("merchantId", "m-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("c-123"));
    }

    @Test
    @DisplayName("POST /api/v1/customers/{id}/payment-methods - should add payment method")
    void shouldAddPaymentMethod() throws Exception {
        var response = CustomerResponse.builder()
                .id("c-123")
                .merchantId("m-1")
                .email("customer@test.com")
                .name("John Doe")
                .status("ACTIVE")
                .createdAt(Instant.now())
                .build();

        given(addPaymentMethodUseCase.addPaymentMethod(any())).willReturn(response);

        String body = """
                {
                    "merchantId": "m-1",
                    "cardNumber": "4111111111111111",
                    "cardExpiryMonth": "12",
                    "cardExpiryYear": "2030",
                    "cardCvv": "123",
                    "cardholderName": "John Doe"
                }
                """;

        mockMvc.perform(post("/api/v1/customers/c-123/payment-methods")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("DELETE /api/v1/customers/{id}/payment-methods/{pmId} - should remove payment method")
    void shouldRemovePaymentMethod() throws Exception {
        var response = CustomerResponse.builder()
                .id("c-123")
                .merchantId("m-1")
                .email("customer@test.com")
                .name("John Doe")
                .status("ACTIVE")
                .createdAt(Instant.now())
                .build();

        given(removePaymentMethodUseCase.removePaymentMethod("c-123", "pm-1")).willReturn(response);

        mockMvc.perform(delete("/api/v1/customers/c-123/payment-methods/pm-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /api/v1/customers - should fail validation with missing fields")
    void shouldFailValidation() throws Exception {
        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
