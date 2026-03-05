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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Currency;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionDomainService Tests")
class TransactionDomainServiceTest {

    @Mock
    private TransactionRepositoryPort repository;

    @Mock
    private TransactionEventPublisherPort eventPublisher;

    private TransactionDomainService transactionDomainService;

    private final String TRANSACTION_ID = "txn_123";
    private final String PAYMENT_ID = "pay_123";
    private final String MERCHANT_ID = "merch_123";
    private final Currency BRL = Currency.getInstance("BRL");
    private final Money AMOUNT = Money.of(150000, BRL);

    @BeforeEach
    void setUp() {
        transactionDomainService = new TransactionDomainService(repository, eventPublisher);
    }

    @Nested
    @DisplayName("Create Transaction")
    class CreateTransactionTests {

        @Test
        @DisplayName("Should create transaction successfully")
        void shouldCreateTransactionSuccessfully() {
            // Given
            Transaction transaction = Transaction.create(PAYMENT_ID, MERCHANT_ID, TransactionType.PAYMENT, AMOUNT, "BRL");
            given(repository.save(any(Transaction.class))).willReturn(transaction);

            // When
            Transaction result = transactionDomainService.createTransaction(
                PAYMENT_ID, MERCHANT_ID, TransactionType.PAYMENT, AMOUNT, "BRL"
            );

            // Then
            assertThat(result).isNotNull();
            verify(repository).save(any(Transaction.class));
            verify(eventPublisher).publishTransactionCreated(any(Transaction.class));
        }
    }

    @Nested
    @DisplayName("Authorize Transaction")
    class AuthorizeTransactionTests {

        @Test
        @DisplayName("Should authorize payment transaction successfully")
        void shouldAuthorizePaymentTransactionSuccessfully() {
            // Given
            Transaction transaction = Transaction.create(PAYMENT_ID, MERCHANT_ID, TransactionType.PAYMENT, AMOUNT, "BRL");
            given(repository.findById(TRANSACTION_ID)).willReturn(Optional.of(transaction));
            given(repository.save(any(Transaction.class))).willReturn(transaction);

            // When
            Transaction result = transactionDomainService.authorizeTransaction(TRANSACTION_ID);

            // Then
            assertThat(result).isEqualTo(transaction);
            verify(repository).save(transaction);
            verify(eventPublisher).publishTransactionCompleted(transaction);
        }

        @Test
        @DisplayName("Should authorize authorization transaction successfully")
        void shouldAuthorizeAuthorizationTransactionSuccessfully() {
            // Given
            Transaction transaction = Transaction.create(PAYMENT_ID, MERCHANT_ID, TransactionType.AUTHORIZATION, AMOUNT, "BRL");
            given(repository.findById(TRANSACTION_ID)).willReturn(Optional.of(transaction));
            given(repository.save(any(Transaction.class))).willReturn(transaction);

            // When
            Transaction result = transactionDomainService.authorizeTransaction(TRANSACTION_ID);

            // Then
            assertThat(result).isEqualTo(transaction);
            verify(repository).save(transaction);
            verify(eventPublisher).publishTransactionCompleted(transaction);
        }

        @Test
        @DisplayName("Should throw exception when authorizing non-authorizable transaction")
        void shouldThrowExceptionWhenAuthorizingNonAuthorizableTransaction() {
            // Given
            Transaction transaction = Transaction.create(PAYMENT_ID, MERCHANT_ID, TransactionType.REFUND, AMOUNT, "BRL");
            given(repository.findById(TRANSACTION_ID)).willReturn(Optional.of(transaction));

            // When & Then
            assertThatThrownBy(() -> transactionDomainService.authorizeTransaction(TRANSACTION_ID))
                .isInstanceOf(TransactionProcessingException.class)
                .hasMessageContaining("Cannot authorize transaction of type");

            verify(repository, never()).save(any(Transaction.class));
            verify(eventPublisher, never()).publishTransactionCompleted(any(Transaction.class));
        }
    }

    @Nested
    @DisplayName("Capture Transaction")
    class CaptureTransactionTests {

