package com.payment.gateway.domain.refund.service;

import com.payment.gateway.commons.model.Money;
import com.payment.gateway.domain.refund.exception.RefundNotFoundException;
import com.payment.gateway.domain.refund.exception.RefundProcessingException;
import com.payment.gateway.domain.refund.model.*;
import com.payment.gateway.domain.refund.port.RefundEventPublisherPort;
import com.payment.gateway.domain.refund.port.RefundRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RefundDomainService Tests")
class RefundDomainServiceTest {

    @Mock
    private RefundRepositoryPort repository;

    @Mock
    private RefundEventPublisherPort eventPublisher;

    private RefundDomainService refundDomainService;

    private final String REFUND_ID = "ref_123";
    private final String PAYMENT_ID = "pay_123";
    private final String TRANSACTION_ID = "txn_123";
    private final String MERCHANT_ID = "merch_123";
    private final Currency BRL = Currency.getInstance("BRL");
    private final Money AMOUNT = Money.of(new BigDecimal("500.00"), BRL);
    private final String IDEMPOTENCY_KEY = "ref_idem_123";
    private final String REASON = "Customer requested refund";

    @BeforeEach
    void setUp() {
        refundDomainService = new RefundDomainService(repository, eventPublisher);
    }

    @Nested
    @DisplayName("Create Refund")
    class CreateRefundTests {

        @Test
        @DisplayName("Should create refund successfully")
        void shouldCreateRefundSuccessfully() {
            // Given
            Refund refund = Refund.create(PAYMENT_ID, TRANSACTION_ID, MERCHANT_ID, RefundType.FULL, AMOUNT, "BRL", IDEMPOTENCY_KEY, REASON);
            given(repository.existsByRefundIdempotencyKey(IDEMPOTENCY_KEY)).willReturn(false);
            given(repository.save(any(Refund.class))).willReturn(refund);

            // When
            Refund result = refundDomainService.createRefund(
                PAYMENT_ID, TRANSACTION_ID, MERCHANT_ID, RefundType.FULL, AMOUNT, "BRL", IDEMPOTENCY_KEY, REASON
            );

            // Then
            assertThat(result).isNotNull();
            verify(repository).existsByRefundIdempotencyKey(IDEMPOTENCY_KEY);
            verify(repository).save(any(Refund.class));
            verify(eventPublisher).publishRefundCreated(refund);
        }

        @Test
        @DisplayName("Should throw exception when idempotency key already exists")
        void shouldThrowExceptionWhenIdempotencyKeyAlreadyExists() {
            // Given
            given(repository.existsByRefundIdempotencyKey(IDEMPOTENCY_KEY)).willReturn(true);

            // When & Then
            assertThatThrownBy(() -> refundDomainService.createRefund(
                PAYMENT_ID, TRANSACTION_ID, MERCHANT_ID, RefundType.FULL, AMOUNT, "BRL", IDEMPOTENCY_KEY, REASON
            ))
                .isInstanceOf(RefundProcessingException.class)
                .hasMessageContaining("already exists");

            verify(repository).existsByRefundIdempotencyKey(IDEMPOTENCY_KEY);
            verify(repository, never()).save(any(Refund.class));
        }

        @Test
        @DisplayName("Should create refund with items successfully")
        void shouldCreateRefundWithItemsSuccessfully() {
            // Given
            Refund refund = Refund.create(PAYMENT_ID, TRANSACTION_ID, MERCHANT_ID, RefundType.FULL, AMOUNT, "BRL", IDEMPOTENCY_KEY, REASON);
            List<RefundItem> items = List.of(RefundItem.create("item_1", 1, "Item 1"));
            given(repository.existsByRefundIdempotencyKey(IDEMPOTENCY_KEY)).willReturn(false);
            given(repository.save(any(Refund.class))).willReturn(refund);

            // When
            Refund result = refundDomainService.createRefundWithItems(
                PAYMENT_ID, TRANSACTION_ID, MERCHANT_ID, RefundType.FULL, AMOUNT, "BRL", IDEMPOTENCY_KEY, REASON, items
            );

            // Then
            assertThat(result).isNotNull();
            verify(repository, times(2)).save(any(Refund.class));
        }
    }

