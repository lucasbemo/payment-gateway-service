package com.payment.gateway.infrastructure.transaction.adapter.out.persistence;

import com.payment.gateway.application.transaction.port.out.TransactionCommandPort;
import com.payment.gateway.application.transaction.port.out.TransactionQueryPort;
import com.payment.gateway.domain.transaction.model.Transaction;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Primary
@RequiredArgsConstructor
public class TransactionPersistenceAdapter implements TransactionQueryPort, TransactionCommandPort {

    private final TransactionJpaRepository transactionJpaRepository;
    private final TransactionMapper transactionMapper;

    @Override
    public Optional<Transaction> findById(String id) {
        return transactionJpaRepository.findById(id).map(transactionMapper::toDomain);
    }

    @Override
    public Optional<Transaction> findByIdAndMerchantId(String id, String merchantId) {
        return transactionJpaRepository.findByIdAndMerchantId(id, merchantId).map(transactionMapper::toDomain);
    }

    @Override
    public Transaction createTransaction(Transaction transaction) {
        var entity = transactionMapper.toEntity(transaction);
        var saved = transactionJpaRepository.save(entity);
        return transactionMapper.toDomain(saved);
    }

    @Override
    public Transaction updateTransaction(Transaction transaction) {
        var entity = transactionMapper.toEntity(transaction);
        var saved = transactionJpaRepository.save(entity);
        return transactionMapper.toDomain(saved);
    }
}
