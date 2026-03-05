package com.payment.gateway.application.customer.port.in;

import com.payment.gateway.application.customer.dto.CustomerResponse;
import com.payment.gateway.application.customer.dto.AddPaymentMethodCommand;

/**
 * Use case for adding a payment method to a customer.
 */
public interface AddPaymentMethodUseCase {

    CustomerResponse addPaymentMethod(AddPaymentMethodCommand command);
}
