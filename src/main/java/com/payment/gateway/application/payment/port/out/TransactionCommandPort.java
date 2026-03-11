package com.payment.gateway.application.payment.port.out;

/**
 * Output port for transaction operations.
 */
public interface TransactionCommandPort {

    /**
     * Create a new transaction for a payment.
     */
    String createTransaction(CreateTransactionCommand command);

    record CreateTransactionCommand(
        String paymentId,
        String merchantId,
        String type,
        Long amount,
        String currency,
        String status
    ) {}
}
