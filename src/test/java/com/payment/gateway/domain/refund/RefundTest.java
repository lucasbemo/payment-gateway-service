package com.payment.gateway.domain.refund;

import com.payment.gateway.commons.model.Money;
import com.payment.gateway.domain.refund.exception.RefundProcessingException;
import com.payment.gateway.domain.refund.model.Refund;
import com.payment.gateway.domain.refund.model.RefundItem;
import com.payment.gateway.domain.refund.model.RefundStatus;
import com.payment.gateway.domain.refund.model.RefundType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Refund aggregate.
 */
class RefundTest {

    private static final String PAYMENT_ID = "pay_123";
    private static final String TRANSACTION_ID = "txn_456";
    private static final String MERCHANT_ID = "merch_789";
    private static final Money AMOUNT = Money.of(BigDecimal.valueOf(50.00), Currency.getInstance("USD"));
    private static final String CURRENCY = "USD";
    private static final String IDEMPOTENCY_KEY = "refund_idem_001";
    private static final String REASON = "Customer request";

    private Refund refund;

    @BeforeEach
    void setUp() {
        refund = Refund.create(PAYMENT_ID, TRANSACTION_ID, MERCHANT_ID, RefundType.PARTIAL,
                                AMOUNT, CURRENCY, IDEMPOTENCY_KEY, REASON);
    }

    @Nested
    @DisplayName("Refund Creation")
    class RefundCreation {

        @Test
        @DisplayName("Should create refund with PENDING status")
        void shouldCreateRefundWithPendingStatus() {
            assertNotNull(refund.getId());
            assertEquals(PAYMENT_ID, refund.getPaymentId());
            assertEquals(TRANSACTION_ID, refund.getTransactionId());
            assertEquals(MERCHANT_ID, refund.getMerchantId());
            assertEquals(RefundType.PARTIAL, refund.getType());
            assertEquals(AMOUNT, refund.getAmount());
            assertEquals(RefundStatus.PENDING, refund.getStatus());
            assertEquals(CURRENCY, refund.getCurrency());
            assertNotNull(refund.getCreatedAt());
        }

        @Test
        @DisplayName("Should create refund with zero refunded amount")
        void shouldCreateRefundWithZeroRefundedAmount() {
            assertTrue(refund.getRefundedAmount().isZero());
        }

        @Test
        @DisplayName("Should create refund with empty items list")
        void shouldCreateRefundWithEmptyItemsList() {
            assertNotNull(refund.getItems());
            assertTrue(refund.getItems().isEmpty());
        }

        @Test
        @DisplayName("Should create refund with zero retry count")
        void shouldCreateRefundWithZeroRetryCount() {
            assertEquals(0, refund.getRetryCount());
        }
    }

    @Nested
    @DisplayName("Refund Approval")
    class Approval {

        @Test
        @DisplayName("Should approve pending refund")
        void shouldApprovePendingRefund() {
            refund.approve();

            assertEquals(RefundStatus.APPROVED, refund.getStatus());
        }

        @Test
        @DisplayName("Should not approve rejected refund")
        void shouldNotApproveRejectedRefund() {
            refund.reject("Invalid request");

            assertThrows(IllegalStateException.class, () -> refund.approve());
        }

        @Test
        @DisplayName("Should not approve completed refund")
        void shouldNotApproveCompletedRefund() {
            refund.approve();
            refund.complete();

            assertThrows(IllegalStateException.class, () -> refund.approve());
        }
    }

    @Nested
    @DisplayName("Refund Rejection")
    class Rejection {

        @Test
        @DisplayName("Should reject pending refund")
        void shouldRejectPendingRefund() {
            refund.reject("Insufficient funds");

            assertEquals(RefundStatus.REJECTED, refund.getStatus());
        }

        @Test
        @DisplayName("Should update reason on rejection")
        void shouldUpdateReasonOnRejection() {
            String newReason = "Fraud suspected";
            refund.reject(newReason);

            assertEquals(newReason, refund.getReason());
        }

        @Test
        @DisplayName("Should not reject already rejected refund")
        void shouldNotRejectAlreadyRejectedRefund() {
            refund.reject("Reason 1");

            assertThrows(IllegalStateException.class, () -> refund.reject("Reason 2"));
        }
    }

    @Nested
    @DisplayName("Refund Completion")
    class Completion {

        @Test
        @DisplayName("Should complete approved refund")
        void shouldCompleteApprovedRefund() {
            refund.approve();
            refund.complete();

            assertEquals(RefundStatus.COMPLETED, refund.getStatus());
        }

        @Test
        @DisplayName("Should complete processing refund")
        void shouldCompleteProcessingRefund() {
            refund.approve();
            refund.complete();

            assertEquals(RefundStatus.COMPLETED, refund.getStatus());
        }

