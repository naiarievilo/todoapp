package dev.naiarievilo.todoapp.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import static dev.naiarievilo.todoapp.validation.ValidationMessages.DOES_NOT_MATCH;

public class MatchingFieldsValidator implements ConstraintValidator<MatchingFields, Object> {

    private String targetField;
    private String matchingField;

    @Override
    public void initialize(MatchingFields matchingFields) {
        this.targetField = matchingFields.targetField();
        this.matchingField = matchingFields.matchingField();
    }

    @Override
    public boolean isValid(Object object, ConstraintValidatorContext constraintValidatorContext) {
        BeanWrapper beanWrapper = new BeanWrapperImpl(object);

        Object targetFieldValue = beanWrapper.getPropertyValue(targetField);
        Object matchingFieldValue = beanWrapper.getPropertyValue(matchingField);

        if (targetFieldValue == null || !targetFieldValue.equals(matchingFieldValue)) {
            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext
                .buildConstraintViolationWithTemplate(
                    ValidationMessages.formatMessage(DOES_NOT_MATCH, matchingField, targetField)
                )
                .addConstraintViolation();

            return false;
        }

        return true;
    }
}
