package com.payment.gateway.domain.refund.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("RefundStatus Enum Tests")
class RefundStatusTest {

    @Nested
    @DisplayName("Status Transitions from PENDING")
    class PendingTransitions {

        @Test
        @DisplayName("PENDING can transition to PROCESSING")
        void pendingCanTransitionToProcessing() {
            assertThat(RefundStatus.PENDING.canTransitionTo(RefundStatus.PROCESSING)).isTrue();
        }

        @Test
        @DisplayName("PENDING can transition to APPROVED")
        void pendingCanTransitionToApproved() {
            assertThat(RefundStatus.PENDING.canTransitionTo(RefundStatus.APPROVED)).isTrue();
        }

        @Test
        @DisplayName("PENDING can transition to REJECTED")
        void pendingCanTransitionToRejected() {
            assertThat(RefundStatus.PENDING.canTransitionTo(RefundStatus.REJECTED)).isTrue();
        }

        @Test
        @DisplayName("PENDING can transition to FAILED")
        void pendingCanTransitionToFailed() {
            assertThat(RefundStatus.PENDING.canTransitionTo(RefundStatus.FAILED)).isTrue();
        }

        @Test
        @DisplayName("PENDING can transition to CANCELLED")
        void pendingCanTransitionToCancelled() {
            assertThat(RefundStatus.PENDING.canTransitionTo(RefundStatus.CANCELLED)).isTrue();
        }

        @Test
        @DisplayName("PENDING cannot transition to COMPLETED")
        void pendingCannotTransitionToCompleted() {
            assertThat(RefundStatus.PENDING.canTransitionTo(RefundStatus.COMPLETED)).isFalse();
        }
    }

    @Nested
    @DisplayName("Status Transitions from PROCESSING")
    class ProcessingTransitions {

        @Test
        @DisplayName("PROCESSING can transition to APPROVED")
        void processingCanTransitionToApproved() {
            assertThat(RefundStatus.PROCESSING.canTransitionTo(RefundStatus.APPROVED)).isTrue();
        }

        @Test
        @DisplayName("PROCESSING can transition to COMPLETED")
        void processingCanTransitionToCompleted() {
            assertThat(RefundStatus.PROCESSING.canTransitionTo(RefundStatus.COMPLETED)).isTrue();
        }

        @Test
        @DisplayName("PROCESSING can transition to FAILED")
        void processingCanTransitionToFailed() {
            assertThat(RefundStatus.PROCESSING.canTransitionTo(RefundStatus.FAILED)).isTrue();
        }

        @Test
        @DisplayName("PROCESSING cannot transition to PENDING")
        void processingCannotTransitionToPending() {
            assertThat(RefundStatus.PROCESSING.canTransitionTo(RefundStatus.PENDING)).isFalse();
        }

        @Test
        @DisplayName("PROCESSING cannot transition to REJECTED")
        void processingCannotTransitionToRejected() {
            assertThat(RefundStatus.PROCESSING.canTransitionTo(RefundStatus.REJECTED)).isFalse();
        }

        @Test
        @DisplayName("PROCESSING cannot transition to CANCELLED")
        void processingCannotTransitionToCancelled() {
            assertThat(RefundStatus.PROCESSING.canTransitionTo(RefundStatus.CANCELLED)).isFalse();
        }
    }

    @Nested
    @DisplayName("Status Transitions from APPROVED")
    class ApprovedTransitions {

        @Test
        @DisplayName("APPROVED can transition to COMPLETED")
        void approvedCanTransitionToCompleted() {
            assertThat(RefundStatus.APPROVED.canTransitionTo(RefundStatus.COMPLETED)).isTrue();
        }

        @Test
        @DisplayName("APPROVED can transition to FAILED")
        void approvedCanTransitionToFailed() {
            assertThat(RefundStatus.APPROVED.canTransitionTo(RefundStatus.FAILED)).isTrue();
        }

        @Test
        @DisplayName("APPROVED can transition to CANCELLED")
        void approvedCanTransitionToCancelled() {
            assertThat(RefundStatus.APPROVED.canTransitionTo(RefundStatus.CANCELLED)).isTrue();
        }

