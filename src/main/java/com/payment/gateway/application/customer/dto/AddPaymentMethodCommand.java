package com.payment.gateway.application.customer.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * Command for adding a payment method to a customer.
 */
@Getter
@Builder
public class AddPaymentMethodCommand {

    private final String customerId;
    private final String merchantId;
    private final String cardNumber;
    private final String cardExpiryMonth;
    private final String cardExpiryYear;
    private final String cardCvv;
    private final String cardholderName;
    private final Boolean isDefault;
}
