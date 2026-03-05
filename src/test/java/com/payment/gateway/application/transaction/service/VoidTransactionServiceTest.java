package com.payment.gateway.application.transaction.service;

import com.payment.gateway.application.transaction.dto.TransactionResponse;
import com.payment.gateway.application.transaction.port.out.TransactionCommandPort;
import com.payment.gateway.application.transaction.port.out.TransactionQueryPort;
import com.payment.gateway.commons.exception.BusinessException;
import com.payment.gateway.commons.model.Money;
import com.payment.gateway.domain.transaction.model.Transaction;
import com.payment.gateway.domain.transaction.model.TransactionStatus;
import com.payment.gateway.domain.transaction.model.TransactionType;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@DisplayName("Void Transaction Service Tests")
@ExtendWith(MockitoExtension.class)
class VoidTransactionServiceTest {

    @Mock
    private TransactionQueryPort transactionQueryPort;

    @Mock
    private TransactionCommandPort transactionCommandPort;

    private VoidTransactionService voidTransactionService;

    @BeforeEach
    void setUp() {
        voidTransactionService = new VoidTransactionService(transactionQueryPort, transactionCommandPort);
    }

    @Nested
    @DisplayName("Successful Transaction Void")
    class SuccessfulVoidTests {

        @Test
        @DisplayName("Should void transaction successfully")
        void shouldVoidTransactionSuccessfully() {
            // Given
            String transactionId = "txn_abc123";
            String merchantId = "merchant-123";
            Transaction transaction = createTransaction(transactionId, merchantId, TransactionStatus.AUTHORIZED);

            given(transactionQueryPort.findByIdAndMerchantId(transactionId, merchantId)).willReturn(Optional.of(transaction));
            given(transactionCommandPort.updateTransaction(any())).willAnswer(invocation -> invocation.getArgument(0));

            // When
            TransactionResponse response = voidTransactionService.voidTransaction(transactionId, merchantId);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(transactionId);
            assertThat(response.getStatus()).isEqualTo(TransactionStatus.REVERSED.name());

            then(transactionQueryPort).should().findByIdAndMerchantId(transactionId, merchantId);
            then(transactionCommandPort).should().updateTransaction(any());
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should throw exception when transaction not found")
        void shouldThrowExceptionWhenTransactionNotFound() {
            // Given
            String transactionId = "invalid-txn";
            given(transactionQueryPort.findByIdAndMerchantId(transactionId, "merchant-123")).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> voidTransactionService.voidTransaction(transactionId, "merchant-123"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Transaction not found");
        }

        @Test
        @DisplayName("Should throw exception when transaction cannot be voided")
        void shouldThrowExceptionWhenTransactionCannotBeVoided() {
            // Given
            String transactionId = "txn_abc123";
            String merchantId = "merchant-123";
            Transaction transaction = createTransaction(transactionId, merchantId, TransactionStatus.SETTLED);

            given(transactionQueryPort.findByIdAndMerchantId(transactionId, merchantId)).willReturn(Optional.of(transaction));

            // When & Then
            assertThatThrownBy(() -> voidTransactionService.voidTransaction(transactionId, merchantId))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Cannot void transaction in current state");
        }
    }

    private Transaction createTransaction(String id, String merchantId, TransactionStatus status) {
        Transaction transaction = Transaction.create(
                "payment-123",
                merchantId,
                TransactionType.PAYMENT,
                Money.of(10000L, Currency.getInstance("USD")),
                "USD"
        );
        setId(transaction, id);
        setStatus(transaction, status);
        return transaction;
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

    private void setStatus(Object obj, TransactionStatus status) {
        try {
            Field statusField = obj.getClass().getDeclaredField("status");
            statusField.setAccessible(true);
            statusField.set(obj, status);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set status field", e);
        }
    }
}
