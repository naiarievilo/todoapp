package dev.naiarievilo.todoapp.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import static dev.naiarievilo.todoapp.validation.ValidationLengths.EMAIL_MAX_LENGTH;
import static dev.naiarievilo.todoapp.validation.ValidationMessages.NOT_VALID;

public class EmailValidator implements ConstraintValidator<Email, String> {

    // For a discussion on the trade-offs in validating email addresses with regular expressions and its strictness,
    // see https://www.regular-expressions.info/email.html
    public static final String EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";

    @Override
    public boolean isValid(String email, ConstraintValidatorContext constraintValidatorContext) {
        if (email.isBlank()) {
            return false;

        } else if (!email.matches(EMAIL_REGEX) || email.length() > EMAIL_MAX_LENGTH) {
            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext.buildConstraintViolationWithTemplate(NOT_VALID).addConstraintViolation();
            return false;
        }

        return true;
    }
}
