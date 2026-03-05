package com.payment.gateway.domain.outbox.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("EventStatus Enum Tests")
class EventStatusTest {

    @Nested
    @DisplayName("Enum Values Exist")
    class EnumValuesExistTests {

        @Test
        @DisplayName("PENDING status exists")
        void pendingExists() {
            assertThat(EventStatus.PENDING).isNotNull();
        }

        @Test
        @DisplayName("PROCESSING status exists")
        void processingExists() {
            assertThat(EventStatus.PROCESSING).isNotNull();
        }

        @Test
        @DisplayName("PUBLISHED status exists")
        void publishedExists() {
            assertThat(EventStatus.PUBLISHED).isNotNull();
        }

        @Test
        @DisplayName("FAILED status exists")
        void failedExists() {
            assertThat(EventStatus.FAILED).isNotNull();
        }

        @Test
        @DisplayName("RETRYING status exists")
        void retryingExists() {
            assertThat(EventStatus.RETRYING).isNotNull();
        }
    }

    @Nested
    @DisplayName("Value Of Tests")
    class ValueOfTests {

        @Test
        @DisplayName("Should return correct status from valueOf")
        void shouldReturnCorrectStatusFromValueOf() {
            assertThat(EventStatus.valueOf("PENDING")).isEqualTo(EventStatus.PENDING);
            assertThat(EventStatus.valueOf("PROCESSING")).isEqualTo(EventStatus.PROCESSING);
            assertThat(EventStatus.valueOf("PUBLISHED")).isEqualTo(EventStatus.PUBLISHED);
            assertThat(EventStatus.valueOf("FAILED")).isEqualTo(EventStatus.FAILED);
            assertThat(EventStatus.valueOf("RETRYING")).isEqualTo(EventStatus.RETRYING);
        }

        @Test
        @DisplayName("Should throw exception for invalid status")
        void shouldThrowExceptionForInvalidStatus() {
            assertThatThrownBy(() -> EventStatus.valueOf("INVALID"))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Values Array Tests")
    class ValuesArrayTests {

        @Test
        @DisplayName("Should return all event statuses")
        void shouldReturnAllEventStatuses() {
            EventStatus[] values = EventStatus.values();
            assertThat(values).hasSize(5);
            assertThat(values).containsExactlyInAnyOrder(
                EventStatus.PENDING,
                EventStatus.PROCESSING,
                EventStatus.PUBLISHED,
                EventStatus.FAILED,
                EventStatus.RETRYING
            );
        }
    }
}
