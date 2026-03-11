package com.payment.gateway.infrastructure.customer.adapter.out.persistence;

import com.payment.gateway.application.customer.port.out.CustomerCommandPort;
import com.payment.gateway.application.payment.port.out.CustomerQueryPort;
import com.payment.gateway.domain.customer.model.Customer;
import com.payment.gateway.domain.customer.model.PaymentMethod;
import com.payment.gateway.domain.customer.port.CustomerRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Persistence adapter for Customer repository.
 * Implements CustomerQueryPort, CustomerCommandPort, and CustomerRepositoryPort.
 */
@Component
@RequiredArgsConstructor
public class CustomerPersistenceAdapter implements CustomerQueryPort, CustomerCommandPort, CustomerRepositoryPort {

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

    @Override
    public Customer saveCustomer(Customer customer) {
        var entity = customerMapper.toEntity(customer);
        var savedEntity = customerJpaRepository.save(entity);

        // Save payment methods associated with the customer
        if (customer.getPaymentMethods() != null) {
            for (PaymentMethod paymentMethod : customer.getPaymentMethods()) {
                var paymentMethodEntity = paymentMethodMapper.toEntity(paymentMethod);
                // Set merchantId from customer if not set
                if (paymentMethodEntity.getMerchantId() == null) {
                    paymentMethodEntity.setMerchantId(savedEntity.getMerchantId());
                }
                paymentMethodJpaRepository.save(paymentMethodEntity);
            }
        }

        return customerMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Customer> findById(String id) {
        return customerJpaRepository.findById(id).map(customerMapper::toDomain);
    }

    @Override
    public Optional<Customer> findByIdAndMerchantId(String id, String merchantId) {
        return customerJpaRepository.findByIdAndMerchantId(id, merchantId).map(customerMapper::toDomain);
    }

    @Override
    public boolean existsByEmailAndMerchantId(String email, String merchantId) {
        return customerJpaRepository.findByEmailAndMerchantId(email, merchantId).isPresent();
    }

    @Override
    public Customer save(Customer customer) {
        return saveCustomer(customer);
    }

    @Override
    public Optional<Customer> findByEmail(String email) {
        return customerJpaRepository.findByEmail(email).map(customerMapper::toDomain);
    }

    @Override
    public Optional<Customer> findByMerchantIdAndExternalId(String merchantId, String externalId) {
        // external_id column doesn't exist in DB schema; return empty
        return Optional.empty();
    }

    @Override
    public List<Customer> findByMerchantId(String merchantId) {
        return customerJpaRepository.findByMerchantId(merchantId).stream()
                .map(customerMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Customer> findByEmailContainingAndMerchantId(String email, String merchantId) {
        return customerJpaRepository.findByEmailContainingAndMerchantId(email, merchantId).stream()
                .map(customerMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByEmail(String email) {
        return customerJpaRepository.existsByEmail(email);
    }

    @Override
    public void deleteById(String id) {
        customerJpaRepository.deleteById(id);
    }
}
