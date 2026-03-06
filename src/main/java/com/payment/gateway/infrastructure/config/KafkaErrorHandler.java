package com.payment.gateway.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

/**
 * Kafka error handling configuration.
 * Configures retry and dead-letter behavior for Kafka consumers.
 */
@Slf4j
@Configuration
public class KafkaErrorHandler {

    private static final long RETRY_INTERVAL_MS = 1000;
    private static final long MAX_RETRIES = 3;

    @Bean
    public CommonErrorHandler kafkaCommonErrorHandler() {
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
                (record, exception) -> {
                    log.error("Kafka message processing failed after {} retries. Topic: {}, Partition: {}, Offset: {}, Error: {}",
                            MAX_RETRIES,
                            record.topic(),
                            record.partition(),
                            record.offset(),
                            exception.getMessage());
                },
                new FixedBackOff(RETRY_INTERVAL_MS, MAX_RETRIES)
        );

        errorHandler.addNotRetryableExceptions(
                IllegalArgumentException.class,
                com.payment.gateway.commons.exception.ValidationException.class
        );

        return errorHandler;
    }
}
