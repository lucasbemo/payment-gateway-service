package com.payment.gateway.application.customer.port.in;

import com.payment.gateway.application.customer.dto.CustomerResponse;
import com.payment.gateway.application.customer.dto.RegisterCustomerCommand;

/**
 * Use case for registering a customer.
 */
public interface RegisterCustomerUseCase {

    CustomerResponse registerCustomer(RegisterCustomerCommand command);
}
