package dev.naiarievilo.todoapp.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.BeanWrapperImpl;

public class PasswordMatchingValidator implements ConstraintValidator<PasswordMatching, Object> {

    private String password;
    private String confirmPassword;

    @Override
    public void initialize(PasswordMatching matching) {
        this.password = matching.password();
        this.confirmPassword = matching.confirmPassword();
    }

    @Override
    public boolean isValid(Object obj, ConstraintValidatorContext constraintValidatorContext) {
        Object passwordValue = new BeanWrapperImpl(obj).getPropertyValue(password);
        Object confirmPasswordValue = new BeanWrapperImpl(obj).getPropertyValue(confirmPassword);

        return passwordValue != null && passwordValue.equals(confirmPasswordValue);
    }
}
