package com.payment.gateway.application.customer.service;

import com.payment.gateway.application.customer.dto.CustomerResponse;
import com.payment.gateway.application.customer.dto.RegisterCustomerCommand;
import com.payment.gateway.application.customer.port.out.CustomerCommandPort;
import com.payment.gateway.commons.exception.BusinessException;
import com.payment.gateway.domain.customer.model.Customer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@DisplayName("Register Customer Service Tests")
@ExtendWith(MockitoExtension.class)
class RegisterCustomerServiceTest {

    @Mock
    private CustomerCommandPort customerCommandPort;

    private RegisterCustomerService registerCustomerService;

    @BeforeEach
    void setUp() {
        registerCustomerService = new RegisterCustomerService(customerCommandPort);
    }

    @Nested
    @DisplayName("Successful Customer Registration")
    class SuccessfulRegistrationTests {

        @Test
        @DisplayName("Should register customer successfully")
        void shouldRegisterCustomerSuccessfully() {
            // Given
            String merchantId = "merchant_123";
            String email = "customer@example.com";
            String name = "John Doe";
            String phone = "+1234567890";
            String externalId = "ext_123";
            String customerId = "cus_123";

            RegisterCustomerCommand command = RegisterCustomerCommand.builder()
                    .merchantId(merchantId)
                    .email(email)
                    .name(name)
                    .phone(phone)
                    .externalId(externalId)
                    .build();

            given(customerCommandPort.existsByEmailAndMerchantId(email, merchantId)).willReturn(false);
            given(customerCommandPort.saveCustomer(any(Customer.class))).willAnswer(invocation -> {
                Customer customer = invocation.getArgument(0);
                setId(customer, customerId);
                return customer;
            });

            // When
            CustomerResponse response = registerCustomerService.registerCustomer(command);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(customerId);
            assertThat(response.getEmail()).isEqualTo(email);
            assertThat(response.getName()).isEqualTo(name);

            then(customerCommandPort).should().saveCustomer(any(Customer.class));
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should throw exception when email already exists")
        void shouldThrowExceptionWhenEmailAlreadyExists() {
            // Given
            String merchantId = "merchant_123";
            String email = "customer@example.com";

            RegisterCustomerCommand command = RegisterCustomerCommand.builder()
                    .merchantId(merchantId)
                    .email(email)
                    .build();

            given(customerCommandPort.existsByEmailAndMerchantId(email, merchantId)).willReturn(true);

            // When & Then
            assertThatThrownBy(() -> registerCustomerService.registerCustomer(command))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("already exists");
        }
    }

    private void setId(Object obj, String id) {
        try {
            Field idField = obj.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(obj, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set id field", e);
        }
    }
}
