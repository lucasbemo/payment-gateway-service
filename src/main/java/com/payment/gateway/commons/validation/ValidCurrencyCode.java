package com.payment.gateway.commons.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Pattern;

import java.lang.annotation.*;

/**
 * Validates that a string is a valid currency code (ISO 4217).
 */
@Pattern(regexp = "^[A-Z]{3}$", message = "Invalid currency code. Must be 3 uppercase letters (ISO 4217)")
@Documented
@Constraint(validatedBy = {})
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidCurrencyCode {

    String message() default "Invalid currency code. Must be 3 uppercase letters (ISO 4217)";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
