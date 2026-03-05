package com.payment.gateway.application.refund.service;

import com.payment.gateway.application.refund.dto.RefundResponse;
import com.payment.gateway.application.refund.port.out.RefundQueryPort;
import com.payment.gateway.commons.exception.BusinessException;
import com.payment.gateway.commons.model.Money;
import com.payment.gateway.domain.refund.model.Refund;
import com.payment.gateway.domain.refund.model.RefundStatus;
import com.payment.gateway.domain.refund.model.RefundType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Currency;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@DisplayName("Get Refund Service Tests")
@ExtendWith(MockitoExtension.class)
class GetRefundServiceTest {

    @Mock
    private RefundQueryPort refundQueryPort;

    private GetRefundService getRefundService;

    @BeforeEach
    void setUp() {
        getRefundService = new GetRefundService(refundQueryPort);
    }

    @Nested
    @DisplayName("Get Refund By ID")
    class GetRefundByIdTests {

        @Test
        @DisplayName("Should get refund successfully by ID")
        void shouldGetRefundSuccessfullyById() {
            // Given
            String refundId = "refund_abc123";
            String merchantId = "merchant-123";
            Refund refund = createRefund(refundId, merchantId);

            given(refundQueryPort.findById(refundId)).willReturn(Optional.of(refund));

            // When
            RefundResponse response = getRefundService.getRefundById(refundId, merchantId);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(refundId);
            assertThat(response.getMerchantId()).isEqualTo(merchantId);
            assertThat(response.getStatus()).isEqualTo(RefundStatus.COMPLETED.name());
            assertThat(response.getAmount()).isEqualTo(5000L);
            assertThat(response.getCurrency()).isEqualTo("USD");
            assertThat(response.getType()).isEqualTo(RefundType.FULL.name());

            then(refundQueryPort).should().findById(refundId);
        }

        @Test
        @DisplayName("Should throw exception when refund not found")
        void shouldThrowExceptionWhenRefundNotFound() {
            // Given
            String refundId = "invalid-refund";
            given(refundQueryPort.findById(refundId)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> getRefundService.getRefundById(refundId, "merchant-123"))
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
            assertThatThrownBy(() -> getRefundService.getRefundById(refundId, requestMerchantId))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Refund does not belong to merchant");
        }
    }

    // Helper methods

    private Refund createRefund(String id, String merchantId) {
        Refund refund = Refund.create(
                "payment-123",
                "transaction-456",
                merchantId,
                RefundType.FULL,
                Money.of(5000L, Currency.getInstance("USD")),
                "USD",
                "idem-key-refund",
                "Customer requested refund"
        );
        setId(refund, id);
        setStatus(refund, RefundStatus.COMPLETED);
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
