package dev.naiarievilo.todoapp.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static dev.naiarievilo.todoapp.validation.ValidationLengths.DEFAULT_MAX_LENGTH;
import static dev.naiarievilo.todoapp.validation.ValidationLengths.DEFAULT_MIN_LENGTH;
import static dev.naiarievilo.todoapp.validation.ValidationMessages.MUST_BE_PROVIDED;

@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NotBlankValidator.class)
public @interface NotBlank {

    String message() default MUST_BE_PROVIDED;

    int min() default DEFAULT_MIN_LENGTH;

    int max() default DEFAULT_MAX_LENGTH;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
