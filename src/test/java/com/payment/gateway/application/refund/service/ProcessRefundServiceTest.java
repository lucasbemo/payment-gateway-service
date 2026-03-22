package com.payment.gateway.application.refund.service;

import com.payment.gateway.application.refund.dto.RefundResponse;
import com.payment.gateway.application.refund.port.out.RefundPaymentQueryPort;
import com.payment.gateway.application.refund.port.out.RefundQueryPort;
import com.payment.gateway.commons.exception.BusinessException;
import com.payment.gateway.commons.model.Money;
import com.payment.gateway.domain.payment.model.Payment;
import com.payment.gateway.domain.payment.model.PaymentMetadata;
import com.payment.gateway.domain.payment.model.PaymentMethod;
import com.payment.gateway.domain.payment.model.PaymentStatus;
import com.payment.gateway.domain.refund.model.Refund;
import com.payment.gateway.domain.refund.model.RefundStatus;
import com.payment.gateway.domain.refund.model.RefundType;
import com.payment.gateway.domain.transaction.model.Transaction;
import com.payment.gateway.domain.transaction.model.TransactionStatus;
import com.payment.gateway.domain.transaction.model.TransactionType;
import com.payment.gateway.application.commons.port.out.MetricsPort;
import com.payment.gateway.application.commons.port.out.AuditPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@DisplayName("Process Refund Service Tests")
@ExtendWith(MockitoExtension.class)
class ProcessRefundServiceTest {

    @Mock
    private RefundQueryPort refundQueryPort;

    @Mock
    private RefundPaymentQueryPort refundPaymentQueryPort;

    @Mock
    private MetricsPort metricsPort;

    @Mock
    private AuditPort auditPort;

    private ProcessRefundService processRefundService;

    @BeforeEach
    void setUp() {
        processRefundService = new ProcessRefundService(refundQueryPort, refundPaymentQueryPort, metricsPort, auditPort);
    }

    @Nested
    @DisplayName("Successful Refund Processing")
    class SuccessfulRefundProcessingTests {

        @Test
        @DisplayName("Should process full refund successfully")
        void shouldProcessFullRefundSuccessfully() {
            // Given
            String paymentId = "pay_123";
            String merchantId = "merchant_123";
            String refundIdempotencyKey = "refund_idem_456";
            String refundId = "refund_789";
            String transactionId = "txn_123";

            Payment payment = createPayment(paymentId, merchantId);
            Transaction transaction = createTransaction(transactionId, paymentId);

            given(refundQueryPort.existsByIdempotencyKey(refundIdempotencyKey)).willReturn(false);
            given(refundPaymentQueryPort.findPaymentById(paymentId)).willReturn(Optional.of(payment));
            given(refundPaymentQueryPort.findLatestTransactionByPaymentId(paymentId)).willReturn(Optional.of(transaction));
            given(refundQueryPort.saveRefund(any(Refund.class))).willAnswer(invocation -> {
                Refund refund = invocation.getArgument(0);
                setId(refund, refundId);
                return refund;
            });

            // When
            RefundResponse response = processRefundService.processRefund(paymentId, merchantId, null, refundIdempotencyKey, "Customer request");

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(refundId);
            assertThat(response.getStatus()).isEqualTo(RefundStatus.COMPLETED.name());
            assertThat(response.getType()).isEqualTo(RefundType.FULL.name());

            then(refundQueryPort).should(times(2)).saveRefund(any(Refund.class));
        }

        @Test
        @DisplayName("Should process partial refund successfully")
        void shouldProcessPartialRefundSuccessfully() {
            // Given
            String paymentId = "pay_123";
            String merchantId = "merchant_123";
            String refundIdempotencyKey = "refund_idem_456";
            String refundId = "refund_789";
            String transactionId = "txn_123";
            Long partialAmount = 5000L;

            Payment payment = createPayment(paymentId, merchantId);
            Transaction transaction = createTransaction(transactionId, paymentId);

            given(refundQueryPort.existsByIdempotencyKey(refundIdempotencyKey)).willReturn(false);
            given(refundPaymentQueryPort.findPaymentById(paymentId)).willReturn(Optional.of(payment));
            given(refundPaymentQueryPort.findLatestTransactionByPaymentId(paymentId)).willReturn(Optional.of(transaction));
            given(refundQueryPort.saveRefund(any(Refund.class))).willAnswer(invocation -> {
                Refund refund = invocation.getArgument(0);
                setId(refund, refundId);
                return refund;
            });

            // When
            RefundResponse response = processRefundService.processRefund(paymentId, merchantId, partialAmount, refundIdempotencyKey, "Partial refund");

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(refundId);
            assertThat(response.getAmount()).isEqualTo(partialAmount);
            assertThat(response.getType()).isEqualTo(RefundType.PARTIAL.name());

            then(refundQueryPort).should(times(2)).saveRefund(any(Refund.class));
        }

