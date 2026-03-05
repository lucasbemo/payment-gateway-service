package com.payment.gateway.application.customer.service;

import com.payment.gateway.application.customer.dto.AddPaymentMethodCommand;
import com.payment.gateway.application.customer.dto.CustomerResponse;
import com.payment.gateway.application.customer.port.out.CustomerCommandPort;
import com.payment.gateway.application.customer.port.out.TokenizationServicePort;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@DisplayName("Add Payment Method Service Tests")
@ExtendWith(MockitoExtension.class)
class AddPaymentMethodServiceTest {

    @Mock
    private CustomerCommandPort customerCommandPort;

    @Mock
    private TokenizationServicePort tokenizationServicePort;

    private AddPaymentMethodService addPaymentMethodService;

    @BeforeEach
    void setUp() {
        addPaymentMethodService = new AddPaymentMethodService(customerCommandPort, tokenizationServicePort);
    }

    @Nested
    @DisplayName("Successful Add Payment Method")
    class SuccessfulAddTests {

        @Test
        @DisplayName("Should add payment method successfully")
        void shouldAddPaymentMethodSuccessfully() {
            // Given
            String customerId = "customer-123";
            String merchantId = "merchant-123";
            Customer customer = createCustomer(customerId, merchantId);
            AddPaymentMethodCommand command = createAddPaymentMethodCommand(customerId, merchantId);

            given(customerCommandPort.findById(customerId)).willReturn(Optional.of(customer));
            given(tokenizationServicePort.tokenize(any(), any(), any(), any())).willReturn("tok_card_xyz");
            given(customerCommandPort.saveCustomer(any())).willAnswer(invocation -> invocation.getArgument(0));

            // When
            CustomerResponse response = addPaymentMethodService.addPaymentMethod(command);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(customerId);

            then(customerCommandPort).should().findById(customerId);
            then(tokenizationServicePort).should().tokenize(any(), any(), any(), any());
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
            AddPaymentMethodCommand command = createAddPaymentMethodCommand(customerId, "merchant-123");
            given(customerCommandPort.findById(customerId)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> addPaymentMethodService.addPaymentMethod(command))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Customer not found");
        }

        @Test
        @DisplayName("Should throw exception when merchant does not own customer")
        void shouldThrowExceptionWhenMerchantDoesNotOwnCustomer() {
            // Given
            String customerId = "customer-123";
            String customerMerchantId = "merchant-123";
            String requestMerchantId = "merchant-456";
            Customer customer = createCustomer(customerId, customerMerchantId);
            AddPaymentMethodCommand command = createAddPaymentMethodCommand(customerId, requestMerchantId);

            given(customerCommandPort.findById(customerId)).willReturn(Optional.of(customer));

            // When & Then
            assertThatThrownBy(() -> addPaymentMethodService.addPaymentMethod(command))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Customer does not belong to merchant");
        }
    }

    private Customer createCustomer(String id, String merchantId) {
        Customer customer = Customer.create(merchantId, "customer@example.com", "Test Customer");
        setId(customer, id);
        return customer;
    }

    private AddPaymentMethodCommand createAddPaymentMethodCommand(String customerId, String merchantId) {
        return AddPaymentMethodCommand.builder()
                .customerId(customerId)
                .merchantId(merchantId)
                .cardNumber("4111111111111111")
                .cardExpiryMonth("12")
                .cardExpiryYear("2027")
                .cardCvv("123")
                .cardholderName("Test Customer")
                .isDefault(true)
                .build();
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
