package com.payment.gateway.domain.transaction.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("TransactionStatus Enum Tests")
class TransactionStatusTest {

    @Nested
    @DisplayName("Status Transitions from PENDING")
    class PendingTransitions {

        @Test
        @DisplayName("PENDING can transition to PROCESSING")
        void pendingCanTransitionToProcessing() {
            assertThat(TransactionStatus.PENDING.canTransitionTo(TransactionStatus.PROCESSING)).isTrue();
        }

        @Test
        @DisplayName("PENDING can transition to AUTHORIZED")
        void pendingCanTransitionToAuthorized() {
            assertThat(TransactionStatus.PENDING.canTransitionTo(TransactionStatus.AUTHORIZED)).isTrue();
        }

        @Test
        @DisplayName("PENDING can transition to CAPTURED")
        void pendingCanTransitionToCaptured() {
            assertThat(TransactionStatus.PENDING.canTransitionTo(TransactionStatus.CAPTURED)).isTrue();
        }

        @Test
        @DisplayName("PENDING can transition to SETTLED")
        void pendingCanTransitionToSettled() {
            assertThat(TransactionStatus.PENDING.canTransitionTo(TransactionStatus.SETTLED)).isTrue();
        }

        @Test
        @DisplayName("PENDING can transition to FAILED")
        void pendingCanTransitionToFailed() {
            assertThat(TransactionStatus.PENDING.canTransitionTo(TransactionStatus.FAILED)).isTrue();
        }

        @Test
        @DisplayName("PENDING cannot transition to REVERSED")
        void pendingCannotTransitionToReversed() {
            assertThat(TransactionStatus.PENDING.canTransitionTo(TransactionStatus.REVERSED)).isFalse();
        }

        @Test
        @DisplayName("PENDING cannot transition to REFUNDED")
        void pendingCannotTransitionToRefunded() {
            assertThat(TransactionStatus.PENDING.canTransitionTo(TransactionStatus.REFUNDED)).isFalse();
        }

        @Test
        @DisplayName("PENDING cannot transition to PARTIALLY_REFUNDED")
        void pendingCannotTransitionToPartiallyRefunded() {
            assertThat(TransactionStatus.PENDING.canTransitionTo(TransactionStatus.PARTIALLY_REFUNDED)).isFalse();
        }
    }

    @Nested
    @DisplayName("Status Transitions from PROCESSING")
    class ProcessingTransitions {

        @Test
        @DisplayName("PROCESSING can transition to AUTHORIZED")
        void processingCanTransitionToAuthorized() {
            assertThat(TransactionStatus.PROCESSING.canTransitionTo(TransactionStatus.AUTHORIZED)).isTrue();
        }

        @Test
        @DisplayName("PROCESSING can transition to CAPTURED")
        void processingCanTransitionToCaptured() {
            assertThat(TransactionStatus.PROCESSING.canTransitionTo(TransactionStatus.CAPTURED)).isTrue();
        }

        @Test
        @DisplayName("PROCESSING can transition to SETTLED")
        void processingCanTransitionToSettled() {
            assertThat(TransactionStatus.PROCESSING.canTransitionTo(TransactionStatus.SETTLED)).isTrue();
        }

        @Test
        @DisplayName("PROCESSING can transition to FAILED")
        void processingCanTransitionToFailed() {
            assertThat(TransactionStatus.PROCESSING.canTransitionTo(TransactionStatus.FAILED)).isTrue();
        }

        @Test
        @DisplayName("PROCESSING cannot transition to PENDING")
        void processingCannotTransitionToPending() {
            assertThat(TransactionStatus.PROCESSING.canTransitionTo(TransactionStatus.PENDING)).isFalse();
        }

        @Test
        @DisplayName("PROCESSING cannot transition to REVERSED")
        void processingCannotTransitionToReversed() {
            assertThat(TransactionStatus.PROCESSING.canTransitionTo(TransactionStatus.REVERSED)).isFalse();
        }
    }

    @Nested
    @DisplayName("Status Transitions from AUTHORIZED")
    class AuthorizedTransitions {

        @Test
        @DisplayName("AUTHORIZED can transition to CAPTURED")
        void authorizedCanTransitionToCaptured() {
            assertThat(TransactionStatus.AUTHORIZED.canTransitionTo(TransactionStatus.CAPTURED)).isTrue();
        }

        @Test
        @DisplayName("AUTHORIZED can transition to SETTLED")
        void authorizedCanTransitionToSettled() {
            assertThat(TransactionStatus.AUTHORIZED.canTransitionTo(TransactionStatus.SETTLED)).isTrue();
        }

