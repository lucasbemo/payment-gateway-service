package com.payment.gateway.domain.transaction.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("TransactionType Enum Tests")
class TransactionTypeTest {

    @Nested
    @DisplayName("Enum Values Exist")
    class EnumValuesExistTests {

        @Test
        @DisplayName("PAYMENT type exists")
        void paymentTypeExists() {
            assertThat(TransactionType.PAYMENT).isNotNull();
        }

        @Test
        @DisplayName("CAPTURE type exists")
        void captureTypeExists() {
            assertThat(TransactionType.CAPTURE).isNotNull();
        }

        @Test
        @DisplayName("AUTHORIZATION type exists")
        void authorizationTypeExists() {
            assertThat(TransactionType.AUTHORIZATION).isNotNull();
        }

        @Test
        @DisplayName("REFUND type exists")
        void refundTypeExists() {
            assertThat(TransactionType.REFUND).isNotNull();
        }

        @Test
        @DisplayName("PARTIAL_REFUND type exists")
        void partialRefundTypeExists() {
            assertThat(TransactionType.PARTIAL_REFUND).isNotNull();
        }

        @Test
        @DisplayName("REVERSAL type exists")
        void reversalTypeExists() {
            assertThat(TransactionType.REVERSAL).isNotNull();
        }

        @Test
        @DisplayName("CHARGEBACK type exists")
        void chargebackTypeExists() {
            assertThat(TransactionType.CHARGEBACK).isNotNull();
        }

        @Test
        @DisplayName("ADJUSTMENT type exists")
        void adjustmentTypeExists() {
            assertThat(TransactionType.ADJUSTMENT).isNotNull();
        }
    }

    @Nested
    @DisplayName("Value Of Tests")
    class ValueOfTests {

        @Test
        @DisplayName("Should return correct type from valueOf")
        void shouldReturnCorrectTypeFromValueOf() {
            assertThat(TransactionType.valueOf("PAYMENT")).isEqualTo(TransactionType.PAYMENT);
            assertThat(TransactionType.valueOf("CAPTURE")).isEqualTo(TransactionType.CAPTURE);
            assertThat(TransactionType.valueOf("AUTHORIZATION")).isEqualTo(TransactionType.AUTHORIZATION);
            assertThat(TransactionType.valueOf("REFUND")).isEqualTo(TransactionType.REFUND);
            assertThat(TransactionType.valueOf("PARTIAL_REFUND")).isEqualTo(TransactionType.PARTIAL_REFUND);
            assertThat(TransactionType.valueOf("REVERSAL")).isEqualTo(TransactionType.REVERSAL);
            assertThat(TransactionType.valueOf("CHARGEBACK")).isEqualTo(TransactionType.CHARGEBACK);
            assertThat(TransactionType.valueOf("ADJUSTMENT")).isEqualTo(TransactionType.ADJUSTMENT);
        }

        @Test
        @DisplayName("Should throw exception for invalid type")
        void shouldThrowExceptionForInvalidType() {
            assertThatThrownBy(() -> TransactionType.valueOf("INVALID"))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Values Array Tests")
    class ValuesArrayTests {

        @Test
        @DisplayName("Should return all transaction types")
        void shouldReturnAllTransactionTypes() {
            TransactionType[] values = TransactionType.values();
            assertThat(values).hasSize(8);
            assertThat(values).containsExactlyInAnyOrder(
                TransactionType.PAYMENT,
                TransactionType.CAPTURE,
                TransactionType.AUTHORIZATION,
                TransactionType.REFUND,
                TransactionType.PARTIAL_REFUND,
                TransactionType.REVERSAL,
                TransactionType.CHARGEBACK,
                TransactionType.ADJUSTMENT
            );
        }
    }
}
