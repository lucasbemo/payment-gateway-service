package com.payment.gateway.domain.customer.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("PaymentMethodStatus Enum Tests")
class PaymentMethodStatusTest {

    @Nested
    @DisplayName("Enum Values Exist")
    class EnumValuesExistTests {

        @Test
        @DisplayName("ACTIVE status exists")
        void activeExists() {
            assertThat(PaymentMethodStatus.ACTIVE).isNotNull();
        }

        @Test
        @DisplayName("INACTIVE status exists")
        void inactiveExists() {
            assertThat(PaymentMethodStatus.INACTIVE).isNotNull();
        }

        @Test
        @DisplayName("EXPIRED status exists")
        void expiredExists() {
            assertThat(PaymentMethodStatus.EXPIRED).isNotNull();
        }

        @Test
        @DisplayName("REVOKED status exists")
        void revokedExists() {
            assertThat(PaymentMethodStatus.REVOKED).isNotNull();
        }

        @Test
        @DisplayName("PENDING_VERIFICATION status exists")
        void pendingVerificationExists() {
            assertThat(PaymentMethodStatus.PENDING_VERIFICATION).isNotNull();
        }

        @Test
        @DisplayName("VERIFIED status exists")
        void verifiedExists() {
            assertThat(PaymentMethodStatus.VERIFIED).isNotNull();
        }

        @Test
        @DisplayName("FAILED_VERIFICATION status exists")
        void failedVerificationExists() {
            assertThat(PaymentMethodStatus.FAILED_VERIFICATION).isNotNull();
        }
    }

    @Nested
    @DisplayName("Value Of Tests")
    class ValueOfTests {

        @Test
        @DisplayName("Should return correct status from valueOf")
        void shouldReturnCorrectStatusFromValueOf() {
            assertThat(PaymentMethodStatus.valueOf("ACTIVE")).isEqualTo(PaymentMethodStatus.ACTIVE);
            assertThat(PaymentMethodStatus.valueOf("INACTIVE")).isEqualTo(PaymentMethodStatus.INACTIVE);
            assertThat(PaymentMethodStatus.valueOf("EXPIRED")).isEqualTo(PaymentMethodStatus.EXPIRED);
            assertThat(PaymentMethodStatus.valueOf("VERIFIED")).isEqualTo(PaymentMethodStatus.VERIFIED);
            assertThat(PaymentMethodStatus.valueOf("FAILED_VERIFICATION")).isEqualTo(PaymentMethodStatus.FAILED_VERIFICATION);
        }

        @Test
        @DisplayName("Should throw exception for invalid status")
        void shouldThrowExceptionForInvalidStatus() {
            assertThatThrownBy(() -> PaymentMethodStatus.valueOf("INVALID"))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Values Array Tests")
    class ValuesArrayTests {

        @Test
        @DisplayName("Should return all payment method statuses")
        void shouldReturnAllPaymentMethodStatuses() {
            PaymentMethodStatus[] values = PaymentMethodStatus.values();
            assertThat(values).hasSize(7);
            assertThat(values).containsExactlyInAnyOrder(
                PaymentMethodStatus.ACTIVE,
                PaymentMethodStatus.INACTIVE,
                PaymentMethodStatus.EXPIRED,
                PaymentMethodStatus.REVOKED,
                PaymentMethodStatus.PENDING_VERIFICATION,
                PaymentMethodStatus.VERIFIED,
                PaymentMethodStatus.FAILED_VERIFICATION
            );
        }
    }
}
