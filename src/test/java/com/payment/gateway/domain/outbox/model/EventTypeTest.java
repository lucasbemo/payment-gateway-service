package com.payment.gateway.domain.outbox.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("EventType Enum Tests")
class EventTypeTest {

    @Nested
    @DisplayName("Enum Values Exist")
    class EnumValuesExistTests {

        @Test
        @DisplayName("PAYMENT_CREATED type exists")
        void paymentCreatedExists() {
            assertThat(EventType.PAYMENT_CREATED).isNotNull();
        }

        @Test
        @DisplayName("PAYMENT_COMPLETED type exists")
        void paymentCompletedExists() {
            assertThat(EventType.PAYMENT_COMPLETED).isNotNull();
        }

        @Test
        @DisplayName("PAYMENT_FAILED type exists")
        void paymentFailedExists() {
            assertThat(EventType.PAYMENT_FAILED).isNotNull();
        }

        @Test
        @DisplayName("PAYMENT_CANCELLED type exists")
        void paymentCancelledExists() {
            assertThat(EventType.PAYMENT_CANCELLED).isNotNull();
        }

        @Test
        @DisplayName("REFUND_PROCESSED type exists")
        void refundProcessedExists() {
            assertThat(EventType.REFUND_PROCESSED).isNotNull();
        }

        @Test
        @DisplayName("TRANSACTION_CREATED type exists")
        void transactionCreatedExists() {
            assertThat(EventType.TRANSACTION_CREATED).isNotNull();
        }

        @Test
        @DisplayName("TRANSACTION_COMPLETED type exists")
        void transactionCompletedExists() {
            assertThat(EventType.TRANSACTION_COMPLETED).isNotNull();
        }

        @Test
        @DisplayName("TRANSACTION_FAILED type exists")
        void transactionFailedExists() {
            assertThat(EventType.TRANSACTION_FAILED).isNotNull();
        }

        @Test
        @DisplayName("CUSTOMER_CREATED type exists")
        void customerCreatedExists() {
            assertThat(EventType.CUSTOMER_CREATED).isNotNull();
        }

        @Test
        @DisplayName("CUSTOMER_UPDATED type exists")
        void customerUpdatedExists() {
            assertThat(EventType.CUSTOMER_UPDATED).isNotNull();
        }

        @Test
        @DisplayName("MERCHANT_ACTIVATED type exists")
        void merchantActivatedExists() {
            assertThat(EventType.MERCHANT_ACTIVATED).isNotNull();
        }

        @Test
        @DisplayName("MERCHANT_SUSPENDED type exists")
        void merchantSuspendedExists() {
            assertThat(EventType.MERCHANT_SUSPENDED).isNotNull();
        }
    }

    @Nested
    @DisplayName("Value Of Tests")
    class ValueOfTests {

        @Test
        @DisplayName("Should return correct type from valueOf")
        void shouldReturnCorrectTypeFromValueOf() {
            assertThat(EventType.valueOf("PAYMENT_CREATED")).isEqualTo(EventType.PAYMENT_CREATED);
            assertThat(EventType.valueOf("PAYMENT_COMPLETED")).isEqualTo(EventType.PAYMENT_COMPLETED);
            assertThat(EventType.valueOf("REFUND_PROCESSED")).isEqualTo(EventType.REFUND_PROCESSED);
            assertThat(EventType.valueOf("TRANSACTION_CREATED")).isEqualTo(EventType.TRANSACTION_CREATED);
            assertThat(EventType.valueOf("CUSTOMER_CREATED")).isEqualTo(EventType.CUSTOMER_CREATED);
            assertThat(EventType.valueOf("MERCHANT_ACTIVATED")).isEqualTo(EventType.MERCHANT_ACTIVATED);
        }

        @Test
        @DisplayName("Should throw exception for invalid type")
        void shouldThrowExceptionForInvalidType() {
            assertThatThrownBy(() -> EventType.valueOf("INVALID"))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Values Array Tests")
    class ValuesArrayTests {

        @Test
        @DisplayName("Should return all event types")
        void shouldReturnAllEventTypes() {
            EventType[] values = EventType.values();
            assertThat(values).hasSize(12);
            assertThat(values).containsExactlyInAnyOrder(
                EventType.PAYMENT_CREATED,
                EventType.PAYMENT_COMPLETED,
                EventType.PAYMENT_FAILED,
                EventType.PAYMENT_CANCELLED,
                EventType.REFUND_PROCESSED,
                EventType.TRANSACTION_CREATED,
                EventType.TRANSACTION_COMPLETED,
                EventType.TRANSACTION_FAILED,
                EventType.CUSTOMER_CREATED,
                EventType.CUSTOMER_UPDATED,
                EventType.MERCHANT_ACTIVATED,
                EventType.MERCHANT_SUSPENDED
            );
        }
    }
}
