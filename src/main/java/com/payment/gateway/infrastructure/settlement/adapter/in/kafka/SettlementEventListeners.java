package com.payment.gateway.infrastructure.settlement.adapter.in.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Kafka listeners for settlement batch events.
 * Handles consumption of settlement-related events from Kafka topics.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SettlementEventListeners {

    private final ObjectMapper objectMapper;

    @Value("${kafka.topics.settlement-batch:settlement.batch}")
    private String settlementBatchTopic;

    @KafkaListener(
        topics = "${kafka.topics.settlement-batch:settlement.batch}",
        groupId = "${spring.kafka.consumer.group-id:payment-gateway-group}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void onSettlementBatch(@Payload Map<String, Object> event,
                                   @Header(value = "kafka_receivedMessageKey", required = false) String receivedKey) {
        log.info("Received settlement.batch event: {}", event);
        try {
            String batchId = (String) event.get("batchId");
            String merchantId = (String) event.get("merchantId");
            String totalAmount = (String) event.get("totalAmount");
            String currency = (String) event.get("currency");
            Integer transactionCount = (Integer) event.get("transactionCount");

            // Process the settlement batch event
            handleSettlementBatch(batchId, merchantId, totalAmount, currency, transactionCount);

            log.info("Successfully processed settlement.batch event for batch: {}", batchId);
        } catch (Exception e) {
            log.error("Error processing settlement.batch event: {}", event, e);
            throw e; // Re-throw to trigger retry
        }
    }

    private void handleSettlementBatch(String batchId, String merchantId, String totalAmount,
                                        String currency, Integer transactionCount) {
        log.info("Handling settlement batch: batchId={}, merchantId={}, totalAmount={} {}, transactionCount={}",
                 batchId, merchantId, totalAmount, currency, transactionCount);
        // Add business logic here (e.g., initiate bank transfer, update merchant balance, generate reports, etc.)
    }
}
