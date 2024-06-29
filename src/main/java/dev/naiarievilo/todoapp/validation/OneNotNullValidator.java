package dev.naiarievilo.todoapp.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ValidationException;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import static dev.naiarievilo.todoapp.validation.ValidationMessages.MUST_BE_PROVIDED;

public class OneNotNullValidator implements ConstraintValidator<OneNotNull, Object> {

    private String[] fields;

    @Override
    public void initialize(OneNotNull oneNotNull) {
        fields = oneNotNull.fields();
        if (fields.length < 2) {
            throw new ValidationException("At least two fields must be provided");
        }

        this.fields = oneNotNull.fields();
    }

    @Override
    public boolean isValid(Object object, ConstraintValidatorContext constraintValidatorContext) {
        BeanWrapper beanWrapper = new BeanWrapperImpl(object);

        for (String field : fields) {
            Object value = beanWrapper.getPropertyValue(field);
            if (value != null) {
                return true;
            }
        }

        // Only format field names when all fields are null
        String fieldsFormatted = ValidationMessages.formatFieldNames(fields, "or ");

        constraintValidatorContext.disableDefaultConstraintViolation();
        constraintValidatorContext
            .buildConstraintViolationWithTemplate(ValidationMessages.formatMessage(MUST_BE_PROVIDED, fieldsFormatted))
            .addConstraintViolation();

        return false;
    }
}
