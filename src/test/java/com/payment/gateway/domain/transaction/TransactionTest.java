package com.payment.gateway.domain.transaction;

import com.payment.gateway.commons.model.Money;
import com.payment.gateway.domain.transaction.exception.InvalidTransactionStateException;
import com.payment.gateway.domain.transaction.model.Transaction;
import com.payment.gateway.domain.transaction.model.TransactionStatus;
import com.payment.gateway.domain.transaction.model.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import java.math.BigDecimal;
import java.util.Currency;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Transaction aggregate.
 */
class TransactionTest {

    private static final String PAYMENT_ID = "pay_123";
    private static final String MERCHANT_ID = "merch_456";
    private static final Money AMOUNT = Money.of(BigDecimal.valueOf(100.00), Currency.getInstance("USD"));
    private static final String CURRENCY = "USD";

    private Transaction transaction;

    @BeforeEach
    void setUp() {
        transaction = Transaction.create(PAYMENT_ID, MERCHANT_ID, TransactionType.PAYMENT, AMOUNT, CURRENCY);
    }

    @Nested
    @DisplayName("Transaction Creation")
    class TransactionCreation {

        @Test
        @DisplayName("Should create transaction with PENDING status")
        void shouldCreateTransactionWithPendingStatus() {
            assertNotNull(transaction.getId());
            assertEquals(PAYMENT_ID, transaction.getPaymentId());
            assertEquals(MERCHANT_ID, transaction.getMerchantId());
            assertEquals(TransactionType.PAYMENT, transaction.getType());
            assertEquals(AMOUNT, transaction.getAmount());
            assertEquals(TransactionStatus.PENDING, transaction.getStatus());
            assertNotNull(transaction.getCreatedAt());
            assertNotNull(transaction.getUpdatedAt());
        }

        @Test
        @DisplayName("Should create transaction with net amount equal to gross amount")
        void shouldCreateTransactionWithNetAmount() {
            assertEquals(AMOUNT, transaction.getNetAmount());
        }

        @Test
        @DisplayName("Should create transaction with zero retry count")
        void shouldCreateTransactionWithZeroRetryCount() {
            assertEquals(0, transaction.getRetryCount());
        }

        @Test
        @DisplayName("Should create transaction with null processed at")
        void shouldCreateTransactionWithNullProcessedAt() {
            assertNull(transaction.getProcessedAt());
        }
    }

    @Nested
    @DisplayName("Transaction Authorization")
    class Authorization {

        @Test
        @DisplayName("Should authorize pending transaction")
        void shouldAuthorizePendingTransaction() {
            transaction.authorize();

            assertEquals(TransactionStatus.AUTHORIZED, transaction.getStatus());
            assertNotNull(transaction.getProcessedAt());
        }

        @Test
        @DisplayName("Should not authorize failed transaction")
        void shouldNotAuthorizeFailedTransaction() {
            transaction.fail("ERROR", "Payment failed");

            assertThrows(IllegalStateException.class, () -> transaction.authorize());
        }

        @Test
        @DisplayName("Should not authorize already authorized transaction")
        void shouldNotAuthorizeAlreadyAuthorizedTransaction() {
            transaction.authorize();

            assertThrows(IllegalStateException.class, () -> transaction.authorize());
        }
    }

    @Nested
    @DisplayName("Transaction Capture")
    class Capture {

        @Test
        @DisplayName("Should capture authorized transaction")
        void shouldCaptureAuthorizedTransaction() {
            transaction.authorize();
            transaction.capture();

            assertEquals(TransactionStatus.CAPTURED, transaction.getStatus());
        }

        @Test
        @DisplayName("Should not capture failed transaction")
        void shouldNotCaptureFailedTransaction() {
            transaction.fail("ERROR", "Payment failed");

            assertThrows(IllegalStateException.class, () -> transaction.capture());
        }

        @Test
        @DisplayName("Should capture pending transaction for sale transactions")
        void shouldCapturePendingTransactionForSaleTransactions() {
            // For sale transactions, payment can go directly to captured
            transaction.capture();
            assertEquals(TransactionStatus.CAPTURED, transaction.getStatus());
        }
    }

    @Nested
    @DisplayName("Transaction Settlement")
    class Settlement {

        @Test
        @DisplayName("Should settle captured transaction")
        void shouldSettleCapturedTransaction() {
            transaction.authorize();
            transaction.capture();
            transaction.settle();

            assertEquals(TransactionStatus.SETTLED, transaction.getStatus());
        }

