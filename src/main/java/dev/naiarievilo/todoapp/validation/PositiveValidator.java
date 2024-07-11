package dev.naiarievilo.todoapp.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.math.BigDecimal;
import java.math.BigInteger;

public class PositiveValidator implements ConstraintValidator<Positive, Number> {

    @Override
    public boolean isValid(Number value, ConstraintValidatorContext constraintValidatorContext) {
        if (value == null) {
            return false;
        }

        try {
            BigDecimal bdValue = new BigDecimal(value.toString());
            BigInteger biValue = new BigInteger(value.toString());
            return bdValue.compareTo(BigDecimal.ZERO) > 0 && biValue.compareTo(BigInteger.ZERO) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
