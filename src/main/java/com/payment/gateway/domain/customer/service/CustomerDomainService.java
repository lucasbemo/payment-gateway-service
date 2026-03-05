package com.payment.gateway.domain.customer.service;

import com.payment.gateway.domain.customer.exception.CustomerNotFoundException;
import com.payment.gateway.domain.customer.exception.DuplicateCustomerException;
import com.payment.gateway.domain.customer.exception.InvalidPaymentMethodException;
import com.payment.gateway.domain.customer.model.CardDetails;
import com.payment.gateway.domain.customer.model.Customer;
import com.payment.gateway.domain.customer.model.PaymentMethod;
import com.payment.gateway.domain.customer.port.CustomerRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

/**
 * Customer domain service.
 * Contains business logic for customer operations.
 */
@Slf4j
@RequiredArgsConstructor
public class CustomerDomainService {

    private final CustomerRepositoryPort repository;

    public Customer createCustomer(String merchantId, String email, String name) {
        log.info("Creating customer with email {} for merchant {}", email, merchantId);

        if (repository.existsByEmail(email)) {
            throw new DuplicateCustomerException(email);
        }

        Customer customer = Customer.create(merchantId, email, name);
        return repository.save(customer);
    }

    public Customer createCustomerWithExternalId(String merchantId, String email, String name, String externalId) {
        log.info("Creating customer with external ID {} for merchant {}", externalId, merchantId);

        if (repository.existsByEmail(email)) {
            throw new DuplicateCustomerException(email);
        }

        Customer customer = Customer.create(merchantId, email, name);
        customer.updateExternalId(externalId);
        return repository.save(customer);
    }

    public Customer getCustomerOrThrow(String customerId) {
        return repository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(customerId));
    }

    public Optional<Customer> getCustomer(String customerId) {
        return repository.findById(customerId);
    }

    public Customer updateCustomerEmail(String customerId, String email) {
        log.info("Updating email for customer {}", customerId);

        Customer customer = getCustomerOrThrow(customerId);
        customer.updateEmail(email);

        return repository.save(customer);
    }

    public Customer updateCustomerPhone(String customerId, String phone) {
        log.info("Updating phone for customer {}", customerId);

        Customer customer = getCustomerOrThrow(customerId);
        customer.updatePhone(phone);

        return repository.save(customer);
    }

    public Customer updateCustomerName(String customerId, String name) {
        log.info("Updating name for customer {}", customerId);

        Customer customer = getCustomerOrThrow(customerId);
        customer.updateName(name);

        return repository.save(customer);
    }

    public Customer addPaymentMethod(String customerId, PaymentMethod paymentMethod) {
        log.info("Adding payment method to customer {}", customerId);

        Customer customer = getCustomerOrThrow(customerId);
        customer.addPaymentMethod(paymentMethod);

        return repository.save(customer);
    }

    public Customer addCardPaymentMethod(String customerId, CardDetails cardDetails, String token) {
        log.info("Adding card payment method to customer {}", customerId);

        Customer customer = getCustomerOrThrow(customerId);
        PaymentMethod paymentMethod = PaymentMethod.createCard(customerId, cardDetails, token);
        customer.addPaymentMethod(paymentMethod);

        return repository.save(customer);
    }

    public Customer removePaymentMethod(String customerId, String paymentMethodId) {
        log.info("Removing payment method {} from customer {}", paymentMethodId, customerId);

        Customer customer = getCustomerOrThrow(customerId);
        customer.removePaymentMethod(paymentMethodId);

        return repository.save(customer);
    }

    public Customer setDefaultPaymentMethod(String customerId, String paymentMethodId) {
        log.info("Setting default payment method {} for customer {}", paymentMethodId, customerId);

        Customer customer = getCustomerOrThrow(customerId);
        customer.setDefaultPaymentMethod(paymentMethodId);

        return repository.save(customer);
    }

    public PaymentMethod getPaymentMethod(String customerId, String paymentMethodId) {
        Customer customer = getCustomerOrThrow(customerId);
        PaymentMethod pm = customer.getPaymentMethod(paymentMethodId);

        if (pm == null) {
            throw new InvalidPaymentMethodException(paymentMethodId, customerId);
        }

        return pm;
    }

    public List<Customer> getCustomersByMerchantId(String merchantId) {
        return repository.findByMerchantId(merchantId);
    }

    public Optional<Customer> getCustomerByExternalId(String merchantId, String externalId) {
        return repository.findByMerchantIdAndExternalId(merchantId, externalId);
    }

    public Customer activateCustomer(String customerId) {
        log.info("Activating customer {}", customerId);

        Customer customer = getCustomerOrThrow(customerId);
        customer.activate();

        return repository.save(customer);
    }

    public Customer deactivateCustomer(String customerId) {
        log.info("Deactivating customer {}", customerId);

        Customer customer = getCustomerOrThrow(customerId);
        customer.deactivate();

        return repository.save(customer);
    }

    public Customer suspendCustomer(String customerId) {
        log.info("Suspending customer {}", customerId);

        Customer customer = getCustomerOrThrow(customerId);
        customer.suspend();

        return repository.save(customer);
    }
}
