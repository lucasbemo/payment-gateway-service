package com.payment.gateway.application.payment.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * Command for processing a payment.
 */
@Getter
@Builder
public class ProcessPaymentCommand {

    private final String merchantId;
    private final Long amount;
    private final String currency;
    private final String paymentMethodType;
    private final String idempotencyKey;
    private final String description;
    private final String customerId;
    private final String cardNumber;
    private final String cardExpiryMonth;
    private final String cardExpiryYear;
    private final String cardCvv;
    private final List<PaymentItemDto> items;

    @Getter
    @Builder
    public static class PaymentItemDto {
        private final String description;
        private final Integer quantity;
        private final Long unitPrice;
    }
}
