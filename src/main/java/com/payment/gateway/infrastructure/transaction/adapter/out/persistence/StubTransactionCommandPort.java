package com.payment.gateway.infrastructure.transaction.adapter.out.persistence;

import com.payment.gateway.application.payment.port.out.TransactionCommandPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Stub implementation of TransactionCommandPort for testing/development.
 */
@Slf4j
@Component
public class StubTransactionCommandPort implements TransactionCommandPort {

    @Override
    public String createTransaction(CreateTransactionCommand command) {
        log.info("StubTransactionCommandPort.createTransaction: paymentId={}, merchantId={}, type={}, amount={}",
                command.paymentId(), command.merchantId(), command.type(), command.amount());
        return "txn-stub-" + command.paymentId();
    }
}
