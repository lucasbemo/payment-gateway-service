package com.payment.gateway.commons.exception;

/**
 * Exception thrown when a resource is not found.
 */
public class NotFoundException extends DomainException {

    public NotFoundException(String message) {
        super("NOT_FOUND", message);
    }

    public NotFoundException(String resourceType, String id) {
        super("NOT_FOUND", resourceType + " not found with id: " + id);
    }
}
