package dev.naiarievilo.todoapp.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static dev.naiarievilo.todoapp.validation.ValidationMessages.MUST_BE_PROVIDED;

@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NoNullElementsValidator.class)
public @interface NoNullElements {

    String message() default MUST_BE_PROVIDED;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