        @Test
        @DisplayName("Should settle pending transaction for direct settlement")
        void shouldSettlePendingTransactionForDirectSettlement() {
            // For some payment flows, can go directly to settled
            transaction.settle();
            assertEquals(TransactionStatus.SETTLED, transaction.getStatus());
        }
    }

    @Nested
    @DisplayName("Transaction Failure")
    class Failure {

        @Test
        @DisplayName("Should fail pending transaction with error details")
        void shouldFailPendingTransaction() {
            transaction.fail("CARD_DECLINED", "The card was declined");

            assertEquals(TransactionStatus.FAILED, transaction.getStatus());
            assertEquals("CARD_DECLINED", transaction.getErrorCode());
            assertEquals("The card was declined", transaction.getErrorMessage());
        }

        @Test
        @DisplayName("Should set error code and message on failure")
        void shouldSetErrorDetailsOnFailure() {
            String errorCode = "INSUFFICIENT_FUNDS";
            String errorMessage = "Insufficient funds in account";

            transaction.fail(errorCode, errorMessage);

            assertEquals(errorCode, transaction.getErrorCode());
            assertEquals(errorMessage, transaction.getErrorMessage());
        }
    }

    @Nested
    @DisplayName("Transaction Reversal")
    class Reversal {

        @Test
        @DisplayName("Should reverse authorized transaction")
        void shouldReverseAuthorizedTransaction() {
            transaction.authorize();
            transaction.reverse();

            assertEquals(TransactionStatus.REVERSED, transaction.getStatus());
        }

        @Test
        @DisplayName("Should not reverse settled transaction")
        void shouldNotReverseSettledTransaction() {
            transaction.authorize();
            transaction.capture();
            transaction.settle();

            assertThrows(IllegalStateException.class, () -> transaction.reverse());
        }
    }

    @Nested
    @DisplayName("Transaction Refund")
    class Refund {

        @Test
        @DisplayName("Should refund settled transaction")
        void shouldRefundSettledTransaction() {
            transaction.authorize();
            transaction.capture();
            transaction.settle();
            transaction.refund();

            assertEquals(TransactionStatus.REFUNDED, transaction.getStatus());
        }

        @Test
        @DisplayName("Should partially refund settled transaction")
        void shouldPartialRefundSettledTransaction() {
            transaction.authorize();
            transaction.capture();
            transaction.settle();
            transaction.partialRefund();

            assertEquals(TransactionStatus.PARTIALLY_REFUNDED, transaction.getStatus());
        }

        @Test
        @DisplayName("Should full refund after partial refund")
        void shouldFullRefundAfterPartialRefund() {
            transaction.authorize();
            transaction.capture();
            transaction.settle();
            transaction.partialRefund();
            transaction.refund();

            assertEquals(TransactionStatus.REFUNDED, transaction.getStatus());
        }

        @Test
        @DisplayName("Should not refund pending transaction")
        void shouldNotRefundPendingTransaction() {
            assertThrows(IllegalStateException.class, () -> transaction.refund());
        }
    }

    @Nested
    @DisplayName("Transaction Retry")
    class Retry {

        @Test
        @DisplayName("Should increment retry count")
        void shouldIncrementRetryCount() {
            transaction.incrementRetry();

            assertEquals(1, transaction.getRetryCount());
        }

        @Test
        @DisplayName("Should increment retry count multiple times")
        void shouldIncrementRetryCountMultipleTimes() {
            transaction.incrementRetry();
            transaction.incrementRetry();
            transaction.incrementRetry();

            assertEquals(3, transaction.getRetryCount());
        }
    }

    @Nested
    @DisplayName("Transaction Status Checks")
    class StatusChecks {

        @Test
        @DisplayName("Should return true for isPending when status is PENDING")
        void shouldBePendingWhenStatusIsPending() {
            assertTrue(transaction.isPending());
        }

        @Test
        @DisplayName("Should return false for isPending when status is AUTHORIZED")
        void shouldNotBePendingWhenStatusIsAuthorized() {
            transaction.authorize();

            assertFalse(transaction.isPending());
        }

        @Test
        @DisplayName("Should return true for isSuccessful when status is CAPTURED")
        void shouldBeSuccessfulWhenStatusIsCaptured() {
            transaction.authorize();
            transaction.capture();

            assertTrue(transaction.isSuccessful());
        }