        @Test
        @DisplayName("Should capture payment transaction successfully")
        void shouldCapturePaymentTransactionSuccessfully() {
            // Given
            Transaction transaction = Transaction.create(PAYMENT_ID, MERCHANT_ID, TransactionType.PAYMENT, AMOUNT, "BRL");
            given(repository.findById(TRANSACTION_ID)).willReturn(Optional.of(transaction));
            given(repository.save(any(Transaction.class))).willReturn(transaction);

            // When
            Transaction result = transactionDomainService.captureTransaction(TRANSACTION_ID);

            // Then
            assertThat(result).isEqualTo(transaction);
            verify(repository).save(transaction);
            verify(eventPublisher).publishTransactionCompleted(transaction);
        }

        @Test
        @DisplayName("Should throw exception when capturing non-capturable transaction")
        void shouldThrowExceptionWhenCapturingNonCapturableTransaction() {
            // Given
            Transaction transaction = Transaction.create(PAYMENT_ID, MERCHANT_ID, TransactionType.REFUND, AMOUNT, "BRL");
            given(repository.findById(TRANSACTION_ID)).willReturn(Optional.of(transaction));

            // When & Then
            assertThatThrownBy(() -> transactionDomainService.captureTransaction(TRANSACTION_ID))
                .isInstanceOf(TransactionProcessingException.class)
                .hasMessageContaining("Cannot capture transaction of type");

            verify(repository, never()).save(any(Transaction.class));
        }
    }

    @Nested
    @DisplayName("Settle Transaction")
    class SettleTransactionTests {

        @Test
        @DisplayName("Should settle transaction successfully")
        void shouldSettleTransactionSuccessfully() {
            // Given
            Transaction transaction = Transaction.create(PAYMENT_ID, MERCHANT_ID, TransactionType.PAYMENT, AMOUNT, "BRL");
            given(repository.findById(TRANSACTION_ID)).willReturn(Optional.of(transaction));
            given(repository.save(any(Transaction.class))).willReturn(transaction);

            // When
            Transaction result = transactionDomainService.settleTransaction(TRANSACTION_ID);

            // Then
            assertThat(result).isEqualTo(transaction);
            verify(repository).save(transaction);
            verify(eventPublisher).publishTransactionCompleted(transaction);
        }
    }

    @Nested
    @DisplayName("Fail Transaction")
    class FailTransactionTests {

        @Test
        @DisplayName("Should fail transaction with error details")
        void shouldFailTransactionWithErrorDetails() {
            // Given
            Transaction transaction = Transaction.create(PAYMENT_ID, MERCHANT_ID, TransactionType.PAYMENT, AMOUNT, "BRL");
            String errorCode = "INSUFFICIENT_FUNDS";
            String errorMessage = "Insufficient funds";
            given(repository.findById(TRANSACTION_ID)).willReturn(Optional.of(transaction));
            given(repository.save(any(Transaction.class))).willReturn(transaction);

            // When
            Transaction result = transactionDomainService.failTransaction(TRANSACTION_ID, errorCode, errorMessage);

            // Then
            assertThat(result).isEqualTo(transaction);
            verify(repository).save(transaction);
            verify(eventPublisher).publishTransactionFailed(transaction);
        }
    }

    @Nested
    @DisplayName("Reverse Transaction")
    class ReverseTransactionTests {

        @Test
        @DisplayName("Should reverse transaction successfully")
        void shouldReverseTransactionSuccessfully() {
            // Given
            Transaction transaction = Transaction.create(PAYMENT_ID, MERCHANT_ID, TransactionType.PAYMENT, AMOUNT, "BRL");
            transaction.authorize(); // Must be AUTHORIZED before reverse
            given(repository.findById(TRANSACTION_ID)).willReturn(Optional.of(transaction));
            given(repository.save(any(Transaction.class))).willReturn(transaction);

            // When
            Transaction result = transactionDomainService.reverseTransaction(TRANSACTION_ID);

            // Then
            assertThat(result).isEqualTo(transaction);
            verify(repository).save(transaction);
            verify(eventPublisher).publishTransactionCompleted(transaction);
        }
    }

    @Nested
    @DisplayName("Refund Transaction")
    class RefundTransactionTests {

        @Test
        @DisplayName("Should fully refund transaction")
        void shouldFullyRefundTransaction() {
            // Given
            Transaction transaction = Transaction.create(PAYMENT_ID, MERCHANT_ID, TransactionType.PAYMENT, AMOUNT, "BRL");
            transaction.capture(); // Must be CAPTURED before refund
            transaction.settle(); // Or SETTLED
            given(repository.findById(TRANSACTION_ID)).willReturn(Optional.of(transaction));
            given(repository.save(any(Transaction.class))).willReturn(transaction);

            // When
            Transaction result = transactionDomainService.refundTransaction(TRANSACTION_ID, false);

            // Then
            assertThat(result).isEqualTo(transaction);
            verify(repository).save(transaction);
            verify(eventPublisher).publishTransactionCompleted(transaction);
        }

