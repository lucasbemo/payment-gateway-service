package com.payment.gateway.domain.payment.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("PaymentStatus Enum Tests")
class PaymentStatusTest {

    @Nested
    @DisplayName("Status Transitions")
    class StatusTransitionTests {

        @Test
        @DisplayName("PENDING can transition to AUTHORIZED")
        void pendingCanTransitionToAuthorized() {
            assertThat(PaymentStatus.PENDING.canTransitionTo(PaymentStatus.AUTHORIZED)).isTrue();
        }

        @Test
        @DisplayName("PENDING can transition to CAPTURED")
        void pendingCanTransitionToCaptured() {
            assertThat(PaymentStatus.PENDING.canTransitionTo(PaymentStatus.CAPTURED)).isTrue();
        }

        @Test
        @DisplayName("PENDING can transition to FAILED")
        void pendingCanTransitionToFailed() {
            assertThat(PaymentStatus.PENDING.canTransitionTo(PaymentStatus.FAILED)).isTrue();
        }

        @Test
        @DisplayName("PENDING can transition to CANCELLED")
        void pendingCanTransitionToCancelled() {
            assertThat(PaymentStatus.PENDING.canTransitionTo(PaymentStatus.CANCELLED)).isTrue();
        }

        @Test
        @DisplayName("PENDING cannot transition to REFUNDED")
        void pendingCannotTransitionToRefunded() {
            assertThat(PaymentStatus.PENDING.canTransitionTo(PaymentStatus.REFUNDED)).isFalse();
        }

        @Test
        @DisplayName("AUTHORIZED can transition to CAPTURED")
        void authorizedCanTransitionToCaptured() {
            assertThat(PaymentStatus.AUTHORIZED.canTransitionTo(PaymentStatus.CAPTURED)).isTrue();
        }

        @Test
        @DisplayName("AUTHORIZED can transition to CANCELLED")
        void authorizedCanTransitionToCancelled() {
            assertThat(PaymentStatus.AUTHORIZED.canTransitionTo(PaymentStatus.CANCELLED)).isTrue();
        }

        @Test
        @DisplayName("AUTHORIZED can transition to FAILED")
        void authorizedCanTransitionToFailed() {
            assertThat(PaymentStatus.AUTHORIZED.canTransitionTo(PaymentStatus.FAILED)).isTrue();
        }

        @Test
        @DisplayName("AUTHORIZED cannot transition to PENDING")
        void authorizedCannotTransitionToPending() {
            assertThat(PaymentStatus.AUTHORIZED.canTransitionTo(PaymentStatus.PENDING)).isFalse();
        }

        @Test
        @DisplayName("AUTHORIZED cannot transition to REFUNDED")
        void authorizedCannotTransitionToRefunded() {
            assertThat(PaymentStatus.AUTHORIZED.canTransitionTo(PaymentStatus.REFUNDED)).isFalse();
        }

        @Test
        @DisplayName("CAPTURED can transition to REFUNDED")
        void capturedCanTransitionToRefunded() {
            assertThat(PaymentStatus.CAPTURED.canTransitionTo(PaymentStatus.REFUNDED)).isTrue();
        }

        @Test
        @DisplayName("CAPTURED cannot transition to other statuses")
        void capturedCannotTransitionToOtherStatuses() {
            assertThat(PaymentStatus.CAPTURED.canTransitionTo(PaymentStatus.PENDING)).isFalse();
            assertThat(PaymentStatus.CAPTURED.canTransitionTo(PaymentStatus.AUTHORIZED)).isFalse();
            assertThat(PaymentStatus.CAPTURED.canTransitionTo(PaymentStatus.FAILED)).isFalse();
            assertThat(PaymentStatus.CAPTURED.canTransitionTo(PaymentStatus.CANCELLED)).isFalse();
        }

        @Test
        @DisplayName("FAILED cannot transition to any status")
        void failedCannotTransitionToAnyStatus() {
            assertThat(PaymentStatus.FAILED.canTransitionTo(PaymentStatus.PENDING)).isFalse();
            assertThat(PaymentStatus.FAILED.canTransitionTo(PaymentStatus.AUTHORIZED)).isFalse();
            assertThat(PaymentStatus.FAILED.canTransitionTo(PaymentStatus.CAPTURED)).isFalse();
            assertThat(PaymentStatus.FAILED.canTransitionTo(PaymentStatus.CANCELLED)).isFalse();
            assertThat(PaymentStatus.FAILED.canTransitionTo(PaymentStatus.REFUNDED)).isFalse();
        }

        @Test
        @DisplayName("CANCELLED cannot transition to any status")
        void cancelledCannotTransitionToAnyStatus() {
            assertThat(PaymentStatus.CANCELLED.canTransitionTo(PaymentStatus.PENDING)).isFalse();
            assertThat(PaymentStatus.CANCELLED.canTransitionTo(PaymentStatus.AUTHORIZED)).isFalse();
            assertThat(PaymentStatus.CANCELLED.canTransitionTo(PaymentStatus.CAPTURED)).isFalse();
            assertThat(PaymentStatus.CANCELLED.canTransitionTo(PaymentStatus.FAILED)).isFalse();
            assertThat(PaymentStatus.CANCELLED.canTransitionTo(PaymentStatus.REFUNDED)).isFalse();
        }

