package com.payment.gateway.infrastructure.customer.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.payment.gateway.domain.customer.model.Customer;
import com.payment.gateway.domain.customer.model.PaymentMethod;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CustomerPersistenceAdapterTest {

    @Mock
    private CustomerJpaRepository customerJpaRepository;

    @Mock
    private PaymentMethodJpaRepository paymentMethodJpaRepository;

    private CustomerPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new CustomerPersistenceAdapter(
                customerJpaRepository, paymentMethodJpaRepository, new CustomerMapper(), new PaymentMethodMapper());
    }

    private static PaymentMethod domainPaymentMethod(String id, String customerId) {
        Instant now = Instant.now();
        return PaymentMethod.builder()
                .id(id)
                .customerId(customerId)
                .type(com.payment.gateway.domain.customer.model.PaymentMethodType.CREDIT_CARD)
                .status(com.payment.gateway.domain.customer.model.PaymentMethodStatus.ACTIVE)
                .token("token-" + id)
                .isDefault(false)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    private static PaymentMethodJpaEntity paymentMethodEntity(
            String id, String customerId, PaymentMethodStatus status) {
        Instant now = Instant.now();
        return PaymentMethodJpaEntity.builder()
                .id(id)
                .customerId(customerId)
                .merchantId("merchant-1")
                .type(PaymentMethodType.CREDIT_CARD)
                .token("token-" + id)
                .status(status)
                .isDefault(false)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    @Nested
    @DisplayName("saveCustomer")
    class SaveCustomer {

        @Test
        @DisplayName("should soft-delete payment methods removed from the aggregate instead of deleting rows")
        void shouldSoftDeleteRemovedPaymentMethods() {
            // given: aggregate keeps pm-1 only, DB has pm-1 and pm-2
            Customer customer = Customer.create("merchant-1", "a@b.com", "Customer A");
            customer.addPaymentMethod(domainPaymentMethod("pm-1", customer.getId()));

            when(customerJpaRepository.save(any(CustomerJpaEntity.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            PaymentMethodJpaEntity existingPm1 =
                    paymentMethodEntity("pm-1", customer.getId(), PaymentMethodStatus.ACTIVE);
            PaymentMethodJpaEntity existingPm2 =
                    paymentMethodEntity("pm-2", customer.getId(), PaymentMethodStatus.ACTIVE);
            when(paymentMethodJpaRepository.findByCustomerId(customer.getId()))
                    .thenReturn(List.of(existingPm1, existingPm2));

            // when
            adapter.saveCustomer(customer);

            // then: pm-2 is marked INACTIVE and saved, nothing is hard-deleted
            assertThat(existingPm2.getStatus()).isEqualTo(PaymentMethodStatus.INACTIVE);
            verify(paymentMethodJpaRepository).save(existingPm2);
            verify(paymentMethodJpaRepository, never()).delete(any());
            verify(paymentMethodJpaRepository, never()).deleteById(any());

            // and: the remaining pm-1 is persisted from the aggregate with ACTIVE status
            ArgumentCaptor<PaymentMethodJpaEntity> captor = ArgumentCaptor.forClass(PaymentMethodJpaEntity.class);
            verify(paymentMethodJpaRepository, org.mockito.Mockito.atLeastOnce())
                    .save(captor.capture());
            assertThat(captor.getAllValues()).anySatisfy(saved -> {
                assertThat(saved.getId()).isEqualTo("pm-1");
                assertThat(saved.getStatus()).isEqualTo(PaymentMethodStatus.ACTIVE);
            });
        }

        @Test
        @DisplayName("should not re-save payment methods that are already INACTIVE")
        void shouldNotResaveAlreadyInactivePaymentMethods() {
            Customer customer = Customer.create("merchant-1", "a@b.com", "Customer A");
            // aggregate has no payment methods; DB row is already soft-deleted
            when(customerJpaRepository.save(any(CustomerJpaEntity.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            PaymentMethodJpaEntity inactivePm =
                    paymentMethodEntity("pm-9", customer.getId(), PaymentMethodStatus.INACTIVE);
            when(paymentMethodJpaRepository.findByCustomerId(customer.getId())).thenReturn(List.of(inactivePm));

            adapter.saveCustomer(customer);

            verify(paymentMethodJpaRepository, never()).save(any(PaymentMethodJpaEntity.class));
        }
    }

    @Nested
    @DisplayName("reads")
    class Reads {

        @Test
        @DisplayName("findById should load only non-INACTIVE payment methods into the aggregate")
        void findByIdShouldExcludeInactivePaymentMethods() {
            CustomerJpaEntity entity = CustomerJpaEntity.builder()
                    .id("cust-1")
                    .merchantId("merchant-1")
                    .token("tok")
                    .email("a@b.com")
                    .name("Customer A")
                    .status(CustomerStatus.ACTIVE)
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();
            when(customerJpaRepository.findById("cust-1")).thenReturn(Optional.of(entity));
            when(paymentMethodJpaRepository.findByCustomerIdAndStatusNot("cust-1", PaymentMethodStatus.INACTIVE))
                    .thenReturn(List.of(paymentMethodEntity("pm-1", "cust-1", PaymentMethodStatus.ACTIVE)));

            Optional<Customer> customer = adapter.findById("cust-1");

            assertThat(customer).isPresent();
            assertThat(customer.get().getPaymentMethods())
                    .extracting(PaymentMethod::getId)
                    .containsExactly("pm-1");
            verify(paymentMethodJpaRepository).findByCustomerIdAndStatusNot("cust-1", PaymentMethodStatus.INACTIVE);
        }

        @Test
        @DisplayName("findPaymentMethodById should exclude INACTIVE payment methods")
        void findPaymentMethodByIdShouldExcludeInactive() {
            when(paymentMethodJpaRepository.findByIdAndStatusNot("pm-1", PaymentMethodStatus.INACTIVE))
                    .thenReturn(Optional.empty());

            assertThat(adapter.findPaymentMethodById("pm-1")).isEmpty();

            verify(paymentMethodJpaRepository).findByIdAndStatusNot("pm-1", PaymentMethodStatus.INACTIVE);
        }

        @Test
        @DisplayName("findPaymentMethodByToken should exclude INACTIVE payment methods")
        void findPaymentMethodByTokenShouldExcludeInactive() {
            when(paymentMethodJpaRepository.findByTokenAndStatusNot("tok-1", PaymentMethodStatus.INACTIVE))
                    .thenReturn(Optional.of(paymentMethodEntity("pm-1", "cust-1", PaymentMethodStatus.ACTIVE)));

            Optional<PaymentMethod> result = adapter.findPaymentMethodByToken("tok-1");

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo("pm-1");
            verify(paymentMethodJpaRepository).findByTokenAndStatusNot("tok-1", PaymentMethodStatus.INACTIVE);
        }
    }
}
