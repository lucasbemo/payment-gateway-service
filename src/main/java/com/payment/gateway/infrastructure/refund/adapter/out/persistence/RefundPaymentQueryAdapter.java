package com.payment.gateway.infrastructure.refund.adapter.out.persistence;

import com.payment.gateway.application.refund.port.out.RefundPaymentQueryPort;
import com.payment.gateway.domain.payment.model.Payment;
import com.payment.gateway.domain.transaction.model.Transaction;
import com.payment.gateway.infrastructure.payment.adapter.out.persistence.PaymentJpaRepository;
import com.payment.gateway.infrastructure.payment.adapter.out.persistence.PaymentMapper;
import com.payment.gateway.infrastructure.transaction.adapter.out.persistence.TransactionJpaRepository;
import com.payment.gateway.infrastructure.transaction.adapter.out.persistence.TransactionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RefundPaymentQueryAdapter implements RefundPaymentQueryPort {

    private final PaymentJpaRepository paymentJpaRepository;
    private final PaymentMapper paymentMapper;
    private final TransactionJpaRepository transactionJpaRepository;
    private final TransactionMapper transactionMapper;

    @Override
    public Optional<Payment> findPaymentById(String id) {
        return paymentJpaRepository.findById(id).map(paymentMapper::toDomain);
    }

    @Override
    public Optional<Transaction> findTransactionById(String id) {
        return transactionJpaRepository.findById(id).map(transactionMapper::toDomain);
    }

    @Override
    public Optional<Transaction> findLatestTransactionByPaymentId(String paymentId) {
        return transactionJpaRepository.findLatestByPaymentId(paymentId).map(transactionMapper::toDomain);
    }
}
