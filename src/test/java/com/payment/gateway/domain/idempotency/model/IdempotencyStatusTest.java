package com.payment.gateway.domain.idempotency.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("IdempotencyStatus Enum Tests")
class IdempotencyStatusTest {

    @Nested
    @DisplayName("Enum Values Exist")
    class EnumValuesExistTests {

        @Test
        @DisplayName("PENDING status exists")
        void pendingExists() {
            assertThat(IdempotencyStatus.PENDING).isNotNull();
        }

        @Test
        @DisplayName("PROCESSING status exists")
        void processingExists() {
            assertThat(IdempotencyStatus.PROCESSING).isNotNull();
        }

        @Test
        @DisplayName("COMPLETED status exists")
        void completedExists() {
            assertThat(IdempotencyStatus.COMPLETED).isNotNull();
        }

        @Test
        @DisplayName("FAILED status exists")
        void failedExists() {
            assertThat(IdempotencyStatus.FAILED).isNotNull();
        }

        @Test
        @DisplayName("EXPIRED status exists")
        void expiredExists() {
            assertThat(IdempotencyStatus.EXPIRED).isNotNull();
        }
    }

    @Nested
    @DisplayName("Value Of Tests")
    class ValueOfTests {

        @Test
        @DisplayName("Should return correct status from valueOf")
        void shouldReturnCorrectStatusFromValueOf() {
            assertThat(IdempotencyStatus.valueOf("PENDING")).isEqualTo(IdempotencyStatus.PENDING);
            assertThat(IdempotencyStatus.valueOf("PROCESSING")).isEqualTo(IdempotencyStatus.PROCESSING);
            assertThat(IdempotencyStatus.valueOf("COMPLETED")).isEqualTo(IdempotencyStatus.COMPLETED);
            assertThat(IdempotencyStatus.valueOf("FAILED")).isEqualTo(IdempotencyStatus.FAILED);
            assertThat(IdempotencyStatus.valueOf("EXPIRED")).isEqualTo(IdempotencyStatus.EXPIRED);
        }

        @Test
        @DisplayName("Should throw exception for invalid status")
        void shouldThrowExceptionForInvalidStatus() {
            assertThatThrownBy(() -> IdempotencyStatus.valueOf("INVALID"))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Values Array Tests")
    class ValuesArrayTests {

        @Test
        @DisplayName("Should return all idempotency statuses")
        void shouldReturnAllIdempotencyStatuses() {
            IdempotencyStatus[] values = IdempotencyStatus.values();
            assertThat(values).hasSize(5);
            assertThat(values).containsExactlyInAnyOrder(
                IdempotencyStatus.PENDING,
                IdempotencyStatus.PROCESSING,
                IdempotencyStatus.COMPLETED,
                IdempotencyStatus.FAILED,
                IdempotencyStatus.EXPIRED
            );
        }
    }
}
