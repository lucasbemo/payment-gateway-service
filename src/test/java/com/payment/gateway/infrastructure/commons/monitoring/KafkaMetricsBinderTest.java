package com.payment.gateway.infrastructure.commons.monitoring;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class KafkaMetricsBinderTest {

    @Mock
    private KafkaLagMonitor kafkaLagMonitor;

    private MeterRegistry meterRegistry;
    private KafkaMetricsBinder kafkaMetricsBinder;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        lenient().when(kafkaLagMonitor.getConsumerGroupId()).thenReturn("test-group");
        lenient().when(kafkaLagMonitor.isHealthy()).thenReturn(true);
        
        kafkaMetricsBinder = new KafkaMetricsBinder(kafkaLagMonitor);
        kafkaMetricsBinder.bindTo(meterRegistry);
    }

    @Test
    void shouldBindConsumerLagMetrics() {
        assertNotNull(meterRegistry.find("kafka.consumer.lag.total").gauge());
        assertNotNull(meterRegistry.find("kafka.consumer.lag.check.timestamp").gauge());
    }

    @Test
    void shouldBindProducerMetrics() {
        assertNotNull(meterRegistry.find("kafka.producer.messages.sent.total").counter());
        assertNotNull(meterRegistry.find("kafka.producer.messages.failed.total").counter());
        assertNotNull(meterRegistry.find("kafka.producer.latency").timer());
    }

    @Test
    void shouldBindConsumerMetrics() {
        assertNotNull(meterRegistry.find("kafka.consumer.messages.consumed.total").counter());
        assertNotNull(meterRegistry.find("kafka.consumer.messages.failed.total").counter());
        assertNotNull(meterRegistry.find("kafka.consumer.latency").timer());
    }

    @Test
    void shouldBindHealthMetrics() {
        assertNotNull(meterRegistry.find("kafka.monitor.healthy").gauge());
    }

    @Test
    void shouldRecordMessageProduced() {
        kafkaMetricsBinder.recordMessageProduced("payment.created");
        
        assertEquals(1.0, meterRegistry.find("kafka.producer.messages.sent.total").counter().count());
        assertEquals(1.0, meterRegistry.find("kafka.producer.messages.sent.topic").tags("topic", "payment.created").counter().count());
    }

    @Test
    void shouldRecordMessageProducedFailed() {
        kafkaMetricsBinder.recordMessageProducedFailed("payment.created", "TimeoutException");
        
        assertEquals(1.0, meterRegistry.find("kafka.producer.messages.failed.total").counter().count());
        assertNotNull(meterRegistry.find("kafka.producer.errors").tags("topic", "payment.created", "error", "TimeoutException").counter());
    }

    @Test
    void shouldRecordMessageConsumed() {
        kafkaMetricsBinder.recordMessageConsumed("payment.created");
        
        assertEquals(1.0, meterRegistry.find("kafka.consumer.messages.consumed.total").counter().count());
        assertEquals(1.0, meterRegistry.find("kafka.consumer.messages.consumed.topic").tags("topic", "payment.created").counter().count());
    }

    @Test
    void shouldRecordMessageConsumedFailed() {
        kafkaMetricsBinder.recordMessageConsumedFailed("payment.created", "DeserializationException");
        
        assertEquals(1.0, meterRegistry.find("kafka.consumer.messages.failed.total").counter().count());
        assertNotNull(meterRegistry.find("kafka.consumer.errors").tags("topic", "payment.created", "error", "DeserializationException").counter());
    }

    @Test
    void shouldUpdateLagMetrics() {
        when(kafkaLagMonitor.getTotalLag()).thenReturn(100L);
        when(kafkaLagMonitor.getLastCheckTimestamp()).thenReturn(System.currentTimeMillis());
        when(kafkaLagMonitor.getTopicLags()).thenReturn(java.util.Map.of("payment.created", 50L, "refund.processed", 50L));
        
        kafkaMetricsBinder.updateLagMetrics();
        
        assertEquals(100L, kafkaMetricsBinder.getTotalLag());
    }

    @Test
    void shouldRecordProducerLatency() {
        var sample = kafkaMetricsBinder.startProducerTimer();
        assertNotNull(sample);
        
        kafkaMetricsBinder.recordProducerLatency(sample);
        
        assertEquals(1.0, meterRegistry.find("kafka.producer.latency").timer().count());
    }

    @Test
    void shouldRecordConsumerLatency() {
        var sample = kafkaMetricsBinder.startConsumerTimer();
        assertNotNull(sample);
        
        kafkaMetricsBinder.recordConsumerLatency(sample);
        
        assertEquals(1.0, meterRegistry.find("kafka.consumer.latency").timer().count());
    }
}