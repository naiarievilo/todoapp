package dev.naiarievilo.todoapp.validation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

import static dev.naiarievilo.todoapp.validation.AnnotationsTestCases.DOES_NOT_RETURN_ERROR_MESSAGE_WHEN;
import static dev.naiarievilo.todoapp.validation.AnnotationsTestCases.RETURNS_ERROR_MESSAGE_WHEN;
import static dev.naiarievilo.todoapp.validation.ValidationMessages.CONTAINS_NULL_ELEMENTS;
import static dev.naiarievilo.todoapp.validation.ValidationMessages.MUST_BE_PROVIDED;
import static org.junit.jupiter.api.Assertions.*;

class NoNullElementsIntegrationTests extends ValidationIntegrationTests {

    @Autowired
    LocalValidatorFactoryBean localValidatorFactoryBean;

    @Test
    @DisplayName("@NoNullElements: " + RETURNS_ERROR_MESSAGE_WHEN + " collection is null")
    void noNullElements_CollectionIsNull_ReturnsConstraintViolationError() {
        var testDTO = new NoNullElementsTestDTO(null);
        Errors errors = new BeanPropertyBindingResult(testDTO, "testDTO");

        localValidatorFactoryBean.validate(testDTO, errors);
        assertTrue(errors.hasErrors());

        FieldError fieldError = Objects.requireNonNull(errors.getFieldError("collection"));
        assertEquals(MUST_BE_PROVIDED, fieldError.getDefaultMessage());
    }

    @Test
    @DisplayName("@NoNullElements: " + RETURNS_ERROR_MESSAGE_WHEN + " collection has at least one null element")
    void noNullElements_CollectionHasNullElement_ReturnsConstraintViolationError() {
        var testDTO = new NoNullElementsTestDTO(Arrays.asList("null", null));
        Errors errors = new BeanPropertyBindingResult(testDTO, "testDTO");

        localValidatorFactoryBean.validate(testDTO, errors);
        assertTrue(errors.hasErrors());

        FieldError fieldError = Objects.requireNonNull(errors.getFieldError("collection"));
        assertEquals(CONTAINS_NULL_ELEMENTS, fieldError.getDefaultMessage());
    }

    @Test
    @DisplayName("@NoNullElements: " + DOES_NOT_RETURN_ERROR_MESSAGE_WHEN + " collection has not null elements")
    void noNullElements_CollectionHasNoNullElements_ReturnsConstraintViolationError() {
        var testDTO = new NoNullElementsTestDTO(Arrays.asList("null", "", "  "));
        Errors errors = new BeanPropertyBindingResult(testDTO, "testDTO");

        localValidatorFactoryBean.validate(testDTO, errors);
        assertFalse(errors.hasErrors());
    }

    record NoNullElementsTestDTO(
        @NoNullElements
        Collection<?> collection
    ) { }
}
