package com.payment.gateway.infrastructure.commons.monitoring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CustomMetricsBinder Tests")
class CustomMetricsBinderTest {

    private CustomMetricsBinder metricsBinder;
    private MeterRegistry meterRegistry;

    @BeforeEach
    void setUp() {
        metricsBinder = new CustomMetricsBinder();
        meterRegistry = new SimpleMeterRegistry();
    }

    @Nested
    @DisplayName("bindTo")
    class BindToTests {

        @Test
        @DisplayName("should register payments processed counter")
        void shouldRegisterPaymentsProcessedCounter() {
            metricsBinder.bindTo(meterRegistry);

            Counter counter = meterRegistry.find("payment.gateway.payments.processed").counter();
            assertThat(counter).isNotNull();
            assertThat(counter.count()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("should register payments failed counter")
        void shouldRegisterPaymentsFailedCounter() {
            metricsBinder.bindTo(meterRegistry);

            Counter counter = meterRegistry.find("payment.gateway.payments.failed").counter();
            assertThat(counter).isNotNull();
            assertThat(counter.count()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("should register refunds processed counter")
        void shouldRegisterRefundsProcessedCounter() {
            metricsBinder.bindTo(meterRegistry);

            Counter counter = meterRegistry.find("payment.gateway.refunds.processed").counter();
            assertThat(counter).isNotNull();
            assertThat(counter.count()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("should register payment processing timer")
        void shouldRegisterPaymentProcessingTimer() {
            metricsBinder.bindTo(meterRegistry);

            Timer timer = meterRegistry.find("payment.gateway.payments.processing.time").timer();
            assertThat(timer).isNotNull();
            assertThat(timer.count()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("recordPaymentProcessed")
    class RecordPaymentProcessedTests {

        @Test
        @DisplayName("should increment payments processed counter")
        void shouldIncrementPaymentsProcessedCounter() {
            metricsBinder.bindTo(meterRegistry);

            metricsBinder.recordPaymentProcessed();

            Counter counter = meterRegistry.find("payment.gateway.payments.processed").counter();
            assertThat(counter).isNotNull();
            assertThat(counter.count()).isEqualTo(1.0);
        }

        @Test
        @DisplayName("should increment counter multiple times")
        void shouldIncrementCounterMultipleTimes() {
            metricsBinder.bindTo(meterRegistry);

            metricsBinder.recordPaymentProcessed();
            metricsBinder.recordPaymentProcessed();
            metricsBinder.recordPaymentProcessed();

            Counter counter = meterRegistry.find("payment.gateway.payments.processed").counter();
            assertThat(counter).isNotNull();
            assertThat(counter.count()).isEqualTo(3.0);
        }

        @Test
        @DisplayName("should not throw when called before bindTo")
        void shouldNotThrowWhenCalledBeforeBindTo() {
            // recordPaymentProcessed checks for null before incrementing
            metricsBinder.recordPaymentProcessed();
            // No exception expected
        }
    }

    @Nested
    @DisplayName("recordPaymentFailed")
    class RecordPaymentFailedTests {

        @Test
        @DisplayName("should increment payments failed counter")
        void shouldIncrementPaymentsFailedCounter() {
            metricsBinder.bindTo(meterRegistry);

            metricsBinder.recordPaymentFailed();

            Counter counter = meterRegistry.find("payment.gateway.payments.failed").counter();
            assertThat(counter).isNotNull();
            assertThat(counter.count()).isEqualTo(1.0);
        }

        @Test
        @DisplayName("should not throw when called before bindTo")
        void shouldNotThrowWhenCalledBeforeBindTo() {
            metricsBinder.recordPaymentFailed();
            // No exception expected
        }
    }

    @Nested
    @DisplayName("recordRefundProcessed")
    class RecordRefundProcessedTests {

        @Test
        @DisplayName("should increment refunds processed counter")
        void shouldIncrementRefundsProcessedCounter() {
            metricsBinder.bindTo(meterRegistry);

            metricsBinder.recordRefundProcessed();

            Counter counter = meterRegistry.find("payment.gateway.refunds.processed").counter();
            assertThat(counter).isNotNull();
            assertThat(counter.count()).isEqualTo(1.0);
        }

        @Test
        @DisplayName("should not throw when called before bindTo")
        void shouldNotThrowWhenCalledBeforeBindTo() {
            metricsBinder.recordRefundProcessed();
            // No exception expected
        }
    }

    @Nested
    @DisplayName("getPaymentProcessingTimer")
    class GetPaymentProcessingTimerTests {

        @Test
        @DisplayName("should return timer after bindTo")
        void shouldReturnTimerAfterBindTo() {
            metricsBinder.bindTo(meterRegistry);

            Timer timer = metricsBinder.getPaymentProcessingTimer();

            assertThat(timer).isNotNull();
        }

        @Test
        @DisplayName("should return null before bindTo")
        void shouldReturnNullBeforeBindTo() {
            Timer timer = metricsBinder.getPaymentProcessingTimer();

            assertThat(timer).isNull();
        }
    }

    @Nested
    @DisplayName("Counter Independence")
    class CounterIndependenceTests {

        @Test
        @DisplayName("should track counters independently")
        void shouldTrackCountersIndependently() {
            metricsBinder.bindTo(meterRegistry);

            metricsBinder.recordPaymentProcessed();
            metricsBinder.recordPaymentProcessed();
            metricsBinder.recordPaymentFailed();
            metricsBinder.recordRefundProcessed();
            metricsBinder.recordRefundProcessed();
            metricsBinder.recordRefundProcessed();

            assertThat(meterRegistry.find("payment.gateway.payments.processed").counter().count())
                    .isEqualTo(2.0);
            assertThat(meterRegistry.find("payment.gateway.payments.failed").counter().count())
                    .isEqualTo(1.0);
            assertThat(meterRegistry.find("payment.gateway.refunds.processed").counter().count())
                    .isEqualTo(3.0);
        }
    }
}