    @Nested
    @DisplayName("Approve Refund")
    class ApproveRefundTests {

        @Test
        @DisplayName("Should approve pending refund successfully")
        void shouldApprovePendingRefundSuccessfully() {
            // Given
            Refund refund = Refund.create(PAYMENT_ID, TRANSACTION_ID, MERCHANT_ID, RefundType.FULL, AMOUNT, "BRL", IDEMPOTENCY_KEY, REASON);
            given(repository.findById(REFUND_ID)).willReturn(Optional.of(refund));
            given(repository.save(any(Refund.class))).willReturn(refund);

            // When
            Refund result = refundDomainService.approveRefund(REFUND_ID);

            // Then
            assertThat(result).isEqualTo(refund);
            verify(repository).save(refund);
            verify(eventPublisher).publishRefundCompleted(refund);
        }

        @Test
        @DisplayName("Should throw exception when approving non-pending refund")
        void shouldThrowExceptionWhenApprovingNonPendingRefund() {
            // Given
            Refund refund = Refund.create(PAYMENT_ID, TRANSACTION_ID, MERCHANT_ID, RefundType.FULL, AMOUNT, "BRL", IDEMPOTENCY_KEY, REASON);
            refund.approve(); // Change status to APPROVED
            given(repository.findById(REFUND_ID)).willReturn(Optional.of(refund));

            // When & Then
            assertThatThrownBy(() -> refundDomainService.approveRefund(REFUND_ID))
                .isInstanceOf(RefundProcessingException.class)
                .hasMessageContaining("Can only approve pending refunds");

            verify(repository, never()).save(any(Refund.class));
        }
    }

    @Nested
    @DisplayName("Reject Refund")
    class RejectRefundTests {

        @Test
        @DisplayName("Should reject refund successfully")
        void shouldRejectRefundSuccessfully() {
            // Given
            Refund refund = Refund.create(PAYMENT_ID, TRANSACTION_ID, MERCHANT_ID, RefundType.FULL, AMOUNT, "BRL", IDEMPOTENCY_KEY, REASON);
            String rejectionReason = "Invalid refund request";
            given(repository.findById(REFUND_ID)).willReturn(Optional.of(refund));
            given(repository.save(any(Refund.class))).willReturn(refund);

            // When
            Refund result = refundDomainService.rejectRefund(REFUND_ID, rejectionReason);

            // Then
            assertThat(result).isEqualTo(refund);
            verify(repository).save(refund);
            verify(eventPublisher).publishRefundCompleted(refund);
        }
    }

    @Nested
    @DisplayName("Complete Refund")
    class CompleteRefundTests {

        @Test
        @DisplayName("Should complete approved refund successfully")
        void shouldCompleteApprovedRefundSuccessfully() {
            // Given
            Refund refund = Refund.create(PAYMENT_ID, TRANSACTION_ID, MERCHANT_ID, RefundType.FULL, AMOUNT, "BRL", IDEMPOTENCY_KEY, REASON);
            refund.approve();
            given(repository.findById(REFUND_ID)).willReturn(Optional.of(refund));
            given(repository.save(any(Refund.class))).willReturn(refund);

            // When
            Refund result = refundDomainService.completeRefund(REFUND_ID);

            // Then
            assertThat(result).isEqualTo(refund);
            verify(repository).save(refund);
            verify(eventPublisher).publishRefundCompleted(refund);
        }

        @Test
        @DisplayName("Should throw exception when completing pending refund")
        void shouldThrowExceptionWhenCompletingPendingRefund() {
            // Given
            Refund refund = Refund.create(PAYMENT_ID, TRANSACTION_ID, MERCHANT_ID, RefundType.FULL, AMOUNT, "BRL", IDEMPOTENCY_KEY, REASON);
            given(repository.findById(REFUND_ID)).willReturn(Optional.of(refund));

            // When & Then
            assertThatThrownBy(() -> refundDomainService.completeRefund(REFUND_ID))
                .isInstanceOf(RefundProcessingException.class)
                .hasMessageContaining("Can only complete approved or processing refunds");

            verify(repository, never()).save(any(Refund.class));
        }
    }

    @Nested
    @DisplayName("Fail Refund")
    class FailRefundTests {

