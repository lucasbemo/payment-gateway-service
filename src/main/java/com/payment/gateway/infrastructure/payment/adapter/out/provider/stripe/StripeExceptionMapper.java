package com.payment.gateway.infrastructure.payment.adapter.out.provider.stripe;

import com.payment.gateway.application.payment.port.out.ExternalPaymentProviderPort;
import com.stripe.exception.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StripeExceptionMapper {

    public ExternalPaymentProviderPort.PaymentProviderResult mapException(StripeException e, String paymentId) {
        String errorCode = mapErrorCode(e);
        String errorMessage = mapErrorMessage(e);
        
        log.error("Stripe error: code={}, message={}", errorCode, errorMessage, e);
        
        return new ExternalPaymentProviderPort.PaymentProviderResult(
            false,
            paymentId,
            errorCode,
            errorMessage
        );
    }

    private String mapErrorCode(StripeException e) {
        if (e instanceof CardException ce) {
            return mapCardErrorCode(ce);
        }
        if (e instanceof InvalidRequestException) {
            return "INVALID_REQUEST";
        }
        if (e instanceof AuthenticationException) {
            return "AUTHENTICATION_FAILED";
        }
        if (e instanceof ApiConnectionException) {
            return "API_CONNECTION_ERROR";
        }
        if (e instanceof RateLimitException) {
            return "RATE_LIMIT_EXCEEDED";
        }
        if (e instanceof PermissionException) {
            return "PERMISSION_DENIED";
        }
        return "STRIPE_ERROR";
    }

    private String mapCardErrorCode(CardException e) {
        String code = e.getCode();
        if (code == null) {
            return "CARD_ERROR";
        }
        return switch (code) {
            case "card_declined" -> "CARD_DECLINED";
            case "expired_card" -> "EXPIRED_CARD";
            case "incorrect_cvc" -> "INVALID_CVV";
            case "insufficient_funds" -> "INSUFFICIENT_FUNDS";
            case "lost_card" -> "CARD_LOST";
            case "stolen_card" -> "CARD_STOLEN";
            case "processing_error" -> "PROCESSING_ERROR";
            case "incorrect_number" -> "INVALID_CARD_NUMBER";
            case "invalid_expiry_month" -> "INVALID_EXPIRY_MONTH";
            case "invalid_expiry_year" -> "INVALID_EXPIRY_YEAR";
            case "invalid_cvc" -> "INVALID_CVV";
            case "invalid_number" -> "INVALID_CARD_NUMBER";
            case "incorrect_zip" -> "INVALID_ZIP_CODE";
            default -> "CARD_ERROR";
        };
    }

    private String mapErrorMessage(StripeException e) {
        if (e instanceof CardException ce) {
            String declineMessage = ce.getDeclineCode();
            if (declineMessage != null && !declineMessage.isEmpty()) {
                return formatMessage(declineMessage);
            }
            return ce.getMessage() != null ? ce.getMessage() : "Card error";
        }
        return e.getMessage() != null ? e.getMessage() : "Stripe error";
    }

    private String formatMessage(String message) {
        return message.replace("_", " ").toLowerCase();
    }
}