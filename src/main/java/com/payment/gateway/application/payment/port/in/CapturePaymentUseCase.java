package com.payment.gateway.application.payment.port.in;

import com.payment.gateway.application.payment.dto.PaymentResponse;

/**
 * Use case for capturing a payment.
 */
public interface CapturePaymentUseCase {

    PaymentResponse capturePayment(String paymentId, String merchantId);
}
