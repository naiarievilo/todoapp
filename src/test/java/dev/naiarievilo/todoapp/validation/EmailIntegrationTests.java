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
import static dev.naiarievilo.todoapp.validation.ValidationMessages.NOT_VALID;
import static org.junit.jupiter.api.Assertions.*;

class EmailIntegrationTests extends ValidationIntegrationTests {

    @Autowired
    LocalValidatorFactoryBean localValidatorFactoryBean;

    @Test
    @DisplayName("@Email: " + RETURNS_ERROR_MESSAGE_WHEN + " email is not provided")
    void email_EmailNotProvided_ReturnsConstraintViolationError() {
        var testDTO = new EmailTestDTO("");
        Errors errors = new BeanPropertyBindingResult(testDTO, "testDTO");

        localValidatorFactoryBean.validate(testDTO, errors);
        assertTrue(errors.hasErrors());

        FieldError fieldError = Objects.requireNonNull(errors.getFieldError("email"));
        assertEquals(MUST_BE_PROVIDED, fieldError.getDefaultMessage());
    }

    @Test
    @DisplayName("@Email: " + RETURNS_ERROR_MESSAGE_WHEN + " email is not valid")
    void email_EmailNotValid_ReturnsConstraintViolationError() {
        var testDTO = new EmailTestDTO("notAValidEmail");
        Errors errors = new BeanPropertyBindingResult(testDTO, "testDTO");

        localValidatorFactoryBean.validate(testDTO, errors);
        assertTrue(errors.hasErrors());

        FieldError fieldError = Objects.requireNonNull(errors.getFieldError("email"));
        assertEquals(NOT_VALID, fieldError.getDefaultMessage());
    }

    @Test
    @DisplayName("@Email: " + DOES_NOT_RETURN_ERROR_MESSAGE_WHEN + " email is provided and valid")
    void email_EmailProvidedAndValid_DoesNotReturnConstraintViolationError() {
        var testDTO = new EmailTestDTO("johnDoe@example.com");
        Errors errors = new BeanPropertyBindingResult(testDTO, "testDTO");

        localValidatorFactoryBean.validate(testDTO, errors);
        assertFalse(errors.hasErrors());
    }

    record EmailTestDTO(
        @Email
        String email
    ) { }
}
