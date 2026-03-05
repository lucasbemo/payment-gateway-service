package com.payment.gateway.domain.merchant.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("MerchantStatus Enum Tests")
class MerchantStatusTest {

    @Nested
    @DisplayName("Status Transitions from PENDING")
    class PendingTransitions {

        @Test
        @DisplayName("PENDING can transition to ACTIVE")
        void pendingCanTransitionToActive() {
            assertThat(MerchantStatus.PENDING.canTransitionTo(MerchantStatus.ACTIVE)).isTrue();
        }

        @Test
        @DisplayName("PENDING can transition to SUSPENDED")
        void pendingCanTransitionToSuspended() {
            assertThat(MerchantStatus.PENDING.canTransitionTo(MerchantStatus.SUSPENDED)).isTrue();
        }

        @Test
        @DisplayName("PENDING cannot transition to CLOSED")
        void pendingCannotTransitionToClosed() {
            assertThat(MerchantStatus.PENDING.canTransitionTo(MerchantStatus.CLOSED)).isFalse();
        }
    }

    @Nested
    @DisplayName("Status Transitions from ACTIVE")
    class ActiveTransitions {

        @Test
        @DisplayName("ACTIVE can transition to SUSPENDED")
        void activeCanTransitionToSuspended() {
            assertThat(MerchantStatus.ACTIVE.canTransitionTo(MerchantStatus.SUSPENDED)).isTrue();
        }

        @Test
        @DisplayName("ACTIVE can transition to CLOSED")
        void activeCanTransitionToClosed() {
            assertThat(MerchantStatus.ACTIVE.canTransitionTo(MerchantStatus.CLOSED)).isTrue();
        }

        @Test
        @DisplayName("ACTIVE cannot transition to PENDING")
        void activeCannotTransitionToPending() {
            assertThat(MerchantStatus.ACTIVE.canTransitionTo(MerchantStatus.PENDING)).isFalse();
        }
    }

    @Nested
    @DisplayName("Status Transitions from SUSPENDED")
    class SuspendedTransitions {

        @Test
        @DisplayName("SUSPENDED can transition to ACTIVE")
        void suspendedCanTransitionToActive() {
            assertThat(MerchantStatus.SUSPENDED.canTransitionTo(MerchantStatus.ACTIVE)).isTrue();
        }

        @Test
        @DisplayName("SUSPENDED can transition to CLOSED")
        void suspendedCanTransitionToClosed() {
            assertThat(MerchantStatus.SUSPENDED.canTransitionTo(MerchantStatus.CLOSED)).isTrue();
        }

        @Test
        @DisplayName("SUSPENDED cannot transition to PENDING")
        void suspendedCannotTransitionToPending() {
            assertThat(MerchantStatus.SUSPENDED.canTransitionTo(MerchantStatus.PENDING)).isFalse();
        }
    }

    @Nested
    @DisplayName("Status Transitions from CLOSED")
    class ClosedTransitions {

        @Test
        @DisplayName("CLOSED cannot transition to any status (terminal)")
        void closedCannotTransitionToAnyStatus() {
            assertThat(MerchantStatus.CLOSED.canTransitionTo(MerchantStatus.PENDING)).isFalse();
            assertThat(MerchantStatus.CLOSED.canTransitionTo(MerchantStatus.ACTIVE)).isFalse();
            assertThat(MerchantStatus.CLOSED.canTransitionTo(MerchantStatus.SUSPENDED)).isFalse();
        }
    }

    @Nested
    @DisplayName("Can Process Payments Tests")
    class CanProcessPaymentsTests {

        @Test
        @DisplayName("PENDING cannot process payments")
        void pendingCannotProcessPayments() {
            assertThat(MerchantStatus.PENDING.canProcessPayments()).isFalse();
        }

        @Test
        @DisplayName("ACTIVE can process payments")
        void activeCanProcessPayments() {
            assertThat(MerchantStatus.ACTIVE.canProcessPayments()).isTrue();
        }

        @Test
        @DisplayName("SUSPENDED cannot process payments")
        void suspendedCannotProcessPayments() {
            assertThat(MerchantStatus.SUSPENDED.canProcessPayments()).isFalse();
        }

        @Test
        @DisplayName("CLOSED cannot process payments")
        void closedCannotProcessPayments() {
            assertThat(MerchantStatus.CLOSED.canProcessPayments()).isFalse();
        }
    }

    @Nested
    @DisplayName("Parse From String")
    class ParseFromStringTests {

        @Test
        @DisplayName("Should parse uppercase status")
        void shouldParseUppercaseStatus() {
            assertThat(MerchantStatus.fromString("PENDING")).isEqualTo(MerchantStatus.PENDING);
            assertThat(MerchantStatus.fromString("ACTIVE")).isEqualTo(MerchantStatus.ACTIVE);
            assertThat(MerchantStatus.fromString("SUSPENDED")).isEqualTo(MerchantStatus.SUSPENDED);
            assertThat(MerchantStatus.fromString("CLOSED")).isEqualTo(MerchantStatus.CLOSED);
        }

        @Test
        @DisplayName("Should parse lowercase status")
        void shouldParseLowercaseStatus() {
            assertThat(MerchantStatus.fromString("pending")).isEqualTo(MerchantStatus.PENDING);
            assertThat(MerchantStatus.fromString("active")).isEqualTo(MerchantStatus.ACTIVE);
            assertThat(MerchantStatus.fromString("suspended")).isEqualTo(MerchantStatus.SUSPENDED);
            assertThat(MerchantStatus.fromString("closed")).isEqualTo(MerchantStatus.CLOSED);
        }

        @Test
        @DisplayName("Should parse mixed case status")
        void shouldParseMixedCaseStatus() {
            assertThat(MerchantStatus.fromString("Pending")).isEqualTo(MerchantStatus.PENDING);
            assertThat(MerchantStatus.fromString("Active")).isEqualTo(MerchantStatus.ACTIVE);
        }

        @Test
        @DisplayName("Should throw exception for invalid status")
        void shouldThrowExceptionForInvalidStatus() {
            assertThatThrownBy(() -> MerchantStatus.fromString("INVALID"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid merchant status: INVALID");
        }

        @Test
        @DisplayName("Should throw exception for null status")
        void shouldThrowExceptionForNullStatus() {
            assertThatThrownBy(() -> MerchantStatus.fromString(null))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
