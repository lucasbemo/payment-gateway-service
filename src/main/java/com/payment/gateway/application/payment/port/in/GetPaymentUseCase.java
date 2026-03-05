package com.payment.gateway.application.payment.port.in;

import com.payment.gateway.application.payment.dto.PaymentResponse;

import java.util.List;

/**
 * Use case for getting payment information.
 */
public interface GetPaymentUseCase {

    PaymentResponse getPaymentById(String paymentId, String merchantId);

    List<PaymentResponse> getPaymentsByMerchantId(String merchantId);
}
