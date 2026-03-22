package com.payment.gateway.infrastructure.commons.monitoring;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ListConsumerGroupOffsetsOptions;
import org.apache.kafka.clients.admin.ListOffsetsOptions;
import org.apache.kafka.clients.admin.OffsetSpec;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class KafkaLagMonitor {

    private final String bootstrapServers;
    private final String consumerGroupId;
    private AdminClient adminClient;
    
    private final Map<TopicPartition, Long> currentLags = new ConcurrentHashMap<>();
    private final Map<String, Long> topicLags = new ConcurrentHashMap<>();
    private long totalLag = 0;
    private long lastCheckTimestamp = 0;
    private volatile boolean healthy = false;

    public KafkaLagMonitor(
            @Value("${spring.kafka.bootstrap-servers:localhost:9093}") String bootstrapServers,
            @Value("${spring.kafka.consumer.group-id:payment-gateway-group}") String consumerGroupId) {
        this.bootstrapServers = bootstrapServers;
        this.consumerGroupId = consumerGroupId;
    }

    @PostConstruct
    public void init() {
        try {
            Properties props = new Properties();
            props.put("bootstrap.servers", bootstrapServers);
            props.put("request.timeout.ms", 5000);
            props.put("default.api.timeout.ms", 5000);
            this.adminClient = AdminClient.create(props);
            this.healthy = true;
            log.info("KafkaLagMonitor initialized with bootstrap servers: {}", bootstrapServers);
        } catch (Exception e) {
            this.healthy = false;
            log.warn("Failed to initialize KafkaLagMonitor: {}", e.getMessage());
        }
    }

    @PreDestroy
    public void destroy() {
        if (adminClient != null) {
            adminClient.close(Duration.ofSeconds(5));
            this.healthy = false;
            log.info("KafkaLagMonitor closed");
        }
    }

    @Scheduled(fixedRate = 30000, initialDelay = 10000)
    public void checkConsumerLag() {
        if (adminClient == null) {
            return;
        }

        try {
            Map<TopicPartition, OffsetAndMetadata> consumerOffsets = adminClient
                    .listConsumerGroupOffsets(consumerGroupId, new ListConsumerGroupOffsetsOptions()
                            .timeoutMs(5000))
                    .partitionsToOffsetAndMetadata()
                    .get(5, TimeUnit.SECONDS);

            if (consumerOffsets.isEmpty()) {
                log.debug("No consumer offsets found for group: {}", consumerGroupId);
                return;
            }

            Map<TopicPartition, OffsetSpec> endOffsetsRequest = new HashMap<>();
            for (TopicPartition tp : consumerOffsets.keySet()) {
                endOffsetsRequest.put(tp, OffsetSpec.latest());
            }

            Map<TopicPartition, Long> endOffsets = adminClient
                    .listOffsets(endOffsetsRequest, new ListOffsetsOptions().timeoutMs(5000))
                    .all()
                    .get(5, TimeUnit.SECONDS)
                    .entrySet()
                    .stream()
                    .collect(HashMap::new, (m, e) -> m.put(e.getKey(), e.getValue().offset()), HashMap::putAll);

            currentLags.clear();
            topicLags.clear();
            totalLag = 0;

            for (Map.Entry<TopicPartition, OffsetAndMetadata> entry : consumerOffsets.entrySet()) {
                TopicPartition tp = entry.getKey();
                long consumerOffset = entry.getValue().offset();
                Long endOffset = endOffsets.get(tp);

                if (endOffset != null) {
                    long lag = Math.max(0, endOffset - consumerOffset);
                    currentLags.put(tp, lag);
                    totalLag += lag;

                    topicLags.merge(tp.topic(), lag, Long::sum);
                }
            }

            lastCheckTimestamp = System.currentTimeMillis();

            if (log.isDebugEnabled() && totalLag > 0) {
                log.debug("Consumer lag check - Group: {}, Total lag: {}, Topics: {}", 
                        consumerGroupId, totalLag, topicLags);
            }

            if (totalLag > 1000) {
                log.warn("High consumer lag detected - Group: {}, Total lag: {}", consumerGroupId, totalLag);
            }

        } catch (Exception e) {
            log.error("Error checking consumer lag: {}", e.getMessage());
        }
    }

    public Map<TopicPartition, Long> getCurrentLags() {
        return Collections.unmodifiableMap(currentLags);
    }

    public Map<String, Long> getTopicLags() {
        return Collections.unmodifiableMap(topicLags);
    }

    public long getTotalLag() {
        return totalLag;
    }

    public long getLagForTopic(String topic) {
        return topicLags.getOrDefault(topic, 0L);
    }

    public long getLastCheckTimestamp() {
        return lastCheckTimestamp;
    }

    public String getConsumerGroupId() {
        return consumerGroupId;
    }

    public boolean isHealthy() {
        return healthy && adminClient != null && lastCheckTimestamp > 0;
    }
}