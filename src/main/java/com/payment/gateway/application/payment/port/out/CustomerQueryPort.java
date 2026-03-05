package com.payment.gateway.application.payment.port.out;

import com.payment.gateway.domain.customer.model.Customer;
import com.payment.gateway.domain.customer.model.PaymentMethod;

import java.util.Optional;

/**
 * Output port for customer and payment method queries.
 */
public interface CustomerQueryPort {

    Optional<Customer> findCustomerById(String id);

    Optional<Customer> findCustomerByIdAndMerchantId(String customerId, String merchantId);

    Optional<PaymentMethod> findPaymentMethodById(String paymentMethodId);

    Optional<PaymentMethod> findPaymentMethodByToken(String token);
}
