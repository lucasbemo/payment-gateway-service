package com.payment.gateway.application.transaction.port.in;

import com.payment.gateway.application.transaction.dto.TransactionResponse;

/**
 * Use case for getting transaction information.
 */
public interface GetTransactionUseCase {

    TransactionResponse getTransactionById(String transactionId, String merchantId);
}
