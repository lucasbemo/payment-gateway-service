package com.payment.gateway.application.payment.service;

import com.payment.gateway.application.payment.dto.PaymentResponse;
import com.payment.gateway.application.payment.port.in.CapturePaymentUseCase;
import com.payment.gateway.application.payment.port.out.ExternalPaymentProviderPort;
import com.payment.gateway.application.payment.port.out.PaymentQueryPort;
import com.payment.gateway.application.transaction.port.out.TransactionQueryPort;
import com.payment.gateway.commons.exception.BusinessException;
import com.payment.gateway.domain.outbox.model.EventType;
import com.payment.gateway.domain.outbox.service.OutboxEventDomainService;
import com.payment.gateway.domain.payment.event.PaymentCompletedEvent;
import com.payment.gateway.domain.payment.model.Payment;
import com.payment.gateway.domain.payment.model.PaymentStatus;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service for capturing authorized payments.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CapturePaymentService implements CapturePaymentUseCase {

    private final PaymentQueryPort paymentQueryPort;
    private final ExternalPaymentProviderPort externalPaymentProviderPort;
    private final OutboxEventDomainService outboxEventService;
    private final TransactionQueryPort transactionQueryPort;

    @Override
    public PaymentResponse capturePayment(String paymentId, String merchantId) {
        log.info("Capturing payment: {} for merchant: {}", paymentId, merchantId);

        Payment payment = paymentQueryPort
                .findById(paymentId)
                .orElseThrow(() -> new BusinessException("Payment not found: " + paymentId));

        // Validate ownership
        if (!payment.getMerchantId().equals(merchantId)) {
            throw new BusinessException("Payment does not belong to merchant: " + merchantId);
        }

        // Validate payment is authorized and can be captured
        if (payment.getStatus() != PaymentStatus.AUTHORIZED) {
            throw new BusinessException(
                    "Payment must be authorized before capture. Current status: " + payment.getStatus());
        }

        // Capture with external provider
        String providerTransactionId = captureWithProvider(payment);

        // Update payment status
        payment.capture();
        Payment savedPayment = paymentQueryPort.savePayment(payment);

        // Outbox event committed atomically with the capture; published by OutboxPollingScheduler
        outboxEventService.publish(
                savedPayment.getId(),
                "PAYMENT",
                EventType.PAYMENT_COMPLETED,
                new PaymentCompletedEvent(
                        savedPayment.getId(),
                        savedPayment.getMerchantId(),
                        String.valueOf(savedPayment.getAmount().getAmountInCents()),
                        savedPayment.getCurrency(),
                        providerTransactionId));

        log.info("Payment captured successfully: {}", paymentId);
        return mapToResponse(savedPayment);
    }

    private String captureWithProvider(Payment payment) {
        log.debug("Capturing payment {} with external provider", payment.getId());

        ExternalPaymentProviderPort.PaymentProviderRequest request =
                new ExternalPaymentProviderPort.PaymentProviderRequest(
                        payment.getId(),
                        payment.getMerchantId(),
                        payment.getAmount().getAmountInCents(),
                        payment.getCurrency(),
                        payment.getPaymentMethodId());

        try {
            ExternalPaymentProviderPort.PaymentProviderResult result =
                    externalPaymentProviderPort.capture(request).join();

            if (!result.success()) {
                log.error("Payment capture failed: {} - {}", result.errorCode(), result.errorMessage());
                throw new BusinessException("Payment capture failed: " + result.errorMessage());
            }
            return result.providerTransactionId();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Payment capture failed with exception: {}", e.getMessage());
            throw new BusinessException("Payment capture failed: " + e.getMessage());
        }
    }

    private PaymentResponse mapToResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .merchantId(payment.getMerchantId())
                .customerId(payment.getCustomerId())
                .paymentMethodId(payment.getPaymentMethodId())
                .transactionId(transactionQueryPort
                        .findLatestByPaymentId(payment.getId())
                        .map(t -> t.getId())
                        .orElse(null))
                .amount(payment.getAmount().getAmountInCents())
                .currency(payment.getCurrency())
                .status(payment.getStatus().name())
                .idempotencyKey(payment.getIdempotencyKey())
                .description(payment.getDescription())
                .items(
                        payment.getItems() != null
                                ? payment.getItems().stream()
                                        .map(item -> PaymentResponse.PaymentItemResponse.builder()
                                                .description(item.getDescription())
                                                .quantity(item.getQuantity())
                                                .unitPrice(item.getUnitPrice().getAmountInCents())
                                                .total(item.getTotal().getAmountInCents())
                                                .build())
                                        .collect(Collectors.toList())
                                : List.of())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }
}