        @Test
        @DisplayName("Should return true for isTerminal when status is SETTLED")
        void shouldBeTerminalWhenStatusIsSettled() {
            transaction.authorize();
            transaction.capture();
            transaction.settle();

            assertTrue(transaction.isTerminal());
        }

        @Test
        @DisplayName("Should return true for isTerminal when status is FAILED")
        void shouldBeTerminalWhenStatusIsFailed() {
            transaction.fail("ERROR", "Test error");

            assertTrue(transaction.isTerminal());
        }

        @Test
        @DisplayName("Should return false for isTerminal when status is PENDING")
        void shouldNotBeTerminalWhenStatusIsPending() {
            assertFalse(transaction.isTerminal());
        }
    }

    @Nested
    @DisplayName("Transaction Gateway ID")
    class GatewayId {

        @Test
        @DisplayName("Should update gateway transaction id")
        void shouldUpdateGatewayTransactionId() {
            String gatewayId = "gw_txn_123456";

            transaction.updateGatewayTransactionId(gatewayId);

            assertEquals(gatewayId, transaction.getGatewayTransactionId());
        }

        @Test
        @DisplayName("Should update updated_at when setting gateway transaction id")
        void shouldUpdateUpdatedAtWhenSettingGatewayTransactionId() {
            var beforeUpdate = transaction.getUpdatedAt();

            transaction.updateGatewayTransactionId("gw_txn_123456");

            assertNotNull(transaction.getUpdatedAt());
            assertTrue(transaction.getUpdatedAt().isAfter(beforeUpdate));
        }
    }

    @Nested
    @DisplayName("Transaction Status Transitions")
    class StatusTransitions {

        @Test
        @DisplayName("Should allow PENDING to PROCESSING transition")
        void shouldAllowPendingToProcessing() {
            assertTrue(TransactionStatus.PENDING.canTransitionTo(TransactionStatus.PROCESSING));
        }

        @Test
        @DisplayName("Should allow PENDING to AUTHORIZED transition")
        void shouldAllowPendingToAuthorized() {
            assertTrue(TransactionStatus.PENDING.canTransitionTo(TransactionStatus.AUTHORIZED));
        }

        @Test
        @DisplayName("Should allow PENDING to CAPTURED transition")
        void shouldAllowPendingToCaptured() {
            assertTrue(TransactionStatus.PENDING.canTransitionTo(TransactionStatus.CAPTURED));
        }

        @Test
        @DisplayName("Should allow PENDING to SETTLED transition for direct settlement")
        void shouldAllowPendingToSettled() {
            assertTrue(TransactionStatus.PENDING.canTransitionTo(TransactionStatus.SETTLED));
        }

        @Test
        @DisplayName("Should allow PENDING to FAILED transition")
        void shouldAllowPendingToFailed() {
            assertTrue(TransactionStatus.PENDING.canTransitionTo(TransactionStatus.FAILED));
        }

        @Test
        @DisplayName("Should not allow from terminal states")
        void shouldNotAllowFromTerminalStates() {
            assertFalse(TransactionStatus.FAILED.canTransitionTo(TransactionStatus.PENDING));
            assertFalse(TransactionStatus.REVERSED.canTransitionTo(TransactionStatus.PENDING));
            assertFalse(TransactionStatus.REFUNDED.canTransitionTo(TransactionStatus.PENDING));
        }
    }

    @Nested
    @DisplayName("Transaction Builder")
    class Builder {

        @Test
        @DisplayName("Should build transaction with all fields")
        void shouldBuildTransactionWithAllFields() {
            Transaction built = Transaction.builder()
                    .id("txn_custom")
                    .paymentId(PAYMENT_ID)
                    .merchantId(MERCHANT_ID)
                    .type(TransactionType.REFUND)
                    .amount(AMOUNT)
                    .currency(CURRENCY)
                    .status(TransactionStatus.PROCESSING)
                    .gatewayTransactionId("gw_123")
                    .errorCode(null)
                    .errorMessage(null)
                    .retryCount(0)
                    .build();

            assertEquals("txn_custom", built.getId());
            assertEquals(TransactionType.REFUND, built.getType());
            assertEquals(TransactionStatus.PROCESSING, built.getStatus());
            assertEquals("gw_123", built.getGatewayTransactionId());
        }
    }
}
