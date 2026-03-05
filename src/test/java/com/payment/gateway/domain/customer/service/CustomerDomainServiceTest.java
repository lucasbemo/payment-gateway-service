package com.payment.gateway.domain.customer.service;

import com.payment.gateway.domain.customer.exception.CustomerNotFoundException;
import com.payment.gateway.domain.customer.exception.DuplicateCustomerException;
import com.payment.gateway.domain.customer.exception.InvalidPaymentMethodException;
import com.payment.gateway.domain.customer.model.CardDetails;
import com.payment.gateway.domain.customer.model.Customer;
import com.payment.gateway.domain.customer.model.PaymentMethod;
import com.payment.gateway.domain.customer.port.CustomerRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomerDomainService Tests")
class CustomerDomainServiceTest {

    @Mock
    private CustomerRepositoryPort repository;

    private CustomerDomainService customerDomainService;

    private final String CUSTOMER_ID = "cust_123";
    private final String MERCHANT_ID = "merch_123";
    private final String EMAIL = "customer@example.com";
    private final String NAME = "John Doe";
    private final String PHONE = "+5511999999999";
    private final String PAYMENT_METHOD_ID = "pm_123";
    private final String TOKEN = "tok_abc123";

    @BeforeEach
    void setUp() {
        customerDomainService = new CustomerDomainService(repository);
    }

    @Nested
    @DisplayName("Create Customer")
    class CreateCustomerTests {

        @Test
        @DisplayName("Should create customer successfully")
        void shouldCreateCustomerSuccessfully() {
            // Given
            Customer customer = Customer.create(MERCHANT_ID, EMAIL, NAME);
            given(repository.existsByEmail(EMAIL)).willReturn(false);
            given(repository.save(any(Customer.class))).willReturn(customer);

            // When
            Customer result = customerDomainService.createCustomer(MERCHANT_ID, EMAIL, NAME);

            // Then
            assertThat(result).isNotNull();
            verify(repository).existsByEmail(EMAIL);
            verify(repository).save(any(Customer.class));
        }

        @Test
        @DisplayName("Should throw exception when email already exists")
        void shouldThrowExceptionWhenEmailAlreadyExists() {
            // Given
            given(repository.existsByEmail(EMAIL)).willReturn(true);

            // When & Then
            assertThatThrownBy(() -> customerDomainService.createCustomer(MERCHANT_ID, EMAIL, NAME))
                .isInstanceOf(DuplicateCustomerException.class)
                .hasMessageContaining(EMAIL);

            verify(repository).existsByEmail(EMAIL);
            verify(repository, never()).save(any(Customer.class));
        }

        @Test
        @DisplayName("Should create customer with external ID successfully")
        void shouldCreateCustomerWithExternalIdSuccessfully() {
            // Given
            String externalId = "ext_123";
            Customer customer = Customer.create(MERCHANT_ID, EMAIL, NAME);
            given(repository.existsByEmail(EMAIL)).willReturn(false);
            given(repository.save(any(Customer.class))).willReturn(customer);

            // When
            Customer result = customerDomainService.createCustomerWithExternalId(MERCHANT_ID, EMAIL, NAME, externalId);

            // Then
            assertThat(result).isNotNull();
            verify(repository).existsByEmail(EMAIL);
            verify(repository).save(any(Customer.class));
        }
    }

    @Nested
    @DisplayName("Get Customer")
    class GetCustomerTests {

        @Test
        @DisplayName("Should return customer when found")
        void shouldReturnCustomerWhenFound() {
            // Given
            Customer customer = Customer.create(MERCHANT_ID, EMAIL, NAME);
            given(repository.findById(CUSTOMER_ID)).willReturn(Optional.of(customer));

            // When
            Customer result = customerDomainService.getCustomerOrThrow(CUSTOMER_ID);

            // Then
            assertThat(result).isEqualTo(customer);
            verify(repository).findById(CUSTOMER_ID);
        }

        @Test
        @DisplayName("Should throw exception when customer not found")
        void shouldThrowExceptionWhenCustomerNotFound() {
            // Given
            given(repository.findById(CUSTOMER_ID)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> customerDomainService.getCustomerOrThrow(CUSTOMER_ID))
                .isInstanceOf(CustomerNotFoundException.class)
                .hasMessageContaining(CUSTOMER_ID);
        }

