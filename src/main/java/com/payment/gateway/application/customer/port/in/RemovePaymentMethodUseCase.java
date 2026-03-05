package com.payment.gateway.application.customer.port.in;

import com.payment.gateway.application.customer.dto.CustomerResponse;

/**
 * Use case for removing a payment method from a customer.
 */
public interface RemovePaymentMethodUseCase {

    CustomerResponse removePaymentMethod(String customerId, String paymentMethodId);
}
