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

import static dev.naiarievilo.todoapp.validation.ValidationMessages.MUST_BE_PROVIDED;
import static dev.naiarievilo.todoapp.validation.ValidationMessages.NOT_VALID;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {MethodValidationPostProcessor.class, LocalValidatorFactoryBean.class})
class EmailIntegrationTests {

    @Autowired
    LocalValidatorFactoryBean localValidatorFactoryBean;

    @Test
    @DisplayName("@Email: returns constraint violation error when email is not provided")
    void email_EmailNotProvided_ReturnsConstraintViolationError() {
        TestDTO testDTO = new TestDTO("");
        Errors errors = new BeanPropertyBindingResult(testDTO, "testDTO");

        localValidatorFactoryBean.validate(testDTO, errors);
        assertTrue(errors.hasErrors());

        FieldError fieldError = Objects.requireNonNull(errors.getFieldError("email"));
        assertEquals(MUST_BE_PROVIDED, fieldError.getDefaultMessage());
    }

    @Test
    @DisplayName("@Email: returns constraint violation error when email is not valid")
    void email_EmailNotValid_ReturnsConstraintViolationError() {
        TestDTO testDTO = new TestDTO("notAValidEmail");
        Errors errors = new BeanPropertyBindingResult(testDTO, "testDTO");

        localValidatorFactoryBean.validate(testDTO, errors);
        assertTrue(errors.hasErrors());

        FieldError fieldError = Objects.requireNonNull(errors.getFieldError("email"));
        assertEquals(NOT_VALID, fieldError.getDefaultMessage());
    }

    @Test
    @DisplayName("@Email: does not return errors when email is provided and valid")
    void email_EmailProvidedAndValid_DoesNotReturnConstraintViolationError() {
        TestDTO testDTO = new TestDTO("johnDoe@example.com");
        Errors errors = new BeanPropertyBindingResult(testDTO, "testDTO");

        localValidatorFactoryBean.validate(testDTO, errors);
        assertFalse(errors.hasErrors());
    }

    record TestDTO(
        @Email
        String email
    ) { }
}
