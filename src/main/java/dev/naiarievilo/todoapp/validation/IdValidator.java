package dev.naiarievilo.todoapp.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class IdValidator implements ConstraintValidator<Id, Long> {

    @Override
    public boolean isValid(Long id, ConstraintValidatorContext constraintValidatorContext) {
        return id != null && id > 0;
    }
}
