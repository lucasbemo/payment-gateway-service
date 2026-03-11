package com.payment.gateway.infrastructure.transaction.adapter.out.persistence;

import com.payment.gateway.application.transaction.port.out.TransactionCommandPort;
import com.payment.gateway.application.transaction.port.out.TransactionQueryPort;
import com.payment.gateway.domain.transaction.model.Transaction;
import com.payment.gateway.domain.transaction.model.TransactionStatus;
import com.payment.gateway.domain.transaction.model.TransactionType;
import com.payment.gateway.domain.transaction.port.TransactionRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Primary
@RequiredArgsConstructor
public class TransactionPersistenceAdapter implements TransactionQueryPort, TransactionCommandPort, TransactionRepositoryPort, com.payment.gateway.application.payment.port.out.TransactionCommandPort {

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
    public String createTransaction(com.payment.gateway.application.payment.port.out.TransactionCommandPort.CreateTransactionCommand command) {
        Currency currency = Currency.getInstance(command.currency());
        var domainTransaction = Transaction.create(
                command.paymentId(),
                command.merchantId(),
                TransactionType.valueOf(command.type()),
                com.payment.gateway.commons.model.Money.of(BigDecimal.valueOf(command.amount()), currency),
                command.currency(),
                command.status() != null ? command.status() : "PENDING"
        );
        var entity = transactionMapper.toEntity(domainTransaction);
        var saved = transactionJpaRepository.save(entity);
        return saved.getId();
    }

    @Override
    public Transaction updateTransaction(Transaction transaction) {
        var entity = transactionMapper.toEntity(transaction);
        var saved = transactionJpaRepository.save(entity);
        return transactionMapper.toDomain(saved);
    }

    @Override
    public Transaction save(Transaction transaction) {
        return createTransaction(transaction);
    }

    @Override
    public Optional<Transaction> findByPaymentIdAndType(String paymentId, String type) {
        return transactionJpaRepository.findByPaymentIdAndType(paymentId, type).map(transactionMapper::toDomain);
    }

    @Override
    public List<Transaction> findByPaymentId(String paymentId) {
        return transactionJpaRepository.findByPaymentId(paymentId).stream()
                .map(transactionMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Transaction> findByMerchantId(String merchantId) {
        return transactionJpaRepository.findByMerchantId(merchantId).stream()
                .map(transactionMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Transaction> findByStatus(TransactionStatus status) {
        return transactionJpaRepository.findByStatus(status.name()).stream()
                .map(transactionMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Transaction> findByPaymentIdAndStatus(String paymentId, TransactionStatus status) {
        return transactionJpaRepository.findByPaymentIdAndStatus(paymentId, status.name()).stream()
                .map(transactionMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByPaymentId(String paymentId) {
        return transactionJpaRepository.existsByPaymentId(paymentId);
    }

    @Override
    public void deleteById(String id) {
        transactionJpaRepository.deleteById(id);
    }
}
