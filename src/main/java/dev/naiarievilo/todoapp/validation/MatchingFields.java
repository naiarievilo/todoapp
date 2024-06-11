package dev.naiarievilo.todoapp.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static dev.naiarievilo.todoapp.validation.ValidationMessages.DOES_NOT_MATCH;

@Constraint(validatedBy = MatchingFieldsValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface MatchingFields {

    String targetField();

    String matchingField();

    String message() default DOES_NOT_MATCH;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