        @Test
        @DisplayName("Should not complete pending refund")
        void shouldNotCompletePendingRefund() {
            assertThrows(IllegalStateException.class, () -> refund.complete());
        }

        @Test
        @DisplayName("Should not complete rejected refund")
        void shouldNotCompleteRejectedRefund() {
            refund.reject("Invalid");

            assertThrows(IllegalStateException.class, () -> refund.complete());
        }
    }

    @Nested
    @DisplayName("Refund Failure")
    class Failure {

        @Test
        @DisplayName("Should fail processing refund with error details")
        void shouldFailProcessingRefund() {
            refund.approve();
            refund.fail("GATEWAY_ERROR", "Gateway timeout");

            assertEquals(RefundStatus.FAILED, refund.getStatus());
            assertEquals("GATEWAY_ERROR", refund.getErrorCode());
            assertEquals("Gateway timeout", refund.getErrorMessage());
        }

        @Test
        @DisplayName("Should set error code and message on failure")
        void shouldSetErrorDetailsOnFailure() {
            refund.fail("CARD_ERROR", "Card declined");

            assertEquals("CARD_ERROR", refund.getErrorCode());
            assertEquals("Card declined", refund.getErrorMessage());
        }
    }

    @Nested
    @DisplayName("Refund Cancellation")
    class Cancellation {

        @Test
        @DisplayName("Should cancel pending refund")
        void shouldCancelPendingRefund() {
            refund.cancel("Customer changed mind");

            assertEquals(RefundStatus.CANCELLED, refund.getStatus());
        }

        @Test
        @DisplayName("Should update reason on cancellation")
        void shouldUpdateReasonOnCancellation() {
            String newReason = "Duplicate refund";
            refund.cancel(newReason);

            assertEquals(newReason, refund.getReason());
        }

        @Test
        @DisplayName("Should not cancel completed refund")
        void shouldNotCancelCompletedRefund() {
            refund.approve();
            refund.complete();

            assertThrows(IllegalStateException.class, () -> refund.cancel("Changed mind"));
        }

        @Test
        @DisplayName("Should not cancel rejected refund")
        void shouldNotCancelRejectedRefund() {
            refund.reject("Invalid");

            assertThrows(IllegalStateException.class, () -> refund.cancel("Changed mind"));
        }
    }

    @Nested
    @DisplayName("Refund Retry")
    class Retry {

        @Test
        @DisplayName("Should retry failed refund")
        void shouldRetryFailedRefund() {
            refund.fail("TEMP_ERROR", "Temporary failure");
            refund.retry();

            assertEquals(RefundStatus.PENDING, refund.getStatus());
            assertNull(refund.getErrorCode());
            assertNull(refund.getErrorMessage());
            assertEquals(1, refund.getRetryCount());
        }

        @Test
        @DisplayName("Should increment retry count on retry")
        void shouldIncrementRetryCountOnRetry() {
            refund.fail("ERROR", "Failed");
            refund.retry();
            refund.fail("ERROR", "Failed again");
            refund.retry();

            assertEquals(2, refund.getRetryCount());
        }

        @Test
        @DisplayName("Should not retry non-failed refund")
        void shouldNotRetryNonFailedRefund() {
            assertThrows(IllegalStateException.class, () -> refund.retry());
        }
    }

    @Nested
    @DisplayName("Refund Items")
    class Items {

        @Test
        @DisplayName("Should add single item to refund")
        void shouldAddSingleItemToRefund() {
            RefundItem item = RefundItem.create("item_1", 2, "Defective product");

            refund.addItem(item);

            assertNotNull(refund.getItems());
            assertEquals(1, refund.getItems().size());
            assertEquals(item, refund.getItems().get(0));
        }

        @Test
        @DisplayName("Should add multiple items to refund")
        void shouldAddMultipleItemsToRefund() {
            RefundItem item1 = RefundItem.create("item_1", 1, "Wrong size");
            RefundItem item2 = RefundItem.create("item_2", 2, "Defective");

            refund.addItems(List.of(item1, item2));

            assertNotNull(refund.getItems());
            assertEquals(2, refund.getItems().size());
        }

        @Test
        @DisplayName("Should update updated_at when adding items")
        void shouldUpdateUpdatedAtWhenAddingItems() {
            var beforeUpdate = refund.getUpdatedAt();

            refund.addItem(RefundItem.create("item_1", 1, "Reason"));

            assertTrue(refund.getUpdatedAt().isAfter(beforeUpdate));
        }
    }

    @Nested
    @DisplayName("Refund Status Checks")
    class StatusChecks {

        @Test
        @DisplayName("Should return true for isPending when status is PENDING")
        void shouldBePendingWhenStatusIsPending() {
            assertTrue(refund.isPending());
        }

        @Test
        @DisplayName("Should return false for isPending when status is APPROVED")
        void shouldNotBePendingWhenStatusIsApproved() {
            refund.approve();

            assertFalse(refund.isPending());
        }

