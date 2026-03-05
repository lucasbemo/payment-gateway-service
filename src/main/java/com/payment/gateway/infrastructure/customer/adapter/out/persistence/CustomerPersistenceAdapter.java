package com.payment.gateway.infrastructure.customer.adapter.out.persistence;

import com.payment.gateway.application.payment.port.out.CustomerQueryPort;
import com.payment.gateway.domain.customer.model.Customer;
import com.payment.gateway.domain.customer.model.PaymentMethod;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Persistence adapter for Customer repository.
 */
@Component
@RequiredArgsConstructor
public class CustomerPersistenceAdapter implements CustomerQueryPort {

    private final CustomerJpaRepository customerJpaRepository;
    private final PaymentMethodJpaRepository paymentMethodJpaRepository;
    private final CustomerMapper customerMapper;
    private final PaymentMethodMapper paymentMethodMapper;

    @Override
    public Optional<Customer> findCustomerById(String id) {
        return customerJpaRepository.findById(id).map(customerMapper::toDomain);
    }

    @Override
    public Optional<Customer> findCustomerByIdAndMerchantId(String customerId, String merchantId) {
        return customerJpaRepository.findByIdAndMerchantId(customerId, merchantId).map(customerMapper::toDomain);
    }

    @Override
    public Optional<PaymentMethod> findPaymentMethodById(String paymentMethodId) {
        return paymentMethodJpaRepository.findById(paymentMethodId).map(paymentMethodMapper::toDomain);
    }

    @Override
    public Optional<PaymentMethod> findPaymentMethodByToken(String token) {
        return paymentMethodJpaRepository.findByToken(token).map(paymentMethodMapper::toDomain);
    }
}
