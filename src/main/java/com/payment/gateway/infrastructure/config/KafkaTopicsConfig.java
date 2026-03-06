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

    @Value("${kafka.topics.payment-created:payment.created}")
    private String paymentCreatedTopic;

    @Value("${kafka.topics.payment-completed:payment.completed}")
    private String paymentCompletedTopic;

    @Value("${kafka.topics.payment-failed:payment.failed}")
    private String paymentFailedTopic;

    @Value("${kafka.topics.payment-cancelled:payment.cancelled}")
    private String paymentCancelledTopic;

    @Value("${kafka.topics.transaction-events:transaction-events}")
    private String transactionEventsTopic;

    @Value("${kafka.topics.refund-events:refund-events}")
    private String refundEventsTopic;

    @Value("${kafka.topics.refund-processed:refund.processed}")
    private String refundProcessedTopic;

    @Value("${kafka.topics.refund-failed:refund.failed}")
    private String refundFailedTopic;

    @Value("${kafka.topics.outbox-events:outbox-events}")
    private String outboxEventsTopic;

    @Value("${kafka.topics.settlement-batch:settlement.batch}")
    private String settlementBatchTopic;

    @Value("${kafka.topics.merchant-notification:merchant.notification}")
    private String merchantNotificationTopic;

    @Value("${kafka.topics.audit-logs:audit-logs}")
    private String auditLogsTopic;

    @Bean
    public NewTopic paymentEventsTopic() {
        return TopicBuilder.name(paymentEventsTopic)
                .partitions(6)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic paymentCreatedTopic() {
        return TopicBuilder.name(paymentCreatedTopic)
                .partitions(6)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic paymentCompletedTopic() {
        return TopicBuilder.name(paymentCompletedTopic)
                .partitions(6)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic paymentFailedTopic() {
        return TopicBuilder.name(paymentFailedTopic)
                .partitions(6)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic paymentCancelledTopic() {
        return TopicBuilder.name(paymentCancelledTopic)
                .partitions(6)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic transactionEventsTopic() {
        return TopicBuilder.name(transactionEventsTopic)
                .partitions(6)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic refundEventsTopic() {
        return TopicBuilder.name(refundEventsTopic)
                .partitions(6)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic refundProcessedTopic() {
        return TopicBuilder.name(refundProcessedTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic refundFailedTopic() {
        return TopicBuilder.name(refundFailedTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic outboxEventsTopic() {
        return TopicBuilder.name(outboxEventsTopic)
                .partitions(6)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic settlementBatchTopic() {
        return TopicBuilder.name(settlementBatchTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic merchantNotificationTopic() {
        return TopicBuilder.name(merchantNotificationTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic auditLogsTopic() {
        return TopicBuilder.name(auditLogsTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