        @Test
        @DisplayName("AUTHORIZED can transition to REVERSED")
        void authorizedCanTransitionToReversed() {
            assertThat(TransactionStatus.AUTHORIZED.canTransitionTo(TransactionStatus.REVERSED)).isTrue();
        }

        @Test
        @DisplayName("AUTHORIZED can transition to FAILED")
        void authorizedCanTransitionToFailed() {
            assertThat(TransactionStatus.AUTHORIZED.canTransitionTo(TransactionStatus.FAILED)).isTrue();
        }

        @Test
        @DisplayName("AUTHORIZED cannot transition to REFUNDED")
        void authorizedCannotTransitionToRefunded() {
            assertThat(TransactionStatus.AUTHORIZED.canTransitionTo(TransactionStatus.REFUNDED)).isFalse();
        }
    }

    @Nested
    @DisplayName("Status Transitions from CAPTURED")
    class CapturedTransitions {

        @Test
        @DisplayName("CAPTURED can transition to SETTLED")
        void capturedCanTransitionToSettled() {
            assertThat(TransactionStatus.CAPTURED.canTransitionTo(TransactionStatus.SETTLED)).isTrue();
        }

        @Test
        @DisplayName("CAPTURED can transition to REFUNDED")
        void capturedCanTransitionToRefunded() {
            assertThat(TransactionStatus.CAPTURED.canTransitionTo(TransactionStatus.REFUNDED)).isTrue();
        }

        @Test
        @DisplayName("CAPTURED can transition to PARTIALLY_REFUNDED")
        void capturedCanTransitionToPartiallyRefunded() {
            assertThat(TransactionStatus.CAPTURED.canTransitionTo(TransactionStatus.PARTIALLY_REFUNDED)).isTrue();
        }

        @Test
        @DisplayName("CAPTURED cannot transition to other statuses")
        void capturedCannotTransitionToOtherStatuses() {
            assertThat(TransactionStatus.CAPTURED.canTransitionTo(TransactionStatus.PENDING)).isFalse();
            assertThat(TransactionStatus.CAPTURED.canTransitionTo(TransactionStatus.AUTHORIZED)).isFalse();
            assertThat(TransactionStatus.CAPTURED.canTransitionTo(TransactionStatus.REVERSED)).isFalse();
        }
    }

    @Nested
    @DisplayName("Status Transitions from SETTLED")
    class SettledTransitions {

        @Test
        @DisplayName("SETTLED can transition to REFUNDED")
        void settledCanTransitionToRefunded() {
            assertThat(TransactionStatus.SETTLED.canTransitionTo(TransactionStatus.REFUNDED)).isTrue();
        }

        @Test
        @DisplayName("SETTLED can transition to PARTIALLY_REFUNDED")
        void settledCanTransitionToPartiallyRefunded() {
            assertThat(TransactionStatus.SETTLED.canTransitionTo(TransactionStatus.PARTIALLY_REFUNDED)).isTrue();
        }

        @Test
        @DisplayName("SETTLED cannot transition to other statuses")
        void settledCannotTransitionToOtherStatuses() {
            assertThat(TransactionStatus.SETTLED.canTransitionTo(TransactionStatus.PENDING)).isFalse();
            assertThat(TransactionStatus.SETTLED.canTransitionTo(TransactionStatus.CAPTURED)).isFalse();
            assertThat(TransactionStatus.SETTLED.canTransitionTo(TransactionStatus.REVERSED)).isFalse();
        }
    }

    @Nested
    @DisplayName("Status Transitions from PARTIALLY_REFUNDED")
    class PartiallyRefundedTransitions {

        @Test
        @DisplayName("PARTIALLY_REFUNDED can transition to REFUNDED")
        void partiallyRefundedCanTransitionToRefunded() {
            assertThat(TransactionStatus.PARTIALLY_REFUNDED.canTransitionTo(TransactionStatus.REFUNDED)).isTrue();
        }

        @Test
        @DisplayName("PARTIALLY_REFUNDED cannot transition to other statuses")
        void partiallyRefundedCannotTransitionToOtherStatuses() {
            assertThat(TransactionStatus.PARTIALLY_REFUNDED.canTransitionTo(TransactionStatus.PENDING)).isFalse();
            assertThat(TransactionStatus.PARTIALLY_REFUNDED.canTransitionTo(TransactionStatus.CAPTURED)).isFalse();
            assertThat(TransactionStatus.PARTIALLY_REFUNDED.canTransitionTo(TransactionStatus.SETTLED)).isFalse();
        }
    }

