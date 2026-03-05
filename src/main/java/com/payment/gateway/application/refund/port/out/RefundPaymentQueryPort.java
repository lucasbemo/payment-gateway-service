package com.payment.gateway.application.refund.port.out;

import com.payment.gateway.domain.payment.model.Payment;
import com.payment.gateway.domain.transaction.model.Transaction;

import java.util.Optional;

/**
 * Output port for payment and transaction queries.
 */
public interface RefundPaymentQueryPort {

    Optional<Payment> findPaymentById(String id);

    Optional<Transaction> findTransactionById(String id);

    Optional<Transaction> findLatestTransactionByPaymentId(String paymentId);
}
