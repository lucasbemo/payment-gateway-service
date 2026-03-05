package com.payment.gateway.domain.refund.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("RefundType Enum Tests")
class RefundTypeTest {

    @Nested
    @DisplayName("Enum Values Exist")
    class EnumValuesExistTests {

        @Test
        @DisplayName("FULL type exists")
        void fullTypeExists() {
            assertThat(RefundType.FULL).isNotNull();
        }

        @Test
        @DisplayName("PARTIAL type exists")
        void partialTypeExists() {
            assertThat(RefundType.PARTIAL).isNotNull();
        }

        @Test
        @DisplayName("MULTIPLE type exists")
        void multipleTypeExists() {
            assertThat(RefundType.MULTIPLE).isNotNull();
        }

        @Test
        @DisplayName("CHARGEBACK type exists")
        void chargebackTypeExists() {
            assertThat(RefundType.CHARGEBACK).isNotNull();
        }

        @Test
        @DisplayName("CANCELLATION type exists")
        void cancellationTypeExists() {
            assertThat(RefundType.CANCELLATION).isNotNull();
        }
    }

    @Nested
    @DisplayName("Value Of Tests")
    class ValueOfTests {

        @Test
        @DisplayName("Should return correct type from valueOf")
        void shouldReturnCorrectTypeFromValueOf() {
            assertThat(RefundType.valueOf("FULL")).isEqualTo(RefundType.FULL);
            assertThat(RefundType.valueOf("PARTIAL")).isEqualTo(RefundType.PARTIAL);
            assertThat(RefundType.valueOf("MULTIPLE")).isEqualTo(RefundType.MULTIPLE);
            assertThat(RefundType.valueOf("CHARGEBACK")).isEqualTo(RefundType.CHARGEBACK);
            assertThat(RefundType.valueOf("CANCELLATION")).isEqualTo(RefundType.CANCELLATION);
        }

        @Test
        @DisplayName("Should throw exception for invalid type")
        void shouldThrowExceptionForInvalidType() {
            assertThatThrownBy(() -> RefundType.valueOf("INVALID"))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Values Array Tests")
    class ValuesArrayTests {

        @Test
        @DisplayName("Should return all refund types")
        void shouldReturnAllRefundTypes() {
            RefundType[] values = RefundType.values();
            assertThat(values).hasSize(5);
            assertThat(values).containsExactlyInAnyOrder(
                RefundType.FULL,
                RefundType.PARTIAL,
                RefundType.MULTIPLE,
                RefundType.CHARGEBACK,
                RefundType.CANCELLATION
            );
        }
    }
}
