package com.payment.gateway.application.customer.service;

import com.payment.gateway.application.customer.dto.CustomerResponse;
import com.payment.gateway.application.customer.port.out.CustomerCommandPort;
import com.payment.gateway.commons.exception.BusinessException;
import com.payment.gateway.domain.customer.model.Customer;
import com.payment.gateway.domain.customer.model.CustomerStatus;
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
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@DisplayName("Get Customer Service Tests")
@ExtendWith(MockitoExtension.class)
class GetCustomerServiceTest {

    @Mock
    private CustomerCommandPort customerCommandPort;

    private GetCustomerService getCustomerService;

    @BeforeEach
    void setUp() {
        getCustomerService = new GetCustomerService(customerCommandPort);
    }

    @Nested
    @DisplayName("Get Customer By ID")
    class GetCustomerByIdTests {

        @Test
        @DisplayName("Should get customer successfully by ID")
        void shouldGetCustomerSuccessfullyById() {
            // Given
            String customerId = "customer-123";
            String merchantId = "merchant-123";
            Customer customer = createCustomer(customerId, merchantId);

            given(customerCommandPort.findByIdAndMerchantId(customerId, merchantId)).willReturn(Optional.of(customer));

            // When
            CustomerResponse response = getCustomerService.getCustomerById(customerId, merchantId);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(customerId);
            assertThat(response.getEmail()).isEqualTo("customer@example.com");
            assertThat(response.getName()).isEqualTo("Test Customer");
            assertThat(response.getStatus()).isEqualTo(CustomerStatus.ACTIVE.name());

            then(customerCommandPort).should().findByIdAndMerchantId(customerId, merchantId);
        }

        @Test
        @DisplayName("Should throw exception when customer not found")
        void shouldThrowExceptionWhenCustomerNotFound() {
            // Given
            String customerId = "invalid-customer";
            String merchantId = "merchant-123";
            given(customerCommandPort.findByIdAndMerchantId(customerId, merchantId)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> getCustomerService.getCustomerById(customerId, merchantId))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Customer not found");
        }
    }

    private Customer createCustomer(String id, String merchantId) {
        Customer customer = Customer.create(merchantId, "customer@example.com", "Test Customer");
        setId(customer, id);
        return customer;
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
