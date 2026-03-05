package com.payment.gateway.application.payment.port.in;

import com.payment.gateway.application.payment.dto.PaymentResponse;

/**
 * Use case for canceling a payment.
 */
public interface CancelPaymentUseCase {

    PaymentResponse cancelPayment(String paymentId, String merchantId);
}
