package com.payment.gateway.application.customer.port.in;

import com.payment.gateway.application.customer.dto.CustomerResponse;

/**
 * Use case for getting customer information.
 */
public interface GetCustomerUseCase {

    CustomerResponse getCustomerById(String customerId, String merchantId);
}
