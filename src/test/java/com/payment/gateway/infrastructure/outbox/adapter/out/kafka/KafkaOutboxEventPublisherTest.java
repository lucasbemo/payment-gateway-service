package com.payment.gateway.infrastructure.outbox.adapter.out.kafka;

import com.payment.gateway.domain.outbox.model.EventStatus;
import com.payment.gateway.domain.outbox.model.EventType;
import com.payment.gateway.domain.outbox.model.OutboxEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for KafkaOutboxEventPublisher.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Kafka Outbox Event Publisher Tests")
class KafkaOutboxEventPublisherTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private CompletableFuture<SendResult<String, Object>> future;

    @Mock
    private SendResult<String, Object> sendResult;

    private KafkaOutboxEventPublisher publisher;

    private OutboxEvent testEvent;

    @BeforeEach
    void setUp() {
        publisher = new KafkaOutboxEventPublisher(kafkaTemplate);
        ReflectionTestUtils.setField(publisher, "outboxEventsTopic", "outbox-events");

        testEvent = OutboxEvent.builder()
                .id("event_test_123")
                .aggregateId("pay_test_123")
                .aggregateType("Payment")
                .eventType(EventType.PAYMENT_CREATED)
                .payload("{\"paymentId\":\"pay_test\",\"amount\":100.00}")
                .status(EventStatus.PENDING)
                .retryCount(0)
                .build();
    }

    @Nested
    @DisplayName("publish")
    class Publish {

        @Test
        @DisplayName("Should publish event to Kafka successfully")
        void shouldPublishEventSuccessfully() throws Exception {
            when(kafkaTemplate.send(eq("outbox-events"), any(), any()))
                    .thenReturn(future);
            when(future.get()).thenReturn(sendResult);

            boolean result = publisher.publish(testEvent);

            assertThat(result).isTrue();

            ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<Object> valueCaptor = ArgumentCaptor.forClass(Object.class);
            verify(kafkaTemplate).send(eq("outbox-events"), keyCaptor.capture(), valueCaptor.capture());

            assertThat(keyCaptor.getValue()).isEqualTo("pay_test_123");
            assertThat(valueCaptor.getValue()).isEqualTo(testEvent.getPayload());
        }

        @Test
        @DisplayName("Should return false when Kafka publish fails")
        void shouldReturnFalseWhenPublishFails() throws Exception {
            when(kafkaTemplate.send(eq("outbox-events"), any(), any()))
                    .thenReturn(future);
            when(future.get()).thenThrow(new RuntimeException("Kafka broker unavailable"));

            boolean result = publisher.publish(testEvent);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should use aggregateId as message key")
        void shouldUseAggregateIdAsKey() throws Exception {
            when(kafkaTemplate.send(eq("outbox-events"), any(), any()))
                    .thenReturn(future);
            when(future.get()).thenReturn(sendResult);

            publisher.publish(testEvent);

            ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
            verify(kafkaTemplate).send(eq("outbox-events"), keyCaptor.capture(), any());

            assertThat(keyCaptor.getValue()).isEqualTo("pay_test_123");
        }

        @Test
        @DisplayName("Should publish event payload to Kafka")
        void shouldPublishEventPayload() throws Exception {
            when(kafkaTemplate.send(eq("outbox-events"), any(), any()))
                    .thenReturn(future);
            when(future.get()).thenReturn(sendResult);

            publisher.publish(testEvent);

            ArgumentCaptor<Object> valueCaptor = ArgumentCaptor.forClass(Object.class);
            verify(kafkaTemplate).send(eq("outbox-events"), any(), valueCaptor.capture());

            assertThat(valueCaptor.getValue()).isEqualTo("{\"paymentId\":\"pay_test\",\"amount\":100.00}");
        }
    }
}
