package com.payment.gateway.domain.customer.port;

import com.payment.gateway.domain.customer.model.Customer;

import java.util.List;
import java.util.Optional;

/**
 * Customer repository port interface.
 */
public interface CustomerRepositoryPort {
    Customer save(Customer customer);
    Optional<Customer> findById(String id);
    Optional<Customer> findByEmail(String email);
    Optional<Customer> findByMerchantIdAndExternalId(String merchantId, String externalId);
    List<Customer> findByMerchantId(String merchantId);
    List<Customer> findByEmailContainingAndMerchantId(String email, String merchantId);
    boolean existsByEmail(String email);
    void deleteById(String id);
}