        @Test
        @DisplayName("Should partially refund transaction")
        void shouldPartiallyRefundTransaction() {
            // Given
            Transaction transaction = Transaction.create(PAYMENT_ID, MERCHANT_ID, TransactionType.PAYMENT, AMOUNT, "BRL");
            transaction.capture(); // Must be CAPTURED before refund
            transaction.settle(); // Or SETTLED
            given(repository.findById(TRANSACTION_ID)).willReturn(Optional.of(transaction));
            given(repository.save(any(Transaction.class))).willReturn(transaction);

            // When
            Transaction result = transactionDomainService.refundTransaction(TRANSACTION_ID, true);

            // Then
            assertThat(result).isEqualTo(transaction);
            verify(repository).save(transaction);
            verify(eventPublisher).publishTransactionCompleted(transaction);
        }
    }

    @Nested
    @DisplayName("Retry Transaction")
    class RetryTransactionTests {

        @Test
        @DisplayName("Should retry pending transaction successfully")
        void shouldRetryPendingTransactionSuccessfully() {
            // Given
            Transaction transaction = Transaction.create(PAYMENT_ID, MERCHANT_ID, TransactionType.PAYMENT, AMOUNT, "BRL");
            given(repository.findById(TRANSACTION_ID)).willReturn(Optional.of(transaction));
            given(repository.save(any(Transaction.class))).willReturn(transaction);

            // When
            Transaction result = transactionDomainService.retryTransaction(TRANSACTION_ID);

            // Then
            assertThat(result).isEqualTo(transaction);
            verify(repository).save(transaction);
        }

        @Test
        @DisplayName("Should throw exception when retrying non-pending transaction")
        void shouldThrowExceptionWhenRetryingNonPendingTransaction() {
            // Given
            Transaction transaction = Transaction.create(PAYMENT_ID, MERCHANT_ID, TransactionType.PAYMENT, AMOUNT, "BRL");
            transaction.authorize(); // Change status from PENDING
            given(repository.findById(TRANSACTION_ID)).willReturn(Optional.of(transaction));

            // When & Then
            assertThatThrownBy(() -> transactionDomainService.retryTransaction(TRANSACTION_ID))
                .isInstanceOf(TransactionProcessingException.class)
                .hasMessageContaining("Cannot retry transaction that is not in pending state");

            verify(repository, never()).save(any(Transaction.class));
        }
    }

    @Nested
    @DisplayName("Get Transaction")
    class GetTransactionTests {

        @Test
        @DisplayName("Should return transaction when found")
        void shouldReturnTransactionWhenFound() {
            // Given
            Transaction transaction = Transaction.create(PAYMENT_ID, MERCHANT_ID, TransactionType.PAYMENT, AMOUNT, "BRL");
            given(repository.findById(TRANSACTION_ID)).willReturn(Optional.of(transaction));

            // When
            Optional<Transaction> result = transactionDomainService.getTransaction(TRANSACTION_ID);

            // Then
            assertThat(result).isPresent().contains(transaction);
        }

        @Test
        @DisplayName("Should return empty when transaction not found")
        void shouldReturnEmptyWhenTransactionNotFound() {
            // Given
            given(repository.findById(TRANSACTION_ID)).willReturn(Optional.empty());

            // When
            Optional<Transaction> result = transactionDomainService.getTransaction(TRANSACTION_ID);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should return transaction when found with orThrow method")
        void shouldReturnTransactionWhenFoundWithOrThrowMethod() {
            // Given
            Transaction transaction = Transaction.create(PAYMENT_ID, MERCHANT_ID, TransactionType.PAYMENT, AMOUNT, "BRL");
            given(repository.findById(TRANSACTION_ID)).willReturn(Optional.of(transaction));

            // When
            Transaction result = transactionDomainService.getTransactionOrThrow(TRANSACTION_ID);

            // Then
            assertThat(result).isEqualTo(transaction);
        }

        @Test
        @DisplayName("Should throw exception when transaction not found with orThrow method")
        void shouldThrowExceptionWhenTransactionNotFoundWithOrThrowMethod() {
            // Given
            given(repository.findById(TRANSACTION_ID)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> transactionDomainService.getTransactionOrThrow(TRANSACTION_ID))
                .isInstanceOf(TransactionNotFoundException.class)
                .hasMessageContaining(TRANSACTION_ID);
        }
    }

