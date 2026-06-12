package com.payment.gateway.application.payment.service;

import com.payment.gateway.application.commons.port.out.AuditPort;
import com.payment.gateway.application.commons.port.out.MetricsPort;
import com.payment.gateway.application.payment.dto.PaymentResponse;
import com.payment.gateway.application.payment.dto.ProcessPaymentCommand;
import com.payment.gateway.application.payment.port.in.ProcessPaymentUseCase;
import com.payment.gateway.application.payment.port.out.CustomerQueryPort;
import com.payment.gateway.application.payment.port.out.ExternalPaymentProviderPort;
import com.payment.gateway.application.payment.port.out.MerchantQueryPort;
import com.payment.gateway.application.payment.port.out.PaymentQueryPort;
import com.payment.gateway.application.payment.port.out.TokenizationServicePort;
import com.payment.gateway.application.payment.port.out.TransactionCommandPort;
import com.payment.gateway.application.transaction.port.out.TransactionQueryPort;
import com.payment.gateway.commons.exception.BusinessException;
import com.payment.gateway.commons.exception.PaymentDeclinedException;
import com.payment.gateway.commons.model.Money;
import com.payment.gateway.commons.utils.IdGenerator;
import com.payment.gateway.domain.merchant.model.Merchant;
import com.payment.gateway.domain.outbox.model.EventType;
import com.payment.gateway.domain.outbox.service.OutboxEventDomainService;
import com.payment.gateway.domain.payment.event.PaymentCreatedEvent;
import com.payment.gateway.domain.payment.event.PaymentFailedEvent;
import com.payment.gateway.domain.payment.model.Payment;
import com.payment.gateway.domain.payment.model.PaymentItem;
import com.payment.gateway.domain.payment.model.PaymentMetadata;
import com.payment.gateway.domain.payment.model.PaymentMethod;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service for processing payments.
 * Implements the ProcessPaymentUseCase interface.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ProcessPaymentService implements ProcessPaymentUseCase {

    private final PaymentQueryPort paymentQueryPort;
    private final MerchantQueryPort merchantQueryPort;
    private final CustomerQueryPort customerQueryPort;
    private final ExternalPaymentProviderPort externalPaymentProviderPort;
    private final TokenizationServicePort tokenizationServicePort;
    private final TransactionCommandPort transactionCommandPort;
    private final TransactionQueryPort transactionQueryPort;
    private final IdGenerator idGenerator;
    private final MetricsPort metricsPort;
    private final AuditPort auditPort;
    private final OutboxEventDomainService outboxEventService;

    @Override
    @Transactional(noRollbackFor = PaymentDeclinedException.class)
    public PaymentResponse processPayment(ProcessPaymentCommand command) {
        log.info(
                "Processing payment for merchant: {}, amount: {} {}",
                command.getMerchantId(),
                command.getAmount(),
                command.getCurrency());

        final long startNanos = System.nanoTime();

        // Validate merchant
        Merchant merchant = validateMerchant(command.getMerchantId());

        // Check for duplicate payment (idempotency)
        Payment existingPayment = checkIdempotency(command.getIdempotencyKey());
        if (existingPayment != null) {
            log.info("Duplicate payment detected for idempotency key: {}", command.getIdempotencyKey());
            return mapToResponse(existingPayment);
        }

        // Validate and create payment method
        String paymentMethodToken = tokenizeCardIfNeeded(command);
        PaymentMethod paymentMethod = getPaymentMethodType(command);

        // Create payment domain object
        Payment payment = createPayment(command, merchant, paymentMethod, paymentMethodToken);

        // Save payment
        Payment savedPayment = paymentQueryPort.savePayment(payment);
        log.info("Payment created with id: {}", savedPayment.getId());

        // Authorize payment with external provider
        authorizeWithProvider(savedPayment);

        // Update payment status after authorization
        savedPayment.authorize();
        savedPayment = paymentQueryPort.savePayment(savedPayment);

        // Create transaction
        createTransaction(savedPayment);

        // Outbox event committed atomically with the payment; published by OutboxPollingScheduler
        outboxEventService.publish(
                savedPayment.getId(),
                "PAYMENT",
                EventType.PAYMENT_CREATED,
                new PaymentCreatedEvent(
                        savedPayment.getId(),
                        savedPayment.getMerchantId(),
                        String.valueOf(savedPayment.getAmount().getAmountInCents()),
                        savedPayment.getCurrency(),
                        savedPayment.getIdempotencyKey()));

        log.info("Payment processed successfully: {}", savedPayment.getId());
        metricsPort.recordPaymentApproved();
        metricsPort.recordPaymentAmount(savedPayment.getAmount().getAmountInCents());
        metricsPort.recordPaymentProcessingDuration(java.time.Duration.ofNanos(System.nanoTime() - startNanos));
        auditPort.logPaymentOperation(
                savedPayment.getId(),
                savedPayment.getMerchantId(),
                "AUTHORIZE",
                "SUCCESS",
                savedPayment.getAmount().getAmountInCents());
        return mapToResponse(savedPayment);
    }

    private Merchant validateMerchant(String merchantId) {
        Merchant merchant = merchantQueryPort
                .findById(merchantId)
                .orElseThrow(() -> new BusinessException("Merchant not found: " + merchantId));
        if (!merchant.getStatus().canProcessPayments()) {
            throw new BusinessException("Merchant is not active: " + merchant.getStatus());
        }
        return merchant;
    }

    private Payment checkIdempotency(String idempotencyKey) {
        return paymentQueryPort.findByIdempotencyKey(idempotencyKey).orElse(null);
    }

    private String tokenizeCardIfNeeded(ProcessPaymentCommand command) {
        if (command.getCardNumber() != null && !command.getCardNumber().isBlank()) {
            log.debug("Tokenizing card for payment");
            return tokenizationServicePort.tokenize(
                    command.getCardNumber(),
                    command.getCardExpiryMonth(),
                    command.getCardExpiryYear(),
                    command.getCardCvv());
        }
        return null;
    }

    private PaymentMethod getPaymentMethodType(ProcessPaymentCommand command) {
        if (command.getPaymentMethodType() != null) {
            try {
                return PaymentMethod.fromString(command.getPaymentMethodType());
            } catch (IllegalArgumentException e) {
                throw new BusinessException("Invalid payment method type: " + command.getPaymentMethodType());
            }
        }
        return PaymentMethod.CREDIT_CARD; // default
    }

    private Payment createPayment(
            ProcessPaymentCommand command, Merchant merchant, PaymentMethod paymentMethod, String paymentMethodToken) {
        Money amount = Money.of(command.getAmount(), Currency.getInstance(command.getCurrency()));

        PaymentMetadata metadata = PaymentMetadata.empty();

        List<PaymentItem> items = command.getItems() != null
                ? command.getItems().stream()
                        .map(itemDto -> {
                            Money unitPrice =
                                    Money.of(itemDto.getUnitPrice(), Currency.getInstance(command.getCurrency()));
                            Money total = unitPrice.multiply(BigDecimal.valueOf(itemDto.getQuantity()));
                            return new PaymentItem(itemDto.getDescription(), itemDto.getQuantity(), unitPrice, total);
                        })
                        .collect(Collectors.toList())
                : List.of();

        return Payment.create(
                merchant.getId(),
                amount,
                command.getCurrency(),
                paymentMethod,
                command.getIdempotencyKey(),
                command.getDescription(),
                metadata,
                items,
                command.getCustomerId());
    }

    private void authorizeWithProvider(Payment payment) {
        log.debug("Authorizing payment {} with external provider", payment.getId());

        ExternalPaymentProviderPort.PaymentProviderRequest request =
                new ExternalPaymentProviderPort.PaymentProviderRequest(
                        payment.getId(),
                        payment.getMerchantId(),
                        payment.getAmount().getAmountInCents(),
                        payment.getCurrency(),
                        payment.getPaymentMethodId());

        try {
            ExternalPaymentProviderPort.PaymentProviderResult result =
                    externalPaymentProviderPort.authorize(request).join();

            if (!result.success()) {
                handleDecline(payment, result.errorCode(), result.errorMessage());
            }
        } catch (BusinessException e) {
            // Re-throw business exceptions (already handled)
            throw e;
        } catch (Exception e) {
            log.error("Payment authorization failed with exception: {}", e.getMessage());
            payment.fail();
            metricsPort.recordPaymentFailed();
            throw new BusinessException("Payment authorization failed: " + e.getMessage());
        }
    }

    /**
     * Handle a provider decline: persist the FAILED payment and its PAYMENT_FAILED
     * outbox event, then throw {@link PaymentDeclinedException}. The exception is
     * declared {@code noRollbackFor} on {@link #processPayment}, so the FAILED row
     * and the outbox event survive the transaction.
     */
    private void handleDecline(Payment payment, String errorCode, String errorMessage) {
        log.warn("Payment {} declined by provider: {} - {}", payment.getId(), errorCode, errorMessage);
        payment.fail();
        paymentQueryPort.savePayment(payment);
        outboxEventService.publish(
                payment.getId(),
                "PAYMENT",
                EventType.PAYMENT_FAILED,
                new PaymentFailedEvent(
                        payment.getId(),
                        payment.getMerchantId(),
                        String.valueOf(payment.getAmount().getAmountInCents()),
                        payment.getCurrency(),
                        errorCode,
                        errorMessage));
        metricsPort.recordPaymentFailed();
        auditPort.logPaymentOperation(
                payment.getId(),
                payment.getMerchantId(),
                "AUTHORIZE",
                "FAILED",
                payment.getAmount().getAmountInCents());
        throw new PaymentDeclinedException(errorCode, errorMessage);
    }

    private void createTransaction(Payment payment) {
        log.debug("Creating transaction for payment {}", payment.getId());

        TransactionCommandPort.CreateTransactionCommand command = new TransactionCommandPort.CreateTransactionCommand(
                payment.getId(),
                payment.getMerchantId(),
                "PAYMENT",
                payment.getAmount().getAmountInCents(),
                payment.getCurrency(),
                payment.getStatus().name());

        transactionCommandPort.createTransaction(command);
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
