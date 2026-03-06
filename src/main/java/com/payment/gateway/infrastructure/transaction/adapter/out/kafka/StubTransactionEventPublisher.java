package com.payment.gateway.infrastructure.transaction.adapter.out.kafka;

import com.payment.gateway.domain.transaction.model.Transaction;
import com.payment.gateway.domain.transaction.port.TransactionEventPublisherPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class StubTransactionEventPublisher implements TransactionEventPublisherPort {

    @Override
    public void publishTransactionCreated(Transaction transaction) {
        log.info("Stub: Transaction created event for {}", transaction.getId());
    }

    @Override
    public void publishTransactionCompleted(Transaction transaction) {
        log.info("Stub: Transaction completed event for {}", transaction.getId());
    }

    @Override
    public void publishTransactionFailed(Transaction transaction) {
        log.info("Stub: Transaction failed event for {}", transaction.getId());
    }
}
