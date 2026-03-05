package com.payment.gateway.domain.transaction.service;

import com.payment.gateway.commons.model.Money;
import com.payment.gateway.domain.transaction.exception.InvalidTransactionStateException;
import com.payment.gateway.domain.transaction.exception.TransactionNotFoundException;
import com.payment.gateway.domain.transaction.exception.TransactionProcessingException;
import com.payment.gateway.domain.transaction.model.Transaction;
import com.payment.gateway.domain.transaction.model.TransactionStatus;
import com.payment.gateway.domain.transaction.model.TransactionType;
import com.payment.gateway.domain.transaction.port.TransactionEventPublisherPort;
import com.payment.gateway.domain.transaction.port.TransactionRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

/**
 * Transaction domain service.
 * Contains business logic for transaction operations.
 */
@Slf4j
@RequiredArgsConstructor
public class TransactionDomainService {

    private final TransactionRepositoryPort repository;
    private final TransactionEventPublisherPort eventPublisher;

    public Transaction createTransaction(String paymentId, String merchantId, TransactionType type,
                                          Money amount, String currency) {
        log.info("Creating {} transaction for payment {} with amount {}", type, paymentId, amount);

        Transaction transaction = Transaction.create(paymentId, merchantId, type, amount, currency);
        transaction = repository.save(transaction);
        eventPublisher.publishTransactionCreated(transaction);

        return transaction;
    }

    public Transaction authorizeTransaction(String transactionId) {
        log.info("Authorizing transaction {}", transactionId);

        Transaction transaction = getTransactionOrThrow(transactionId);

        if (transaction.getType() != TransactionType.AUTHORIZATION &&
            transaction.getType() != TransactionType.PAYMENT) {
            throw new TransactionProcessingException(
                "Cannot authorize transaction of type: " + transaction.getType()
            );
        }

        transaction.authorize();
        transaction = repository.save(transaction);
        eventPublisher.publishTransactionCompleted(transaction);

        return transaction;
    }

    public Transaction captureTransaction(String transactionId) {
        log.info("Capturing transaction {}", transactionId);

        Transaction transaction = getTransactionOrThrow(transactionId);

        if (transaction.getType() != TransactionType.CAPTURE &&
            transaction.getType() != TransactionType.PAYMENT) {
            throw new TransactionProcessingException(
                "Cannot capture transaction of type: " + transaction.getType()
            );
        }

        transaction.capture();
        transaction = repository.save(transaction);
        eventPublisher.publishTransactionCompleted(transaction);

        return transaction;
    }

    public Transaction settleTransaction(String transactionId) {
        log.info("Settling transaction {}", transactionId);

        Transaction transaction = getTransactionOrThrow(transactionId);
        transaction.settle();
        transaction = repository.save(transaction);
        eventPublisher.publishTransactionCompleted(transaction);

        return transaction;
    }

    public Transaction failTransaction(String transactionId, String errorCode, String errorMessage) {
        log.error("Failing transaction {} with error: {} - {}", transactionId, errorCode, errorMessage);

        Transaction transaction = getTransactionOrThrow(transactionId);
        transaction.fail(errorCode, errorMessage);
        transaction = repository.save(transaction);
        eventPublisher.publishTransactionFailed(transaction);

        return transaction;
    }

    public Transaction reverseTransaction(String transactionId) {
        log.info("Reversing transaction {}", transactionId);

        Transaction transaction = getTransactionOrThrow(transactionId);
        transaction.reverse();
        transaction = repository.save(transaction);
        eventPublisher.publishTransactionCompleted(transaction);

        return transaction;
    }

    public Transaction refundTransaction(String transactionId, boolean isPartial) {
        log.info("{} refunding transaction {}", isPartial ? "Partially" : "Fully", transactionId);

        Transaction transaction = getTransactionOrThrow(transactionId);

        if (isPartial) {
            transaction.partialRefund();
        } else {
            transaction.refund();
        }

        transaction = repository.save(transaction);
        eventPublisher.publishTransactionCompleted(transaction);

        return transaction;
    }

    public Transaction retryTransaction(String transactionId) {
        log.info("Retrying transaction {}", transactionId);

        Transaction transaction = getTransactionOrThrow(transactionId);

        if (!transaction.isPending()) {
            throw new TransactionProcessingException(
                "Cannot retry transaction that is not in pending state: " + transaction.getStatus()
            );
        }

        transaction.incrementRetry();
        return repository.save(transaction);
    }

    public Optional<Transaction> getTransaction(String transactionId) {
        return repository.findById(transactionId);
    }

    public Transaction getTransactionOrThrow(String transactionId) {
        return repository.findById(transactionId)
                .orElseThrow(() -> new TransactionNotFoundException(transactionId));
    }

    public List<Transaction> getTransactionsByPaymentId(String paymentId) {
        return repository.findByPaymentId(paymentId);
    }

    public List<Transaction> getTransactionsByMerchantId(String merchantId) {
        return repository.findByMerchantId(merchantId);
    }

    public List<Transaction> getTransactionsByStatus(TransactionStatus status) {
        return repository.findByStatus(status);
    }

    public boolean hasTransactionForPayment(String paymentId) {
        return repository.existsByPaymentId(paymentId);
    }

    public Transaction updateGatewayTransactionId(String transactionId, String gatewayTransactionId) {
        log.info("Updating gateway transaction id for {}", transactionId);

        Transaction transaction = getTransactionOrThrow(transactionId);
        transaction.updateGatewayTransactionId(gatewayTransactionId);

        return repository.save(transaction);
    }
}
