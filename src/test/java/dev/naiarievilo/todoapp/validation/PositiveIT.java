package dev.naiarievilo.todoapp.validation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.Objects;

import static dev.naiarievilo.todoapp.validation.AnnotationsTestCases.DOES_NOT_RETURN_ERROR_MESSAGE_WHEN;
import static dev.naiarievilo.todoapp.validation.AnnotationsTestCases.RETURNS_ERROR_MESSAGE_WHEN;
import static dev.naiarievilo.todoapp.validation.ValidationMessages.NOT_VALID;
import static org.junit.jupiter.api.Assertions.*;

class PositiveIT extends ValidationIT {

    @Autowired
    LocalValidatorFactoryBean localValidatorFactoryBean;

    @Test
    @DisplayName("@Positive: " + RETURNS_ERROR_MESSAGE_WHEN + " id is not valid")
    void id_IdNotValid_ReturnsConstraintViolationError() {
        var testDTO = new IdTestDTO(null);
        Errors errors = new BeanPropertyBindingResult(testDTO, "testDTO");
        localValidatorFactoryBean.validate(testDTO, errors);
        assertTrue(errors.hasErrors());
        assertEquals(NOT_VALID, Objects.requireNonNull(errors.getFieldError("id")).getDefaultMessage());

        var secondTestDTO = new IdTestDTO(-1L);
        Errors secondErrors = new BeanPropertyBindingResult(secondTestDTO, "secondTestDTO");
        localValidatorFactoryBean.validate(secondTestDTO, secondErrors);
        assertTrue(errors.hasErrors());
        assertEquals(NOT_VALID, Objects.requireNonNull(errors.getFieldError("id")).getDefaultMessage());
    }

    @Test
    @DisplayName("@Positive: " + DOES_NOT_RETURN_ERROR_MESSAGE_WHEN + "id provided is valid")
    void id_IdValid_DoesNotReturnConstraintViolationError() {
        var testDTO = new IdTestDTO(1L);
        Errors errors = new BeanPropertyBindingResult(testDTO, "testDTO");

        localValidatorFactoryBean.validate(testDTO, errors);
        assertFalse(errors.hasErrors());
    }

    record IdTestDTO(
        @Positive
        Long id
    ) { }
}
