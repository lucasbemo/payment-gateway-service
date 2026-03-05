package com.payment.gateway.commons.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Pattern;

import java.lang.annotation.*;

/**
 * Validates that a string contains only alphanumeric characters.
 */
@Pattern(regexp = "^[a-zA-Z0-9]+$", message = "Must contain only alphanumeric characters")
@Documented
@Constraint(validatedBy = {})
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Alphanumeric {

    String message() default "Must contain only alphanumeric characters";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
