package dev.naiarievilo.todoapp.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import static dev.naiarievilo.todoapp.validation.ValidationMessages.NOT_VALID;

public class PasswordValidator implements ConstraintValidator<Password, String> {

    // Password should contain at least one number,
    private static final String PASSWORD_CONSTRAINT = "(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*\\W).{14,72}";

    @Override
    public boolean isValid(String password, ConstraintValidatorContext constraintValidatorContext) {
        if (password.isBlank()) {
            return false;
        } else if (!password.matches(PASSWORD_CONSTRAINT)) {
            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext.buildConstraintViolationWithTemplate(NOT_VALID).addConstraintViolation();
            return false;
        }

        return true;
    }
}