    @Nested
    @DisplayName("Get Transactions By Payment ID")
    class GetTransactionsByPaymentIdTests {

        @Test
        @DisplayName("Should return list of transactions by payment ID")
        void shouldReturnListOfTransactionsByPaymentId() {
            // Given
            Transaction transaction = Transaction.create(PAYMENT_ID, MERCHANT_ID, TransactionType.PAYMENT, AMOUNT, "BRL");
            given(repository.findByPaymentId(PAYMENT_ID)).willReturn(List.of(transaction));

            // When
            List<Transaction> result = transactionDomainService.getTransactionsByPaymentId(PAYMENT_ID);

            // Then
            assertThat(result).hasSize(1);
            verify(repository).findByPaymentId(PAYMENT_ID);
        }
    }

    @Nested
    @DisplayName("Get Transactions By Merchant ID")
    class GetTransactionsByMerchantIdTests {

        @Test
        @DisplayName("Should return list of transactions by merchant ID")
        void shouldReturnListOfTransactionsByMerchantId() {
            // Given
            Transaction transaction = Transaction.create(PAYMENT_ID, MERCHANT_ID, TransactionType.PAYMENT, AMOUNT, "BRL");
            given(repository.findByMerchantId(MERCHANT_ID)).willReturn(List.of(transaction));

            // When
            List<Transaction> result = transactionDomainService.getTransactionsByMerchantId(MERCHANT_ID);

            // Then
            assertThat(result).hasSize(1);
            verify(repository).findByMerchantId(MERCHANT_ID);
        }
    }

    @Nested
    @DisplayName("Get Transactions By Status")
    class GetTransactionsByStatusTests {

        @Test
        @DisplayName("Should return list of transactions by status")
        void shouldReturnListOfTransactionsByStatus() {
            // Given
            Transaction transaction = Transaction.create(PAYMENT_ID, MERCHANT_ID, TransactionType.PAYMENT, AMOUNT, "BRL");
            given(repository.findByStatus(TransactionStatus.PENDING)).willReturn(List.of(transaction));

            // When
            List<Transaction> result = transactionDomainService.getTransactionsByStatus(TransactionStatus.PENDING);

            // Then
            assertThat(result).hasSize(1);
            verify(repository).findByStatus(TransactionStatus.PENDING);
        }
    }

    @Nested
    @DisplayName("Has Transaction For Payment")
    class HasTransactionForPaymentTests {

        @Test
        @DisplayName("Should return true when transaction exists for payment")
        void shouldReturnTrueWhenTransactionExistsForPayment() {
            // Given
            given(repository.existsByPaymentId(PAYMENT_ID)).willReturn(true);

            // When
            boolean result = transactionDomainService.hasTransactionForPayment(PAYMENT_ID);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return false when transaction does not exist for payment")
        void shouldReturnFalseWhenTransactionDoesNotExistForPayment() {
            // Given
            given(repository.existsByPaymentId(PAYMENT_ID)).willReturn(false);

            // When
            boolean result = transactionDomainService.hasTransactionForPayment(PAYMENT_ID);

            // Then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("Update Gateway Transaction ID")
    class UpdateGatewayTransactionIdTests {

        @Test
        @DisplayName("Should update gateway transaction ID successfully")
        void shouldUpdateGatewayTransactionIdSuccessfully() {
            // Given
            String gatewayTransactionId = "gateway_txn_123";
            Transaction transaction = Transaction.create(PAYMENT_ID, MERCHANT_ID, TransactionType.PAYMENT, AMOUNT, "BRL");
            given(repository.findById(TRANSACTION_ID)).willReturn(Optional.of(transaction));
            given(repository.save(any(Transaction.class))).willReturn(transaction);

            // When
            Transaction result = transactionDomainService.updateGatewayTransactionId(TRANSACTION_ID, gatewayTransactionId);

            // Then
            assertThat(result).isEqualTo(transaction);
            verify(repository).save(transaction);
        }
    }
}
