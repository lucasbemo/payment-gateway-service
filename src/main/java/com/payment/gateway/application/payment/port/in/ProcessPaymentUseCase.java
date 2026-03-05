package com.payment.gateway.application.payment.port.in;

import com.payment.gateway.application.payment.dto.PaymentResponse;
import com.payment.gateway.application.payment.dto.ProcessPaymentCommand;

/**
 * Use case for processing a payment.
 */
public interface ProcessPaymentUseCase {

    PaymentResponse processPayment(ProcessPaymentCommand command);
}