        @Test
        @DisplayName("APPROVED cannot transition to PENDING")
        void approvedCannotTransitionToPending() {
            assertThat(RefundStatus.APPROVED.canTransitionTo(RefundStatus.PENDING)).isFalse();
        }

        @Test
        @DisplayName("APPROVED cannot transition to PROCESSING")
        void approvedCannotTransitionToProcessing() {
            assertThat(RefundStatus.APPROVED.canTransitionTo(RefundStatus.PROCESSING)).isFalse();
        }

        @Test
        @DisplayName("APPROVED cannot transition to REJECTED")
        void approvedCannotTransitionToRejected() {
            assertThat(RefundStatus.APPROVED.canTransitionTo(RefundStatus.REJECTED)).isFalse();
        }
    }

    @Nested
    @DisplayName("Status Transitions from REJECTED")
    class RejectedTransitions {

        @Test
        @DisplayName("REJECTED cannot transition to any status (terminal)")
        void rejectedCannotTransitionToAnyStatus() {
            assertThat(RefundStatus.REJECTED.canTransitionTo(RefundStatus.PENDING)).isFalse();
            assertThat(RefundStatus.REJECTED.canTransitionTo(RefundStatus.PROCESSING)).isFalse();
            assertThat(RefundStatus.REJECTED.canTransitionTo(RefundStatus.APPROVED)).isFalse();
            assertThat(RefundStatus.REJECTED.canTransitionTo(RefundStatus.COMPLETED)).isFalse();
            assertThat(RefundStatus.REJECTED.canTransitionTo(RefundStatus.FAILED)).isFalse();
            assertThat(RefundStatus.REJECTED.canTransitionTo(RefundStatus.CANCELLED)).isFalse();
        }
    }

    @Nested
    @DisplayName("Status Transitions from COMPLETED")
    class CompletedTransitions {

        @Test
        @DisplayName("COMPLETED cannot transition to any status (terminal)")
        void completedCannotTransitionToAnyStatus() {
            assertThat(RefundStatus.COMPLETED.canTransitionTo(RefundStatus.PENDING)).isFalse();
            assertThat(RefundStatus.COMPLETED.canTransitionTo(RefundStatus.PROCESSING)).isFalse();
            assertThat(RefundStatus.COMPLETED.canTransitionTo(RefundStatus.APPROVED)).isFalse();
            assertThat(RefundStatus.COMPLETED.canTransitionTo(RefundStatus.REJECTED)).isFalse();
            assertThat(RefundStatus.COMPLETED.canTransitionTo(RefundStatus.FAILED)).isFalse();
            assertThat(RefundStatus.COMPLETED.canTransitionTo(RefundStatus.CANCELLED)).isFalse();
        }
    }

    @Nested
    @DisplayName("Status Transitions from FAILED")
    class FailedTransitions {

        @Test
        @DisplayName("FAILED can transition to PENDING (can retry)")
        void failedCanTransitionToPending() {
            assertThat(RefundStatus.FAILED.canTransitionTo(RefundStatus.PENDING)).isTrue();
        }

        @Test
        @DisplayName("FAILED cannot transition to other statuses")
        void failedCannotTransitionToOtherStatuses() {
            assertThat(RefundStatus.FAILED.canTransitionTo(RefundStatus.PROCESSING)).isFalse();
            assertThat(RefundStatus.FAILED.canTransitionTo(RefundStatus.APPROVED)).isFalse();
            assertThat(RefundStatus.FAILED.canTransitionTo(RefundStatus.COMPLETED)).isFalse();
            assertThat(RefundStatus.FAILED.canTransitionTo(RefundStatus.REJECTED)).isFalse();
            assertThat(RefundStatus.FAILED.canTransitionTo(RefundStatus.CANCELLED)).isFalse();
        }
    }

    @Nested
    @DisplayName("Status Transitions from CANCELLED")
    class CancelledTransitions {

