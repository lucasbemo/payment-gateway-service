package com.payment.gateway.application.transaction.port.in;

import com.payment.gateway.application.transaction.dto.TransactionResponse;

/**
 * Use case for capturing a transaction.
 */
public interface CaptureTransactionUseCase {

    TransactionResponse captureTransaction(String transactionId, String merchantId);
}