    @Nested
    @DisplayName("Terminal Status Tests")
    class TerminalStatusTests {

        @Test
        @DisplayName("PENDING is not terminal")
        void pendingIsNotTerminal() {
            assertThat(TransactionStatus.PENDING.isTerminal()).isFalse();
        }

        @Test
        @DisplayName("PROCESSING is not terminal")
        void processingIsNotTerminal() {
            assertThat(TransactionStatus.PROCESSING.isTerminal()).isFalse();
        }

        @Test
        @DisplayName("AUTHORIZED is not terminal")
        void authorizedIsNotTerminal() {
            assertThat(TransactionStatus.AUTHORIZED.isTerminal()).isFalse();
        }

        @Test
        @DisplayName("CAPTURED is not terminal")
        void capturedIsNotTerminal() {
            assertThat(TransactionStatus.CAPTURED.isTerminal()).isFalse();
        }

        @Test
        @DisplayName("SETTLED is terminal")
        void settledIsTerminal() {
            assertThat(TransactionStatus.SETTLED.isTerminal()).isTrue();
        }

        @Test
        @DisplayName("REVERSED is terminal")
        void reversedIsTerminal() {
            assertThat(TransactionStatus.REVERSED.isTerminal()).isTrue();
        }

        @Test
        @DisplayName("FAILED is terminal")
        void failedIsTerminal() {
            assertThat(TransactionStatus.FAILED.isTerminal()).isTrue();
        }

        @Test
        @DisplayName("REFUNDED is terminal")
        void refundedIsTerminal() {
            assertThat(TransactionStatus.REFUNDED.isTerminal()).isTrue();
        }

        @Test
        @DisplayName("PARTIALLY_REFUNDED is not terminal")
        void partiallyRefundedIsNotTerminal() {
            assertThat(TransactionStatus.PARTIALLY_REFUNDED.isTerminal()).isFalse();
        }
    }

    @Nested
    @DisplayName("Pending Status Tests")
    class PendingStatusTests {

        @Test
        @DisplayName("PENDING is pending")
        void pendingIsPending() {
            assertThat(TransactionStatus.PENDING.isPending()).isTrue();
        }

        @Test
        @DisplayName("PROCESSING is pending")
        void processingIsPending() {
            assertThat(TransactionStatus.PROCESSING.isPending()).isTrue();
        }

        @Test
        @DisplayName("AUTHORIZED is not pending")
        void authorizedIsNotPending() {
            assertThat(TransactionStatus.AUTHORIZED.isPending()).isFalse();
        }

        @Test
        @DisplayName("CAPTURED is not pending")
        void capturedIsNotPending() {
            assertThat(TransactionStatus.CAPTURED.isPending()).isFalse();
        }
    }

    @Nested
    @DisplayName("Successful Status Tests")
    class SuccessfulStatusTests {

        @Test
        @DisplayName("PENDING is not successful")
        void pendingIsNotSuccessful() {
            assertThat(TransactionStatus.PENDING.isSuccessful()).isFalse();
        }

        @Test
        @DisplayName("PROCESSING is not successful")
        void processingIsNotSuccessful() {
            assertThat(TransactionStatus.PROCESSING.isSuccessful()).isFalse();
        }

        @Test
        @DisplayName("AUTHORIZED is successful")
        void authorizedIsSuccessful() {
            assertThat(TransactionStatus.AUTHORIZED.isSuccessful()).isTrue();
        }

        @Test
        @DisplayName("CAPTURED is successful")
        void capturedIsSuccessful() {
            assertThat(TransactionStatus.CAPTURED.isSuccessful()).isTrue();
        }

        @Test
        @DisplayName("SETTLED is successful")
        void settledIsSuccessful() {
            assertThat(TransactionStatus.SETTLED.isSuccessful()).isTrue();
        }

        @Test
        @DisplayName("FAILED is not successful")
        void failedIsNotSuccessful() {
            assertThat(TransactionStatus.FAILED.isSuccessful()).isFalse();
        }

        @Test
        @DisplayName("REFUNDED is successful")
        void refundedIsSuccessful() {
            assertThat(TransactionStatus.REFUNDED.isSuccessful()).isTrue();
        }

        @Test
        @DisplayName("PARTIALLY_REFUNDED is successful")
        void partiallyRefundedIsSuccessful() {
            assertThat(TransactionStatus.PARTIALLY_REFUNDED.isSuccessful()).isTrue();
        }
    }
}
