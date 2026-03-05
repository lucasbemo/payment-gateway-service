package com.payment.gateway.domain.customer.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("CustomerStatus Enum Tests")
class CustomerStatusTest {

    @Nested
    @DisplayName("Enum Values Exist")
    class EnumValuesExistTests {

        @Test
        @DisplayName("ACTIVE status exists")
        void activeExists() {
            assertThat(CustomerStatus.ACTIVE).isNotNull();
        }

        @Test
        @DisplayName("INACTIVE status exists")
        void inactiveExists() {
            assertThat(CustomerStatus.INACTIVE).isNotNull();
        }

        @Test
        @DisplayName("SUSPENDED status exists")
        void suspendedExists() {
            assertThat(CustomerStatus.SUSPENDED).isNotNull();
        }

        @Test
        @DisplayName("BLOCKED status exists")
        void blockedExists() {
            assertThat(CustomerStatus.BLOCKED).isNotNull();
        }

        @Test
        @DisplayName("PENDING_VERIFICATION status exists")
        void pendingVerificationExists() {
            assertThat(CustomerStatus.PENDING_VERIFICATION).isNotNull();
        }

        @Test
        @DisplayName("VERIFIED status exists")
        void verifiedExists() {
            assertThat(CustomerStatus.VERIFIED).isNotNull();
        }
    }

    @Nested
    @DisplayName("Status Transitions")
    class StatusTransitionTests {

        @Test
        @DisplayName("ACTIVE can transition to INACTIVE")
        void activeCanTransitionToInactive() {
            assertThat(CustomerStatus.ACTIVE.canTransitionTo(CustomerStatus.INACTIVE)).isTrue();
        }

        @Test
        @DisplayName("ACTIVE can transition to SUSPENDED")
        void activeCanTransitionToSuspended() {
            assertThat(CustomerStatus.ACTIVE.canTransitionTo(CustomerStatus.SUSPENDED)).isTrue();
        }

        @Test
        @DisplayName("ACTIVE can transition to BLOCKED")
        void activeCanTransitionToBlocked() {
            assertThat(CustomerStatus.ACTIVE.canTransitionTo(CustomerStatus.BLOCKED)).isTrue();
        }

        @Test
        @DisplayName("INACTIVE can transition to ACTIVE")
        void inactiveCanTransitionToActive() {
            assertThat(CustomerStatus.INACTIVE.canTransitionTo(CustomerStatus.ACTIVE)).isTrue();
        }

        @Test
        @DisplayName("INACTIVE can transition to SUSPENDED")
        void inactiveCanTransitionToSuspended() {
            assertThat(CustomerStatus.INACTIVE.canTransitionTo(CustomerStatus.SUSPENDED)).isTrue();
        }

        @Test
        @DisplayName("SUSPENDED can transition to ACTIVE")
        void suspendedCanTransitionToActive() {
            assertThat(CustomerStatus.SUSPENDED.canTransitionTo(CustomerStatus.ACTIVE)).isTrue();
        }

        @Test
        @DisplayName("SUSPENDED can transition to INACTIVE")
        void suspendedCanTransitionToInactive() {
            assertThat(CustomerStatus.SUSPENDED.canTransitionTo(CustomerStatus.INACTIVE)).isTrue();
        }

        @Test
        @DisplayName("SUSPENDED can transition to BLOCKED")
        void suspendedCanTransitionToBlocked() {
            assertThat(CustomerStatus.SUSPENDED.canTransitionTo(CustomerStatus.BLOCKED)).isTrue();
        }

        @Test
        @DisplayName("BLOCKED can transition to SUSPENDED")
        void blockedCanTransitionToSuspended() {
            assertThat(CustomerStatus.BLOCKED.canTransitionTo(CustomerStatus.SUSPENDED)).isTrue();
        }

        @Test
        @DisplayName("PENDING_VERIFICATION can transition to VERIFIED")
        void pendingVerificationCanTransitionToVerified() {
            assertThat(CustomerStatus.PENDING_VERIFICATION.canTransitionTo(CustomerStatus.VERIFIED)).isTrue();
        }

        @Test
        @DisplayName("PENDING_VERIFICATION can transition to ACTIVE")
        void pendingVerificationCanTransitionToActive() {
            assertThat(CustomerStatus.PENDING_VERIFICATION.canTransitionTo(CustomerStatus.ACTIVE)).isTrue();
        }

        @Test
        @DisplayName("PENDING_VERIFICATION can transition to INACTIVE")
        void pendingVerificationCanTransitionToInactive() {
            assertThat(CustomerStatus.PENDING_VERIFICATION.canTransitionTo(CustomerStatus.INACTIVE)).isTrue();
        }

        @Test
        @DisplayName("VERIFIED can transition to ACTIVE")
        void verifiedCanTransitionToActive() {
            assertThat(CustomerStatus.VERIFIED.canTransitionTo(CustomerStatus.ACTIVE)).isTrue();
        }

        @Test
        @DisplayName("VERIFIED can transition to INACTIVE")
        void verifiedCanTransitionToInactive() {
            assertThat(CustomerStatus.VERIFIED.canTransitionTo(CustomerStatus.INACTIVE)).isTrue();
        }

        @Test
        @DisplayName("VERIFIED can transition to SUSPENDED")
        void verifiedCanTransitionToSuspended() {
            assertThat(CustomerStatus.VERIFIED.canTransitionTo(CustomerStatus.SUSPENDED)).isTrue();
        }
    }

    @Nested
    @DisplayName("Is Active Tests")
    class IsActiveTests {

        @Test
        @DisplayName("ACTIVE is active")
        void activeIsActive() {
            assertThat(CustomerStatus.ACTIVE.isActive()).isTrue();
        }

        @Test
        @DisplayName("INACTIVE is not active")
        void inactiveIsNotActive() {
            assertThat(CustomerStatus.INACTIVE.isActive()).isFalse();
        }

        @Test
        @DisplayName("SUSPENDED is not active")
        void suspendedIsNotActive() {
            assertThat(CustomerStatus.SUSPENDED.isActive()).isFalse();
        }

        @Test
        @DisplayName("BLOCKED is not active")
        void blockedIsNotActive() {
            assertThat(CustomerStatus.BLOCKED.isActive()).isFalse();
        }

        @Test
        @DisplayName("PENDING_VERIFICATION is not active")
        void pendingVerificationIsNotActive() {
            assertThat(CustomerStatus.PENDING_VERIFICATION.isActive()).isFalse();
        }

        @Test
        @DisplayName("VERIFIED is active")
        void verifiedIsActive() {
            assertThat(CustomerStatus.VERIFIED.isActive()).isTrue();
        }
    }

    @Nested
    @DisplayName("Value Of Tests")
    class ValueOfTests {

        @Test
        @DisplayName("Should return correct status from valueOf")
        void shouldReturnCorrectStatusFromValueOf() {
            assertThat(CustomerStatus.valueOf("ACTIVE")).isEqualTo(CustomerStatus.ACTIVE);
            assertThat(CustomerStatus.valueOf("INACTIVE")).isEqualTo(CustomerStatus.INACTIVE);
            assertThat(CustomerStatus.valueOf("SUSPENDED")).isEqualTo(CustomerStatus.SUSPENDED);
            assertThat(CustomerStatus.valueOf("VERIFIED")).isEqualTo(CustomerStatus.VERIFIED);
        }

        @Test
        @DisplayName("Should throw exception for invalid status")
        void shouldThrowExceptionForInvalidStatus() {
            assertThatThrownBy(() -> CustomerStatus.valueOf("INVALID"))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
