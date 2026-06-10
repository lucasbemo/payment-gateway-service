package com.payment.gateway.application.customer.port.in;

import com.payment.gateway.application.customer.dto.AddPaymentMethodCommand;
import com.payment.gateway.application.customer.dto.CustomerResponse;

/**
 * Use case for adding a payment method to a customer.
 */
public interface AddPaymentMethodUseCase {

    CustomerResponse addPaymentMethod(AddPaymentMethodCommand command);
}