        @Test
        @DisplayName("Should return true for isSuccessful when status is COMPLETED")
        void shouldBeSuccessfulWhenStatusIsCompleted() {
            refund.approve();
            refund.complete();

            assertTrue(refund.isSuccessful());
        }

        @Test
        @DisplayName("Should return true for isTerminal when status is COMPLETED")
        void shouldBeTerminalWhenStatusIsCompleted() {
            refund.approve();
            refund.complete();

            assertTrue(refund.isTerminal());
        }

        @Test
        @DisplayName("Should return true for isTerminal when status is REJECTED")
        void shouldBeTerminalWhenStatusIsRejected() {
            refund.reject("Invalid");

            assertTrue(refund.isTerminal());
        }

        @Test
        @DisplayName("Should return true for isTerminal when status is CANCELLED")
        void shouldBeTerminalWhenStatusIsCancelled() {
            refund.cancel("Customer request");

            assertTrue(refund.isTerminal());
        }

        @Test
        @DisplayName("Should return false for isTerminal when status is PENDING")
        void shouldNotBeTerminalWhenStatusIsPending() {
            assertFalse(refund.isTerminal());
        }

        @Test
        @DisplayName("Should return false for isTerminal when status is FAILED")
        void shouldNotBeTerminalWhenStatusIsFailed() {
            refund.fail("ERROR", "Test");

            assertFalse(refund.isTerminal());
        }
    }

    @Nested
    @DisplayName("Refund Gateway ID")
    class GatewayId {

        @Test
        @DisplayName("Should update gateway refund id")
        void shouldUpdateGatewayRefundId() {
            String gatewayId = "gw_refund_123456";

            refund.updateGatewayRefundId(gatewayId);

            assertEquals(gatewayId, refund.getGatewayRefundId());
        }

        @Test
        @DisplayName("Should update updated_at when setting gateway refund id")
        void shouldUpdateUpdatedAtWhenSettingGatewayRefundId() {
            var beforeUpdate = refund.getUpdatedAt();

            refund.updateGatewayRefundId("gw_refund_123456");

            assertNotNull(refund.getUpdatedAt());
            assertTrue(refund.getUpdatedAt().isAfter(beforeUpdate));
        }
    }

    @Nested
    @DisplayName("Refund Status Transitions")
    class StatusTransitions {

        @Test
        @DisplayName("Should allow PENDING to PROCESSING transition")
        void shouldAllowPendingToProcessing() {
            assertTrue(RefundStatus.PENDING.canTransitionTo(RefundStatus.PROCESSING));
        }

        @Test
        @DisplayName("Should allow PENDING to APPROVED transition")
        void shouldAllowPendingToApproved() {
            assertTrue(RefundStatus.PENDING.canTransitionTo(RefundStatus.APPROVED));
        }

        @Test
        @DisplayName("Should allow PENDING to REJECTED transition")
        void shouldAllowPendingToRejected() {
            assertTrue(RefundStatus.PENDING.canTransitionTo(RefundStatus.REJECTED));
        }

        @Test
        @DisplayName("Should allow PROCESSING to COMPLETED transition")
        void shouldAllowProcessingToCompleted() {
            assertTrue(RefundStatus.PROCESSING.canTransitionTo(RefundStatus.COMPLETED));
        }

        @Test
        @DisplayName("Should allow FAILED to PENDING transition for retry")
        void shouldAllowFailedToPending() {
            assertTrue(RefundStatus.FAILED.canTransitionTo(RefundStatus.PENDING));
        }

        @Test
        @DisplayName("Should not allow from terminal states")
        void shouldNotAllowFromTerminalStates() {
            assertFalse(RefundStatus.COMPLETED.canTransitionTo(RefundStatus.PENDING));
            assertFalse(RefundStatus.REJECTED.canTransitionTo(RefundStatus.PENDING));
            assertFalse(RefundStatus.CANCELLED.canTransitionTo(RefundStatus.PENDING));
        }
    }

    @Nested
    @DisplayName("Refund Builder")
    class Builder {

        @Test
        @DisplayName("Should build refund with all fields")
        void shouldBuildRefundWithAllFields() {
            Refund built = Refund.builder()
                    .id("refund_custom")
                    .paymentId(PAYMENT_ID)
                    .transactionId(TRANSACTION_ID)
                    .merchantId(MERCHANT_ID)
                    .type(RefundType.FULL)
                    .amount(AMOUNT)
                    .currency(CURRENCY)
                    .status(RefundStatus.APPROVED)
                    .reason("Test reason")
                    .gatewayRefundId("gw_123")
                    .build();

            assertEquals("refund_custom", built.getId());
            assertEquals(RefundType.FULL, built.getType());
            assertEquals(RefundStatus.APPROVED, built.getStatus());
            assertEquals("gw_123", built.getGatewayRefundId());
        }
    }
}
