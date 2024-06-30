package dev.naiarievilo.todoapp.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Collection;

import static dev.naiarievilo.todoapp.validation.ValidationMessages.CONTAINS_NULL_ELEMENTS;

public class NoNullElementsValidator implements ConstraintValidator<NoNullElements, Collection<?>> {

    @Override
    public boolean isValid(Collection<?> collection, ConstraintValidatorContext constraintValidatorContext) {
        if (collection == null) {
            return false;
        }

        boolean containsNullElements = false;
        for (Object element : collection) {
            if (element == null) {
                containsNullElements = true;
                break;
            }
        }

        if (!containsNullElements) {
            return true;
        }

        constraintValidatorContext.disableDefaultConstraintViolation();
        constraintValidatorContext
            .buildConstraintViolationWithTemplate(CONTAINS_NULL_ELEMENTS).addConstraintViolation();
        return false;
    }
}
