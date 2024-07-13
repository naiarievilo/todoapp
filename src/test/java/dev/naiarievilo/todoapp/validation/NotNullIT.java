package dev.naiarievilo.todoapp.validation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.Objects;

import static dev.naiarievilo.todoapp.validation.AnnotationsTestCases.DOES_NOT_RETURN_ERROR_MESSAGE_WHEN;
import static dev.naiarievilo.todoapp.validation.AnnotationsTestCases.RETURNS_ERROR_MESSAGE_WHEN;
import static dev.naiarievilo.todoapp.validation.ValidationMessages.MUST_BE_PROVIDED;
import static org.junit.jupiter.api.Assertions.*;

class NotNullIT extends ValidationIT {

    @Autowired
    LocalValidatorFactoryBean localValidatorFactoryBean;

    @Test
    @DisplayName("@NotNull: " + RETURNS_ERROR_MESSAGE_WHEN + " field is null")
    void notNull_FieldNull_ReturnsConstraintViolationError() {
        var testDTO = new NotNullTestDTO(null);
        Errors errors = new BeanPropertyBindingResult(testDTO, "testDTO");

        localValidatorFactoryBean.validate(testDTO, errors);
        assertTrue(errors.hasErrors());

        FieldError fieldError = Objects.requireNonNull(errors.getFieldError("field"));
        assertEquals(MUST_BE_PROVIDED, fieldError.getDefaultMessage());
    }

    @Test
    @DisplayName("@NotNull: " + DOES_NOT_RETURN_ERROR_MESSAGE_WHEN + " field is not null")
    void notNull_FieldNotNull_ReturnsConstraintViolationError() {
        var testDTO = new NotNullTestDTO("notNull");
        Errors errors = new BeanPropertyBindingResult(testDTO, "testDTO");

        localValidatorFactoryBean.validate(testDTO, errors);
        assertFalse(errors.hasErrors());
    }

    record NotNullTestDTO(
        @NotNull
        String field
    ) { }
}
