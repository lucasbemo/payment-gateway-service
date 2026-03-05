package com.payment.gateway.application.customer.port.out;

import com.payment.gateway.domain.customer.model.Customer;

import java.util.Optional;

/**
 * Output port for customer operations.
 */
public interface CustomerCommandPort {

    Customer saveCustomer(Customer customer);

    Optional<Customer> findById(String id);

    Optional<Customer> findByIdAndMerchantId(String id, String merchantId);

    boolean existsByEmailAndMerchantId(String email, String merchantId);
}