        @Test
        @DisplayName("Should fail refund with error details")
        void shouldFailRefundWithErrorDetails() {
            // Given
            Refund refund = Refund.create(PAYMENT_ID, TRANSACTION_ID, MERCHANT_ID, RefundType.FULL, AMOUNT, "BRL", IDEMPOTENCY_KEY, REASON);
            String errorCode = "PROCESSING_ERROR";
            String errorMessage = "Gateway timeout";
            given(repository.findById(REFUND_ID)).willReturn(Optional.of(refund));
            given(repository.save(any(Refund.class))).willReturn(refund);

            // When
            Refund result = refundDomainService.failRefund(REFUND_ID, errorCode, errorMessage);

            // Then
            assertThat(result).isEqualTo(refund);
            verify(repository).save(refund);
            verify(eventPublisher).publishRefundFailed(refund);
        }
    }

    @Nested
    @DisplayName("Cancel Refund")
    class CancelRefundTests {

        @Test
        @DisplayName("Should cancel pending refund successfully")
        void shouldCancelPendingRefundSuccessfully() {
            // Given
            Refund refund = Refund.create(PAYMENT_ID, TRANSACTION_ID, MERCHANT_ID, RefundType.FULL, AMOUNT, "BRL", IDEMPOTENCY_KEY, REASON);
            String cancellationReason = "Customer changed mind";
            given(repository.findById(REFUND_ID)).willReturn(Optional.of(refund));
            given(repository.save(any(Refund.class))).willReturn(refund);

            // When
            Refund result = refundDomainService.cancelRefund(REFUND_ID, cancellationReason);

            // Then
            assertThat(result).isEqualTo(refund);
            verify(repository).save(refund);
            verify(eventPublisher).publishRefundCompleted(refund);
        }

        @Test
        @DisplayName("Should throw exception when cancelling completed refund")
        void shouldThrowExceptionWhenCancellingCompletedRefund() {
            // Given
            Refund refund = Refund.create(PAYMENT_ID, TRANSACTION_ID, MERCHANT_ID, RefundType.FULL, AMOUNT, "BRL", IDEMPOTENCY_KEY, REASON);
            refund.approve();
            refund.complete(); // Now in COMPLETED state (terminal)
            given(repository.findById(REFUND_ID)).willReturn(Optional.of(refund));

            // When & Then
            assertThatThrownBy(() -> refundDomainService.cancelRefund(REFUND_ID, "Reason"))
                .isInstanceOf(RefundProcessingException.class)
                .hasMessageContaining("Cannot cancel terminal refund");

            verify(repository, never()).save(any(Refund.class));
        }
    }

    @Nested
    @DisplayName("Retry Refund")
    class RetryRefundTests {

        @Test
        @DisplayName("Should retry refund successfully")
        void shouldRetryRefundSuccessfully() {
            // Given
            Refund refund = Refund.create(PAYMENT_ID, TRANSACTION_ID, MERCHANT_ID, RefundType.FULL, AMOUNT, "BRL", IDEMPOTENCY_KEY, REASON);
            refund.fail("ERROR_CODE", "Temporary error"); // Must be FAILED to retry
            given(repository.findById(REFUND_ID)).willReturn(Optional.of(refund));
            given(repository.save(any(Refund.class))).willReturn(refund);

            // When
            Refund result = refundDomainService.retryRefund(REFUND_ID);

            // Then
            assertThat(result).isEqualTo(refund);
            verify(repository).save(refund);
        }
    }

    @Nested
    @DisplayName("Get Refund")
    class GetRefundTests {

        @Test
        @DisplayName("Should return refund when found")
        void shouldReturnRefundWhenFound() {
            // Given
            Refund refund = Refund.create(PAYMENT_ID, TRANSACTION_ID, MERCHANT_ID, RefundType.FULL, AMOUNT, "BRL", IDEMPOTENCY_KEY, REASON);
            given(repository.findById(REFUND_ID)).willReturn(Optional.of(refund));

            // When
            Optional<Refund> result = refundDomainService.getRefund(REFUND_ID);

            // Then
            assertThat(result).isPresent().contains(refund);
        }

