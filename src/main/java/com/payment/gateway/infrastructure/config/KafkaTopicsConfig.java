package com.payment.gateway.infrastructure.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Configuration for Kafka topics.
 */
@Configuration
public class KafkaTopicsConfig {

    @Value("${kafka.topics.payment-events:payment-events}")
    private String paymentEventsTopic;

    @Value("${kafka.topics.transaction-events:transaction-events}")
    private String transactionEventsTopic;

    @Value("${kafka.topics.refund-events:refund-events}")
    private String refundEventsTopic;

    @Value("${kafka.topics.outbox-events:outbox-events}")
    private String outboxEventsTopic;

    @Value("${kafka.topics.audit-logs:audit-logs}")
    private String auditLogsTopic;

    @Bean
    public NewTopic paymentEventsTopic() {
        return TopicBuilder.name(paymentEventsTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic transactionEventsTopic() {
        return TopicBuilder.name(transactionEventsTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic refundEventsTopic() {
        return TopicBuilder.name(refundEventsTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic outboxEventsTopic() {
        return TopicBuilder.name(outboxEventsTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic auditLogsTopic() {
        return TopicBuilder.name(auditLogsTopic)
                .partitions(1)
                .replicas(1)
                .build();
    }
}
