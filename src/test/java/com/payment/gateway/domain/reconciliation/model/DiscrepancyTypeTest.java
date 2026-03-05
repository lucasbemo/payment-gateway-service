package com.payment.gateway.domain.reconciliation.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("DiscrepancyType Enum Tests")
class DiscrepancyTypeTest {

    @Nested
    @DisplayName("Enum Values Exist")
    class EnumValuesExistTests {

        @Test
        @DisplayName("MISSING_IN_GATEWAY type exists")
        void missingInGatewayExists() {
            assertThat(DiscrepancyType.MISSING_IN_GATEWAY).isNotNull();
        }

        @Test
        @DisplayName("MISSING_IN_SYSTEM type exists")
        void missingInSystemExists() {
            assertThat(DiscrepancyType.MISSING_IN_SYSTEM).isNotNull();
        }

        @Test
        @DisplayName("AMOUNT_MISMATCH type exists")
        void amountMismatchExists() {
            assertThat(DiscrepancyType.AMOUNT_MISMATCH).isNotNull();
        }

        @Test
        @DisplayName("STATUS_MISMATCH type exists")
        void statusMismatchExists() {
            assertThat(DiscrepancyType.STATUS_MISMATCH).isNotNull();
        }

        @Test
        @DisplayName("CURRENCY_MISMATCH type exists")
        void currencyMismatchExists() {
            assertThat(DiscrepancyType.CURRENCY_MISMATCH).isNotNull();
        }

        @Test
        @DisplayName("FEE_MISMATCH type exists")
        void feeMismatchExists() {
            assertThat(DiscrepancyType.FEE_MISMATCH).isNotNull();
        }

        @Test
        @DisplayName("TIMING_DIFFERENCE type exists")
        void timingDifferenceExists() {
            assertThat(DiscrepancyType.TIMING_DIFFERENCE).isNotNull();
        }

        @Test
        @DisplayName("DUPLICATE_TRANSACTION type exists")
        void duplicateTransactionExists() {
            assertThat(DiscrepancyType.DUPLICATE_TRANSACTION).isNotNull();
        }
    }

    @Nested
    @DisplayName("Value Of Tests")
    class ValueOfTests {

        @Test
        @DisplayName("Should return correct type from valueOf")
        void shouldReturnCorrectTypeFromValueOf() {
            assertThat(DiscrepancyType.valueOf("MISSING_IN_GATEWAY")).isEqualTo(DiscrepancyType.MISSING_IN_GATEWAY);
            assertThat(DiscrepancyType.valueOf("AMOUNT_MISMATCH")).isEqualTo(DiscrepancyType.AMOUNT_MISMATCH);
            assertThat(DiscrepancyType.valueOf("STATUS_MISMATCH")).isEqualTo(DiscrepancyType.STATUS_MISMATCH);
            assertThat(DiscrepancyType.valueOf("TIMING_DIFFERENCE")).isEqualTo(DiscrepancyType.TIMING_DIFFERENCE);
        }

        @Test
        @DisplayName("Should throw exception for invalid type")
        void shouldThrowExceptionForInvalidType() {
            assertThatThrownBy(() -> DiscrepancyType.valueOf("INVALID"))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Values Array Tests")
    class ValuesArrayTests {

        @Test
        @DisplayName("Should return all discrepancy types")
        void shouldReturnAllDiscrepancyTypes() {
            DiscrepancyType[] values = DiscrepancyType.values();
            assertThat(values).hasSize(8);
            assertThat(values).containsExactlyInAnyOrder(
                DiscrepancyType.MISSING_IN_GATEWAY,
                DiscrepancyType.MISSING_IN_SYSTEM,
                DiscrepancyType.AMOUNT_MISMATCH,
                DiscrepancyType.STATUS_MISMATCH,
                DiscrepancyType.CURRENCY_MISMATCH,
                DiscrepancyType.FEE_MISMATCH,
                DiscrepancyType.TIMING_DIFFERENCE,
                DiscrepancyType.DUPLICATE_TRANSACTION
            );
        }
    }
}
