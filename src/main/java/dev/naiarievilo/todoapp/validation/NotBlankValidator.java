package dev.naiarievilo.todoapp.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import static dev.naiarievilo.todoapp.validation.ValidationMessages.NOT_VALID;

public class NotBlankValidator implements ConstraintValidator<NotBlank, String> {

    private int min;
    private int max;

    @Override
    public void initialize(NotBlank notBlank) {
        this.min = notBlank.min();
        this.max = notBlank.max();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        if (value.isBlank()) {
            return false;
        } else if (value.length() < min || value.length() > max) {
            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext.buildConstraintViolationWithTemplate(NOT_VALID).addConstraintViolation();
            return false;
        }

        return true;
    }
}
