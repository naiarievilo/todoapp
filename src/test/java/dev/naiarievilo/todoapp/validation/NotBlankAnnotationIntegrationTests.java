package dev.naiarievilo.todoapp.validation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

import java.util.Objects;

import static dev.naiarievilo.todoapp.validation.AnnotationsTestCaseMessages.DOES_NOT_RETURN_ERROR_MESSAGE_WHEN;
import static dev.naiarievilo.todoapp.validation.AnnotationsTestCaseMessages.RETURNS_ERROR_MESSAGE_WHEN;
import static dev.naiarievilo.todoapp.validation.ValidationMessages.MUST_BE_PROVIDED;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {MethodValidationPostProcessor.class, LocalValidatorFactoryBean.class})
class NotBlankAnnotationIntegrationTests {

    @Autowired
    LocalValidatorFactoryBean localValidatorFactoryBean;

    @Test
    @DisplayName("@NotBlank: " + RETURNS_ERROR_MESSAGE_WHEN + " field is blank")
    void notBlank_BlankField_ReturnsConstraintViolationError() {
        var testDTO = new NotBlankTestDTO("  ");
        Errors errors = new BeanPropertyBindingResult(testDTO, "testDTO");

        localValidatorFactoryBean.validate(testDTO, errors);
        assertTrue(errors.hasErrors());

        FieldError fieldError = Objects.requireNonNull(errors.getFieldError("field"));
        assertEquals(MUST_BE_PROVIDED, fieldError.getDefaultMessage());
    }

    @Test
    @DisplayName("@NotBlank: " + DOES_NOT_RETURN_ERROR_MESSAGE_WHEN + " field is not blank")
    void notBlank_FieldNotBlank_DoesNotReturnConstraintViolationError() {
        var testDTO = new NotBlankTestDTO("notBlank");
        Errors errors = new BeanPropertyBindingResult(testDTO, "testDTO");

        localValidatorFactoryBean.validate(testDTO, errors);
        assertFalse(errors.hasErrors());
    }

    record NotBlankTestDTO(
        @NotBlank
        String field
    ) { }
}
