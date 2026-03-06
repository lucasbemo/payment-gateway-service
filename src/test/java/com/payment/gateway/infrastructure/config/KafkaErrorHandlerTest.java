package com.payment.gateway.infrastructure.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DefaultErrorHandler;

import static org.assertj.core.api.Assertions.assertThat;

class KafkaErrorHandlerTest {

    private KafkaErrorHandler kafkaErrorHandler;

    @BeforeEach
    void setUp() {
        kafkaErrorHandler = new KafkaErrorHandler();
    }

    @Test
    @DisplayName("should create a CommonErrorHandler bean")
    void shouldCreateCommonErrorHandlerBean() {
        CommonErrorHandler errorHandler = kafkaErrorHandler.kafkaCommonErrorHandler();

        assertThat(errorHandler).isNotNull();
        assertThat(errorHandler).isInstanceOf(DefaultErrorHandler.class);
    }

    @Test
    @DisplayName("should return a DefaultErrorHandler instance")
    void shouldReturnDefaultErrorHandler() {
        CommonErrorHandler errorHandler = kafkaErrorHandler.kafkaCommonErrorHandler();

        assertThat(errorHandler).isExactlyInstanceOf(DefaultErrorHandler.class);
    }

    @Test
    @DisplayName("should create a new instance on each call")
    void shouldCreateNewInstanceOnEachCall() {
        CommonErrorHandler handler1 = kafkaErrorHandler.kafkaCommonErrorHandler();
        CommonErrorHandler handler2 = kafkaErrorHandler.kafkaCommonErrorHandler();

        assertThat(handler1).isNotSameAs(handler2);
    }
}
