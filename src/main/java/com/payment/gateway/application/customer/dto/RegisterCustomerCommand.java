package com.payment.gateway.application.customer.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * Command for registering a customer.
 */
@Getter
@Builder
public class RegisterCustomerCommand {

    private final String merchantId;
    private final String email;
    private final String name;
    private final String phone;
    private final String externalId;
}
