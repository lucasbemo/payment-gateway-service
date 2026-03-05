package com.payment.gateway.commons.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;

import java.lang.annotation.*;

/**
 * Validates that a number is a valid monetary amount (positive, with up to 2 decimal places).
 */
@DecimalMin(value = "0.01", message = "Amount must be greater than 0")
@Digits(integer = 17, fraction = 2, message = "Amount must have up to 2 decimal places")
@Documented
@Constraint(validatedBy = {})
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidAmount {

    String message() default "Invalid amount. Must be greater than 0 with up to 2 decimal places";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
