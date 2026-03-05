package com.payment.gateway.application.customer.service;

import com.payment.gateway.application.customer.dto.CustomerResponse;
import com.payment.gateway.application.customer.port.out.CustomerCommandPort;
import com.payment.gateway.commons.exception.BusinessException;
import com.payment.gateway.domain.customer.model.CardDetails;
import com.payment.gateway.domain.customer.model.Customer;
import com.payment.gateway.domain.customer.model.PaymentMethod;
import com.payment.gateway.domain.customer.model.PaymentMethodType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Currency;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@DisplayName("Remove Payment Method Service Tests")
@ExtendWith(MockitoExtension.class)
class RemovePaymentMethodServiceTest {

    @Mock
    private CustomerCommandPort customerCommandPort;

    private RemovePaymentMethodService removePaymentMethodService;

    @BeforeEach
    void setUp() {
        removePaymentMethodService = new RemovePaymentMethodService(customerCommandPort);
    }

    @Nested
    @DisplayName("Successful Remove Payment Method")
    class SuccessfulRemoveTests {

        @Test
        @DisplayName("Should remove payment method successfully")
        void shouldRemovePaymentMethodSuccessfully() {
            // Given
            String customerId = "customer-123";
            String paymentMethodId = "pm_123";
            Customer customer = createCustomerWithPaymentMethod(customerId, paymentMethodId);

            given(customerCommandPort.findById(customerId)).willReturn(Optional.of(customer));
            given(customerCommandPort.saveCustomer(any())).willAnswer(invocation -> invocation.getArgument(0));

            // When
            CustomerResponse response = removePaymentMethodService.removePaymentMethod(customerId, paymentMethodId);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(customerId);

            then(customerCommandPort).should().findById(customerId);
            then(customerCommandPort).should().saveCustomer(any());
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should throw exception when customer not found")
        void shouldThrowExceptionWhenCustomerNotFound() {
            // Given
            String customerId = "invalid-customer";
            given(customerCommandPort.findById(customerId)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> removePaymentMethodService.removePaymentMethod(customerId, "pm_123"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Customer not found");
        }

        @Test
        @DisplayName("Should throw exception when payment method not found")
        void shouldThrowExceptionWhenPaymentMethodNotFound() {
            // Given
            String customerId = "customer-123";
            String invalidPaymentMethodId = "pm_invalid";
            Customer customer = createCustomerWithPaymentMethod(customerId, "pm_123");

            given(customerCommandPort.findById(customerId)).willReturn(Optional.of(customer));

            // When & Then
            assertThatThrownBy(() -> removePaymentMethodService.removePaymentMethod(customerId, invalidPaymentMethodId))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Payment method not found");
        }
    }

    private Customer createCustomerWithPaymentMethod(String customerId, String paymentMethodId) {
        Customer customer = Customer.create("merchant-123", "customer@example.com", "Test Customer");
        setId(customer, customerId);
        
        // Add a payment method to the customer
        CardDetails cardDetails = CardDetails.create("1234", "411111", "VISA", 12, 2025, "Test Customer");
        PaymentMethod paymentMethod = PaymentMethod.createCard(customerId, cardDetails, "tok_xyz");
        setId(paymentMethod, paymentMethodId);
        customer.addPaymentMethod(paymentMethod);
        
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
