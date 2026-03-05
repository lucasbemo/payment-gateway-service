package com.payment.gateway.application.transaction.service;

import com.payment.gateway.application.transaction.dto.TransactionResponse;
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
import static org.mockito.BDDMockito.given;

@DisplayName("Get Transaction Service Tests")
@ExtendWith(MockitoExtension.class)
class GetTransactionServiceTest {

    @Mock
    private TransactionQueryPort transactionQueryPort;

    private GetTransactionService getTransactionService;

    @BeforeEach
    void setUp() {
        getTransactionService = new GetTransactionService(transactionQueryPort);
    }

    @Nested
    @DisplayName("Successful Transaction Retrieval")
    class SuccessfulRetrievalTests {

        @Test
        @DisplayName("Should get transaction by id successfully")
        void shouldGetTransactionByIdSuccessfully() {
            // Given
            String transactionId = "txn_123";
            String merchantId = "merchant_123";
            String paymentId = "pay_123";

            Transaction transaction = createTransaction(transactionId, paymentId, merchantId);

            given(transactionQueryPort.findByIdAndMerchantId(transactionId, merchantId))
                    .willReturn(Optional.of(transaction));

            // When
            TransactionResponse response = getTransactionService.getTransactionById(transactionId, merchantId);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(transactionId);
            assertThat(response.getPaymentId()).isEqualTo(paymentId);
            assertThat(response.getMerchantId()).isEqualTo(merchantId);
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should throw exception when transaction not found")
        void shouldThrowExceptionWhenTransactionNotFound() {
            // Given
            String transactionId = "invalid_txn";
            String merchantId = "merchant_123";

            given(transactionQueryPort.findByIdAndMerchantId(transactionId, merchantId))
                    .willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> getTransactionService.getTransactionById(transactionId, merchantId))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Transaction not found");
        }
    }

    private Transaction createTransaction(String id, String paymentId, String merchantId) {
        Transaction transaction = Transaction.create(
                paymentId,
                merchantId,
                TransactionType.PAYMENT,
                Money.of(10000L, Currency.getInstance("USD")),
                "USD"
        );
        setId(transaction, id);
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
}
