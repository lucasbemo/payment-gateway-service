package com.payment.gateway.application.refund.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.payment.gateway.application.refund.dto.RefundResponse;
import com.payment.gateway.application.refund.port.out.RefundQueryPort;
import com.payment.gateway.commons.exception.BusinessException;
import com.payment.gateway.commons.model.Money;
import com.payment.gateway.domain.refund.model.Refund;
import com.payment.gateway.domain.refund.model.RefundStatus;
import com.payment.gateway.domain.refund.model.RefundType;
import java.lang.reflect.Field;
import java.util.Currency;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("Cancel Refund Service Tests")
@ExtendWith(MockitoExtension.class)
class CancelRefundServiceTest {

    @Mock
    private RefundQueryPort refundQueryPort;

    private CancelRefundService cancelRefundService;

    @BeforeEach
    void setUp() {
        cancelRefundService = new CancelRefundService(refundQueryPort);
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should throw exception when refund not found")
        void shouldThrowExceptionWhenRefundNotFound() {
            // Given
            String refundId = "invalid-refund";
            given(refundQueryPort.findById(refundId)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() ->
                            cancelRefundService.cancelRefund(refundId, "merchant-123", "Test cancellation reason"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Refund not found");
        }

        @Test
        @DisplayName("Should throw exception when merchant does not own refund")
        void shouldThrowExceptionWhenMerchantDoesNotOwnRefund() {
            // Given
            String refundId = "refund_abc123";
            String refundMerchantId = "merchant-123";
            String requestMerchantId = "merchant-456";
            Refund refund = createRefund(refundId, refundMerchantId);

            given(refundQueryPort.findById(refundId)).willReturn(Optional.of(refund));

            // When & Then
            assertThatThrownBy(() -> cancelRefundService.cancelRefund(refundId, requestMerchantId, "Test reason"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Refund does not belong to merchant");
        }

        @Test
        @DisplayName("Should throw exception when refund is in a terminal state")
        void shouldThrowExceptionWhenRefundIsTerminal() {
            // Given
            String refundId = "refund_terminal";
            String merchantId = "merchant-123";
            Refund refund = createRefund(refundId, merchantId);
            setStatus(refund, RefundStatus.COMPLETED); // terminal

            given(refundQueryPort.findById(refundId)).willReturn(Optional.of(refund));

            // When & Then
            assertThatThrownBy(() -> cancelRefundService.cancelRefund(refundId, merchantId, "Too late"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Cannot cancel refund in terminal state");
        }
    }

    @Nested
    @DisplayName("Successful Cancellation")
    class SuccessfulCancellationTests {

        @Test
        @DisplayName("Should cancel a pending refund and return the mapped response")
        void shouldCancelPendingRefundSuccessfully() {
            // Given
            String refundId = "refund_abc123";
            String merchantId = "merchant-123";
            String reason = "Customer changed their mind";
            Refund refund = createRefund(refundId, merchantId);

            given(refundQueryPort.findById(refundId)).willReturn(Optional.of(refund));
            given(refundQueryPort.saveRefund(any())).willAnswer(invocation -> invocation.getArgument(0));

            // When
            RefundResponse response = cancelRefundService.cancelRefund(refundId, merchantId, reason);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(refundId);
            assertThat(response.getMerchantId()).isEqualTo(merchantId);
            assertThat(response.getStatus()).isEqualTo(RefundStatus.CANCELLED.name());
            assertThat(response.getReason()).isEqualTo(reason);
            assertThat(refund.getStatus()).isEqualTo(RefundStatus.CANCELLED);
            then(refundQueryPort).should().saveRefund(refund);
        }
    }

    private Refund createRefund(String id, String merchantId) {
        Refund refund = Refund.create(
                "payment-123",
                "transaction-456",
                merchantId,
                RefundType.FULL,
                Money.of(5000L, Currency.getInstance("USD")),
                "USD",
                "idem-key-refund",
                "Customer requested refund");
        setId(refund, id);
        setStatus(refund, RefundStatus.PENDING);
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

    private void setStatus(Object obj, RefundStatus status) {
        try {
            Field statusField = obj.getClass().getDeclaredField("status");
            statusField.setAccessible(true);
            statusField.set(obj, status);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set status field", e);
        }
    }
}
