package com.payment.gateway.infrastructure.payment.adapter.in.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payment.gateway.application.payment.port.in.GetPaymentUseCase;
import com.payment.gateway.application.payment.port.out.PaymentQueryPort;
import com.payment.gateway.domain.payment.model.Payment;
import com.payment.gateway.domain.payment.port.PaymentEventPublisherPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Kafka listeners for payment events.
 * Handles consumption of payment-related events from Kafka topics.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventListeners {

    private final PaymentQueryPort paymentQueryPort;
    private final PaymentEventPublisherPort paymentEventPublisher;
    private final ObjectMapper objectMapper;

    @Value("${kafka.topics.payment-created:payment.created}")
    private String paymentCreatedTopic;

    @Value("${kafka.topics.payment-completed:payment.completed}")
    private String paymentCompletedTopic;

    @Value("${kafka.topics.payment-failed:payment.failed}")
    private String paymentFailedTopic;

    @Value("${kafka.topics.payment-cancelled:payment.cancelled}")
    private String paymentCancelledTopic;

    @KafkaListener(
        topics = "${kafka.topics.payment-created:payment.created}",
        groupId = "${spring.kafka.consumer.group-id:payment-gateway-group}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void onPaymentCreated(@Payload Map<String, Object> event,
                                  @Header(value = "kafka_receivedMessageKey", required = false) String receivedKey) {
        log.info("Received payment.created event: {}", event);
        try {
            String paymentId = (String) event.get("aggregateId");
            String merchantId = (String) event.get("merchantId");

            // Process the payment created event
            handlePaymentCreated(paymentId, merchantId);

            log.info("Successfully processed payment.created event for payment: {}", paymentId);
        } catch (Exception e) {
            log.error("Error processing payment.created event: {}", event, e);
            throw e; // Re-throw to trigger retry
        }
    }

    @KafkaListener(
        topics = "${kafka.topics.payment-completed:payment.completed}",
        groupId = "${spring.kafka.consumer.group-id:payment-gateway-group}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void onPaymentCompleted(@Payload Map<String, Object> event,
                                    @Header(value = "kafka_receivedMessageKey", required = false) String receivedKey) {
        log.info("Received payment.completed event: {}", event);
        try {
            String paymentId = (String) event.get("aggregateId");
            String providerTransactionId = (String) event.get("providerTransactionId");

            // Process the payment completed event
            handlePaymentCompleted(paymentId, providerTransactionId);

            log.info("Successfully processed payment.completed event for payment: {}", paymentId);
        } catch (Exception e) {
            log.error("Error processing payment.completed event: {}", event, e);
            throw e; // Re-throw to trigger retry
        }
    }

    @KafkaListener(
        topics = "${kafka.topics.payment-failed:payment.failed}",
        groupId = "${spring.kafka.consumer.group-id:payment-gateway-group}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void onPaymentFailed(@Payload Map<String, Object> event,
                                 @Header(value = "kafka_receivedMessageKey", required = false) String receivedKey) {
        log.info("Received payment.failed event: {}", event);
        try {
            String paymentId = (String) event.get("aggregateId");
            String errorCode = (String) event.get("errorCode");
            String errorMessage = (String) event.get("errorMessage");

            // Process the payment failed event
            handlePaymentFailed(paymentId, errorCode, errorMessage);

            log.info("Successfully processed payment.failed event for payment: {}", paymentId);
        } catch (Exception e) {
            log.error("Error processing payment.failed event: {}", event, e);
            throw e; // Re-throw to trigger retry
        }
    }

    @KafkaListener(
        topics = "${kafka.topics.payment-cancelled:payment.cancelled}",
        groupId = "${spring.kafka.consumer.group-id:payment-gateway-group}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void onPaymentCancelled(@Payload Map<String, Object> event,
                                    @Header(value = "kafka_receivedMessageKey", required = false) String receivedKey) {
        log.info("Received payment.cancelled event: {}", event);
        try {
            String paymentId = (String) event.get("aggregateId");
            String reason = (String) event.get("reason");

            // Process the payment cancelled event
            handlePaymentCancelled(paymentId, reason);

            log.info("Successfully processed payment.cancelled event for payment: {}", paymentId);
        } catch (Exception e) {
            log.error("Error processing payment.cancelled event: {}", event, e);
            throw e; // Re-throw to trigger retry
        }
    }

    private void handlePaymentCreated(String paymentId, String merchantId) {
        log.info("Handling payment created: paymentId={}, merchantId={}", paymentId, merchantId);
        // Add business logic here (e.g., send notification, update analytics, etc.)
    }

    private void handlePaymentCompleted(String paymentId, String providerTransactionId) {
        log.info("Handling payment completed: paymentId={}, providerTransactionId={}",
                 paymentId, providerTransactionId);
        // Add business logic here (e.g., trigger settlement, send confirmation, etc.)
    }

    private void handlePaymentFailed(String paymentId, String errorCode, String errorMessage) {
        log.info("Handling payment failed: paymentId={}, errorCode={}, errorMessage={}",
                 paymentId, errorCode, errorMessage);
        // Add business logic here (e.g., notify merchant, trigger retry, etc.)
    }

    private void handlePaymentCancelled(String paymentId, String reason) {
        log.info("Handling payment cancelled: paymentId={}, reason={}", paymentId, reason);
        // Add business logic here (e.g., release reserved funds, notify customer, etc.)
    }
}
