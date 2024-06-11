package dev.naiarievilo.todoapp.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static dev.naiarievilo.todoapp.validation.ValidationMessages.MUST_BE_PROVIDED;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {EmailValidator.class})
public @interface Email {

    String message() default MUST_BE_PROVIDED;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