        @Test
        @DisplayName("Should return existing refund for duplicate idempotency key")
        void shouldReturnExistingRefundForDuplicateIdempotencyKey() {
            // Given
            String paymentId = "pay_123";
            String merchantId = "merchant_123";
            String refundIdempotencyKey = "refund_idem_456";
            String existingRefundId = "refund_existing";

            Refund existingRefund = createRefund(existingRefundId, paymentId, merchantId, refundIdempotencyKey);

            given(refundQueryPort.existsByIdempotencyKey(refundIdempotencyKey)).willReturn(true);
            given(refundQueryPort.findByIdempotencyKey(refundIdempotencyKey)).willReturn(Optional.of(existingRefund));

            // When
            RefundResponse response = processRefundService.processRefund(paymentId, merchantId, null, refundIdempotencyKey, "Customer request");

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(existingRefundId);

            then(refundPaymentQueryPort).shouldHaveNoInteractions();
            then(refundQueryPort).shouldHaveNoMoreInteractions();
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should throw exception when payment not found")
        void shouldThrowExceptionWhenPaymentNotFound() {
            // Given
            String paymentId = "invalid_payment";
            String merchantId = "merchant_123";
            String refundIdempotencyKey = "refund_idem_456";

            given(refundQueryPort.existsByIdempotencyKey(refundIdempotencyKey)).willReturn(false);
            given(refundPaymentQueryPort.findPaymentById(paymentId)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> processRefundService.processRefund(paymentId, merchantId, null, refundIdempotencyKey, "Reason"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Payment not found");
        }

        @Test
        @DisplayName("Should throw exception when merchant doesn't own payment")
        void shouldThrowExceptionWhenMerchantDoesntOwnPayment() {
            // Given
            String paymentId = "pay_123";
            String merchantId = "wrong_merchant";
            String refundIdempotencyKey = "refund_idem_456";

            Payment payment = createPayment(paymentId, "different_merchant");

            given(refundQueryPort.existsByIdempotencyKey(refundIdempotencyKey)).willReturn(false);
            given(refundPaymentQueryPort.findPaymentById(paymentId)).willReturn(Optional.of(payment));

            // When & Then
            assertThatThrownBy(() -> processRefundService.processRefund(paymentId, merchantId, null, refundIdempotencyKey, "Reason"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Payment does not belong to merchant");
        }

        @Test
        @DisplayName("Should throw exception when transaction not found")
        void shouldThrowExceptionWhenTransactionNotFound() {
            // Given
            String paymentId = "pay_123";
            String merchantId = "merchant_123";
            String refundIdempotencyKey = "refund_idem_456";

            Payment payment = createPayment(paymentId, merchantId);

            given(refundQueryPort.existsByIdempotencyKey(refundIdempotencyKey)).willReturn(false);
            given(refundPaymentQueryPort.findPaymentById(paymentId)).willReturn(Optional.of(payment));
            given(refundPaymentQueryPort.findLatestTransactionByPaymentId(paymentId)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> processRefundService.processRefund(paymentId, merchantId, null, refundIdempotencyKey, "Reason"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("No transaction found for payment");
        }
    }

    // Helper methods

    private Payment createPayment(String id, String merchantId) {
        Payment payment = Payment.create(
                merchantId,
                Money.of(10000L, Currency.getInstance("USD")),
                "USD",
                PaymentMethod.CREDIT_CARD,
                "idem_key_" + System.currentTimeMillis(),
                "Test payment",
                PaymentMetadata.empty(),
                List.of(),
                null
        );
        setId(payment, id);
        return payment;
    }

    private Transaction createTransaction(String id, String paymentId) {
        Transaction transaction = Transaction.create(
                paymentId,
                "merchant_123",
                TransactionType.PAYMENT,
                Money.of(10000L, Currency.getInstance("USD")),
                "USD"
        );
        setId(transaction, id);
        setStatus(transaction, TransactionStatus.CAPTURED);
        return transaction;
    }

    private Refund createRefund(String id, String paymentId, String merchantId, String refundIdempotencyKey) {
        Refund refund = Refund.create(
                paymentId,
                "txn_123",
                merchantId,
                RefundType.FULL,
                Money.of(10000L, Currency.getInstance("USD")),
                "USD",
                refundIdempotencyKey,
                "Test refund"
        );
        setId(refund, id);
        return refund;
    }

    private void setId(Object obj, String id) {
        try {
            Field idField = obj.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(obj, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set id field", e);
        }
    }

    private void setStatus(Object obj, Enum status) {
        try {
            Field statusField = obj.getClass().getDeclaredField("status");
            statusField.setAccessible(true);
            statusField.set(obj, status);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set status field", e);
        }
    }
}
