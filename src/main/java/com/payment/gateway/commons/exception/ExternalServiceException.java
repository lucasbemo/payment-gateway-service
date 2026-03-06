package com.payment.gateway.commons.exception;

/**
 * Exception thrown when an external service call fails.
 */
public class ExternalServiceException extends DomainException {

    public ExternalServiceException(String message) {
        super("EXTERNAL_SERVICE_ERROR", message);
    }

    public ExternalServiceException(String serviceName, String message) {
        super("EXTERNAL_SERVICE_ERROR", "External service '" + serviceName + "' failed: " + message);
    }

    public ExternalServiceException(String serviceName, String message, Throwable cause) {
        super("EXTERNAL_SERVICE_ERROR", "External service '" + serviceName + "' failed: " + message, cause);
    }
}
