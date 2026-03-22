package com.payment.gateway.infrastructure.commons.monitoring;

import io.micrometer.core.instrument.*;
import io.micrometer.core.instrument.binder.MeterBinder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaMetricsBinder implements MeterBinder {

    private final KafkaLagMonitor kafkaLagMonitor;
    
    private MeterRegistry registry;
    
    private final AtomicLong totalConsumerLag = new AtomicLong(0);
    private final AtomicLong lastLagCheckTimestamp = new AtomicLong(0);
    
    private Counter messagesProducedTotal;
    private Counter messagesProducedFailed;
    private Counter messagesConsumedTotal;
    private Counter messagesConsumedFailed;
    private Timer producerLatency;
    private Timer consumerLatency;
    
    private final ConcurrentMap<String, AtomicLong> topicLagGauges = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Counter> messagesProducedPerTopic = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Counter> messagesConsumedPerTopic = new ConcurrentHashMap<>();

    @Override
    public void bindTo(MeterRegistry registry) {
        this.registry = registry;
        
        bindConsumerLagMetrics(registry);
        bindProducerMetrics(registry);
        bindConsumerMetrics(registry);
        bindHealthMetrics(registry);
        
        log.info("KafkaMetricsBinder initialized with consumer lag monitoring");
    }

    private void bindConsumerLagMetrics(MeterRegistry registry) {
        Gauge.builder("kafka.consumer.lag.total", totalConsumerLag, AtomicLong::get)
                .description("Total consumer lag across all partitions")
                .tag("group", kafkaLagMonitor.getConsumerGroupId())
                .register(registry);

        Gauge.builder("kafka.consumer.lag.check.timestamp", lastLagCheckTimestamp, AtomicLong::get)
                .description("Timestamp of last consumer lag check")
                .tag("group", kafkaLagMonitor.getConsumerGroupId())
                .register(registry);
    }

    private void bindProducerMetrics(MeterRegistry registry) {
        messagesProducedTotal = Counter.builder("kafka.producer.messages.sent.total")
                .description("Total number of messages sent by the producer")
                .tag("type", "producer")
                .register(registry);

        messagesProducedFailed = Counter.builder("kafka.producer.messages.failed.total")
                .description("Total number of failed message sends")
                .tag("type", "producer")
                .register(registry);

        producerLatency = Timer.builder("kafka.producer.latency")
                .description("Producer send latency")
                .tag("type", "producer")
                .publishPercentiles(0.5, 0.95, 0.99)
                .publishPercentileHistogram()
                .register(registry);
    }

    private void bindConsumerMetrics(MeterRegistry registry) {
        messagesConsumedTotal = Counter.builder("kafka.consumer.messages.consumed.total")
                .description("Total number of messages consumed")
                .tag("type", "consumer")
                .tag("group", kafkaLagMonitor.getConsumerGroupId())
                .register(registry);

        messagesConsumedFailed = Counter.builder("kafka.consumer.messages.failed.total")
                .description("Total number of failed message consumptions")
                .tag("type", "consumer")
                .tag("group", kafkaLagMonitor.getConsumerGroupId())
                .register(registry);

        consumerLatency = Timer.builder("kafka.consumer.latency")
                .description("Consumer processing latency")
                .tag("type", "consumer")
                .tag("group", kafkaLagMonitor.getConsumerGroupId())
                .publishPercentiles(0.5, 0.95, 0.99)
                .publishPercentileHistogram()
                .register(registry);
    }

    private void bindHealthMetrics(MeterRegistry registry) {
        Gauge.builder("kafka.monitor.healthy", kafkaLagMonitor, monitor -> monitor.isHealthy() ? 1 : 0)
                .description("Kafka lag monitor health status (1=healthy, 0=unhealthy)")
                .tag("component", "lag-monitor")
                .register(registry);
    }

    public void updateLagMetrics() {
        totalConsumerLag.set(kafkaLagMonitor.getTotalLag());
        lastLagCheckTimestamp.set(kafkaLagMonitor.getLastCheckTimestamp());
        
        for (var entry : kafkaLagMonitor.getTopicLags().entrySet()) {
            String topic = entry.getKey();
            long lag = entry.getValue();
            
            topicLagGauges.computeIfAbsent(topic, t -> {
                AtomicLong gauge = new AtomicLong(0);
                Gauge.builder("kafka.consumer.lag.topic", gauge, AtomicLong::get)
                        .description("Consumer lag per topic")
                        .tag("topic", t)
                        .tag("group", kafkaLagMonitor.getConsumerGroupId())
                        .register(registry);
                return gauge;
            }).set(lag);
        }
    }

    public void recordMessageProduced(String topic) {
        if (messagesProducedTotal != null) {
            messagesProducedTotal.increment();
        }
        
        messagesProducedPerTopic.computeIfAbsent(topic, t -> 
                Counter.builder("kafka.producer.messages.sent.topic")
                        .description("Messages sent per topic")
                        .tag("topic", t)
                        .register(registry))
                .increment();
    }

    public void recordMessageProducedFailed(String topic, String errorType) {
        if (messagesProducedFailed != null) {
            messagesProducedFailed.increment();
        }
        
        Counter.builder("kafka.producer.errors")
                .description("Producer errors")
                .tag("topic", topic)
                .tag("error", errorType)
                .register(registry)
                .increment();
    }

    public void recordMessageConsumed(String topic) {
        if (messagesConsumedTotal != null) {
            messagesConsumedTotal.increment();
        }
        
        messagesConsumedPerTopic.computeIfAbsent(topic, t ->
                Counter.builder("kafka.consumer.messages.consumed.topic")
                        .description("Messages consumed per topic")
                        .tag("topic", t)
                        .tag("group", kafkaLagMonitor.getConsumerGroupId())
                        .register(registry))
                .increment();
    }

    public void recordMessageConsumedFailed(String topic, String errorType) {
        if (messagesConsumedFailed != null) {
            messagesConsumedFailed.increment();
        }
        
        Counter.builder("kafka.consumer.errors")
                .description("Consumer errors")
                .tag("topic", topic)
                .tag("error", errorType)
                .tag("group", kafkaLagMonitor.getConsumerGroupId())
                .register(registry)
                .increment();
    }

    public Timer.Sample startProducerTimer() {
        return Timer.start(registry);
    }

    public void recordProducerLatency(Timer.Sample sample) {
        if (producerLatency != null && sample != null) {
            sample.stop(producerLatency);
        }
    }

    public Timer.Sample startConsumerTimer() {
        return Timer.start(registry);
    }

    public void recordConsumerLatency(Timer.Sample sample) {
        if (consumerLatency != null && sample != null) {
            sample.stop(consumerLatency);
        }
    }

    public long getTotalLag() {
        return totalConsumerLag.get();
    }

    public long getTopicLag(String topic) {
        AtomicLong lag = topicLagGauges.get(topic);
        return lag != null ? lag.get() : 0;
    }
}