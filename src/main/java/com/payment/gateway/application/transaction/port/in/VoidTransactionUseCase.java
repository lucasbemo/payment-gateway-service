package com.payment.gateway.application.transaction.port.in;

import com.payment.gateway.application.transaction.dto.TransactionResponse;

/**
 * Use case for voiding a transaction.
 */
public interface VoidTransactionUseCase {

    TransactionResponse voidTransaction(String transactionId, String merchantId);
}
