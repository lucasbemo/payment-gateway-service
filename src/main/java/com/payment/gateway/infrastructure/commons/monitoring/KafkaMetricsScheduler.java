package com.payment.gateway.infrastructure.commons.monitoring;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaMetricsScheduler {

    private final KafkaLagMonitor kafkaLagMonitor;
    private final KafkaMetricsBinder kafkaMetricsBinder;

    @Scheduled(fixedRate = 30000, initialDelay = 15000)
    public void updateKafkaMetrics() {
        try {
            kafkaMetricsBinder.updateLagMetrics();
            
            long totalLag = kafkaMetricsBinder.getTotalLag();
            if (totalLag > 1000) {
                log.warn("High Kafka consumer lag detected: {} messages", totalLag);
            }
        } catch (Exception e) {
            log.error("Error updating Kafka metrics: {}", e.getMessage());
        }
    }
}