        @Test
        @DisplayName("CANCELLED cannot transition to any status (terminal)")
        void cancelledCannotTransitionToAnyStatus() {
            assertThat(RefundStatus.CANCELLED.canTransitionTo(RefundStatus.PENDING)).isFalse();
            assertThat(RefundStatus.CANCELLED.canTransitionTo(RefundStatus.PROCESSING)).isFalse();
            assertThat(RefundStatus.CANCELLED.canTransitionTo(RefundStatus.APPROVED)).isFalse();
            assertThat(RefundStatus.CANCELLED.canTransitionTo(RefundStatus.COMPLETED)).isFalse();
            assertThat(RefundStatus.CANCELLED.canTransitionTo(RefundStatus.REJECTED)).isFalse();
            assertThat(RefundStatus.CANCELLED.canTransitionTo(RefundStatus.FAILED)).isFalse();
        }
    }

    @Nested
    @DisplayName("Terminal Status Tests")
    class TerminalStatusTests {

        @Test
        @DisplayName("PENDING is not terminal")
        void pendingIsNotTerminal() {
            assertThat(RefundStatus.PENDING.isTerminal()).isFalse();
        }

        @Test
        @DisplayName("PROCESSING is not terminal")
        void processingIsNotTerminal() {
            assertThat(RefundStatus.PROCESSING.isTerminal()).isFalse();
        }

        @Test
        @DisplayName("APPROVED is not terminal")
        void approvedIsNotTerminal() {
            assertThat(RefundStatus.APPROVED.isTerminal()).isFalse();
        }

        @Test
        @DisplayName("REJECTED is terminal")
        void rejectedIsTerminal() {
            assertThat(RefundStatus.REJECTED.isTerminal()).isTrue();
        }

        @Test
        @DisplayName("COMPLETED is terminal")
        void completedIsTerminal() {
            assertThat(RefundStatus.COMPLETED.isTerminal()).isTrue();
        }

        @Test
        @DisplayName("FAILED is not terminal (can retry)")
        void failedIsNotTerminal() {
            assertThat(RefundStatus.FAILED.isTerminal()).isFalse();
        }

        @Test
        @DisplayName("CANCELLED is terminal")
        void cancelledIsTerminal() {
            assertThat(RefundStatus.CANCELLED.isTerminal()).isTrue();
        }
    }

    @Nested
    @DisplayName("Pending Status Tests")
    class PendingStatusTests {

        @Test
        @DisplayName("PENDING is pending")
        void pendingIsPending() {
            assertThat(RefundStatus.PENDING.isPending()).isTrue();
        }

        @Test
        @DisplayName("PROCESSING is pending")
        void processingIsPending() {
            assertThat(RefundStatus.PROCESSING.isPending()).isTrue();
        }

        @Test
        @DisplayName("APPROVED is not pending")
        void approvedIsNotPending() {
            assertThat(RefundStatus.APPROVED.isPending()).isFalse();
        }

        @Test
        @DisplayName("COMPLETED is not pending")
        void completedIsNotPending() {
            assertThat(RefundStatus.COMPLETED.isPending()).isFalse();
        }
    }

    @Nested
    @DisplayName("Successful Status Tests")
    class SuccessfulStatusTests {

        @Test
        @DisplayName("PENDING is not successful")
        void pendingIsNotSuccessful() {
            assertThat(RefundStatus.PENDING.isSuccessful()).isFalse();
        }

        @Test
        @DisplayName("PROCESSING is not successful")
        void processingIsNotSuccessful() {
            assertThat(RefundStatus.PROCESSING.isSuccessful()).isFalse();
        }

        @Test
        @DisplayName("APPROVED is successful")
        void approvedIsSuccessful() {
            assertThat(RefundStatus.APPROVED.isSuccessful()).isTrue();
        }

        @Test
        @DisplayName("REJECTED is not successful")
        void rejectedIsNotSuccessful() {
            assertThat(RefundStatus.REJECTED.isSuccessful()).isFalse();
        }

        @Test
        @DisplayName("COMPLETED is successful")
        void completedIsSuccessful() {
            assertThat(RefundStatus.COMPLETED.isSuccessful()).isTrue();
        }

        @Test
        @DisplayName("FAILED is not successful")
        void failedIsNotSuccessful() {
            assertThat(RefundStatus.FAILED.isSuccessful()).isFalse();
        }

        @Test
        @DisplayName("CANCELLED is not successful")
        void cancelledIsNotSuccessful() {
            assertThat(RefundStatus.CANCELLED.isSuccessful()).isFalse();
        }
    }
}
