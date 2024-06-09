package dev.naiarievilo.todoapp.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static dev.naiarievilo.todoapp.validation.ValidationErrorMessages.PASSWORD_CONFIRMATION_MUST_MATCH;

@Constraint(validatedBy = PasswordMatchingValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface PasswordMatching {

    String password();

    String confirmPassword();

    String message() default PASSWORD_CONFIRMATION_MUST_MATCH;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
