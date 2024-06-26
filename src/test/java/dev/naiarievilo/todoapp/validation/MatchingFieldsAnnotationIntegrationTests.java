package dev.naiarievilo.todoapp.validation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

import java.util.Objects;

import static dev.naiarievilo.todoapp.validation.AnnotationsTestCaseMessages.DOES_NOT_RETURN_ERROR_MESSAGE_WHEN;
import static dev.naiarievilo.todoapp.validation.AnnotationsTestCaseMessages.RETURNS_ERROR_MESSAGE_WHEN;
import static dev.naiarievilo.todoapp.validation.ValidationMessages.DOES_NOT_MATCH;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {MethodValidationPostProcessor.class, LocalValidatorFactoryBean.class})
class MatchingFieldsAnnotationIntegrationTests {

    private static final String FIELD_VALUE = "fieldValue";
    private static final String OTHER_FIELD_VALUE = "otherFieldValue";

    @Autowired
    private LocalValidatorFactoryBean localValidatorFactoryBean;

    @Test
    @DisplayName("@MatchingFields: " + RETURNS_ERROR_MESSAGE_WHEN + " field values don't match")
    void matchingFields_PasswordsDoNotMatch_ReturnsConstraintViolationError() {
        var testDTO = new MatchingFieldsTestDTO(FIELD_VALUE, OTHER_FIELD_VALUE);
        Errors errors = new BeanPropertyBindingResult(testDTO, "testDTO");

        localValidatorFactoryBean.validate(testDTO, errors);
        assertTrue(errors.hasErrors());

        ObjectError objectError = Objects.requireNonNull(errors.getGlobalError());
        assertEquals(
            ValidationMessages.formatMessage(DOES_NOT_MATCH, "passwordConfirmation", "password"),
            objectError.getDefaultMessage()
        );
    }

    @Test
    @DisplayName("@MatchingFields: " + DOES_NOT_RETURN_ERROR_MESSAGE_WHEN + " field values match")
    void matchingFields_PasswordsMatch_DoesNotReturnConstraintViolationError() {
        var testDTO = new MatchingFieldsTestDTO(FIELD_VALUE, FIELD_VALUE);
        Errors errors = new BeanPropertyBindingResult(testDTO, "testDTO");

        localValidatorFactoryBean.validate(testDTO, errors);
        assertFalse(errors.hasErrors());
    }

    @MatchingFields(targetField = "password", matchingField = "passwordConfirmation")
    record MatchingFieldsTestDTO(
        String password,
        String passwordConfirmation
    ) { }
}