        @Test
        @DisplayName("REFUNDED cannot transition to any status")
        void refundedCannotTransitionToAnyStatus() {
            assertThat(PaymentStatus.REFUNDED.canTransitionTo(PaymentStatus.PENDING)).isFalse();
            assertThat(PaymentStatus.REFUNDED.canTransitionTo(PaymentStatus.AUTHORIZED)).isFalse();
            assertThat(PaymentStatus.REFUNDED.canTransitionTo(PaymentStatus.CAPTURED)).isFalse();
            assertThat(PaymentStatus.REFUNDED.canTransitionTo(PaymentStatus.FAILED)).isFalse();
            assertThat(PaymentStatus.REFUNDED.canTransitionTo(PaymentStatus.CANCELLED)).isFalse();
        }
    }

    @Nested
    @DisplayName("Terminal Status Checks")
    class TerminalStatusTests {

        @Test
        @DisplayName("PENDING is not terminal")
        void pendingIsNotTerminal() {
            assertThat(PaymentStatus.PENDING.isTerminal()).isFalse();
        }

        @Test
        @DisplayName("AUTHORIZED is not terminal")
        void authorizedIsNotTerminal() {
            assertThat(PaymentStatus.AUTHORIZED.isTerminal()).isFalse();
        }

        @Test
        @DisplayName("CAPTURED is terminal")
        void capturedIsTerminal() {
            assertThat(PaymentStatus.CAPTURED.isTerminal()).isTrue();
        }

        @Test
        @DisplayName("FAILED is terminal")
        void failedIsTerminal() {
            assertThat(PaymentStatus.FAILED.isTerminal()).isTrue();
        }

        @Test
        @DisplayName("CANCELLED is terminal")
        void cancelledIsTerminal() {
            assertThat(PaymentStatus.CANCELLED.isTerminal()).isTrue();
        }

        @Test
        @DisplayName("REFUNDED is terminal")
        void refundedIsTerminal() {
            assertThat(PaymentStatus.REFUNDED.isTerminal()).isTrue();
        }
    }

    @Nested
    @DisplayName("Success Status Checks")
    class SuccessStatusTests {

        @Test
        @DisplayName("PENDING is not success")
        void pendingIsNotSuccess() {
            assertThat(PaymentStatus.PENDING.isSuccess()).isFalse();
        }

        @Test
        @DisplayName("AUTHORIZED is not success")
        void authorizedIsNotSuccess() {
            assertThat(PaymentStatus.AUTHORIZED.isSuccess()).isFalse();
        }

        @Test
        @DisplayName("FAILED is not success")
        void failedIsNotSuccess() {
            assertThat(PaymentStatus.FAILED.isSuccess()).isFalse();
        }

        @Test
        @DisplayName("CANCELLED is not success")
        void cancelledIsNotSuccess() {
            assertThat(PaymentStatus.CANCELLED.isSuccess()).isFalse();
        }

        @Test
        @DisplayName("CAPTURED is success")
        void capturedIsSuccess() {
            assertThat(PaymentStatus.CAPTURED.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("REFUNDED is success")
        void refundedIsSuccess() {
            assertThat(PaymentStatus.REFUNDED.isSuccess()).isTrue();
        }
    }

    @Nested
    @DisplayName("Parse From String")
    class ParseFromStringTests {

        @Test
        @DisplayName("Should parse uppercase status")
        void shouldParseUppercaseStatus() {
            assertThat(PaymentStatus.fromString("PENDING")).isEqualTo(PaymentStatus.PENDING);
            assertThat(PaymentStatus.fromString("AUTHORIZED")).isEqualTo(PaymentStatus.AUTHORIZED);
            assertThat(PaymentStatus.fromString("CAPTURED")).isEqualTo(PaymentStatus.CAPTURED);
            assertThat(PaymentStatus.fromString("FAILED")).isEqualTo(PaymentStatus.FAILED);
            assertThat(PaymentStatus.fromString("CANCELLED")).isEqualTo(PaymentStatus.CANCELLED);
            assertThat(PaymentStatus.fromString("REFUNDED")).isEqualTo(PaymentStatus.REFUNDED);
        }

        @Test
        @DisplayName("Should parse lowercase status")
        void shouldParseLowercaseStatus() {
            assertThat(PaymentStatus.fromString("pending")).isEqualTo(PaymentStatus.PENDING);
            assertThat(PaymentStatus.fromString("authorized")).isEqualTo(PaymentStatus.AUTHORIZED);
            assertThat(PaymentStatus.fromString("captured")).isEqualTo(PaymentStatus.CAPTURED);
            assertThat(PaymentStatus.fromString("failed")).isEqualTo(PaymentStatus.FAILED);
            assertThat(PaymentStatus.fromString("cancelled")).isEqualTo(PaymentStatus.CANCELLED);
            assertThat(PaymentStatus.fromString("refunded")).isEqualTo(PaymentStatus.REFUNDED);
        }

        @Test
        @DisplayName("Should parse mixed case status")
        void shouldParseMixedCaseStatus() {
            assertThat(PaymentStatus.fromString("Pending")).isEqualTo(PaymentStatus.PENDING);
            assertThat(PaymentStatus.fromString("Authorized")).isEqualTo(PaymentStatus.AUTHORIZED);
            assertThat(PaymentStatus.fromString("Captured")).isEqualTo(PaymentStatus.CAPTURED);
        }

        @Test
        @DisplayName("Should throw exception for invalid status")
        void shouldThrowExceptionForInvalidStatus() {
            assertThatThrownBy(() -> PaymentStatus.fromString("INVALID"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid payment status: INVALID");
        }

        @Test
        @DisplayName("Should throw exception for null status")
        void shouldThrowExceptionForNullStatus() {
            assertThatThrownBy(() -> PaymentStatus.fromString(null))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should throw exception for empty status")
        void shouldThrowExceptionForEmptyStatus() {
            assertThatThrownBy(() -> PaymentStatus.fromString(""))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
