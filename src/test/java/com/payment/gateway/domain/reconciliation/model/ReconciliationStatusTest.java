package com.payment.gateway.domain.reconciliation.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ReconciliationStatus Enum Tests")
class ReconciliationStatusTest {

    @Nested
    @DisplayName("Enum Values Exist")
    class EnumValuesExistTests {

        @Test
        @DisplayName("PENDING status exists")
        void pendingExists() {
            assertThat(ReconciliationStatus.PENDING).isNotNull();
        }

        @Test
        @DisplayName("PROCESSING status exists")
        void processingExists() {
            assertThat(ReconciliationStatus.PROCESSING).isNotNull();
        }

        @Test
        @DisplayName("RECONCILING status exists")
        void reconcilingExists() {
            assertThat(ReconciliationStatus.RECONCILING).isNotNull();
        }

        @Test
        @DisplayName("COMPLETED status exists")
        void completedExists() {
            assertThat(ReconciliationStatus.COMPLETED).isNotNull();
        }

        @Test
        @DisplayName("FAILED status exists")
        void failedExists() {
            assertThat(ReconciliationStatus.FAILED).isNotNull();
        }

        @Test
        @DisplayName("PARTIALLY_RECONCILED status exists")
        void partiallyReconciledExists() {
            assertThat(ReconciliationStatus.PARTIALLY_RECONCILED).isNotNull();
        }
    }

    @Nested
    @DisplayName("Value Of Tests")
    class ValueOfTests {

        @Test
        @DisplayName("Should return correct status from valueOf")
        void shouldReturnCorrectStatusFromValueOf() {
            assertThat(ReconciliationStatus.valueOf("PENDING")).isEqualTo(ReconciliationStatus.PENDING);
            assertThat(ReconciliationStatus.valueOf("PROCESSING")).isEqualTo(ReconciliationStatus.PROCESSING);
            assertThat(ReconciliationStatus.valueOf("RECONCILING")).isEqualTo(ReconciliationStatus.RECONCILING);
            assertThat(ReconciliationStatus.valueOf("COMPLETED")).isEqualTo(ReconciliationStatus.COMPLETED);
            assertThat(ReconciliationStatus.valueOf("FAILED")).isEqualTo(ReconciliationStatus.FAILED);
            assertThat(ReconciliationStatus.valueOf("PARTIALLY_RECONCILED")).isEqualTo(ReconciliationStatus.PARTIALLY_RECONCILED);
        }

        @Test
        @DisplayName("Should throw exception for invalid status")
        void shouldThrowExceptionForInvalidStatus() {
            assertThatThrownBy(() -> ReconciliationStatus.valueOf("INVALID"))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Values Array Tests")
    class ValuesArrayTests {

        @Test
        @DisplayName("Should return all reconciliation statuses")
        void shouldReturnAllReconciliationStatuses() {
            ReconciliationStatus[] values = ReconciliationStatus.values();
            assertThat(values).hasSize(6);
            assertThat(values).containsExactlyInAnyOrder(
                ReconciliationStatus.PENDING,
                ReconciliationStatus.PROCESSING,
                ReconciliationStatus.RECONCILING,
                ReconciliationStatus.COMPLETED,
                ReconciliationStatus.FAILED,
                ReconciliationStatus.PARTIALLY_RECONCILED
            );
        }
    }
}
