package com.payment.gateway.domain.reconciliation.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("DiscrepancyStatus Enum Tests")
class DiscrepancyStatusTest {

    @Nested
    @DisplayName("Enum Values Exist")
    class EnumValuesExistTests {

        @Test
        @DisplayName("OPEN status exists")
        void openExists() {
            assertThat(DiscrepancyStatus.OPEN).isNotNull();
        }

        @Test
        @DisplayName("UNDER_REVIEW status exists")
        void underReviewExists() {
            assertThat(DiscrepancyStatus.UNDER_REVIEW).isNotNull();
        }

        @Test
        @DisplayName("RESOLVED status exists")
        void resolvedExists() {
            assertThat(DiscrepancyStatus.RESOLVED).isNotNull();
        }

        @Test
        @DisplayName("ESCALATED status exists")
        void escalatedExists() {
            assertThat(DiscrepancyStatus.ESCALATED).isNotNull();
        }

        @Test
        @DisplayName("CLOSED status exists")
        void closedExists() {
            assertThat(DiscrepancyStatus.CLOSED).isNotNull();
        }
    }

    @Nested
    @DisplayName("Value Of Tests")
    class ValueOfTests {

        @Test
        @DisplayName("Should return correct status from valueOf")
        void shouldReturnCorrectStatusFromValueOf() {
            assertThat(DiscrepancyStatus.valueOf("OPEN")).isEqualTo(DiscrepancyStatus.OPEN);
            assertThat(DiscrepancyStatus.valueOf("UNDER_REVIEW")).isEqualTo(DiscrepancyStatus.UNDER_REVIEW);
            assertThat(DiscrepancyStatus.valueOf("RESOLVED")).isEqualTo(DiscrepancyStatus.RESOLVED);
            assertThat(DiscrepancyStatus.valueOf("ESCALATED")).isEqualTo(DiscrepancyStatus.ESCALATED);
            assertThat(DiscrepancyStatus.valueOf("CLOSED")).isEqualTo(DiscrepancyStatus.CLOSED);
        }

        @Test
        @DisplayName("Should throw exception for invalid status")
        void shouldThrowExceptionForInvalidStatus() {
            assertThatThrownBy(() -> DiscrepancyStatus.valueOf("INVALID"))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Values Array Tests")
    class ValuesArrayTests {

        @Test
        @DisplayName("Should return all discrepancy statuses")
        void shouldReturnAllDiscrepancyStatuses() {
            DiscrepancyStatus[] values = DiscrepancyStatus.values();
            assertThat(values).hasSize(5);
            assertThat(values).containsExactlyInAnyOrder(
                DiscrepancyStatus.OPEN,
                DiscrepancyStatus.UNDER_REVIEW,
                DiscrepancyStatus.RESOLVED,
                DiscrepancyStatus.ESCALATED,
                DiscrepancyStatus.CLOSED
            );
        }
    }
}
