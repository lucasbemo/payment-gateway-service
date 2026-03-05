package com.payment.gateway.commons.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Pattern;

import java.lang.annotation.*;

/**
 * Validates that a string is a valid phone number (E.164 format).
 */
@Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number. Use E.164 format (e.g., +1234567890)")
@Documented
@Constraint(validatedBy = {})
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPhone {

    String message() default "Invalid phone number. Use E.164 format (e.g., +1234567890)";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