        @Test
        @DisplayName("Should return refund with orThrow method")
        void shouldReturnRefundWithOrThrowMethod() {
            // Given
            Refund refund = Refund.create(PAYMENT_ID, TRANSACTION_ID, MERCHANT_ID, RefundType.FULL, AMOUNT, "BRL", IDEMPOTENCY_KEY, REASON);
            given(repository.findById(REFUND_ID)).willReturn(Optional.of(refund));

            // When
            Refund result = refundDomainService.getRefundOrThrow(REFUND_ID);

            // Then
            assertThat(result).isEqualTo(refund);
        }

        @Test
        @DisplayName("Should throw exception when refund not found")
        void shouldThrowExceptionWhenRefundNotFound() {
            // Given
            given(repository.findById(REFUND_ID)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> refundDomainService.getRefundOrThrow(REFUND_ID))
                .isInstanceOf(RefundNotFoundException.class)
                .hasMessageContaining(REFUND_ID);
        }
    }

    @Nested
    @DisplayName("Get Refunds By Payment ID")
    class GetRefundsByPaymentIdTests {

        @Test
        @DisplayName("Should return list of refunds by payment ID")
        void shouldReturnListOfRefundsByPaymentId() {
            // Given
            Refund refund = Refund.create(PAYMENT_ID, TRANSACTION_ID, MERCHANT_ID, RefundType.FULL, AMOUNT, "BRL", IDEMPOTENCY_KEY, REASON);
            given(repository.findAllByPaymentId(PAYMENT_ID)).willReturn(List.of(refund));

            // When
            List<Refund> result = refundDomainService.getRefundsByPaymentId(PAYMENT_ID);

            // Then
            assertThat(result).hasSize(1);
            verify(repository).findAllByPaymentId(PAYMENT_ID);
        }
    }

    @Nested
    @DisplayName("Get Refunds By Merchant ID")
    class GetRefundsByMerchantIdTests {

        @Test
        @DisplayName("Should return list of refunds by merchant ID")
        void shouldReturnListOfRefundsByMerchantId() {
            // Given
            Refund refund = Refund.create(PAYMENT_ID, TRANSACTION_ID, MERCHANT_ID, RefundType.FULL, AMOUNT, "BRL", IDEMPOTENCY_KEY, REASON);
            given(repository.findByMerchantId(MERCHANT_ID)).willReturn(List.of(refund));

            // When
            List<Refund> result = refundDomainService.getRefundsByMerchantId(MERCHANT_ID);

            // Then
            assertThat(result).hasSize(1);
            verify(repository).findByMerchantId(MERCHANT_ID);
        }
    }

    @Nested
    @DisplayName("Get Refunds By Status")
    class GetRefundsByStatusTests {

        @Test
        @DisplayName("Should return list of refunds by status")
        void shouldReturnListOfRefundsByStatus() {
            // Given
            Refund refund = Refund.create(PAYMENT_ID, TRANSACTION_ID, MERCHANT_ID, RefundType.FULL, AMOUNT, "BRL", IDEMPOTENCY_KEY, REASON);
            given(repository.findByStatus(RefundStatus.PENDING)).willReturn(List.of(refund));

            // When
            List<Refund> result = refundDomainService.getRefundsByStatus(RefundStatus.PENDING);

            // Then
            assertThat(result).hasSize(1);
            verify(repository).findByStatus(RefundStatus.PENDING);
        }
    }

    @Nested
    @DisplayName("Update Gateway Refund ID")
    class UpdateGatewayRefundIdTests {

        @Test
        @DisplayName("Should update gateway refund ID successfully")
        void shouldUpdateGatewayRefundIdSuccessfully() {
            // Given
            String gatewayRefundId = "gateway_ref_123";
            Refund refund = Refund.create(PAYMENT_ID, TRANSACTION_ID, MERCHANT_ID, RefundType.FULL, AMOUNT, "BRL", IDEMPOTENCY_KEY, REASON);
            given(repository.findById(REFUND_ID)).willReturn(Optional.of(refund));
            given(repository.save(any(Refund.class))).willReturn(refund);

            // When
            Refund result = refundDomainService.updateGatewayRefundId(REFUND_ID, gatewayRefundId);

            // Then
            assertThat(result).isEqualTo(refund);
            verify(repository).save(refund);
        }
    }
}