        @Test
        @DisplayName("Should return optional customer when found")
        void shouldReturnOptionalCustomerWhenFound() {
            // Given
            Customer customer = Customer.create(MERCHANT_ID, EMAIL, NAME);
            given(repository.findById(CUSTOMER_ID)).willReturn(Optional.of(customer));

            // When
            Optional<Customer> result = customerDomainService.getCustomer(CUSTOMER_ID);

            // Then
            assertThat(result).isPresent().contains(customer);
        }

        @Test
        @DisplayName("Should return empty optional when customer not found")
        void shouldReturnEmptyOptionalWhenCustomerNotFound() {
            // Given
            given(repository.findById(CUSTOMER_ID)).willReturn(Optional.empty());

            // When
            Optional<Customer> result = customerDomainService.getCustomer(CUSTOMER_ID);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Update Customer Email")
    class UpdateCustomerEmailTests {

        @Test
        @DisplayName("Should update customer email successfully")
        void shouldUpdateCustomerEmailSuccessfully() {
            // Given
            String newEmail = "newemail@example.com";
            Customer customer = Customer.create(MERCHANT_ID, EMAIL, NAME);
            given(repository.findById(CUSTOMER_ID)).willReturn(Optional.of(customer));
            given(repository.save(any(Customer.class))).willReturn(customer);

            // When
            Customer result = customerDomainService.updateCustomerEmail(CUSTOMER_ID, newEmail);

            // Then
            assertThat(result).isEqualTo(customer);
            verify(repository).findById(CUSTOMER_ID);
            verify(repository).save(customer);
        }
    }

    @Nested
    @DisplayName("Update Customer Phone")
    class UpdateCustomerPhoneTests {

        @Test
        @DisplayName("Should update customer phone successfully")
        void shouldUpdateCustomerPhoneSuccessfully() {
            // Given
            Customer customer = Customer.create(MERCHANT_ID, EMAIL, NAME);
            given(repository.findById(CUSTOMER_ID)).willReturn(Optional.of(customer));
            given(repository.save(any(Customer.class))).willReturn(customer);

            // When
            Customer result = customerDomainService.updateCustomerPhone(CUSTOMER_ID, PHONE);

            // Then
            assertThat(result).isEqualTo(customer);
            verify(repository).findById(CUSTOMER_ID);
            verify(repository).save(customer);
        }
    }

    @Nested
    @DisplayName("Update Customer Name")
    class UpdateCustomerNameTests {

        @Test
        @DisplayName("Should update customer name successfully")
        void shouldUpdateCustomerNameSuccessfully() {
            // Given
            String newName = "Jane Doe";
            Customer customer = Customer.create(MERCHANT_ID, EMAIL, NAME);
            given(repository.findById(CUSTOMER_ID)).willReturn(Optional.of(customer));
            given(repository.save(any(Customer.class))).willReturn(customer);

            // When
            Customer result = customerDomainService.updateCustomerName(CUSTOMER_ID, newName);

            // Then
            assertThat(result).isEqualTo(customer);
            verify(repository).findById(CUSTOMER_ID);
            verify(repository).save(customer);
        }
    }

    @Nested
    @DisplayName("Add Payment Method")
    class AddPaymentMethodTests {

        @Test
        @DisplayName("Should add payment method to customer successfully")
        void shouldAddPaymentMethodToCustomerSuccessfully() {
            // Given
            PaymentMethod paymentMethod = PaymentMethod.createCard(CUSTOMER_ID, createCardDetails(), TOKEN);
            Customer customer = Customer.create(MERCHANT_ID, EMAIL, NAME);
            given(repository.findById(CUSTOMER_ID)).willReturn(Optional.of(customer));
            given(repository.save(any(Customer.class))).willReturn(customer);

            // When
            Customer result = customerDomainService.addPaymentMethod(CUSTOMER_ID, paymentMethod);

            // Then
            assertThat(result).isEqualTo(customer);
            verify(repository).findById(CUSTOMER_ID);
            verify(repository).save(customer);
        }

        @Test
        @DisplayName("Should add card payment method to customer successfully")
        void shouldAddCardPaymentMethodToCustomerSuccessfully() {
            // Given
            CardDetails cardDetails = createCardDetails();
            Customer customer = Customer.create(MERCHANT_ID, EMAIL, NAME);
            given(repository.findById(CUSTOMER_ID)).willReturn(Optional.of(customer));
            given(repository.save(any(Customer.class))).willReturn(customer);

            // When
            Customer result = customerDomainService.addCardPaymentMethod(CUSTOMER_ID, cardDetails, TOKEN);

            // Then
            assertThat(result).isEqualTo(customer);
            verify(repository).findById(CUSTOMER_ID);
            verify(repository).save(customer);
        }
    }

    @Nested
    @DisplayName("Remove Payment Method")
    class RemovePaymentMethodTests {

        @Test
        @DisplayName("Should remove payment method from customer successfully")
        void shouldRemovePaymentMethodFromCustomerSuccessfully() {
            // Given
            Customer customer = Customer.create(MERCHANT_ID, EMAIL, NAME);
            given(repository.findById(CUSTOMER_ID)).willReturn(Optional.of(customer));
            given(repository.save(any(Customer.class))).willReturn(customer);

            // When
            Customer result = customerDomainService.removePaymentMethod(CUSTOMER_ID, PAYMENT_METHOD_ID);

            // Then
            assertThat(result).isEqualTo(customer);
            verify(repository).findById(CUSTOMER_ID);
            verify(repository).save(customer);
        }
    }

    @Nested
    @DisplayName("Set Default Payment Method")
    class SetDefaultPaymentMethodTests {

        @Test
        @DisplayName("Should set default payment method successfully")
        void shouldSetDefaultPaymentMethodSuccessfully() {
            // Given
            Customer customer = Customer.create(MERCHANT_ID, EMAIL, NAME);
            PaymentMethod paymentMethod = PaymentMethod.createCard(CUSTOMER_ID, createCardDetails(), TOKEN);
            customer.addPaymentMethod(paymentMethod);
            given(repository.findById(CUSTOMER_ID)).willReturn(Optional.of(customer));
            given(repository.save(any(Customer.class))).willReturn(customer);

            // When
            Customer result = customerDomainService.setDefaultPaymentMethod(CUSTOMER_ID, paymentMethod.getId());

            // Then
            assertThat(result).isEqualTo(customer);
            verify(repository).findById(CUSTOMER_ID);
            verify(repository).save(customer);
        }
    }

    @Nested
    @DisplayName("Get Payment Method")
    class GetPaymentMethodTests {

        @Test
        @DisplayName("Should return payment method when found")
        void shouldReturnPaymentMethodWhenFound() {
            // Given
            Customer customer = Customer.create(MERCHANT_ID, EMAIL, NAME);
            PaymentMethod paymentMethod = PaymentMethod.createCard(CUSTOMER_ID, createCardDetails(), TOKEN);
            customer.addPaymentMethod(paymentMethod);
            given(repository.findById(CUSTOMER_ID)).willReturn(Optional.of(customer));

            // When
            PaymentMethod result = customerDomainService.getPaymentMethod(CUSTOMER_ID, paymentMethod.getId());

            // Then
            assertThat(result).isEqualTo(paymentMethod);
            verify(repository).findById(CUSTOMER_ID);
        }

        @Test
        @DisplayName("Should throw exception when payment method not found")
        void shouldThrowExceptionWhenPaymentMethodNotFound() {
            // Given
            Customer customer = Customer.create(MERCHANT_ID, EMAIL, NAME);
            given(repository.findById(CUSTOMER_ID)).willReturn(Optional.of(customer));

            // When & Then
            assertThatThrownBy(() -> customerDomainService.getPaymentMethod(CUSTOMER_ID, "nonexistent_pm"))
                .isInstanceOf(InvalidPaymentMethodException.class)
                .hasMessageContaining("nonexistent_pm");

            verify(repository).findById(CUSTOMER_ID);
        }
    }

    @Nested
    @DisplayName("Get Customers By Merchant ID")
    class GetCustomersByMerchantIdTests {

        @Test
        @DisplayName("Should return list of customers by merchant ID")
        void shouldReturnListOfCustomersByMerchantId() {
            // Given
            Customer customer1 = Customer.create(MERCHANT_ID, EMAIL, NAME);
            Customer customer2 = Customer.create(MERCHANT_ID, "other@example.com", "Other Person");
            given(repository.findByMerchantId(MERCHANT_ID)).willReturn(List.of(customer1, customer2));

            // When
            List<Customer> result = customerDomainService.getCustomersByMerchantId(MERCHANT_ID);

            // Then
            assertThat(result).hasSize(2);
            verify(repository).findByMerchantId(MERCHANT_ID);
        }
    }

    @Nested
    @DisplayName("Get Customer By External ID")
    class GetCustomerByExternalIdTests {

        @Test
        @DisplayName("Should return customer when found by external ID")
        void shouldReturnCustomerWhenFoundByExternalId() {
            // Given
            String externalId = "ext_123";
            Customer customer = Customer.create(MERCHANT_ID, EMAIL, NAME);
            customer.updateExternalId(externalId);
            given(repository.findByMerchantIdAndExternalId(MERCHANT_ID, externalId)).willReturn(Optional.of(customer));

            // When
            Optional<Customer> result = customerDomainService.getCustomerByExternalId(MERCHANT_ID, externalId);

            // Then
            assertThat(result).isPresent().contains(customer);
            verify(repository).findByMerchantIdAndExternalId(MERCHANT_ID, externalId);
        }

        @Test
        @DisplayName("Should return empty when customer not found by external ID")
        void shouldReturnEmptyWhenCustomerNotFoundByExternalId() {
            // Given
            String externalId = "ext_123";
            given(repository.findByMerchantIdAndExternalId(MERCHANT_ID, externalId)).willReturn(Optional.empty());

            // When
            Optional<Customer> result = customerDomainService.getCustomerByExternalId(MERCHANT_ID, externalId);

            // Then
            assertThat(result).isEmpty();
            verify(repository).findByMerchantIdAndExternalId(MERCHANT_ID, externalId);
        }
    }

    @Nested
    @DisplayName("Activate Customer")
    class ActivateCustomerTests {

        @Test
        @DisplayName("Should activate customer successfully")
        void shouldActivateCustomerSuccessfully() {
            // Given
            Customer customer = Customer.create(MERCHANT_ID, EMAIL, NAME);
            given(repository.findById(CUSTOMER_ID)).willReturn(Optional.of(customer));
            given(repository.save(any(Customer.class))).willReturn(customer);

            // When
            Customer result = customerDomainService.activateCustomer(CUSTOMER_ID);

            // Then
            assertThat(result).isEqualTo(customer);
            verify(repository).findById(CUSTOMER_ID);
            verify(repository).save(customer);
        }
    }

    @Nested
    @DisplayName("Deactivate Customer")
    class DeactivateCustomerTests {

        @Test
        @DisplayName("Should deactivate customer successfully")
        void shouldDeactivateCustomerSuccessfully() {
            // Given
            Customer customer = Customer.create(MERCHANT_ID, EMAIL, NAME);
            given(repository.findById(CUSTOMER_ID)).willReturn(Optional.of(customer));
            given(repository.save(any(Customer.class))).willReturn(customer);

            // When
            Customer result = customerDomainService.deactivateCustomer(CUSTOMER_ID);

            // Then
            assertThat(result).isEqualTo(customer);
            verify(repository).findById(CUSTOMER_ID);
            verify(repository).save(customer);
        }
    }

    @Nested
    @DisplayName("Suspend Customer")
    class SuspendCustomerTests {

        @Test
        @DisplayName("Should suspend customer successfully")
        void shouldSuspendCustomerSuccessfully() {
            // Given
            Customer customer = Customer.create(MERCHANT_ID, EMAIL, NAME);
            given(repository.findById(CUSTOMER_ID)).willReturn(Optional.of(customer));
            given(repository.save(any(Customer.class))).willReturn(customer);

            // When
            Customer result = customerDomainService.suspendCustomer(CUSTOMER_ID);

            // Then
            assertThat(result).isEqualTo(customer);
            verify(repository).findById(CUSTOMER_ID);
            verify(repository).save(customer);
        }
    }

    private CardDetails createCardDetails() {
        return CardDetails.create(
            "1111",
            "4111",
            "VISA",
            12,
            2027,
            "John Doe"
        );
    }
}
